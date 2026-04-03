package com.birbuket.productservice.dto.category;


import lombok.Data;

@Data
public class UpdateCategoryResponse {

    Long id;

    String title;

    String subtitle;

    String imageUrl;
}
