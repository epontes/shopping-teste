package br.com.shopping.application.service;

import br.com.shopping.application.dto.ProductDTO;
import br.com.shopping.application.mapper.ProductMapper;
import br.com.shopping.domain.entity.Product;
import br.com.shopping.domain.exception.ProductNotFoundException;
import br.com.shopping.domain.exception.StockInsufficientException;
import br.com.shopping.domain.repository.ProductRepository;
import br.com.shopping.infrastructure.elasticsearch.document.ProductDocument;
import br.com.shopping.infrastructure.elasticsearch.repository.ProductElasticsearchRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductElasticsearchRepository elasticsearchRepository;
    private final ProductMapper productMapper;

    public List<ProductDTO> getAllProducts() {
        return productRepository.findByActiveTrue()
                .stream()
                .map(productMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<ProductDTO> getAllProductsInStock() {
        return productRepository.findAllInStock()
                .stream()
                .map(productMapper::toDTO)
                .collect(Collectors.toList());
    }

    public ProductDTO getProductById(Long id) {
        return productRepository.findById(id)
                .filter(Product::getActive)
                .map(productMapper::toDTO)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
    }

    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO) {
        Product product = productMapper.toEntity(productDTO);
        product.setActive(true);
        
        Product savedProduct = productRepository.save(product);

        ProductDocument document = productMapper.toDocument(savedProduct);
        elasticsearchRepository.save(document);
        
        return productMapper.toDTO(savedProduct);
    }

    @Transactional
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
        
        productMapper.updateEntityFromDTO(productDTO, existingProduct);
        Product updatedProduct = productRepository.save(existingProduct);
        
        ProductDocument document = productMapper.toDocument(updatedProduct);
        elasticsearchRepository.save(document);
        
        return productMapper.toDTO(updatedProduct);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
        product.setActive(false);
        productRepository.save(product);
        
        elasticsearchRepository.deleteById(String.valueOf(id));
    }

    @Transactional
    public void updateProductStock(Long productId, Integer quantity) {
        int updatedRows = productRepository.decreaseStock(productId, quantity);
        
        if (updatedRows == 0) {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));
            
            if (product.getStock() < quantity) {
                throw new StockInsufficientException("Insufficient stock for product: " + product.getName());
            }
        }
        
        Product updatedProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));
        
        ProductDocument document = productMapper.toDocument(updatedProduct);
        elasticsearchRepository.save(document);
    }

    public boolean checkProductStock(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));
        
        return product.getStock() >= quantity;
    }

    public List<ProductDTO> searchProductsByName(String name) {
        return elasticsearchRepository.findByNameContainingAndStockGreaterThanAndActive(name, 0, true)
                .stream()
                .map(productMapper::dtoFromDocument)
                .collect(Collectors.toList());
    }

    public List<ProductDTO> searchProductsByCategory(String category) {
        return elasticsearchRepository.findByCategoryAndStockGreaterThanAndActive(category, 0, true)
                .stream()
                .map(productMapper::dtoFromDocument)
                .collect(Collectors.toList());
    }

    public List<ProductDTO> searchProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return elasticsearchRepository.findByPriceBetweenAndStockGreaterThanAndActive(minPrice, maxPrice)
                .stream()
                .map(productMapper::dtoFromDocument)
                .collect(Collectors.toList());
    }
}