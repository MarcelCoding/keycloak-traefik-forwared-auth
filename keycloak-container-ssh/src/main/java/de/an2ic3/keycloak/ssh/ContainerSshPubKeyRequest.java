package de.an2ic3.keycloak.ssh;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ContainerSshPubKeyRequest {

  private final String username;
  private final String remoteAddress;
  private final String publicKey;

  public ContainerSshPubKeyRequest(
      @JsonProperty("username") final String username,
      @JsonProperty("remoteAddress") final String remoteAddress,
      @JsonProperty("publicKey") final String publicKey
  ) {
    this.username = username;
    this.remoteAddress = remoteAddress;
    this.publicKey = publicKey;
  }
}
