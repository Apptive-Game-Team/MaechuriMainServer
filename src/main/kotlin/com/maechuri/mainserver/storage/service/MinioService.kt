package com.maechuri.mainserver.storage.service

import com.maechuri.mainserver.storage.config.MinioProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import java.time.Duration

@Service
class MinioService(
    private val s3Client: S3Client,
    private val s3Presigner: S3Presigner,
    private val minioProperties: MinioProperties
) {

    /**
     * Get a pre-signed URL for a file in MinIO
     * @param fileName The name/key of the file in the bucket
     * @param expirationMinutes The duration in minutes for which the URL will be valid (default: 60 minutes)
     * @return Pre-signed URL as a String
     */
    fun getUrl(fileName: String, expirationMinutes: Long = 60): String {
        val getObjectRequest = GetObjectRequest.builder()
            .bucket(minioProperties.bucketName)
            .key(fileName)
            .build()

        val presignRequest = GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(expirationMinutes))
            .getObjectRequest(getObjectRequest)
            .build()

        val presignedRequest = s3Presigner.presignGetObject(presignRequest)
        return presignedRequest.url().toString()
    }

    /**
     * Get the permanent URL for a file (works only if bucket has public read access)
     * @param fileName The name/key of the file in the bucket
     * @return Permanent URL as a String
     */
    fun getPermanentUrl(fileName: String): String {
        return "${minioProperties.endpoint}/${minioProperties.bucketName}/$fileName"
    }

    /**
     * Upload a byte array as an object to MinIO
     * @param key The key (path) under which to store the object
     * @param content The raw bytes to upload
     * @param contentType The MIME type of the content
     */
    suspend fun uploadObject(key: String, content: ByteArray, contentType: String) {
        withContext(Dispatchers.IO) {
            val request = PutObjectRequest.builder()
                .bucket(minioProperties.bucketName)
                .key(key)
                .contentType(contentType)
                .contentLength(content.size.toLong())
                .build()
            s3Client.putObject(request, RequestBody.fromBytes(content))
        }
    }

    /**
     * Upload a text string as an object to MinIO
     * @param key The key (path) under which to store the object
     * @param content The text content to upload
     * @param contentType The MIME type of the content (default: application/json)
     */
    suspend fun uploadText(key: String, content: String, contentType: String = "application/json") {
        uploadObject(key, content.toByteArray(Charsets.UTF_8), contentType)
    }

    /**
     * Download an object from MinIO as a byte array
     * @param key The key (path) of the object to download
     * @return The raw bytes of the object
     */
    suspend fun downloadObject(key: String): ByteArray {
        return withContext(Dispatchers.IO) {
            val request = GetObjectRequest.builder()
                .bucket(minioProperties.bucketName)
                .key(key)
                .build()
            s3Client.getObject(request).readAllBytes()
        }
    }
}
