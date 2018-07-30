package com.netshoes.athena.gateways.mongo;

import com.netshoes.athena.domains.Project;
import com.netshoes.athena.domains.RequestOfPage;
import com.netshoes.athena.gateways.ProjectGateway;
import com.netshoes.athena.gateways.mongo.docs.ProjectDoc;
import com.netshoes.athena.gateways.mongo.repositories.ProjectRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@AllArgsConstructor
@Slf4j
public class ProjectMongoGateway implements ProjectGateway {

  private final ProjectRepository projectRepository;
  private final PaginationHelper paginationHelper;

  @Override
  public Mono<Project> findById(String id) {
    final Mono<ProjectDoc> mono = projectRepository.findById(id);
    return mono.map(doc -> doc.toDomain(true));
  }

  @Override
  public Flux<Project> findAll() {
    return projectRepository.findAll().map(p -> p.toDomain(true));
  }

  @Override
  public Flux<Project> findAll(RequestOfPage requestOfPage) {
    return orderByName(requestOfPage)
        .flatMapMany(projectRepository::findAll)
        .map(project -> project.toDomain(true));
  }

  @Override
  public Flux<Project> findByNameContaining(RequestOfPage requestOfPage, String name) {
    return orderByName(requestOfPage)
        .flatMapMany(
            pageRequest ->
                projectRepository
                    .findByNameContaining(name, pageRequest)
                    .map(project -> project.toDomain(true)));
  }

  @Override
  public Mono<Long> count() {
    return projectRepository.count();
  }

  @Override
  public Mono<Long> countByNameContaining(String name) {
    return projectRepository.countByNameContaining(name);
  }

  private Mono<PageRequest> orderByName(RequestOfPage requestOfPage) {
    return paginationHelper.createRequest(requestOfPage, new Sort(Direction.ASC, "name"));
  }

  @Override
  public Mono<Project> save(Project project) {
    return Mono.just(project)
        .map(ProjectDoc::new)
        .flatMap(projectRepository::save)
        .map(
            doc -> {
              log.debug("Project {} - {} saved.", doc.getId(), doc.getName());
              return doc;
            })
        .map(p -> p.toDomain(true));
  }
}
