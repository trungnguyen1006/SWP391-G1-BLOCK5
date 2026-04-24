package com.hotelmanage.repository.feedback;

import com.hotelmanage.entity.Enum.FeedbackCategory;
import com.hotelmanage.entity.feedback.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    List<Feedback> findAllByOrderByCreatedAtDesc();

    List<Feedback> findByCategoryOrderByCreatedAtDesc(FeedbackCategory category);
}

