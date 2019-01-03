package com.netshoes.athena.usecases;

import com.netshoes.athena.domains.DependencyManagementDescriptor;
import com.netshoes.athena.domains.Project;
import com.netshoes.athena.usecases.exceptions.DescriptorContentNotFoundException;
import com.netshoes.athena.usecases.exceptions.DescriptorNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class GetDescriptors {

  private final GetProjects getProjects;
  private final GetContentDescriptors getContentDescriptors;

  public Flux<DependencyManagementDescriptor> byProject(String projectId) {
    return getProjects.byId(projectId).flatMapIterable(Project::getDescriptors);
  }

  public Mono<DependencyManagementDescriptor> byId(String projectId, String descriptorId) {
    return getProjects
        .byId(projectId)
        .flatMapIterable(Project::getDescriptors)
        .filter(descriptor -> descriptorId.equals(descriptor.getDependencyDescriptorId()))
        .switchIfEmpty(Mono.defer(() -> Mono.error(new DescriptorNotFoundException(descriptorId))))
        .single();
  }

  public Mono<String> contentById(String projectId, String descriptorId) {
    return byId(projectId, descriptorId)
        .flatMap(getContentDescriptors::from)
        .switchIfEmpty(
            Mono.defer(() -> Mono.error(new DescriptorContentNotFoundException(descriptorId))));
  }
}
