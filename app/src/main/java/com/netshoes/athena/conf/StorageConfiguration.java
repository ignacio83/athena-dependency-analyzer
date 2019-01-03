package com.netshoes.athena.conf;

import com.netshoes.athena.gateways.FileStorageGateway;
import com.netshoes.athena.gateways.local.LocalFileStorageGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties({StorageProperties.class})
public class StorageConfiguration {
  private final StorageProperties storageProperties;

  @Bean
  public FileStorageGateway fileStorageGateway() {
    return new LocalFileStorageGateway(storageProperties);
  }
}
