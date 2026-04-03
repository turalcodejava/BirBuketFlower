package com.birbuket.productservice.dto.category;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateCategoryResponse {

    Long id;

    String title;

    String subtitle;

    String imageUrl;
}