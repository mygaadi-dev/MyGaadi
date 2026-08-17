package com.mygaadi.model.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "buyer_profiles")
@Getter @Setter @SuperBuilder @NoArgsConstructor @AllArgsConstructor
public class BuyerProfile extends RoleProfileBase {
    @Column(name = "preferred_city", length = 80)
    private String preferredCity;

    @Column(name = "budget_min")
    private Long budgetMin;

    @Column(name = "budget_max")
    private Long budgetMax;
}
