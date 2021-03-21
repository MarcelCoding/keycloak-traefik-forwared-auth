package de.an2ic3.keycloak.forwardauth;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import javax.ws.rs.core.HttpHeaders;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;

@Slf4j
public final class ForwardAuthUtils {

  private static final String X_FORWARDED_PROTO = "X-Forwarded-Proto";
  private static final String X_FORWARDED_HOST = "X-Forwarded-Host";
  private static final String X_FORWARDED_PORT = "X-Forwarded-Port";
  private static final String X_FORWARDED_URI = "X-Forwarded-Uri";
  private static final String CF_CONNECTION_IP = "Cf-Connecting-Ip";
  private static final String X_FORWARDED_FOR = "X-Forwarded-For";

  private ForwardAuthUtils() {
    throw new UnsupportedOperationException();
  }

  public static Optional<String> getHost(final HttpHeaders headers) {
    return Optional.ofNullable(headers.getHeaderString(X_FORWARDED_HOST));
  }

  public static Optional<URL> getUrl(final HttpHeaders headers) {
    final String proto = headers.getHeaderString(X_FORWARDED_PROTO);
    if (proto == null) {
      return Optional.empty();
    }

    final String host = headers.getHeaderString(X_FORWARDED_HOST);
    if (host == null) {
      return Optional.empty();
    }

    final String portString = headers.getHeaderString(X_FORWARDED_PORT);
    if (portString == null) {
      return Optional.empty();
    }

    final int port;
    try {
      port = Integer.parseInt(portString);
    }
    catch (NumberFormatException e) {
      log.error("Invalid port from reverse proxy: \"{}\"", portString, e);
      return Optional.empty();
    }

    final String uri = headers.getHeaderString(X_FORWARDED_URI);
    if (uri == null) {
      return Optional.empty();
    }

    try {
      return Optional.of(new URL(proto, host, port, uri));
    }
    catch (MalformedURLException e) {
      log.error("Unable to create url: \"{}\", \"{}\", \"{}\", \"{}\"", new Object[]{proto, host, port, uri, e});
      return Optional.empty();
    }
  }

  public static Optional<String> getIp(final HttpHeaders headers) {
    return Optional.ofNullable(headers.getHeaderString(CF_CONNECTION_IP)) // cloudflare proxy
        .or(() -> Optional.ofNullable(headers.getHeaderString(X_FORWARDED_FOR)));
  }

  public static Stream<UserModel> getUsers(final KeycloakSession session, final Set<RoleModel> roles) {
    return session.users()
        .getUsersStream(session.getContext().getRealm())
        .filter(user -> roles.stream().anyMatch(user::hasRole));
  }
}
