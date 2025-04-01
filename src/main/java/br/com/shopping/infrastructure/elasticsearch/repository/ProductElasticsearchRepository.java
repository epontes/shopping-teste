package br.com.shopping.infrastructure.elasticsearch.repository;

import br.com.shopping.infrastructure.elasticsearch.document.ProductDocument;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductElasticsearchRepository extends ElasticsearchRepository<ProductDocument, String> {

    List<ProductDocument> findByNameContainingAndStockGreaterThanAndActive(String name, Integer stock, Boolean active);
    
    List<ProductDocument> findByCategoryAndStockGreaterThanAndActive(String category, Integer stock, Boolean active);
    
    @Query("{\"bool\": {\"must\": [{\"range\": {\"price\": {\"gte\": \"?0\", \"lte\": \"?1\"}}}, {\"range\": {\"stock\": {\"gt\": 0}}}, {\"term\": {\"active\": true}}]}}")
    List<ProductDocument> findByPriceBetweenAndStockGreaterThanAndActive(BigDecimal minPrice, BigDecimal maxPrice);
}