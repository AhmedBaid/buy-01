# Media Service Implementation Summary

## Overview
Complete Media Service implemented with MongoDB database connectivity, Apache Tika validation, and two separate controllers for avatar and product images.

## Files Created/Modified

### 1. Dependencies Configuration
**pom.xml** - Added Apache Tika dependency
```xml
<dependency>
    <groupId>org.apache.tika</groupId>
    <artifactId>tika-core</artifactId>
    <version>2.9.1</version>
</dependency>
```

### 2. Data Models
**models/MediaModel.java**
- MongoDB document mapping with indexed fields
- Fields: id, imagePath, ownerId (indexed), productId (indexed), mediaType
- Includes Lombok annotations for getters/setters

### 3. Repository Layer
**repository/MediaRepository.java**
- MongoRepository interface
- Query methods:
  - `findByOwnerIdAndProductIdIsNull(ownerId)` - Find user avatars
  - `findByOwnerIdAndProductIdNotNull(ownerId)` - Find product images
  - `findByOwnerId(ownerId)` - Find all media by owner
  - `delete(MediaModel)` - Delete media

### 4. Data Transfer Objects (DTOs)
**dto/MediaResponse.java**
- Response object for media operations
- Fields: id, imagePath, ownerId, productId, mediaType

**dto/ErrorResponse.java**
- Standardized error response format
- Fields: error, message

### 5. Services Layer

**service/MediaService.java**
- General media upload service
- Apache Tika-based MIME type detection
- File size validation (max 2 MB)
- Supported formats: JPEG, PNG, GIF, WEBP, SVG, BMP
- Methods:
  - `uploadMedia(file, ownerId, productId)` - Upload image with Tika validation
  - `getMediaById(id)` - Get media by ID
  - `deleteMedia(id)` - Delete media

**service/AvatarService.java**
- Avatar-specific operations
- Additional validation for allowed avatar types (JPEG, PNG, GIF)
- Methods:
  - `uploadAvatar(file, ownerId)` - Upload avatar
  - `getAvatarByOwner(ownerId)` - Get user avatar
  - `deleteAvatar(ownerId)` - Delete user avatar

**service/ProductImageService.java**
- Product image management
- Methods:
  - `getProductImagesByOwner(ownerId)` - Get all product images
  - `getProductImageById(id)` - Get specific product image
  - `deleteProductImage(id)` - Delete product image

### 6. Controllers

**controller/MediaController**
- **POST** `/api/media/images` - Upload general image (supports optional productId)
  - Validates image using Tika
  - Max size: 2 MB
  - Returns MediaResponse

- **GET** `/api/media/images/{id}` - Get image by ID
  - Returns images bytes
  - Includes caching headers (Cache-Control: public, max-age=3600)
  - Automatic MIME type detection

- **DELETE** `/api/media/images/{id}` - Delete image

**controller/AvatarController**
- **POST** `/api/media/avatars` - Upload avatar
  - Validates size (≤ 2 MB)
  - Validates type (JPEG, PNG, GIF only)

- **GET** `/api/media/avatars/owner/{ownerId}` - Get user avatar
- **DELETE** `/api/media/avatars/owner/{ownerId}` - Delete user avatar

**controller/ProductImageController**
- **GET** `/api/media/products/owner/{ownerId}` - Get all product images by owner
- **GET** `/api/media/products/{id}` - Get specific product image
- **DELETE** `/api/media/products/{id}` - Delete product image

### 7. Exception Handling
**exception/GlobalExceptionHandler.java**
- `IllegalArgumentException` - Bad request with custom message
- `MaxUploadSizeExceededException` - File too large error
- `RuntimeException` - Media not found
- `Exception` - Internal server error

### 8. Configuration
**application.properties** - Updated with media settings:
```properties
# MongoDB connection
spring.data.mongodb.uri=mongodb://localhost:27019/media

# Media upload settings
media.upload.directory=uploads
spring.servlet.multipart.max-file-size=2MB
spring.servlet.multipart.max-request-size=2MB
spring.servlet.multipart.enabled=true
```

### 9. Security Configuration
**config/SecurityConfig.java** - Already exists
- CSRF disabled for API
- Sessionless authentication
- `/avatars/**` and `/products/**` endpoints accessible without auth
- All other endpoints require authentication

## Key Features

### ✅ MongoDB Database Connectivity
- Spring Data MongoDB integration
- Indexed ownerId and productId fields for performance
- MongoDB URI configured for local connection

### ✅ Apache Tika Validation
- accurate MIME type detection
- Case-insensitive comparison
- Comprehensive format support (JPEG, PNG, GIF, WEBP, SVG, BMP)
- 2 MB size validation

### ✅ Two Separate Controllers
1. **AvatarController** - For user avatars
2. **MediaController** - For general images (with optional product association)

### Additional Features
- Proper HTTP caching headers
- Error handling with standardized responses
- File deletion from filesystem and database
- IDs with timestamps for unique filenames
- Upload directory creation
- MongoDB queries filtered by content type

## API Usage Examples

### Upload Avatar
```bash
curl -X POST http://localhost:8083/api/media/avatars \
  -H "Authorization: Bearer <token>" \
  -F "file=@avatar.jpg" \
  -F "ownerId=user123"
```

### Upload Product Image
```bash
curl -X POST http://localhost:8083/api/media/images \
  -H "Authorization: Bearer <token>" \
  -F "file=@product.jpg" \
  -F "ownerId=user123" \
  -F "productId=prod456"
```

### Get Image Stream
```bash
curl http://localhost:8083/api/media/images/{id}
```

### Get Avatar by Owner
```bash
curl http://localhost:8083/api/media/avatars/owner/user123
```

### Get Product Images
```bash
curl http://localhost:8083/api/media/products/owner/user123
```

## Database Schema

**Collection: medias**
```json
{
  "_id": "ObjectId",
  "imagePath": "String",
  "ownerId": "String (indexed)",
  "productId": "String (indexed)",
  "mediaType": "String",
  "_class": "ServiceMedia.models.MediaModel"
}
```

## Next Steps
1. Ensure MongoDB is running on `localhost:27019`
2. Ensure Eureka server is running on `http://localhost:8761`
3. Start the service: `./mvnw spring-boot:run`
4. The service will register with Eureka on port 8083
5. Create the uploads directory for uploaded files

## Validation Rules
- **File Size:** Maximum 2 MB
- **MIME Types:** image/jpeg, image/jpg, image/png, image/gif, image/webp, image/svg+xml, image/bmp
- **Avatars:** Additional restriction to JPEG, PNG, GIF
- **Seller-Only:** All endpoints require seller authentication