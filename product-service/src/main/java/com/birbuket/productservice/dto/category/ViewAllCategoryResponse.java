package com.birbuket.productservice.dto.category;

import lombok.Data;

import java.util.List;


@Data
public class ViewAllCategoryResponse {

    List<CategoryByIdResponse> category;
}
