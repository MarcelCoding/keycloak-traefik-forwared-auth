package de.an2ic3.keycloak.ssh;

import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;

/**
 * @see <a href="https://containerssh.io/getting-started/authserver/">ContainerSSH auth server docs</a>
 */
@Slf4j
@RequiredArgsConstructor
public class ContainerSshResource {

  private final KeycloakSession session;

  @POST
  @Path("pubkey")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response ip(final ContainerSshPubKeyRequest request) {
    if (request.getUsername() == null
        || request.getRemoteAddress() == null
        || request.getPublicKey() == null) {
      return Response.status(Status.BAD_REQUEST).build();
    }

    final UserModel user = this.session.users()
        .getUserByUsername(request.getUsername(), this.session.getContext().getRealm());

    if (user == null) {
      return Response.status(Status.UNAUTHORIZED).build();
    }

    final boolean validIp = user.getAttributeStream("ip_address")
        .anyMatch(ip -> request.getRemoteAddress().equals(ip));

    if (!validIp) {
      return Response.status(Status.FORBIDDEN).build();
    }

    final boolean validKey = user.getAttributeStream("public_key")
        .anyMatch(key -> request.getPublicKey().equals(key));

    if (!validKey) {
      return Response.status(Status.FORBIDDEN).build();
    }

    return Response.ok(Map.of("success", true)).build();
  }
}
