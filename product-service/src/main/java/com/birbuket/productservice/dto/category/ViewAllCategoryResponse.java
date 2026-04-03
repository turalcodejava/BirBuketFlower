package com.birbuket.productservice.dto.category;

import com.birbuket.productservice.models.ProductCategory;
import lombok.Data;

import java.util.List;


@Data
public class ViewAllCategoryResponse {

    List<CategoryByIdResponse> category;
}
