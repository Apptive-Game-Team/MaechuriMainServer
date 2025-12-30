package com.maechuri.mainserver.storage.service

import com.maechuri.mainserver.storage.config.MinioProperties
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
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
}
