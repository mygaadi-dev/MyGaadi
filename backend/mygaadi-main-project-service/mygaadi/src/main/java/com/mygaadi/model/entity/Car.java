package com.mygaadi.model.entity;

import com.mygaadi.model.enums.CarStatus;
import com.mygaadi.model.enums.FuelType;
import com.mygaadi.model.enums.Transmission;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cars", indexes = {
        @Index(name = "idx_cars_status", columnList = "status"),
        @Index(name = "idx_cars_location", columnList = "location_city, location_state")
})
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Car {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "car_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(nullable = false, length = 80)
    private String brand;

    @Column(nullable = false, length = 80)
    private String model;

    @Column(length = 80)
    private String variant;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "booking_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal bookingAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "fuel_type", nullable = false, length = 20)
    private FuelType fuelType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Transmission transmission;

    @Column(name = "mileage_km", nullable = false)
    private Integer mileageKm;

    @Column(name = "engine_cc")
    private Integer engineCc;

    @Column(length = 40)
    private String color;

    @Column(name = "no_of_owners", nullable = false)
    private Integer noOfOwners;

    @Column(name = "insurance_valid_till")
    private LocalDate insuranceValidTill;

    @Builder.Default
    @Column(name = "rc_available", nullable = false)
    private boolean rcAvailable = true;

    @Column(name = "location_city", nullable = false, length = 80)
    private String locationCity;

    @Column(name = "location_state", nullable = false, length = 80)
    private String locationState;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CarStatus status;

    @Column(name = "is_featured", nullable = false)
    private boolean featured;

    @Column(name = "view_count", nullable = false)
    private int viewCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Builder.Default
    @OneToMany(mappedBy = "car", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<CarImage> images = new ArrayList<>();

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) status = CarStatus.PENDING;
        if (mileageKm == null) mileageKm = 0;
        if (noOfOwners == null) noOfOwners = 1;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public void addImage(CarImage image) {
        images.add(image);
        image.setCar(this);
    }

    public void clearImages() {
        images.forEach(img -> img.setCar(null));
        images.clear();
    }
}
