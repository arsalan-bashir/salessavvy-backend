package com.spring.salessavvy.services;

import com.spring.salessavvy.entities.Category;
import com.spring.salessavvy.entities.Product;
import com.spring.salessavvy.entities.ProductImage;
import com.spring.salessavvy.repositories.CategoryRepository;
import com.spring.salessavvy.repositories.ProductImageRepository;
import com.spring.salessavvy.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    ProductRepository productRepository;
    CategoryRepository categoryRepository;
    ProductImageRepository productImageRepository;

    @Autowired
    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository, ProductImageRepository productImageRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productImageRepository = productImageRepository;
    }

    public List<Product> getProductsByCategory(String categoryName) {
        if (categoryName != null && !categoryName.isEmpty()) {
            Optional<Category> categoryOpt = categoryRepository.findByCategoryName(categoryName);
            if (categoryOpt.isPresent()) {
                Category category = categoryOpt.get();
                return productRepository.findByCategory_CategoryId(category.getCategoryId());
            }
            else {
                throw new RuntimeException("Category not found");
            }
        }
        else {
            return productRepository.findAll();
        }
    }

    public List<String> getProductImages(int productId) {
        List<ProductImage> productImages = productImageRepository.findByProduct_ProductId(productId);
        List<String> imageUrls = new ArrayList<>();
        for(ProductImage img: productImages) {
            imageUrls.add(img.getImageUrl());
        }
        return imageUrls;
    }

    public List<String> getCategories() {
        List<String> categories = new ArrayList<>();

        categoryRepository.findAll().forEach(category -> {
            categories.add(category.getCategoryName());
        });
        return categories;
    }
}
