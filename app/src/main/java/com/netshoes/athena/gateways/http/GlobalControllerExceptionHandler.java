package com.netshoes.athena.gateways.http;

import com.netshoes.athena.gateways.http.jsons.ErrorJson;
import com.netshoes.athena.usecases.exceptions.DomainAlreadyExistsException;
import com.netshoes.athena.usecases.exceptions.DomainNotFoundException;
import com.netshoes.athena.usecases.exceptions.RequestScanException;
import com.netshoes.athena.usecases.exceptions.ScmApiRateLimitExceededException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalControllerExceptionHandler {
  private static final String REQUEST_SCAN_EXCEPTION_KEY = "requestProjectScan.error";
  private static final String SCM_API_RATE_LIMIT_EXCEEDED_EXCEPTION_KEY =
      "scmApi.rateLimit.exceeded";
  private static final String DOMAIN_NOT_FOUND_EXCEPTION_KEY = "domain.notFound.error";
  private static final String DOMAIN_NOT_MODIFIED_EXCEPTION_KEY = "domain.notModified.error";
  private static final String DOMAIN_ALREADY_EXISTS_EXCEPTION_KEY = "domain.alreadyExists.error";
  private static final String VALIDATION_EXCEPTION_KEY = "domain.validation.error";
  private static final String EXCEPTION_KEY = "internal.error";

  @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
  @ExceptionHandler(RequestScanException.class)
  public ErrorJson handleRequestScanException(RequestScanException e) {
    log.error(e.getMessage(), e);
    return new ErrorJson(REQUEST_SCAN_EXCEPTION_KEY, e.getMessage());
  }

  @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
  @ExceptionHandler(ScmApiRateLimitExceededException.class)
  public ErrorJson handleRequestScanException(ScmApiRateLimitExceededException e) {
    log.error(e.getMessage(), e);
    final ErrorJson errorJson =
        new ErrorJson(SCM_API_RATE_LIMIT_EXCEEDED_EXCEPTION_KEY, e.getMessage());
    errorJson.putDetail("minutesToReset", e.getMinutesToReset().toString());

    return errorJson;
  }

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(DomainNotFoundException.class)
  public ErrorJson handleDomainNotFoundException(DomainNotFoundException e) {
    log.error(e.getMessage(), e);
    final ErrorJson errorJson = new ErrorJson(DOMAIN_NOT_FOUND_EXCEPTION_KEY, e.getMessage());
    errorJson.putDetail("id", String.valueOf(e.getId()));
    return errorJson;
  }

  @ResponseStatus(HttpStatus.CONFLICT)
  @ExceptionHandler(DomainAlreadyExistsException.class)
  public ErrorJson handleDomainAlreadyExistsException(DomainAlreadyExistsException e) {
    log.error(e.getMessage(), e);
    final ErrorJson errorJson = new ErrorJson(DOMAIN_ALREADY_EXISTS_EXCEPTION_KEY, e.getMessage());
    errorJson.putDetail("id", String.valueOf(e.getId()));
    return errorJson;
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(BindException.class)
  public ErrorJson handleBindException(BindException e) {
    final ErrorJson errorJson = new ErrorJson(VALIDATION_EXCEPTION_KEY, "Validation error");

    final List<FieldError> fieldErrors = e.getFieldErrors();
    fieldErrors.forEach(x -> errorJson.putDetail(x.getField(), x.getDefaultMessage()));
    return errorJson;
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ErrorJson handleBindException(MethodArgumentNotValidException e) {
    final ErrorJson errorJson = new ErrorJson(VALIDATION_EXCEPTION_KEY, "Validation error");
    final List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
    fieldErrors.forEach(x -> errorJson.putDetail(x.getField(), x.getDefaultMessage()));
    return errorJson;
  }

  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler(Exception.class)
  public ErrorJson handleException(Exception e) {
    log.error(e.getMessage(), e);
    return new ErrorJson(EXCEPTION_KEY, "Internal server error");
  }
}
