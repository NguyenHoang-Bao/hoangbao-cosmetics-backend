package com.hoangbaoshop.hoangbaocosmetics_backend.service;

import com.hoangbaoshop.hoangbaocosmetics_backend.dto.common.PageResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.product.ProductRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.request.product.ProductVariantRequest;
import com.hoangbaoshop.hoangbaocosmetics_backend.dto.response.product.ProductResponse;
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.Brand;
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.Category;
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.Product;
import com.hoangbaoshop.hoangbaocosmetics_backend.entity.ProductVariant;
import com.hoangbaoshop.hoangbaocosmetics_backend.exception.ResourceNotFoundException;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.BrandRepository;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.CategoryRepository;
import com.hoangbaoshop.hoangbaocosmetics_backend.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private BrandRepository brandRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void testGetProductById_Success() {
        Category category = Category.builder().idCategory(1).name("Chăm sóc da").slug("cham-soc-da").build();
        Brand brand = Brand.builder().idBrand(1).name("Cocoon").build();
        Product product = Product.builder()
                .idProduct(10)
                .name("Tẩy trang bí đao")
                .category(category)
                .brand(brand)
                .isActive(true)
                .variants(new ArrayList<>())
                .images(new ArrayList<>())
                .build();

        when(productRepository.findById(10)).thenReturn(Optional.of(product));

        ProductResponse response = productService.getProductById(10);

        assertNotNull(response);
        assertEquals(10, response.getIdProduct());
        assertEquals("Tẩy trang bí đao", response.getName());
    }

    @Test
    void testGetProductById_NotFound_ThrowsException() {
        when(productRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(999));
    }

    @Test
    void testGetAllProducts_ReturnsPagedResponse() {
        Pageable pageable = PageRequest.of(0, 10);
        Product product = Product.builder()
                .idProduct(1)
                .name("Son dưỡng")
                .isActive(true)
                .variants(new ArrayList<>())
                .images(new ArrayList<>())
                .build();
        Page<Product> page = new PageImpl<>(Collections.singletonList(product), pageable, 1);

        when(productRepository.searchProducts("son", null, null, pageable)).thenReturn(page);

        PageResponse<ProductResponse> response = productService.getAllProducts("son", null, null, pageable);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals(1L, response.getTotalElements());
    }

    @Test
    void testUpdateProduct_InPlaceVariantMerging_PreservesExistingStockAndSoftDeletesRemoved() {
        Category category = Category.builder().idCategory(1).name("Skincare").build();
        Brand brand = Brand.builder().idBrand(1).name("Cocoon").build();

        ProductVariant existingVar1 = ProductVariant.builder()
                .idVariant(101)
                .sku("SKU-100ML")
                .capacity("100ml")
                .price(BigDecimal.valueOf(150000))
                .originalPrice(BigDecimal.valueOf(120000))
                .stockQuantity(50) // Quan trọng: Tồn kho phải giữ nguyên
                .isActive(true)
                .build();

        ProductVariant existingVar2 = ProductVariant.builder()
                .idVariant(102)
                .sku("SKU-300ML")
                .capacity("300ml")
                .price(BigDecimal.valueOf(300000))
                .originalPrice(BigDecimal.valueOf(250000))
                .stockQuantity(30)
                .isActive(true)
                .build();

        List<ProductVariant> variantsList = new ArrayList<>(Arrays.asList(existingVar1, existingVar2));

        Product existingProduct = Product.builder()
                .idProduct(10)
                .name("Nước bí đao")
                .category(category)
                .brand(brand)
                .variants(variantsList)
                .images(new ArrayList<>())
                .isActive(true)
                .build();

        when(productRepository.findById(10)).thenReturn(Optional.of(existingProduct));
        when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
        when(brandRepository.findById(1)).thenReturn(Optional.of(brand));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Request: cập nhật var101 (giá mới), xóa var102, thêm var mới 500ml
        ProductVariantRequest reqVar1 = ProductVariantRequest.builder()
                .idVariant(101)
                .sku("SKU-100ML-NEW")
                .capacity("100ml")
                .price(BigDecimal.valueOf(160000))
                .originalPrice(BigDecimal.valueOf(130000))
                .build();

        ProductVariantRequest reqNewVar = ProductVariantRequest.builder()
                .idVariant(null)
                .sku("SKU-500ML")
                .capacity("500ml")
                .price(BigDecimal.valueOf(450000))
                .originalPrice(BigDecimal.valueOf(380000))
                .stockQuantity(20)
                .build();

        ProductRequest updateReq = ProductRequest.builder()
                .name("Nước bí đao cải tiến")
                .idCategory(1)
                .idBrand(1)
                .variants(Arrays.asList(reqVar1, reqNewVar))
                .build();

        ProductResponse updatedResponse = productService.updateProduct(10, updateReq);

        assertNotNull(updatedResponse);
        // var101 giữ nguyên tồn kho 50 và cập nhật giá mới
        assertEquals(50, existingVar1.getStockQuantity());
        assertEquals(0, BigDecimal.valueOf(160000).compareTo(existingVar1.getPrice()));
        assertEquals("SKU-100ML-NEW", existingVar1.getSku());
        assertTrue(existingVar1.getIsActive());

        // var102 không còn trong request -> Bị soft-delete (isActive = false), KHÔNG bị xóa khỏi list
        assertFalse(existingVar2.getIsActive());

        // Biến thể mới được thêm vào collection
        assertEquals(3, existingProduct.getVariants().size());
        verify(productRepository, times(1)).save(existingProduct);
    }

    @Test
    void testDeleteProduct_SoftDeletesProductAndVariants() {
        ProductVariant v1 = ProductVariant.builder().idVariant(1).isActive(true).build();
        ProductVariant v2 = ProductVariant.builder().idVariant(2).isActive(true).build();
        Product product = Product.builder()
                .idProduct(5)
                .isActive(true)
                .variants(new ArrayList<>(Arrays.asList(v1, v2)))
                .build();

        when(productRepository.findById(5)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        productService.deleteProduct(5);

        assertFalse(product.getIsActive());
        assertFalse(v1.getIsActive());
        assertFalse(v2.getIsActive());
        verify(productRepository, times(1)).save(product);
        verify(productRepository, never()).delete(any());
        verify(productRepository, never()).deleteById(any());
    }
}
