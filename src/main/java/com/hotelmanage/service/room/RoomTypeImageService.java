package com.hotelmanage.service.room;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.hotelmanage.entity.room.RoomType;
import com.hotelmanage.entity.room.RoomTypeImage;
import com.hotelmanage.repository.room.RoomTypeImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RoomTypeImageService {

    private final RoomTypeImageRepository roomTypeImageRepository;
    private final Cloudinary cloudinary;

    /**
     * Đặt ảnh làm ảnh chính
     */
    public void setPrimaryImage(Integer imageId) {
        RoomTypeImage image = roomTypeImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ảnh"));

        // Reset tất cả ảnh khác của cùng room type
        List<RoomTypeImage> allImages = roomTypeImageRepository
                .findByRoomType_RoomTypeId(image.getRoomType().getRoomTypeId());

        allImages.forEach(img -> {
            img.setIsPrimary(false);
            roomTypeImageRepository.save(img);
        });

        // Set ảnh này làm primary
        image.setIsPrimary(true);
        roomTypeImageRepository.save(image);
    }

    /**
     * Upload ảnh lên Cloudinary
     */
    public List<RoomTypeImage> uploadImages(RoomType roomType, MultipartFile[] files, boolean setPrimary) throws IOException {
        List<RoomTypeImage> uploadedImages = new ArrayList<>();

        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                String contentType = file.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    throw new IllegalArgumentException("File phải là ảnh!");
                }

                // Upload lên Cloudinary
                Map uploadResult = cloudinary.uploader().upload(
                        file.getBytes(),
                        ObjectUtils.asMap(
                                "folder", "hotel/room-types",
                                "resource_type", "image"
                        )
                );

                String imageUrl = (String) uploadResult.get("secure_url");
                String publicId = (String) uploadResult.get("public_id");

                // Lưu vào database
                RoomTypeImage image = new RoomTypeImage();
                image.setImageUrl(imageUrl);
                image.setRoomType(roomType);
                image.setIsPrimary(setPrimary && uploadedImages.isEmpty());

                RoomTypeImage saved = roomTypeImageRepository.save(image);
                uploadedImages.add(saved);

                log.info("Uploaded image to Cloudinary: {} - Public ID: {}", imageUrl, publicId);
            }
        }

        return uploadedImages;
    }

    /**
     * Xóa ảnh từ Cloudinary và database
     */
    public void deleteImage(Integer imageId) throws IOException {
        RoomTypeImage image = roomTypeImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ảnh"));

        // Lấy public_id từ URL
        String imageUrl = image.getImageUrl();
        String publicId = extractPublicIdFromUrl(imageUrl);

        // Xóa từ Cloudinary
        if (publicId != null) {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("Deleted image from Cloudinary: {}", publicId);
        }

        // Xóa từ database
        roomTypeImageRepository.delete(image);
    }

    /**
     * Trích xuất public_id từ Cloudinary URL
     */
    private String extractPublicIdFromUrl(String imageUrl) {
        try {
            // URL format: https://res.cloudinary.com/xxx/image/upload/v123/hotel/room-types/abc.jpg
            String[] parts = imageUrl.split("/upload/");
            if (parts.length > 1) {
                String pathWithVersion = parts[1];
                // Bỏ version number (vXXX/)
                String path = pathWithVersion.replaceFirst("v\\d+/", "");
                // Bỏ extension
                return path.substring(0, path.lastIndexOf('.'));
            }
        } catch (Exception e) {
            log.error("Error extracting public_id from URL: {}", imageUrl, e);
        }
        return null;
    }
}
