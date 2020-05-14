package com.zkztch.docker;

import com.spotify.docker.client.LogsResponseReader;
import com.spotify.docker.client.ObjectMapperProvider;
import com.spotify.docker.client.ProgressResponseReader;
import com.spotify.docker.client.shaded.org.glassfish.jersey.internal.util.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.glassfish.jersey.apache.connector.ApacheClientProperties;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;
import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

public class DefaultDockerRegistryClient implements DockerRegistryClient {
    private final Client client;
    private final URI uri;
    private final String apiVersion;
    private final String authHeader;
    private final ClientConfig defaultConfig = new ClientConfig(
            ObjectMapperProvider.class,
            JacksonFeature.class,
            LogsResponseReader.class,
            ProgressResponseReader.class);

    private DefaultDockerRegistryClient(Builder builder) {
        this.uri = checkNotNull(builder.uri());
        checkNotNull(this.uri.getScheme());
        this.apiVersion = StringUtils.isNotBlank(builder.apiVersion()) ? builder.apiVersion() : "v2";

        final ClientConfig config = defaultConfig.connectorProvider(new ApacheConnectorProvider())
                .property(ApacheClientProperties.CONNECTION_MANAGER, new BasicHttpClientConnectionManager());
        this.client = ClientBuilder.newBuilder().withConfig(config).build();
        authHeader = authHeader(builder);
    }

    private String authHeader(Builder builder) {
        if (StringUtils.isNotBlank(builder.username) && StringUtils.isNotBlank(builder.password)) {
            return "Basic " + Base64.encodeAsString((builder.username + ":" + builder.password).getBytes(StandardCharsets.UTF_8));
        } else {
            return null;
        }
    }

    private WebTarget resource() {
        final WebTarget target = client.target(uri);
        if (!isNullOrEmpty(apiVersion)) {
            return target.path(apiVersion);
        }
        return target;
    }

    @Override
    public TagsList tagsList(String image) throws DockerRegistryRequestException {
        WebTarget resource = resource().path(image).path("tags").path("list");
        Invocation.Builder builder = resource.request(APPLICATION_JSON_TYPE);
        if (StringUtils.isNotBlank(authHeader)) {
            builder.header("Authorization", authHeader);
        }
        try (Response response = builder.method(GET, Response.class);) {
            if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                throw new DockerRegistryRequestException(response.getStatus());
            } else {
                return response.readEntity(TagsList.class);
            }
        }

    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Builder() {
        }

        private URI uri;
        private String apiVersion = "v2";
        private String username;
        private String password;

        public URI uri() {
            return uri;
        }

        public Builder uri(String uri) {
            return uri(URI.create(uri));
        }

        public Builder uri(URI uri) {
            this.uri = uri;
            return this;
        }

        public String apiVersion() {
            return this.apiVersion;
        }

        public Builder apiVersion(String apiVersion) {
            this.apiVersion = apiVersion;
            return this;
        }

        public String username() {
            return this.username;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }


        public String password() {
            return this.password;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }


        public DockerRegistryClient build() {
            return new DefaultDockerRegistryClient(this);
        }


    }
}
