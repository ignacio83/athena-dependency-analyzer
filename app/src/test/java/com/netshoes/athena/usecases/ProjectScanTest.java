package com.netshoes.athena.usecases;

import static br.com.six2six.fixturefactory.Fixture.from;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.six2six.fixturefactory.loader.FixtureFactoryLoader;
import com.netshoes.athena.domains.PendingProjectAnalyze;
import com.netshoes.athena.domains.ScmApiRateLimit;
import com.netshoes.athena.domains.ScmApiRateLimitTemplateLoader;
import com.netshoes.athena.domains.ScmRepository;
import com.netshoes.athena.domains.ScmRepositoryTemplateLoader;
import com.netshoes.athena.gateways.DependencyManagerGateway;
import com.netshoes.athena.gateways.FileStorageGateway;
import com.netshoes.athena.gateways.PendingProjectAnalyzeGateway;
import com.netshoes.athena.gateways.ProjectGateway;
import com.netshoes.athena.gateways.ScmApiGatewayRateLimitExceededException;
import com.netshoes.athena.gateways.ScmGateway;
import java.io.IOException;
import java.time.LocalDateTime;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@RunWith(MockitoJUnitRunner.class)
public class ProjectScanTest {
  @Mock private ScmGateway scmGateway;
  @Mock private DependencyManagerGateway dependencyManagerGateway;
  @Mock private ProjectGateway projectGateway;
  @Mock private PendingProjectAnalyzeGateway pendingProjectAnalyzeGateway;
  @Mock private AnalyzeProjectDependencies analyzeProjectDependencies;
  @Mock private FileStorageGateway fileStorageGateway;
  @Captor private ArgumentCaptor<PendingProjectAnalyze> pendingProjectAnalyzeCaptor;

  private ProjectScan projectScan;

  @BeforeClass
  public static void loadTemplates() {
    FixtureFactoryLoader.loadTemplates("com.netshoes.athena");
  }

  @Before
  public void setup() {
    projectScan =
        new ProjectScan(
            scmGateway,
            fileStorageGateway,
            dependencyManagerGateway,
            projectGateway,
            pendingProjectAnalyzeGateway,
            analyzeProjectDependencies);
  }

  @Test
  public void whenProjectDoNotExistsRequestsAvailableButExhausted() {
    final ScmRepository scmRepository =
        from(ScmRepository.class).gimme(ScmRepositoryTemplateLoader.DEFAULT);
    final ScmApiRateLimit scmApiRateLimit =
        from(ScmApiRateLimit.class).gimme(ScmApiRateLimitTemplateLoader.TEN_PERCENT_AVAILABLE_NO_CONFIGURED_LIMIT);

    when(projectGateway.findById(eq("NONE"))).thenReturn(Mono.empty());
    when(scmGateway.getRepository(eq(scmRepository.getId()))).thenReturn(Mono.just(scmRepository));
    when(scmGateway.getContents(eq(scmRepository), eq(scmRepository.getMasterBranch()), eq("/")))
        .thenThrow(new ScmApiGatewayRateLimitExceededException(new IOException("Mocked"), 1000L));
    when(scmGateway.getRateLimit()).thenReturn(Mono.just(scmApiRateLimit));

    when(pendingProjectAnalyzeGateway.save(any(PendingProjectAnalyze.class)))
        .then(
            (Answer<Mono<PendingProjectAnalyze>>)
                invocation -> Mono.just(invocation.getArgument(0)));

    StepVerifier.create(
            projectScan.execute("NONE", scmRepository.getId(), scmRepository.getMasterBranch()))
        .expectNextMatches(
            project -> {
              assertThat(project.getScmRepository().getId())
                  .isEqualTo("netshoes/default-repository");
              return true;
            })
        .verifyComplete();

    verify(pendingProjectAnalyzeGateway).save(pendingProjectAnalyzeCaptor.capture());

    final PendingProjectAnalyze pendingProjectAnalyze = pendingProjectAnalyzeCaptor.getValue();
    assertThat(pendingProjectAnalyze.getProject().getScmRepository().getId())
        .isEqualTo(scmRepository.getId());
    assertThat(pendingProjectAnalyze.getScheduledDate())
        .isAfter(LocalDateTime.now().plusMinutes(5));
    assertThat(pendingProjectAnalyze.getReason()).isEqualTo("Mocked");
    assertThat(pendingProjectAnalyze.getStackTraceReason())
        .startsWith("com.netshoes.athena.gateways.ScmApiGatewayRateLimitExceeded");
  }
}
