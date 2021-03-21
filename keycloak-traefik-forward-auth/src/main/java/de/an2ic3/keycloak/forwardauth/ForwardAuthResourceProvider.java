package de.an2ic3.keycloak.forwardauth;

import lombok.RequiredArgsConstructor;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;

@RequiredArgsConstructor
public class ForwardAuthResourceProvider implements RealmResourceProvider {

  private final ForwardAuthResource resource;

  public ForwardAuthResourceProvider(final String publicUrl, final KeycloakSession session) {
    this.resource = new ForwardAuthResource(publicUrl, session);
  }

  @Override
  public Object getResource() {
    return this.resource;
  }

  @Override
  public void close() {
  }
}
