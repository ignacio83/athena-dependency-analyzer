package com.netshoes.athena.gateways.mongo;

import com.netshoes.athena.domains.PendingProjectAnalyze;
import com.netshoes.athena.gateways.PendingProjectAnalyzeGateway;
import com.netshoes.athena.gateways.mongo.docs.PendingProjectAnalyzeDoc;
import com.netshoes.athena.gateways.mongo.repositories.PendingProjectAnalyzeRepository;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class PendingProjectAnalyzeMongoGateway implements PendingProjectAnalyzeGateway {

  private final PendingProjectAnalyzeRepository pendingProjectAnalyzeRepository;

  @Override
  public PendingProjectAnalyze findById(String id) {
    final Optional<PendingProjectAnalyzeDoc> opDoc = pendingProjectAnalyzeRepository.findById(id);
    return opDoc.map(doc -> doc.toDomain()).orElse(null);
  }

  @Override
  public Stream<PendingProjectAnalyze> readAll() {
    return pendingProjectAnalyzeRepository.readAll().map(p -> p.toDomain());
  }

  @Override
  public void delete(String id) {
    final Optional<PendingProjectAnalyzeDoc> opDoc = pendingProjectAnalyzeRepository.findById(id);
    opDoc.ifPresent(
        doc -> {
          pendingProjectAnalyzeRepository.delete(doc);

          log.trace("PendingProjectAnalyze {} deleted.", doc.getId());
        });
  }

  @Override
  public void save(PendingProjectAnalyze pendingProjectAnalyze) {
    final PendingProjectAnalyzeDoc doc = new PendingProjectAnalyzeDoc(pendingProjectAnalyze);
    pendingProjectAnalyzeRepository.save(doc);

    log.trace("PendingProjectAnalyze {} saved.", doc.getId());
  }
}
