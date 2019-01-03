package com.netshoes.athena.usecases.exceptions;

public class DescriptorContentNotFoundException extends DomainNotFoundException {
  public DescriptorContentNotFoundException(String descriptorId) {
    super("Descriptor content not found", descriptorId);
  }
}
