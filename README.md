# Keycloak Extensions

This is a collections of [Keycloak](https://www.keycloak.org/) extensions, and a docker image with all extensions
pre-installed.

This extension provides the user with the ability to share their current IP address and public key with Keycloak.

## Extensions

### Container SSH

[ContainerSSH](https://containerssh.io/) is a ssh server implementation that provides a separate Docker container for
each new connection. This extension implements the auth service for ContainerSSH.
<br>
The ip address, and the public key is validated.

### Traefik Forward Auth

This project is a Keycloak extension to add support for [Traefik](https://traefik.io)'s forwarded auth middleware.

