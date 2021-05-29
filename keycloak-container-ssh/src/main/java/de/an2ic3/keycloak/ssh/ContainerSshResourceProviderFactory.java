package de.an2ic3.keycloak.ssh;

import com.google.auto.service.AutoService;
import java.io.File;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.Config.Scope;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resource.RealmResourceProviderFactory;

@Slf4j
@AutoService(RealmResourceProviderFactory.class)
public class ContainerSshResourceProviderFactory implements RealmResourceProviderFactory {

  private static final String ENV_KEYS_FOLDER = "KEYS_FOLDER";
  private static final String ID = "container_ssh";
  private File keysFile;

  @Override
  public RealmResourceProvider create(final KeycloakSession session) {
    return new ContainerSshAuthResourceProvider(session, this.keysFile);
  }

  @Override
  public void init(final Scope config) {
    final String keysFile = System.getenv(ENV_KEYS_FOLDER);

    if (keysFile == null) {
      log.error("Environment variable \"{}\" is missing, container ssh support is disabled.", ENV_KEYS_FOLDER);
    }
    else {
      this.keysFile = new File(keysFile).getAbsoluteFile();
      log.info("Using \"{}\" as key store directory.", this.keysFile.getPath());
    }
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
