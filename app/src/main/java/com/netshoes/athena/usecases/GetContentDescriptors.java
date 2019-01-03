package com.netshoes.athena.usecases;

import com.netshoes.athena.domains.DependencyManagementDescriptor;
import com.netshoes.athena.domains.File;
import com.netshoes.athena.gateways.FileStorageGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class GetContentDescriptors {
  private final FileStorageGateway fileStorageGateway;

  public Mono<String> from(DependencyManagementDescriptor dependencyManagementDescriptor) {
    return dependencyManagementDescriptor
        .getStoragePath()
        .map(path -> fileStorageGateway.retrieve(path).map(File::getData).map(String::new))
        .orElse(Mono.empty());
  }
}
