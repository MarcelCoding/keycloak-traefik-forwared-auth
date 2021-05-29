package de.an2ic3.keycloak.ssh;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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
import org.apache.commons.io.FileUtils;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;

/**
 * @see <a href="https://containerssh.io/getting-started/authserver/">ContainerSSH auth server docs</a>
 */
@Slf4j
@RequiredArgsConstructor
public class ContainerSshResource {

  private static final Charset CHARSET = StandardCharsets.UTF_8;
  private final KeycloakSession session;
  private final File keysFolder;

  @POST
  @Path("pubkey")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response ip(final ContainerSshPubKeyRequest request) throws IOException {

    if (request.getUsername() == null
        || request.getRemoteAddress() == null
        || request.getPublicKey() == null) {
      return Response.status(Status.BAD_REQUEST).build();
    }

    final UserModel user = this.session.users()
        .getUserByUsername(this.session.getContext().getRealm(), request.getUsername());

    if (user == null) {
      return Response.status(Status.UNAUTHORIZED).build();
    }

    if (user.getAttributeStream("ip_address").noneMatch(ip -> request.getRemoteAddress().equals(ip))) {
      return Response.status(Status.FORBIDDEN).build();
    }

    if (!request.getPublicKey().equals(this.getKey(user.getId()))) {
      return Response.status(Status.FORBIDDEN).build();
    }

    return Response.ok(Map.of("success", true)).build();
  }

  private String getKey(final String userId) throws IOException {
    final File file = new File(this.keysFolder, userId + ".pubkey");

    if (!file.getAbsolutePath().startsWith(this.keysFolder.getPath())) {
      throw new IllegalArgumentException();
    }

    return !file.exists() ? null : FileUtils.readFileToString(file, CHARSET);
  }
}
