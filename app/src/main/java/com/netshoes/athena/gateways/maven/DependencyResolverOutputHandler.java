package com.netshoes.athena.gateways.maven;

import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import lombok.Getter;
import org.apache.maven.model.Dependency;
import org.apache.maven.shared.invoker.InvocationOutputHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DependencyResolverOutputHandler implements InvocationOutputHandler {
  private static final String EMPTY = "";
  private static final String INITIAL_MARKUP = "The following files have been resolved:";
  private final Logger log = LoggerFactory.getLogger("mavenOutput");
  private final StringBuilder errorLog = new StringBuilder();
  private boolean resolveDependenciesOutput = false;

  @Getter private final List<Dependency> dependencies = new LinkedList<>();

  @Override
  public void consumeLine(String line) {
    log.debug(line);
    if (isError(line)) {
      final String trimmedLine = line.replaceFirst("\\[ERROR\\]", EMPTY).trim();
      errorLog.append(trimmedLine).append("\n");
    } else {
      final String trimmedLine = line.replaceFirst("\\[INFO\\]", EMPTY).trim();
      if (INITIAL_MARKUP.equals(trimmedLine)) {
        resolveDependenciesOutput = true;
      } else if (EMPTY.equals(trimmedLine) || "none".equals(trimmedLine)) {
        resolveDependenciesOutput = false;
      } else if (resolveDependenciesOutput) {
        final Dependency dependency = parseArtifact(trimmedLine);
        dependencies.add(dependency);
      }
    }
  }

  private boolean isError(String line) {
    return line.startsWith("[ERROR]");
  }

  private Dependency parseArtifact(String line) {
    final StringTokenizer tokenizer = new StringTokenizer(line, ":");
    final Dependency dependency = new Dependency();
    dependency.setGroupId(tokenizer.nextToken());
    dependency.setArtifactId(tokenizer.nextToken());
    dependency.setType(tokenizer.nextToken());
    dependency.setVersion(tokenizer.nextToken());
    dependency.setScope(tokenizer.nextToken());
    return dependency;
  }

  public String getErrorLog() {
    return errorLog.toString();
  }
}
