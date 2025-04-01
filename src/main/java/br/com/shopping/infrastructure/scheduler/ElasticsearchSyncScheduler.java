package br.com.shopping.infrastructure.scheduler;

import br.com.shopping.application.mapper.ProductMapper;
import br.com.shopping.domain.entity.Product;
import br.com.shopping.domain.repository.ProductRepository;
import br.com.shopping.infrastructure.elasticsearch.document.ProductDocument;
import br.com.shopping.infrastructure.elasticsearch.repository.ProductElasticsearchRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ElasticsearchSyncScheduler {

    private final ProductRepository productRepository;
    private final ProductElasticsearchRepository elasticsearchRepository;
    private final ProductMapper productMapper;


    public ElasticsearchSyncScheduler(
            ProductRepository productRepository,
            ProductElasticsearchRepository elasticsearchRepository,
            ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.elasticsearchRepository = elasticsearchRepository;
        this.productMapper = productMapper;
    }


    @Scheduled(fixedRate = 600000)
    public void syncProductsToElasticsearch() {
        log.info("Iniciando sincronização agendada de produtos com Elasticsearch");
        
        try {

            List<Product> products = productRepository.findByActiveTrue();
            
            List<ProductDocument> documents = products.stream()
                    .map(productMapper::toDocument)
                    .collect(Collectors.toList());
            
            elasticsearchRepository.deleteAll();
            elasticsearchRepository.saveAll(documents);
            
            log.info("Sincronização agendada concluída. {} produtos indexados", documents.size());
        } catch (Exception e) {
            log.error("Erro durante a sincronização agendada com Elasticsearch", e);
        }
    }
}