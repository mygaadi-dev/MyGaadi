package com.mygaadi.repository;

import com.mygaadi.model.entity.Car;
import com.mygaadi.model.entity.User;
import com.mygaadi.model.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    List<Wishlist> findByUserAndDeletedFalseOrderByAddedAtDesc(User user);
    Optional<Wishlist> findByUserAndCar(User user, Car car);
    Optional<Wishlist> findByUserAndCarAndDeletedFalse(User user, Car car);
    boolean existsByUserAndCarAndDeletedFalse(User user, Car car);
    long countByUserAndDeletedFalse(User user);
    void deleteByUserAndCar(User user, Car car);
}
