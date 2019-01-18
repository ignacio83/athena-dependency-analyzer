package com.netshoes.athena.gateways.local;

import com.netshoes.athena.conf.StorageProperties;
import com.netshoes.athena.domains.File;
import com.netshoes.athena.gateways.FileStorageGateway;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class LocalFileStorageGateway implements FileStorageGateway {
  private final StorageProperties storageProperties;

  @Override
  public Mono<Void> store(File file, boolean override) {
    try {
      final Path path = buildPath(file.getPath());
      Files.createDirectories(path.getParent());
      if (override && Files.exists(path)) {
        Files.delete(path);
        log.debug("File {} deleted.", path.toAbsolutePath());
      }
      final Path newFile = Files.createFile(path);
      Files.write(newFile, file.getData());
      log.debug("File {} stored.", newFile.toAbsolutePath());
    } catch (IOException e) {
      throw new LocalFileStorageException(e);
    }
    return Mono.empty();
  }

  @Override
  public Mono<File> retrieve(String pathStr) {
    File file;
    try {
      final Path path = buildPath(pathStr);
      final byte[] bytes = Files.readAllBytes(path);
      file = new File(pathStr, bytes);
      log.debug("File {} retrieved.", path.toAbsolutePath().toString());
    } catch (IOException e) {
      throw new LocalFileStorageException(e);
    }
    return Mono.just(file);
  }

  private Path buildPath(String filePath) {
    return Paths.get(
        String.format("%s/%s/%s", storageProperties.getLocal().getPath(), "descriptors", filePath));
  }
}
