package com.netshoes.athena.conf;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.RequiredArgsConstructor;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.Invoker;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MavenProperties.class)
@RequiredArgsConstructor
public class MavenConfiguration {
  private final MavenProperties mavenProperties;

  @Bean
  public MavenXpp3Reader mavenReader() {
    return new MavenXpp3Reader();
  }

  @Bean
  public Invoker mavenInvoker() throws IOException {
    final Invoker invoker = new DefaultInvoker();
    final String mavenHome = mavenProperties.getHome();
    if (mavenHome == null || mavenHome.isEmpty()) {
      throw new IllegalArgumentException(
          "You must configure property 'application.maven.home' or set environment variable 'MAVEN_HOME'");
    }
    invoker.setMavenHome(new File(mavenHome));

    final String localRepositoryDirectory = mavenProperties.getLocalRepositoryDirectory();
    if (localRepositoryDirectory != null && !localRepositoryDirectory.isEmpty()) {
      final Path directory = Paths.get(localRepositoryDirectory);
      Files.createDirectories(directory);
      invoker.setLocalRepositoryDirectory(directory.toFile());
    }
    return invoker;
  }
}
