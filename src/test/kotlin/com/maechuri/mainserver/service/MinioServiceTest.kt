package com.maechuri.mainserver.service

import com.maechuri.mainserver.config.MinioProperties
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest
import java.net.URL

@ExtendWith(MockitoExtension::class)
class MinioServiceTest {

    @Mock
    private lateinit var s3Client: S3Client

    @Mock
    private lateinit var s3Presigner: S3Presigner

    @Mock
    private lateinit var minioProperties: MinioProperties

    @InjectMocks
    private lateinit var minioService: MinioService

    @Test
    fun `getPermanentUrl should return correct URL format`() {
        // Given
        val fileName = "test-file.jpg"
        val endpoint = "http://localhost:9000"
        val bucketName = "test-bucket"
        
        `when`(minioProperties.endpoint).thenReturn(endpoint)
        `when`(minioProperties.bucketName).thenReturn(bucketName)

        // When
        val result = minioService.getPermanentUrl(fileName)

        // Then
        assertEquals("$endpoint/$bucketName/$fileName", result)
        assertTrue(result.contains(fileName))
        assertTrue(result.contains(bucketName))
    }

    @Test
    fun `getUrl should return presigned URL`() {
        // Given
        val fileName = "test-file.jpg"
        val bucketName = "test-bucket"
        val mockUrl = URL("http://localhost:9000/test-bucket/test-file.jpg?signature=xyz")
        
        `when`(minioProperties.bucketName).thenReturn(bucketName)
        
        val mockPresignedRequest = mock(PresignedGetObjectRequest::class.java)
        `when`(mockPresignedRequest.url()).thenReturn(mockUrl)
        
        `when`(s3Presigner.presignGetObject(any(GetObjectPresignRequest::class.java)))
            .thenReturn(mockPresignedRequest)

        // When
        val result = minioService.getUrl(fileName)

        // Then
        assertNotNull(result)
        assertEquals(mockUrl.toString(), result)
    }
}
