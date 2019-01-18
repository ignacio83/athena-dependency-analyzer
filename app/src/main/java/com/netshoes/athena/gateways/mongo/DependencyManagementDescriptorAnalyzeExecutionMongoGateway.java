package com.netshoes.athena.gateways.mongo;

import com.netshoes.athena.domains.DependencyManagementDescriptorAnalyzeExecution;
import com.netshoes.athena.gateways.DependencyManagementDescriptorAnalyzeExecutionGateway;
import com.netshoes.athena.gateways.mongo.docs.DependencyManagementDescriptorAnalyzeExecutionDoc;
import com.netshoes.athena.gateways.mongo.repositories.DependencyManagementDescriptorAnalyzeExecutionRepository;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class DependencyManagementDescriptorAnalyzeExecutionMongoGateway
    implements DependencyManagementDescriptorAnalyzeExecutionGateway {
  private static int SIZE_10_MB = 1024 * 1024 * 10;
  private static final String COLLECTION_NAME = "dependencyManagementDescriptorAnalyzeExecution";
  private final DependencyManagementDescriptorAnalyzeExecutionRepository repository;
  private final MongoTemplate mongoTemplate;

  @PostConstruct
  public void init() {
    final String json =
        String.format("{ convertToCapped: '%s', size: %d}", COLLECTION_NAME, SIZE_10_MB);
    mongoTemplate.executeCommand(json);
  }

  @Override
  public Mono<DependencyManagementDescriptorAnalyzeExecution> save(
      DependencyManagementDescriptorAnalyzeExecution execution) {

    return Mono.just(execution)
        .map(DependencyManagementDescriptorAnalyzeExecutionDoc::new)
        .flatMap(repository::save)
        .doOnNext(
            doc ->
                log.debug(
                    "DependencyManagementAnalyzeExecution for project {} - {} saved.",
                    doc.getProject().getName(),
                    doc.getDescriptorPath()))
        .map(DependencyManagementDescriptorAnalyzeExecutionDoc::toDomain);
  }
}
