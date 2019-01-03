package com.netshoes.athena.domains;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class File {
  private final String path;
  private final byte[] data;
}
