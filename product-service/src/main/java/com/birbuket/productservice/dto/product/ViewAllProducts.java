package com.birbuket.productservice.dto.product;

import lombok.Data;

import java.util.List;


@Data
public class ViewAllProducts {

    List<ProductByIdResponse> products;
}
