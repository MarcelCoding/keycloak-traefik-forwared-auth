package com.github.marcelcoding.keycloak.forwardauth;

import com.google.auto.service.AutoService;
import org.keycloak.Config.Scope;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resource.RealmResourceProviderFactory;

@AutoService(RealmResourceProviderFactory.class)
public class ForwardAuthResourceProviderFactory implements RealmResourceProviderFactory {

  private static final String ID = "forwarded_auth";

  @Override
  public RealmResourceProvider create(final KeycloakSession session) {
    return new ForwardAuthResourceProvider(session);
  }

  @Override
  public void init(final Scope config) {
  }

  @Override
  public void postInit(final KeycloakSessionFactory factory) {
  }

  @Override
  public void close() {
  }

  @Override
  public String getId() {
    return ID;
  }
}
