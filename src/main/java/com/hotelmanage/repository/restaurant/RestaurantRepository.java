package com.hotelmanage.repository.restaurant;


import com.hotelmanage.entity.restaurant.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    List<Restaurant> findAllByOrderByNameAsc();
    Page<Restaurant> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
