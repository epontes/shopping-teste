package br.com.shopping.application.mapper;

import br.com.shopping.application.dto.ProductDTO;
import br.com.shopping.domain.entity.Product;
import br.com.shopping.infrastructure.elasticsearch.document.ProductDocument;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductDTO toDTO(Product entity) {
        return ProductDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .stock(entity.getStock())
                .category(entity.getCategory())
                .active(entity.getActive())
                .build();
    }
    
    public Product toEntity(ProductDTO dto) {
        return Product.builder()
                .id(dto.getId())
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .stock(dto.getStock())
                .category(dto.getCategory())
                .active(dto.getActive())
                .build();
    }
    
    public void updateEntityFromDTO(ProductDTO dto, Product entity) {
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setPrice(dto.getPrice());
        entity.setStock(dto.getStock());
        entity.setCategory(dto.getCategory());
        entity.setActive(dto.getActive());
    }
    
    public ProductDocument toDocument(Product entity) {
        return ProductDocument.builder()
                .id(entity.getId().toString())
                .name(entity.getName())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .stock(entity.getStock())
                .category(entity.getCategory())
                .active(entity.getActive())
                .build();
    }
    
    public ProductDTO dtoFromDocument(ProductDocument document) {
        return ProductDTO.builder()
                .id(Long.valueOf(document.getId()))
                .name(document.getName())
                .description(document.getDescription())
                .price(document.getPrice())
                .stock(document.getStock())
                .category(document.getCategory())
                .active(document.getActive())
                .build();
    }
}