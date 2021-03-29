package de.an2ic3.keycloak.ssh;

import lombok.RequiredArgsConstructor;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;

@RequiredArgsConstructor
public class ContainerSshAuthResourceProvider implements RealmResourceProvider {

  private final ContainerSshResource resource;

  public ContainerSshAuthResourceProvider(final KeycloakSession session) {
    this.resource = new ContainerSshResource(session);
  }

  @Override
  public Object getResource() {
    return this.resource;
  }

  @Override
  public void close() {
  }
}
