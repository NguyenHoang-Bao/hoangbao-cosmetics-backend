package com.hoangbaoshop.hoangbaocosmetics_backend.service;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.product.CategoryRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.product.CategoryResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.Category;
import com.hoangbaoshop.hoangbaocosmetics_backend.exception.BadRequestException;
import com.hoangbaoshop.hoangbaocosmetics_backend.exception.ResourceNotFoundException;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.CategoryRepository;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Integer id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với ID: " + id));
        return mapToResponse(category);
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByName(request.getName().trim())) {
            throw new BadRequestException("Tên danh mục đã tồn tại: " + request.getName());
        }

        String slug = (request.getSlug() != null && !request.getSlug().isBlank())
                ? toSlug(request.getSlug())
                : toSlug(request.getName());

        if (categoryRepository.existsBySlug(slug)) {
            slug = slug + "-" + UUID.randomUUID().toString().substring(0, 4);
        }

        Category category = Category.builder()
                .name(request.getName().trim())
                .slug(slug)
                .build();

        Category saved = categoryRepository.save(category);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Integer id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với ID: " + id));

        if (!category.getName().equalsIgnoreCase(request.getName().trim()) 
                && categoryRepository.existsByName(request.getName().trim())) {
            throw new BadRequestException("Tên danh mục đã tồn tại: " + request.getName());
        }

        category.setName(request.getName().trim());

        if (request.getSlug() != null && !request.getSlug().isBlank()) {
            String newSlug = toSlug(request.getSlug());
            if (!newSlug.equals(category.getSlug()) && categoryRepository.existsBySlug(newSlug)) {
                newSlug = newSlug + "-" + UUID.randomUUID().toString().substring(0, 4);
            }
            category.setSlug(newSlug);
        }

        Category updated = categoryRepository.save(category);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteCategory(Integer id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục với ID: " + id));

        if (productRepository.existsByCategory_IdCategory(id)) {
            throw new BadRequestException("Không thể xóa danh mục đang có sản phẩm liên kết!");
        }

        categoryRepository.delete(category);
    }

    private CategoryResponse mapToResponse(Category category) {
        return CategoryResponse.builder()
                .idCategory(category.getIdCategory())
                .name(category.getName())
                .slug(category.getSlug())
                .build();
    }

    private String toSlug(String input) {
        if (input == null || input.isBlank()) {
            return UUID.randomUUID().toString().substring(0, 8);
        }
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        String noAccents = Pattern.compile("\\p{InCombiningDiacriticalMarks}+").matcher(normalized).replaceAll("");
        String slug = noAccents.toLowerCase(Locale.ROOT)
                .replace("đ", "d")
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-");
        return slug.isEmpty() ? UUID.randomUUID().toString().substring(0, 8) : slug;
    }
}
