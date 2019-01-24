package com.netshoes.athena.gateways;

import com.netshoes.athena.domains.ScmApiRateLimit;
import com.netshoes.athena.domains.ScmApiUser;
import com.netshoes.athena.domains.ScmRepository;
import com.netshoes.athena.domains.ScmRepositoryBranch;
import com.netshoes.athena.domains.ScmRepositoryContent;
import com.netshoes.athena.domains.ScmRepositoryContentData;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ScmGateway {

  Mono<ScmRepository> getRepository(String id);

  Mono<ScmRepositoryBranch> getBranch(ScmRepository repository, String branch);

  Flux<ScmRepository> getRepositoriesFromConfiguredOrganization();

  Flux<ScmRepositoryContent> getContents(ScmRepository repository, String branch, String path);

  Mono<ScmRepositoryContentData> retrieveContentData(ScmRepositoryContent content);

  Mono<ScmApiUser> getApiUser();

  Mono<ScmApiRateLimit> getRateLimit();
}
