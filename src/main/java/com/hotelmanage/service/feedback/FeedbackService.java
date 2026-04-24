package com.hotelmanage.service.feedback;

import com.hotelmanage.entity.Enum.FeedbackCategory;
import com.hotelmanage.entity.User;
import com.hotelmanage.entity.feedback.Feedback;
import com.hotelmanage.entity.restaurant.Restaurant;
import com.hotelmanage.entity.room.RoomType;
import com.hotelmanage.repository.UserRepository;
import com.hotelmanage.repository.feedback.FeedbackRepository;
import com.hotelmanage.repository.restaurant.RestaurantRepository;
import com.hotelmanage.repository.room.RoomTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final RoomTypeRepository roomTypeRepository;

    @Transactional
    public Feedback submitFeedback(String username,
                                   FeedbackCategory category,
                                   Long restaurantId,
                                   Integer roomTypeId,
                                   String content) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng: " + username));

        Feedback feedback = new Feedback();
        feedback.setUser(user);
        feedback.setCategory(category);
        feedback.setContent(content);

        if (restaurantId != null) {
            Restaurant restaurant = restaurantRepository.findById(restaurantId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhà hàng với ID: " + restaurantId));
            feedback.setRestaurant(restaurant);
        }

        if (roomTypeId != null) {
            RoomType roomType = roomTypeRepository.findById(roomTypeId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy loại phòng với ID: " + roomTypeId));
            feedback.setRoomType(roomType);
        }

        Feedback saved = feedbackRepository.save(feedback);
        log.info("User {} submitted feedback in category {}", username, category);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Feedback> findAll() {
        return feedbackRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<Feedback> findByCategory(FeedbackCategory category) {
        return feedbackRepository.findByCategoryOrderByCreatedAtDesc(category);
    }
}

