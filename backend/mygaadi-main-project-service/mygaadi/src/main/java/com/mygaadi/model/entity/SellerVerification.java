package com.mygaadi.model.entity;

import com.mygaadi.model.enums.DocumentType;
import com.mygaadi.model.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "seller_verification", indexes = @Index(name = "idx_verification_status", columnList = "status"))
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class SellerVerification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "verification_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 20)
    private DocumentType documentType;

    @Column(name = "document_number", nullable = false, length = 80)
    private String documentNumber;

    @Column(name = "document_url", nullable = false, columnDefinition = "TEXT")
    private String documentUrl;

    @Column(name = "document_back_url", columnDefinition = "TEXT")
    private String documentBackUrl;

    @Column(name = "selfie_url", columnDefinition = "TEXT")
    private String selfieUrl;

    @Column(name = "extra_document_urls", columnDefinition = "JSON")
    private String extraDocumentUrls;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VerificationStatus status;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "submitted_at", nullable = false, updatable = false)
    private Instant submittedAt;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @PrePersist
    void onCreate() {
        submittedAt = Instant.now();
        if (status == null) status = VerificationStatus.PENDING;
    }
}
