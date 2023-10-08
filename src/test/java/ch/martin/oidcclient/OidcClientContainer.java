package ch.martin.oidcclient;

import lombok.NonNull;
import org.testcontainers.containers.GenericContainer;

public class OidcClientContainer extends GenericContainer<OidcClientContainer> {

    public static final int DEFAULT_HTTP_PORT = 8090;

    public OidcClientContainer() {
        super("admiralduck/oidc-client:0.0.1-SNAPSHOT");
        withExposedPorts(DEFAULT_HTTP_PORT);
    }

    public OidcClientContainer withHttpPort(int port) {
        withEnv("SERVER_PORT", Integer.toString(port));
        withExposedPorts(port);
        return this;
    }

    public OidcClientContainer withClientId(@NonNull String clientId) {
        withEnv("CLIENT_ID", clientId);
        return this;
    }

    public OidcClientContainer withClientSecret(@NonNull String clientId) {
        withEnv("CLIENT_SECRET", clientId);
        return this;
    }

    public OidcClientContainer withIssuerUri(@NonNull String issuerUri) {
        withEnv("ISSUER_URI", issuerUri);
        return this;
    }

}
