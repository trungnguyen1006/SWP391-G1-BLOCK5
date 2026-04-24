package com.hotelmanage.service.room;

import com.hotelmanage.entity.User;
import com.hotelmanage.entity.room.RoomType;
import com.hotelmanage.entity.room.RoomTypeReview;
import com.hotelmanage.repository.UserRepository;
import com.hotelmanage.repository.room.RoomTypeRepository;
import com.hotelmanage.repository.room.RoomTypeReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomTypeReviewService {

    private final RoomTypeReviewRepository reviewRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<RoomTypeReview> getReviewsForRoomType(Integer roomTypeId) {
        return reviewRepository.findAllByRoomTypeIdWithUser(roomTypeId);
    }

    @Transactional(readOnly = true)
    public Optional<RoomTypeReview> findUserReview(Integer roomTypeId, Long userId) {
        return reviewRepository.findByRoomTypeIdAndUserId(roomTypeId, userId);
    }

    @Transactional
    public RoomTypeReview submitReview(Integer roomTypeId, String username, Integer rating, String comment) {
        validateRating(rating);

        RoomType roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy loại phòng với ID " + roomTypeId));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng: " + username));

        RoomTypeReview review = reviewRepository.findByRoomTypeIdAndUserId(roomTypeId, user.getId())
                .orElseGet(() -> {
                    RoomTypeReview newReview = new RoomTypeReview();
                    newReview.setRoomType(roomType);
                    newReview.setUser(user);
                    return newReview;
                });

        review.setRating(rating);
        review.setComment(comment != null && !comment.isBlank() ? comment.trim() : null);

        RoomTypeReview savedReview = reviewRepository.save(review);
        log.info("User {} submitted review for room type {} with rating {}", username, roomTypeId, rating);
        return savedReview;
    }

    @Transactional(readOnly = true)
    public double calculateAverageRating(Integer roomTypeId) {
        Double average = reviewRepository.findAverageRatingByRoomTypeId(roomTypeId);
        return average != null ? average : 0.0d;
    }

    @Transactional(readOnly = true)
    public long countReviews(Integer roomTypeId) {
        return reviewRepository.countByRoomTypeRoomTypeId(roomTypeId);
    }

    private void validateRating(Integer rating) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Điểm đánh giá phải nằm trong khoảng từ 1 đến 5.");
        }
    }
}

