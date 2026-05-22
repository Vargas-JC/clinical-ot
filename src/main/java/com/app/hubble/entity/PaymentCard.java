package com.app.hubble.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table("payment_cards")
public class PaymentCard {
    @Id
    private UUID id;

    @Column("user_id")
    private UUID userId;
    private String provider;

    @Column("provider_card_token")
    private String providerCardToken;
    private String brand;

    @Column("last_four")
    private String lastFour;

    @Column("exp_month")
    private short expMonth;

    @Column("exp_year")
    private short expYear;

    @Column("holder_name")
    private String holderName;

    @Column("is_default")
    private boolean defaultForPatient;
    private boolean active;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;

    @Column("deleted_at")
    private LocalDateTime deletedAt;
}
