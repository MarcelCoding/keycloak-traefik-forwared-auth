package com.github.marcelcoding.keycloak.forwardauth;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.models.ClientModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RoleModel;

@Slf4j
@RequiredArgsConstructor
public class ForwardAuthResource {

  private final KeycloakSession session;

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public Response forwardAuth(@Context final HttpHeaders headers) {
    final Optional<String> host = ForwardAuthUtils.getHost(headers);
    if (host.isEmpty()) {
      return Response.status(Status.BAD_REQUEST).entity("Missing X-Forwarded-Host header").build();
    }

    final Optional<String> ip = ForwardAuthUtils.getId(headers);
    if (ip.isEmpty()) {
      return Response.status(Status.BAD_REQUEST).entity("Missing X-Forwarded-For/Cf-Connecting-Ip header").build();
    }

    final Set<RoleModel> roles = this.session.getContext()
        .getRealm()
        .getClientsStream()
        .filter(client -> ForwardAuthUtils.clientFilter(client, host.get()))
        .flatMap(ClientModel::getRolesStream)
        .collect(Collectors.toSet());

    if (roles.isEmpty()) {
      log.warn("No client with associated roles for host \"{}\" found.", host.get());
      return Response.status(Status.NOT_FOUND).entity("See logs").build();
    }

    if (ForwardAuthUtils.getUsers(this.session, roles)
        .flatMap(user -> user.getAttributeStream("ip_address"))
        .collect(Collectors.toSet())
        .contains(ip.get())) {
      return Response.status(Status.OK).entity("OK").build();
    }

    return Response.status(Status.FORBIDDEN).entity("Forbidden").build();
  }
}
