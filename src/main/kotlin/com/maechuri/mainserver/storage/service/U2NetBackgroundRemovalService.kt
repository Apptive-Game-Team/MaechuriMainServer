package com.maechuri.mainserver.storage.service

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import com.maechuri.mainserver.storage.config.U2NetProperties
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.FloatBuffer
import java.nio.file.Paths
import javax.imageio.ImageIO

private val u2netLogger = KotlinLogging.logger {}

/**
 * Background removal service that uses the U2Net model via ONNX Runtime.
 *
 * The pipeline:
 * 1. Pre-process: resize to 320x320, normalise (ImageNet mean/std), convert HWC to CHW.
 * 2. Inference: run the ONNX session and extract the saliency mask.
 * 3. Post-process: resize the mask back to the original dimensions and apply it as the
 *    alpha channel of the original image, then encode as PNG.
 *
 * The model file path is configurable via the `u2net.model-path` application property.
 * If the model file is not found or inference fails, the original bytes are returned
 * unchanged so that higher-level fallback logic (e.g. [HybridBackgroundRemovalService])
 * can take over.
 */
@Service("u2NetBackgroundRemovalService")
class U2NetBackgroundRemovalService(
    private val properties: U2NetProperties
) : BackgroundRemovalService {

    companion object {
        private const val INPUT_SIZE = 320

        // ImageNet mean and std used by the original U2Net pre-processing
        private val MEAN = floatArrayOf(0.485f, 0.456f, 0.406f)
        private val STD  = floatArrayOf(0.229f, 0.224f, 0.225f)
    }

    // Lazily initialise the ONNX session so that application startup is not blocked
    // when the model file is absent (e.g. in test environments).
    private val ortEnv: OrtEnvironment by lazy { OrtEnvironment.getEnvironment() }

    private val ortSession: OrtSession? by lazy {
        val modelFile = Paths.get(properties.modelPath).toFile()
        if (modelFile.exists()) {
            u2netLogger.info { "Loading U2Net ONNX model from ${modelFile.absolutePath}" }
            ortEnv.createSession(modelFile.absolutePath, OrtSession.SessionOptions())
        } else {
            u2netLogger.warn { "U2Net model not found at ${modelFile.absolutePath}. U2NetBackgroundRemovalService will be unavailable." }
            null
        }
    }

    override suspend fun removeBackground(imageBytes: ByteArray): ByteArray {
        val session = ortSession
            ?: throw IllegalStateException(
                "U2Net ONNX model file not found at '${properties.modelPath}'. " +
                "Ensure the model file exists or update the u2net.model-path configuration property."
            )

        val original = ImageIO.read(ByteArrayInputStream(imageBytes))
            ?: error("Could not decode input image bytes")

        val origWidth  = original.width
        val origHeight = original.height

        // 1. Pre-process
        val resized = resizeImage(original, INPUT_SIZE, INPUT_SIZE)
        val inputTensor = buildInputTensor(resized)

        // 2. Inference
        val inputName = session.inputNames.iterator().next()
        val maskData: FloatArray = session.run(mapOf(inputName to inputTensor)).use { result ->
            // U2Net outputs several side-output maps; the first one (d0) is the final prediction.
            val outputTensor = result.first().value as OnnxTensor
            val floatBuf = outputTensor.floatBuffer
            FloatArray(floatBuf.remaining()).also { floatBuf.get(it) }.let { normalise01(it) }
        }
        inputTensor.close()

        // 3. Post-process: apply mask as alpha channel on the original image
        val outputImage = applyMaskAsAlpha(original, origWidth, origHeight, maskData)

        val out = ByteArrayOutputStream()
        ImageIO.write(outputImage, "png", out)
        return out.toByteArray()
    }

    // -------------------------------------------------------------------------
    // Pre-processing
    // -------------------------------------------------------------------------

    private fun resizeImage(src: BufferedImage, width: Int, height: Int): BufferedImage {
        val dst = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val g2d = dst.createGraphics()
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        g2d.drawImage(src, 0, 0, width, height, null)
        g2d.dispose()
        return dst
    }

    /**
     * Converts a [BufferedImage] to a float tensor with shape [1, 3, H, W] (CHW order),
     * applying ImageNet normalisation.  Uses [BufferedImage.getRGB] for pixel access which
     * works correctly for all [BufferedImage] types.
     */
    private fun buildInputTensor(image: BufferedImage): OnnxTensor {
        val h = image.height
        val w = image.width
        val channelSize = h * w
        val data = FloatArray(3 * channelSize)

        // Retrieve all pixels at once to minimise per-pixel JNI overhead
        val pixels = IntArray(channelSize)
        image.getRGB(0, 0, w, h, pixels, 0, w)

        for (idx in 0 until channelSize) {
            val rgb = pixels[idx]
            val r = ((rgb shr 16) and 0xFF) / 255f
            val g = ((rgb shr  8) and 0xFF) / 255f
            val b = ( rgb         and 0xFF) / 255f

            data[0 * channelSize + idx] = (r - MEAN[0]) / STD[0]  // R channel
            data[1 * channelSize + idx] = (g - MEAN[1]) / STD[1]  // G channel
            data[2 * channelSize + idx] = (b - MEAN[2]) / STD[2]  // B channel
        }

        return OnnxTensor.createTensor(
            ortEnv,
            FloatBuffer.wrap(data),
            longArrayOf(1, 3, h.toLong(), w.toLong())
        )
    }

    // -------------------------------------------------------------------------
    // Post-processing
    // -------------------------------------------------------------------------

    /**
     * Normalises values in [data] to the [0, 1] range using min-max scaling.
     */
    private fun normalise01(data: FloatArray): FloatArray {
        var min = Float.MAX_VALUE
        var max = Float.MIN_VALUE
        for (v in data) {
            if (v < min) min = v
            if (v > max) max = v
        }
        val range = max - min
        // If all values are identical, treat the mask as fully opaque (foreground).
        return if (range == 0f) FloatArray(data.size) { 1f }
        else FloatArray(data.size) { i -> (data[i] - min) / range }
    }

    /**
     * Resizes the [maskData] (produced at [INPUT_SIZE]×[INPUT_SIZE]) back to
     * [origWidth]×[origHeight] using nearest-neighbour interpolation, then applies
     * it as the alpha channel of [original], returning a [BufferedImage.TYPE_INT_ARGB]
     * image.
     */
    private fun applyMaskAsAlpha(
        original: BufferedImage,
        origWidth: Int,
        origHeight: Int,
        maskData: FloatArray,
    ): BufferedImage {
        val output = BufferedImage(origWidth, origHeight, BufferedImage.TYPE_INT_ARGB)
        val scaleX = INPUT_SIZE.toFloat() / origWidth
        val scaleY = INPUT_SIZE.toFloat() / origHeight

        // Batch-retrieve original pixels
        val srcPixels = IntArray(origWidth * origHeight)
        original.getRGB(0, 0, origWidth, origHeight, srcPixels, 0, origWidth)
        val dstPixels = IntArray(origWidth * origHeight)

        for (y in 0 until origHeight) {
            val maskY = (y * scaleY).toInt().coerceIn(0, INPUT_SIZE - 1)
            for (x in 0 until origWidth) {
                val maskX = (x * scaleX).toInt().coerceIn(0, INPUT_SIZE - 1)
                val alpha = (maskData[maskY * INPUT_SIZE + maskX] * 255f).toInt().coerceIn(0, 255)
                val rgb = srcPixels[y * origWidth + x] and 0x00FFFFFF
                dstPixels[y * origWidth + x] = (alpha shl 24) or rgb
            }
        }

        output.setRGB(0, 0, origWidth, origHeight, dstPixels, 0, origWidth)
        return output
    }
}
