package com.mygaadi.model.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "seller_profiles")
@Getter @Setter @SuperBuilder @NoArgsConstructor @AllArgsConstructor
public class SellerProfile extends RoleProfileBase {
    @Column(name = "business_name", length = 160)
    private String businessName;

    @Column(name = "pan_number", length = 10)
    private String panNumber;

    @Column(name = "kyc_holder_name", length = 120)
    private String kycHolderName;

    @Column(name = "kyc_verified", nullable = false)
    private boolean kycVerified;
}
