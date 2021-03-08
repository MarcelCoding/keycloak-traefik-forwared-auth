package de.an2ic3.keycloak.forwardauth;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import javax.ws.rs.core.HttpHeaders;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;

@Slf4j
public final class ForwardAuthUtils {

  private static final String X_FORWARDED_HOST = "X-Forwarded-Host";
  private static final String X_FORWARDED_URI = "X-Forwarded-Uri";
  private static final String CF_CONNECTION_IP = "Cf-Connecting-Ip";
  private static final String X_FORWARDED_FOR = "X-Forwarded-For";

  private ForwardAuthUtils() {
    throw new UnsupportedOperationException();
  }

  public static Optional<String> getHost(final HttpHeaders headers) {
    return Optional.ofNullable(headers.getHeaderString(X_FORWARDED_HOST));
  }

  public static Optional<String> getUri(final HttpHeaders headers) {
    return Optional.ofNullable(headers.getHeaderString(X_FORWARDED_URI));
  }

  public static Optional<String> getIp(final HttpHeaders headers) {
    return Optional.ofNullable(headers.getHeaderString(CF_CONNECTION_IP)) // cloudflare proxy
        .or(() -> Optional.ofNullable(headers.getHeaderString(X_FORWARDED_FOR)));
  }

  public static boolean clientFilter(final ClientModel client, final String host) {
    final Optional<String> url = Optional.ofNullable(client.getBaseUrl())
        .or(() -> Optional.ofNullable(client.getRootUrl()));

    if (url.isEmpty()) {
      return false;
    }

    try {
      final String clientHost = new URI(url.get()).getHost();
      return host.equals(clientHost);
    }
    catch (URISyntaxException e) {
      log.error("Unable to parse client url: {}", url.get(), e);
    }

    return false;
  }

  public static Stream<UserModel> getUsers(final KeycloakSession session, final Set<RoleModel> roles) {
    return session.users()
        .getUsersStream(session.getContext().getRealm())
        .filter(user -> roles.stream().anyMatch(user::hasRole));
  }
}
