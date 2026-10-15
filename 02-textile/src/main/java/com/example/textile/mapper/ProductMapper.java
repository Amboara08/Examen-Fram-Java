package com.example.textile.mapper;

import com.example.textile.entity.Product;
import com.example.textile.dto.request.ProductRequest;
import com.example.textile.dto.response.ProductResponse;
public final class ProductMapper {
    private ProductMapper() {}
    public static ProductResponse response(Product e) { return new ProductResponse(e.getId(), e.getVersion(), e.getName(), e.getReference(), e.getDescription(), e.getUnitPrice()); }
    public static void apply(ProductRequest r, Product e) {
        e.setName(r.name());
        e.setReference(r.reference());
        e.setDescription(r.description());
        e.setUnitPrice(r.unitPrice());
    }
}
