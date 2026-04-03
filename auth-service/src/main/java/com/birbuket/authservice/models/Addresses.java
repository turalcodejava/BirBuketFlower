package com.birbuket.authservice.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "addresses")
public class Addresses {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "City boş ola bilməz")
    @Size(min = 2, max = 50)
    @Pattern(
            regexp = "^[A-Za-zƏəĞğİıÖöŞşÜüÇç\\s]+$",
            message = "City yalnız hərflərdən ibarət olmalıdır"
    )
    @Column(name = "city", nullable = false, length = 50)
    private String city;

    @Column(name = "district", length = 50)
    private String district;

    @Column(name = "street", length = 100)
    private String street;

    @Column(name = "building", length = 20)
    private String building;

    @Column(name = "apartment", length = 20)
    private String apartment;

    @Column(name = "full_address_line", length = 255)
    private String fullAddressLine;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;
}