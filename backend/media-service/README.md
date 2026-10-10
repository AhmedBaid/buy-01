# Media Service

A Spring Boot service for handling image uploads with Apache Tika validation and MongoDB persistence.

## Endpoints

### Image Upload (General)
**POST** `/api/media/images`

Uploads images associated with either owners or products.

**Request Parameters:**
- `file` (MultipartFile): The image file to upload
- `ownerId` (String): Identifier of the owner
- `productId` (String, optional): Identifier of the product (optional)

**Validation:**
- MIME type validation (must be image/*)
- File size validation (max 2 MB)
- Apache Tika for accurate MIME type detection

**Response:**
```json
{
  "id": "media-id",
  "imagePath": "/path/to/file.jpg",
  "ownerId": "owner-123",
  "productId": "product-456",
  "mediaType": "image/jpeg"
}
```

### Get Image
**GET** `/api/media/images/{id}`

Retrieves an image by its ID with proper caching headers.

**Headers:**
- Cache-Control: `public, max-age=3600` (1 hour)

### Delete Image
**DELETE** `/api/media/images/{id}`

Deletes an image record and its associated file.

---

### Avatar Upload
**POST** `/api/media/avatars`

Uploads a profile avatar for a user.

**Request Parameters:**
- `file` (MultipartFile): The avatar image
- `ownerId` (String): Identifier of the user

**Validation:**
- Size validation (max 2 MB)
- Allowed MIME types: JPEG, PNG, GIF only

**Response:**
```json
{
  "id": "media-id",
  "imagePath": "/path/to/avatar.jpg",
  "ownerId": "owner-123",
  "productId": null,
  "mediaType": "image/jpeg"
}
```

### Get Avatar by Owner
**GET** `/api/media/avatars/owner/{ownerId}`

Retrieves the avatar for a specific user.

### Delete Avatar
**DELETE** `/api/media/avatars/owner/{ownerId}`

Deletes the avatar for a specific user.

---

### Product Image
**GET** `/api/media/products/owner/{ownerId}`

Retrieves all product images for a specific owner.

**Response:**
```json
[
  {
    "id": "media-id",
    "imagePath": "/path/to/product.jpg",
    "ownerId": "owner-123",
    "productId": "product-456",
    "mediaType": "image/jpeg"
  }
]
```

### Get Product Image
**GET** `/api/media/products/{id}`

Retrieves a specific product image.

### Delete Product Image
**DELETE** `/api/media/products/{id}`

Deletes a product image.

## Configuration

**application.properties:**
```properties
# MongoDB connection
spring.data.mongodb.uri=mongodb://localhost:27019/media

# Eureka service discovery
eureka.client.service-url.defaultZone=http://localhost:8761/eureka
eureka.instance.prefer-ip-address=true

# Media upload settings
media.upload.directory=uploads
spring.servlet.multipart.max-file-size=2MB
spring.servlet.multipart.max-request-size=2MB
spring.servlet.multipart.enabled=true
```

## Dependencies

- Spring Boot 4.1.1
- Spring Data MongoDB
- Spring Security
- Apache Tika 2.9.1 (for MIME type detection)
- Lombok

## Validation

Images are validated using Apache Tika for accurate MIME type detection:

- **Allowed types:** JPEG, PNG, GIF, WEBP, SVG, BMP
- **Maximum size:** 2 MB
- **Validation logic:**
  1. Check file size
  2. Detect MIME type with Apache Tika
  3. Validate against allowed types
  4. Save to filesystem and database