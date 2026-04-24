package com.hotelmanage.service.room;

import com.hotelmanage.entity.Enum.RoomStatus;
import com.hotelmanage.entity.room.Room;
import com.hotelmanage.repository.room.RoomRepository;
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
public class RoomService {

    private final RoomRepository roomRepository;

    /**
     * Tìm phòng còn trống theo loại phòng và khoảng thời gian
     */
    public Room findAvailableRoom(Integer roomTypeId, LocalDate checkInDate, LocalDate checkOutDate) {
        log.info("Finding available room for roomType={}, checkIn={}, checkOut={}",
                roomTypeId, checkInDate, checkOutDate);

        return roomRepository.findAvailableRoomByRoomTypeAndDateRange(
                        roomTypeId, checkInDate, checkOutDate)
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy phòng trống cho loại phòng này trong thời gian đã chọn!"));
    }

    /**
     * Lấy tất cả các phòng chưa bị xóa
     */
    public List<Room> findAll() {
        log.info("Fetching all active rooms");
        return roomRepository.findAll()
                .stream()
                .filter(room -> room.getDeletedAt() == null)
                .collect(Collectors.toList());
    }

    /**
     * Phân trang danh sách phòng
     */
    public Page<Room> findPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return roomRepository.findAllActive(pageable);
    }

    /**
     * Tìm kiếm phòng có phân trang
     */
    public Page<Room> searchRooms(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        if (keyword == null || keyword.trim().isEmpty()) {
            return roomRepository.findAllActive(pageable);
        }
        return roomRepository.searchActiveRooms(keyword, pageable);
    }

    /**
     * Lấy phòng theo ID
     */
    public Room findById(Integer id) {
        log.info("Finding room by id: {}", id);
        return roomRepository.findById(id)
                .filter(room -> room.getDeletedAt() == null)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng với ID: " + id));
    }

    /**
     * Thêm mới phòng
     */
    public Room save(Room room) {
        log.info("Saving new room: {}", room.getRoomNumber());
        if (room.getStatus() == null) {
            room.setStatus(RoomStatus.AVAILABLE);
        }
        return roomRepository.save(room);
    }

    /**
     * Cập nhật thông tin phòng
     */
    public Room update(Room room) {
        log.info("Updating room: {}", room.getRoomId());
        Room existing = findById(room.getRoomId());
        existing.setRoomNumber(room.getRoomNumber());
        existing.setRoomType(room.getRoomType());
        existing.setStatus(room.getStatus());
        return roomRepository.save(existing);
    }

    /**
     * Xóa mềm phòng (đánh dấu thời gian xóa)
     */
    public void delete(Integer id) {
        log.info("Deleting room id: {}", id);
        Room room = findById(id);
        room.setDeletedAt(LocalDateTime.now());
        roomRepository.save(room);
    }
}