package de.an2ic3.keycloak.ssh;

import com.google.auto.service.AutoService;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.Config.Scope;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resource.RealmResourceProviderFactory;

@Slf4j
@AutoService(RealmResourceProviderFactory.class)
public class ContainerSshResourceProviderFactory implements RealmResourceProviderFactory {

  private static final String ID = "container_ssh";

  @Override
  public RealmResourceProvider create(final KeycloakSession session) {
    return new ContainerSshAuthResourceProvider(session);
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
