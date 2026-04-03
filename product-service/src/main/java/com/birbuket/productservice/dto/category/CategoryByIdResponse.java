package com.birbuket.productservice.dto.category;


import lombok.Data;

@Data
public class CategoryByIdResponse {

    Long id;

    String title;

    String subtitle;

    String imageUrl;
}
