package com.hoangbaoshop.hoangbaocosmetics_backend.service;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.product.CategoryRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.product.CategoryResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.Category;
import com.hoangbaoshop.hoangbaocosmetics_backend.exception.BadRequestException;
import com.hoangbaoshop.hoangbaocosmetics_backend.exception.ResourceNotFoundException;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.CategoryRepository;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    void testGetAllCategories_ReturnsList() {
        Category cat = Category.builder().idCategory(1).name("Chăm sóc da").slug("cham-soc-da").build();
        when(categoryRepository.findAll()).thenReturn(Collections.singletonList(cat));

        List<CategoryResponse> list = categoryService.getAllCategories();

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("Chăm sóc da", list.get(0).getName());
    }

    @Test
    void testGetCategoryById_Success() {
        Category cat = Category.builder().idCategory(1).name("Chăm sóc da").slug("cham-soc-da").build();
        when(categoryRepository.findById(1)).thenReturn(Optional.of(cat));

        CategoryResponse res = categoryService.getCategoryById(1);

        assertNotNull(res);
        assertEquals(1, res.getIdCategory());
    }

    @Test
    void testGetCategoryById_NotFound_ThrowsException() {
        when(categoryRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.getCategoryById(99));
    }

    @Test
    void testCreateCategory_Success() {
        CategoryRequest req = CategoryRequest.builder().name("Son môi").build();
        when(categoryRepository.existsByName("Son môi")).thenReturn(false);
        when(categoryRepository.existsBySlug("son-moi")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CategoryResponse res = categoryService.createCategory(req);

        assertNotNull(res);
        assertEquals("Son môi", res.getName());
        assertEquals("son-moi", res.getSlug());
    }

    @Test
    void testCreateCategory_DuplicateName_ThrowsBadRequestException() {
        CategoryRequest req = CategoryRequest.builder().name("Son môi").build();
        when(categoryRepository.existsByName("Son môi")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> categoryService.createCategory(req));
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void testDeleteCategory_HasLinkedProducts_ThrowsBadRequestException() {
        Category cat = Category.builder().idCategory(1).build();
        when(categoryRepository.findById(1)).thenReturn(Optional.of(cat));
        when(productRepository.existsByCategory_IdCategory(1)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> categoryService.deleteCategory(1));
        verify(categoryRepository, never()).delete(any());
    }

    @Test
    void testDeleteCategory_Success() {
        Category cat = Category.builder().idCategory(1).build();
        when(categoryRepository.findById(1)).thenReturn(Optional.of(cat));
        when(productRepository.existsByCategory_IdCategory(1)).thenReturn(false);

        categoryService.deleteCategory(1);

        verify(categoryRepository, times(1)).delete(cat);
    }
}
