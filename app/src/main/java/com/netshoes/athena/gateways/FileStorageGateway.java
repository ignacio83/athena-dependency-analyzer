package com.netshoes.athena.gateways;

import com.netshoes.athena.domains.File;
import reactor.core.publisher.Mono;

public interface FileStorageGateway {
  Mono<Void> store(File file, boolean override);

  Mono<File> retrieve(String path);
}
