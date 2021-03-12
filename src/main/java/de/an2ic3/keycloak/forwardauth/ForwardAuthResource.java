package de.an2ic3.keycloak.forwardauth;

import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.TokenVerifier;
import org.keycloak.common.VerificationException;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.representations.IDToken;

@Slf4j
@RequiredArgsConstructor
public class ForwardAuthResource {

  private final String publicUrl;
  private final KeycloakSession session;

  @GET
  @Path("ip")
  @Produces(MediaType.TEXT_PLAIN)
  public Response ip(
      @Context final HttpHeaders headers
  ) {
    final Optional<String> optionalHost = ForwardAuthUtils.getHost(headers);
    if (optionalHost.isEmpty()) {
      return Response.status(Status.BAD_REQUEST).entity("Missing X-Forwarded-Host header").build();
    }

    final String host = optionalHost.get();

    final Optional<String> optionalIp = ForwardAuthUtils.getIp(headers);
    if (optionalIp.isEmpty()) {
      return Response.status(Status.BAD_REQUEST).entity("Missing X-Forwarded-For/Cf-Connecting-Ip header").build();
    }

    final Set<RoleModel> roles = this.session.getContext()
        .getRealm()
        .getClientsStream()
        .filter(client -> client.getClientId().equals(host))
        .flatMap(ClientModel::getRolesStream)
        .collect(Collectors.toSet());

    if (roles.isEmpty()) {
      log.warn("No client with associated roles for host \"{}\" found.", host);
      return Response.status(Status.NOT_FOUND).entity("See logs").build();
    }

    final String ip = optionalIp.get();

    if (ForwardAuthUtils.getUsers(this.session, roles)
        .flatMap(user -> user.getAttributeStream("ip_address"))
        .anyMatch(ip::equals)) {
      return Response.status(Status.OK).entity("OK").build();
    }

    return Response.status(Status.FORBIDDEN).entity("Forbidden").build();
  }

  @GET
  @Path("/session")
  @Produces(MediaType.TEXT_PLAIN)
  public Response session(
      @Context final HttpHeaders headers,
      @QueryParam("id_token") final String idToken
  ) {
    if (this.publicUrl == null) {
      return Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity("Please finish server configuration, see logs!")
          .build();
    }

    final Optional<String> optionalHost = ForwardAuthUtils.getHost(headers);
    if (optionalHost.isEmpty()) {
      return Response.status(Status.BAD_REQUEST).entity("Missing X-Forwarded-Host header").build();
    }

    final String host = optionalHost.get();

    final RealmModel realm = this.session.getContext().getRealm();

    final Set<ClientModel> clients = realm
        .getClientsStream()
        .filter(client -> client.getClientId().equals(host))
        .collect(Collectors.toSet());

    if (clients.isEmpty()) {
      log.warn("No associated client for host \"{}\" found.", host);
      return Response.status(Status.NOT_FOUND).entity("See logs").build();
    }

    if (clients.size() != 1) {
      log.warn("To many clients for host \"{}\" found.", host);
      return Response.status(Status.INTERNAL_SERVER_ERROR).entity("See logs").build();
    }

    final ClientModel client = clients.stream().findFirst().get();

    if (idToken == null) {
      final Optional<URL> referUri = ForwardAuthUtils.getUrl(headers);

      if (referUri.isEmpty()) {
        return Response.status(Status.BAD_REQUEST).entity("Missing X-Forwarded-* headers").build();
      }

      final String uri = String.format(
          "%s/auth/realms/%s/protocol/openid-connect/auth"
              + "?access_type=online"
              + "&nonce=abc"
              + "&client_id=%s"
              + "&redirect_uri=%s"
              + "&response_type=id_token"
              + "&scope=profile",
          this.publicUrl,
          URLEncoder.encode(realm.getId(), StandardCharsets.US_ASCII),
          URLEncoder.encode(client.getClientId(), StandardCharsets.US_ASCII),
          URLEncoder.encode(referUri.get().toString(), StandardCharsets.US_ASCII)
      );

      return Response.status(Status.FOUND)
          .header(HttpHeaders.LOCATION, uri)
          .build();
    }
    else {
      final TokenVerifier<IDToken> idTokenTokenVerifier = TokenVerifier.create(idToken, IDToken.class);

      try {
        if (!idTokenTokenVerifier.getToken().getIssuedFor().equals(client.getClientId())) {
          return Response.status(Status.FORBIDDEN).entity("Forbidden").build();
        }
      }
      catch (VerificationException e) {
        return Response.status(Status.BAD_REQUEST).entity("bad token").build();
      }

      try {
        idTokenTokenVerifier.verifySignature();
      }
      catch (VerificationException e) {
        return Response.status(Status.UNAUTHORIZED).entity("Unauthorized").build();
      }

      return Response.ok().entity(idToken).build();
    }
  }
}
