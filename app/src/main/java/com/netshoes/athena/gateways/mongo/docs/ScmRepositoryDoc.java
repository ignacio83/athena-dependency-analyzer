package com.netshoes.athena.gateways.mongo.docs;

import com.netshoes.athena.domains.ScmRepository;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class ScmRepositoryDoc {

  private String id;
  private String name;
  private String description;
  private String url;
  private String masterBranch;
  private LocalDateTime lastPushDate;

  public ScmRepositoryDoc(ScmRepository domain) {
    this.id = domain.getId();
    this.name = domain.getName();
    this.description = domain.getDescription();
    this.url = domain.getUrl() != null ? domain.getUrl().toString() : null;
    this.masterBranch = domain.getMasterBranch();
    domain.getLastPushDate().ifPresent(dateTime -> this.lastPushDate = dateTime.toLocalDateTime());
  }

  public ScmRepository toDomain() {
    final ScmRepository domain = new ScmRepository();
    domain.setId(id);
    domain.setName(name);
    domain.setDescription(description);
    domain.setMasterBranch(masterBranch);
    if (lastPushDate != null) {
      final OffsetDateTime offsetDateTime =
          OffsetDateTime.of(
              lastPushDate, ZoneOffset.systemDefault().getRules().getOffset(Instant.now()));
      domain.setLastPushDate(Optional.of(offsetDateTime));
    }
    if (url != null) {
      try {
        domain.setUrl(new URL(url));
      } catch (MalformedURLException e) {
        throw new RuntimeException(e);
      }
    }
    return domain;
  }
}
