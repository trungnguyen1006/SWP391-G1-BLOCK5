package com.hotelmanage.service.booking;

import com.hotelmanage.entity.booking.Promotion;
import com.hotelmanage.repository.booking.PromotionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PromotionService {

    private final PromotionRepository promotionRepository;

    public List<Promotion> searchAndSort(String keyword, String sortDirection) {
        org.springframework.data.domain.Sort sort = org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "promotionId"); // Default sort by ID desc

        if ("asc".equalsIgnoreCase(sortDirection)) {
            sort = org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "discountAmount");
        } else if ("desc".equalsIgnoreCase(sortDirection)) {
            sort = org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "discountAmount");
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            return promotionRepository.findByCodeContainingIgnoreCase(keyword.trim(), sort);
        } else {
            return promotionRepository.findAll(sort);
        }
    }

    /**
     * Validate và trả về promotion nếu hợp lệ
     */
    public Promotion validatePromotion(String code) {
        log.info("Validating promotion code: {}", code);

        Promotion promotion = promotionRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Mã giảm giá không tồn tại!"));

        LocalDate today = LocalDate.now();

        if (!promotion.getIsActive()) {
            throw new RuntimeException("Mã giảm giá đã hết hiệu lực!");
        }

        if (today.isBefore(promotion.getStartDate())) {
            throw new RuntimeException("Mã giảm giá chưa có hiệu lực!");
        }

        if (today.isAfter(promotion.getEndDate())) {
            throw new RuntimeException("Mã giảm giá đã hết hạn!");
        }

        log.info("Promotion code validated successfully: {}", code);
        return promotion;
    }

    public List<Promotion> findAllActive() {
        LocalDate today = LocalDate.now();
        return promotionRepository.findByIsActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                today, today);
    }

    public Promotion findById(Integer id) {
        return promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy promotion với ID: " + id));
    }

    public Promotion save(Promotion promotion) {
        if (promotionRepository.existsByCode(promotion.getCode())) {
            throw new RuntimeException("Mã giảm giá '" + promotion.getCode() + "' đã tồn tại!");
        }
        return promotionRepository.save(promotion);
    }

    public Promotion update(Promotion promotion) {
        Promotion existing = findById(promotion.getPromotionId());

        if (!existing.getCode().equalsIgnoreCase(promotion.getCode()) &&
                promotionRepository.existsByCodeAndPromotionIdNot(promotion.getCode(), promotion.getPromotionId())) {
            throw new RuntimeException("Mã giảm giá '" + promotion.getCode() + "' đã tồn tại!");
        }

        existing.setCode(promotion.getCode());
        existing.setDiscountAmount(promotion.getDiscountAmount());
        existing.setStartDate(promotion.getStartDate());
        existing.setEndDate(promotion.getEndDate());
        existing.setIsActive(promotion.getIsActive());
        return promotionRepository.save(existing);
    }

    public void deactivate(Integer id) {
        Promotion promotion = findById(id);
        promotion.setIsActive(false);
        promotionRepository.save(promotion);
    }
}
