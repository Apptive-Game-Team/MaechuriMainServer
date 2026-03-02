package com.maechuri.mainserver.storage.service

import org.springframework.stereotype.Service
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

@Service
class WhiteToTransparentService : BackgroundRemovalService {

    companion object {
        private const val WHITE_THRESHOLD = 245 // R, G, B 값이 모두 이 값 이상이면 흰색으로 간주
    }

    override suspend fun removeBackground(imageBytes: ByteArray): ByteArray {
        val originalImage: BufferedImage = ImageIO.read(ByteArrayInputStream(imageBytes))
            ?: return imageBytes // 이미지를 읽을 수 없으면 원본 반환

        val newImage = BufferedImage(originalImage.width, originalImage.height, BufferedImage.TYPE_INT_ARGB)
        val g2d = newImage.createGraphics()
        g2d.drawImage(originalImage, 0, 0, null)
        g2d.dispose()

        for (y in 0 until newImage.height) {
            for (x in 0 until newImage.width) {
                val pixel = Color(newImage.getRGB(x, y), true)
                if (pixel.red >= WHITE_THRESHOLD && pixel.green >= WHITE_THRESHOLD && pixel.blue >= WHITE_THRESHOLD) {
                    // 알파 채널을 0으로 만들어 투명하게 설정
                    newImage.setRGB(x, y, pixel.rgb and 0x00FFFFFF)
                }
            }
        }

        val outputStream = ByteArrayOutputStream()
        ImageIO.write(newImage, "png", outputStream) // 배경 투명도를 지원하는 PNG로 출력
        return outputStream.toByteArray()
    }
}
