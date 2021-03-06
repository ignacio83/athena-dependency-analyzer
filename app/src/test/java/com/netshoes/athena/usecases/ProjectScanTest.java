package com.netshoes.athena.usecases;

import static br.com.six2six.fixturefactory.Fixture.from;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.six2six.fixturefactory.loader.FixtureFactoryLoader;
import com.netshoes.athena.conf.FeatureProperties;
import com.netshoes.athena.domains.PendingProjectAnalyze;
import com.netshoes.athena.domains.ScmApiRateLimit;
import com.netshoes.athena.domains.ScmApiRateLimitTemplateLoader;
import com.netshoes.athena.domains.ScmRepository;
import com.netshoes.athena.domains.ScmRepositoryBranch;
import com.netshoes.athena.domains.ScmRepositoryTemplateLoader;
import com.netshoes.athena.gateways.DependencyManagementDescriptorAnalyzeExecutionGateway;
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

  @Mock
  private DependencyManagementDescriptorAnalyzeExecutionGateway
      dependencyManagementDescriptorAnalyzeExecutionGateway;

  @Mock private AnalyzeProjectDependencies analyzeProjectDependencies;
  @Mock private FileStorageGateway fileStorageGateway;
  private FeatureProperties featureProperties = new FeatureProperties();
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
            dependencyManagementDescriptorAnalyzeExecutionGateway,
            analyzeProjectDependencies,
            featureProperties);
  }

  @Test
  public void whenProjectDoNotExistsRequestsAvailableButExhausted() {
    final ScmRepository scmRepository =
        from(ScmRepository.class).gimme(ScmRepositoryTemplateLoader.DEFAULT);
    final ScmRepositoryBranch scmRepositoryBranch =
        new ScmRepositoryBranch(scmRepository.getMasterBranch(), "sha", scmRepository);
    final ScmApiRateLimit scmApiRateLimit =
        from(ScmApiRateLimit.class)
            .gimme(ScmApiRateLimitTemplateLoader.TEN_PERCENT_AVAILABLE_NO_CONFIGURED_LIMIT);

    when(pendingProjectAnalyzeGateway.delete(anyString())).thenReturn(Mono.just("").then());
    when(projectGateway.findById(eq("NONE"))).thenReturn(Mono.empty());
    when(scmGateway.getRepository(eq(scmRepository.getId()))).thenReturn(Mono.just(scmRepository));
    when(scmGateway.getBranch(eq(scmRepository), eq(scmRepositoryBranch.getName())))
        .thenReturn(Mono.just(scmRepositoryBranch));
    when(scmGateway.getContents(eq(scmRepository), eq(scmRepositoryBranch.getName()), eq("/")))
        .thenThrow(new ScmApiGatewayRateLimitExceededException(new IOException("Mocked"), 1000L));
    when(scmGateway.getRateLimit()).thenReturn(Mono.just(scmApiRateLimit));

    when(pendingProjectAnalyzeGateway.save(any(PendingProjectAnalyze.class)))
        .then(
            (Answer<Mono<PendingProjectAnalyze>>)
                invocation -> Mono.just(invocation.getArgument(0)));

    StepVerifier.create(
            projectScan.execute("NONE", scmRepository.getId(), scmRepository.getMasterBranch()))
        .expectNextMatches(
            result -> {
              assertThat(result.getProject().getScmRepository().getId())
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
