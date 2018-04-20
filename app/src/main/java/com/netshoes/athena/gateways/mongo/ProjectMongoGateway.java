package com.netshoes.athena.gateways.mongo;

import com.netshoes.athena.domains.PaginatedResponse;
import com.netshoes.athena.domains.Project;
import com.netshoes.athena.domains.RequestOfPage;
import com.netshoes.athena.gateways.ProjectGateway;
import com.netshoes.athena.gateways.mongo.docs.ProjectDoc;
import com.netshoes.athena.gateways.mongo.repositories.ProjectRepository;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class ProjectMongoGateway implements ProjectGateway {

  private final ProjectRepository projectRepository;
  private final PaginationHelper paginationHelper;

  @Override
  public Stream<Project> readAll() {
    return projectRepository.readAll().map(p -> p.toDomain(true));
  }

  @Override
  public Optional<Project> findById(String id) {
    final Optional<ProjectDoc> opDoc = projectRepository.findById(id);
    return opDoc.map(doc -> doc.toDomain(true));
  }

  @Override
  public PaginatedResponse<Project> findAll(RequestOfPage requestOfPage) {
    final PageRequest pageRequest = orderByName(requestOfPage);
    final Page<ProjectDoc> page = projectRepository.findAll(pageRequest);
    return paginationHelper.createResponse(page, p -> p.toDomain(true));
  }

  @Override
  public PaginatedResponse<Project> findByNameContaining(RequestOfPage requestOfPage, String name) {
    final PageRequest pageRequest = orderByName(requestOfPage);
    final Page<ProjectDoc> page = projectRepository.findByNameContaining(name, pageRequest);
    return paginationHelper.createResponse(page, p -> p.toDomain(true));
  }

  private PageRequest orderByName(RequestOfPage requestOfPage) {
    return paginationHelper.createRequest(requestOfPage, new Sort(Direction.ASC, "name"));
  }

  @Override
  public Project save(Project project) {
    final ProjectDoc doc = new ProjectDoc(project);
    projectRepository.save(doc);
    log.debug("Project {} saved.", doc.getId());
    return doc.toDomain(true);
  }
}
