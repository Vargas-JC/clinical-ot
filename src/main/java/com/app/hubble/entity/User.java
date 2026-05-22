package com.app.hubble.entity;

import com.app.hubble.enumeration.SpecialityType;
import com.app.hubble.enumeration.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table("users")
public class User {
    @Id
    private UUID id;

    private String email;

    @Column("password_hash")
    private String passwordHash;

    @Column("full_name")
    private String fullName;

    private String phone;
    private UserRole role;
    private boolean active;

    @Column("document_number")
    private String documentNumber;

    @Column("birth_date")
    private LocalDate birthDate;

    private SpecialityType specialty;

    @Column("avatar_file_id")
    private UUID avatarFileId;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;

    @Column("deleted_at")
    private LocalDateTime deletedAt;
}
