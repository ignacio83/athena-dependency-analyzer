package com.netshoes.athena.gateways.mongo.repositories;

import com.netshoes.athena.gateways.mongo.docs.ProjectDoc;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface ProjectRepository
    extends ReactiveCrudRepository<ProjectDoc, String>, CustomizedProjectRepository {

  @Query("{}")
  Flux<ProjectDoc> findAll(Pageable pageable);

  @Query(
      "{ 'descriptors.artifacts' : {"
          + "$elemMatch : {"
          + "groupId : ?0,"
          + "artifactId : ?1,"
          + "version : ?2 } } }")
  Flux<ProjectDoc> findByDescriptorsArtifacts(
      String groupId, String artifactId, String version, Sort sort);
}
