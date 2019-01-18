package com.netshoes.athena.domains;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.Base64Utils;

@Getter
public class Artifact implements Serializable, Comparable {

  private final String id;
  private final String groupId;
  private final String artifactId;
  private final String version;
  private final ArtifactOrigin origin;
  private final Set<Technology> relatedTechnologies;
  @Setter private Optional<ArtifactVersionReport> report;
  @Setter private boolean modified;

  public Artifact(String groupId, String artifactId, String version, ArtifactOrigin origin) {
    this.id = generateId(groupId, artifactId, version);
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.version = version;
    this.origin = origin;
    this.report = Optional.empty();
    this.relatedTechnologies = new HashSet<>();
  }

  public static Artifact ofParent(String groupId, String artifactId, String version) {
    return new Artifact(groupId, artifactId, version, ArtifactOrigin.PARENT);
  }

  public static Artifact ofProject(String groupId, String artifactId, String version) {
    return new Artifact(groupId, artifactId, version, ArtifactOrigin.PROJECT);
  }

  public static Artifact ofProject(
      String groupId, String artifactId, String version, Artifact parent) {
    final String resolvedGroupId = groupId == null ? parent.getGroupId() : groupId;
    final String resolvedVersion = version == null ? parent.getVersion() : version;

    return new Artifact(resolvedGroupId, artifactId, resolvedVersion, ArtifactOrigin.PROJECT);
  }

  static String generateId(String groupId, String artifactId, String version) {
    final String baseId = MessageFormat.format("{0}:{1}:{2}", groupId, artifactId, version);
    return Base64Utils.encodeToUrlSafeString(baseId.getBytes(StandardCharsets.UTF_8));
  }

  public Artifact setReport(ArtifactVersionReport report) {
    if (!this.report.isPresent() && report != null || !this.report.equals(report)) {
      this.report = Optional.of(report);
      modified = true;
    }
    return this;
  }

  public Artifact addRelatedTechnologies(Set<Technology> technologies) {
    if (!relatedTechnologies.equals(technologies)) {
      relatedTechnologies.addAll(technologies);
      modified = true;
    }
    return this;
  }

  @Override
  public String toString() {
    return MessageFormat.format("{0}:{1}:{2}", groupId, artifactId, version);
  }

  @Override
  public int compareTo(Object o) {
    return Comparator.comparing(Artifact::getReport, this::compareVersionReport)
        .thenComparing(
            Artifact::getOrigin,
            (o1, o2) -> Comparator.comparingInt(ArtifactOrigin::getOrder).compare(o1, o2))
        .thenComparing(Artifact::getGroupId)
        .thenComparing(Artifact::getArtifactId)
        .thenComparing(Artifact::getVersion, Comparator.nullsFirst(Comparator.naturalOrder()))
        .thenComparing(Artifact::getOrigin)
        .compare(this, (Artifact) o);
  }

  private int compareVersionReport(
      Optional<ArtifactVersionReport> opR1, Optional<ArtifactVersionReport> opR2) {
    final boolean stable1 = opR1.map(ArtifactVersionReport::isStable).orElse(true);
    final boolean stable2 = opR2.map(ArtifactVersionReport::isStable).orElse(true);
    return !stable1 && stable2 ? -1 : 1;
  }
}
