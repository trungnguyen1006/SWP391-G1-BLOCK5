package com.hotelmanage.service.room;

import com.hotelmanage.entity.room.RoomType;
import com.hotelmanage.repository.room.RoomTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RoomTypeService {

    private final RoomTypeRepository roomTypeRepository;

    public List<RoomType> searchAvailableRoomTypes(LocalDate checkInDate,
                                                   LocalDate checkOutDate,
                                                   Integer amountPerson) {
        log.info("Searching room types: checkIn={}, checkOut={}, persons={}",
                checkInDate, checkOutDate, amountPerson);

        List<Object[]> results = roomTypeRepository.findAvailableRoomTypes(
                checkInDate, checkOutDate, amountPerson);

        return results.stream()
                .map(result -> {
                    RoomType roomType = (RoomType) result[0];
                    Long availableCount = (Long) result[1];
                    roomType.setAvailableCount(availableCount.intValue());
                    return roomType;
                })
                .collect(Collectors.toList());
    }

    public RoomType findById(Integer roomTypeId) {
        return roomTypeRepository.findById(roomTypeId)
                .filter(rt -> rt.getDeletedAt() == null)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy loại phòng với ID: " + roomTypeId));
    }

    public List<RoomType> findAll() {
        return roomTypeRepository.findAll().stream()
                .filter(rt -> rt.getDeletedAt() == null)
                .collect(Collectors.toList());
    }

    public RoomType save(RoomType roomType) {
        return roomTypeRepository.save(roomType);
    }

    public RoomType update(RoomType roomType) {
        RoomType existing = findById(roomType.getRoomTypeId());
        existing.setRoomTypeName(roomType.getRoomTypeName());
        existing.setDescription(roomType.getDescription());
        existing.setDevice(roomType.getDevice());
        existing.setUtilities(roomType.getUtilities());
        existing.setPrice(roomType.getPrice());
        existing.setAmountPerson(roomType.getAmountPerson());
        return roomTypeRepository.save(existing);
    }

    public void delete(Integer id) {
        RoomType roomType = findById(id);
        if (roomType.getRooms() != null && !roomType.getRooms().isEmpty() &&
                roomType.getRooms().stream().anyMatch(r -> r.getDeletedAt() == null)) {
            throw new RuntimeException("Không thể xóa loại phòng vẫn còn phòng đang hoạt động!");
        }
        roomType.setDeletedAt(LocalDateTime.now());
        roomTypeRepository.save(roomType);
    }

    /**
     * Tìm tất cả loại phòng với phân trang
     */
    public Page<RoomType> findAll(Pageable pageable) {
        return roomTypeRepository.findAllActive(pageable);
    }

}
