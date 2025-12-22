# MinIO S3 Integration Setup Guide

## Overview
This project integrates MinIO object storage using the AWS S3 SDK interface. MinIO provides S3-compatible object storage that can be self-hosted.

## Configuration

### 1. Environment Variables Setup

Copy the `.env.example` file to create your own `.env` file:
```bash
cp .env.example .env
```

Edit the `.env` file with your MinIO credentials:
```properties
MINIO_ACCESS_KEY=your-access-key
MINIO_SECRET_KEY=your-secret-key
MINIO_ENDPOINT=http://localhost:9000
MINIO_BUCKET_NAME=your-bucket-name
MINIO_REGION=us-east-1
```

### 2. Application Configuration

The `application.yml` file automatically reads from environment variables with default fallback values:

```yaml
minio:
  access-key: ${MINIO_ACCESS_KEY:minioadmin}
  secret-key: ${MINIO_SECRET_KEY:minioadmin}
  endpoint: ${MINIO_ENDPOINT:http://localhost:9000}
  bucket-name: ${MINIO_BUCKET_NAME:maechuri-bucket}
  region: ${MINIO_REGION:us-east-1}
```

## Usage

### MinioService Methods

The `MinioService` provides two methods for getting file URLs:

#### 1. Get Pre-signed URL (Recommended for private files)
```kotlin
@Autowired
private lateinit var minioService: MinioService

// Get a pre-signed URL valid for 60 minutes (default)
val url = minioService.getUrl("my-file.jpg")

// Get a pre-signed URL valid for custom duration (in minutes)
val url = minioService.getUrl("my-file.jpg", expirationMinutes = 120)
```

Pre-signed URLs are temporary and provide secure access to private objects without requiring authentication.

#### 2. Get Permanent URL (For public files only)
```kotlin
// Get a permanent URL (works only if bucket has public read access)
val url = minioService.getPermanentUrl("my-file.jpg")
```

**Note**: Permanent URLs only work if your MinIO bucket is configured with public read access.

## Example Usage in Controller

```kotlin
@RestController
@RequestMapping("/api/files")
class FileController(private val minioService: MinioService) {

    @GetMapping("/{fileName}")
    fun getFileUrl(@PathVariable fileName: String): ResponseEntity<Map<String, String>> {
        val url = minioService.getUrl(fileName)
        return ResponseEntity.ok(mapOf("url" to url))
    }
}
```

## Running MinIO Locally

To run MinIO locally for development:

```bash
docker run -p 9000:9000 -p 9001:9001 \
  -e "MINIO_ROOT_USER=minioadmin" \
  -e "MINIO_ROOT_PASSWORD=minioadmin" \
  quay.io/minio/minio server /data --console-address ":9001"
```

Access the MinIO Console at: http://localhost:9001

## Creating a Bucket

You can create a bucket using the MinIO Console or the MinIO Client (mc):

```bash
# Install MinIO Client
wget https://dl.min.io/client/mc/release/linux-amd64/mc
chmod +x mc
sudo mv mc /usr/local/bin/

# Configure alias
mc alias set local http://localhost:9000 minioadmin minioadmin

# Create bucket
mc mb local/maechuri-bucket

# Set bucket policy to public (optional)
mc anonymous set download local/maechuri-bucket
```

## Dependencies

The following dependencies are required and already added to `build.gradle`:

```gradle
implementation 'software.amazon.awssdk:s3:2.20.26'
implementation 'software.amazon.awssdk:netty-nio-client:2.20.26'
```

## Testing

Run the MinIO service tests:
```bash
./gradlew test --tests MinioServiceTest
```

Run all tests:
```bash
./gradlew test
```

## Security Notes

1. **Never commit the `.env` file** - It contains sensitive credentials and is already in `.gitignore`
2. Use strong, unique credentials for production environments
3. Enable SSL/TLS for production MinIO endpoints
4. Use pre-signed URLs for temporary access to private files
5. Set appropriate bucket policies based on your security requirements

## Troubleshooting

### Connection refused error
- Ensure MinIO server is running
- Check the `MINIO_ENDPOINT` URL is correct
- Verify firewall settings allow connections to MinIO port

### Access denied error
- Verify `MINIO_ACCESS_KEY` and `MINIO_SECRET_KEY` are correct
- Check bucket policies and permissions
- Ensure the bucket exists

### URL not working
- For permanent URLs, ensure bucket has public read access
- For pre-signed URLs, check that the URL hasn't expired
- Verify the file exists in the bucket

## Additional Resources

- [MinIO Documentation](https://min.io/docs/minio/linux/index.html)
- [AWS S3 SDK Documentation](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/home.html)
- [Spring Boot Configuration Properties](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
