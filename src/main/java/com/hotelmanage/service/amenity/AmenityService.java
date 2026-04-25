package com.hotelmanage.service.amenity;

import com.hotelmanage.entity.amenity.Amenity;
import com.hotelmanage.repository.amenity.AmenityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AmenityService {
    private final AmenityRepository amenityRepository;

    /**
     * Lấy tất cả các amenity chưa bị xóa
     */
    public List<Amenity> findAll() {
        log.info("Fetching all active amenity");
        return amenityRepository.findAll()
                .stream()
                .filter(a -> a.getDeletedAt() == null)
                .collect(Collectors.toList());
    }

    /**
     * Lấy amenity theo ID
     */
    public Amenity findById(Integer id) {
        log.info("Finding amenity by id: {}", id);
        return amenityRepository.findById(id)
                .filter(a -> a.getDeletedAt() == null)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy amenity với ID: " + id));
    }

    /**
     * Thêm mới amenity
     */
    public Amenity save(Amenity amenity) {
        log.info("Saving new amenity: {}", amenity.getAmenityName());
        return amenityRepository.save(amenity);
    }

    /**
     * Cập nhật thông amenity
     */
    public Amenity update(Amenity amenity) {
        log.info("Updating amenity: {}", amenity.getAmenityId());
        Amenity existing = findById(amenity.getAmenityId());
        existing.setAmenityName(amenity.getAmenityName());
        existing.setImageUrl(amenity.getImageUrl());
        existing.setDescription(amenity.getDescription());
        return amenityRepository.save(existing);
    }

    /**
     * Xóa mềm amenity (đánh dấu thời gian xóa)
     */
    public void delete(Integer id) {
        log.info("Deleting amenity id: {}", id);
        Amenity amenity = findById(id);
        amenity.setDeletedAt(LocalDateTime.now());
        amenityRepository.save(amenity);
    }

    public Page<Amenity> getAmenities(int page, int size) {
        return amenityRepository.findByDeletedAtIsNull(PageRequest.of(page, size));
    }

}
