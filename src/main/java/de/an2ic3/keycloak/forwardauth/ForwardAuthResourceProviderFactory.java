package de.an2ic3.keycloak.forwardauth;

import com.google.auto.service.AutoService;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.Config.Scope;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resource.RealmResourceProviderFactory;

@Slf4j
@AutoService(RealmResourceProviderFactory.class)
public class ForwardAuthResourceProviderFactory implements RealmResourceProviderFactory {

  private static final String ENV_FORWARD_AUTH_PUBLIC_URI = "FORWARD_AUTH_PUBLIC_URI";
  private static final String ID = "forwarded_auth";
  private String publicUrl;

  @Override
  public RealmResourceProvider create(final KeycloakSession session) {
    return new ForwardAuthResourceProvider(this.publicUrl, session);
  }

  @Override
  public void init(final Scope config) {
    this.publicUrl = System.getenv(ENV_FORWARD_AUTH_PUBLIC_URI);

    if (this.publicUrl == null) {
      log.error("Environment variable \"{}\" is missing, forwarded auth is disabeld.", ENV_FORWARD_AUTH_PUBLIC_URI);
    }
    else if (this.publicUrl.charAt(this.publicUrl.length() - 1) == '/') {
      this.publicUrl = this.publicUrl.substring(this.publicUrl.length() - 1);
    }

    log.info("Using \"{}\" as public url.", this.publicUrl);
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
