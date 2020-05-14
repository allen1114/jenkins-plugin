package com.zkztch.docker.registry;

import com.google.common.io.CharStreams;
import com.spotify.docker.client.LogsResponseReader;
import com.spotify.docker.client.ObjectMapperProvider;
import com.spotify.docker.client.ProgressResponseReader;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.exceptions.DockerRequestException;
import com.spotify.docker.client.exceptions.DockerTimeoutException;
import com.spotify.docker.client.shaded.org.glassfish.jersey.internal.util.Base64;
import com.zkztch.docker.registry.model.Catalog;
import com.zkztch.docker.registry.model.TagsList;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.jersey.apache.connector.ApacheClientProperties;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.*;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

public class DefaultDockerRegistryClient implements DockerRegistryClient {

    private final Client client;
    private final URI uri;
    private final String authHeader;

    private DefaultDockerRegistryClient(Builder builder) {
        this.uri = checkNotNull(builder.uri());
        checkNotNull(this.uri.getScheme());

        ClientConfig config = new ClientConfig();
        config.register(ObjectMapperProvider.class);
        config.register(JacksonFeature.class);
        config.register(LogsResponseReader.class);
        config.register(ProgressResponseReader.class);
        config.connectorProvider(new ApacheConnectorProvider());
        config.property(ApacheClientProperties.CONNECTION_MANAGER, new BasicHttpClientConnectionManager());
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
        return client.target(uri).path("v2");
    }

    private Invocation.Builder auth(Invocation.Builder request) {
        if (StringUtils.isNotBlank(authHeader)) {
            request.header("Authorization", authHeader);
        }
        return request;
    }

    private <T> T request(final String method, final Class<T> clazz, final WebTarget resource, final Invocation.Builder request)
            throws DockerException, InterruptedException {
        try {
            return auth(request).async().method(method, clazz).get();
        } catch (ExecutionException | MultiException e) {
            throw propagate(method, resource, e);
        }
    }

    private <T> T request(final String method, final GenericType<T> type, final WebTarget resource, final Invocation.Builder request)
            throws DockerException, InterruptedException {
        try {
            return auth(request).async().method(method, type).get();
        } catch (ExecutionException | MultiException e) {
            throw propagate(method, resource, e);
        }
    }

    private RuntimeException propagate(final String method, final WebTarget resource, final Exception ex)
            throws DockerException, InterruptedException {
        Throwable cause = ex.getCause();

        // Sometimes e is a org.glassfish.hk2.api.MultiException
        // which contains the cause we're actually interested in.
        // So we unpack it here.
        if (ex instanceof MultiException) {
            cause = cause.getCause();
        }

        Response response = null;
        if (cause instanceof ResponseProcessingException) {
            response = ((ResponseProcessingException) cause).getResponse();
        } else if (cause instanceof WebApplicationException) {
            response = ((WebApplicationException) cause).getResponse();
        } else if ((cause instanceof ProcessingException) && (cause.getCause() != null)) {
            // For a ProcessingException, The exception message or nested Throwable cause SHOULD contain
            // additional information about the reason of the processing failure.
            cause = cause.getCause();
        }

        if (response != null) {
            throw new DockerRequestException(method, resource.getUri(), response.getStatus(),
                    message(response), cause);
        } else if ((cause instanceof SocketTimeoutException)
                || (cause instanceof ConnectTimeoutException)) {
            throw new DockerTimeoutException(method, resource.getUri(), ex);
        } else if ((cause instanceof InterruptedIOException)
                || (cause instanceof InterruptedException)) {
            throw new InterruptedException("Interrupted: " + method + " " + resource);
        } else {
            throw new DockerException(ex);
        }
    }

    private String message(final Response response) {
        final Readable reader;
        try {
            reader = new InputStreamReader(response.readEntity(InputStream.class), UTF_8);
        } catch (IllegalStateException e) {
            return null;
        }

        try {
            return CharStreams.toString(reader);
        } catch (IOException ignore) {
            return null;
        }
    }

    @Override
    public List<String> listTags(String repository) throws DockerRegistryException {
        WebTarget resource = resource().path(repository).path("tags").path("list");
        try {
            return request(GET, TagsList.class, resource, resource.request(APPLICATION_JSON_TYPE)).getTags();
        } catch (DockerException | InterruptedException e) {
            throw new DockerRegistryException(e);
        }
    }

    @Override
    public List<String> listRepositories() throws DockerRegistryException {
        WebTarget resource = resource().path("_catalog");
        try {
            return request(GET, Catalog.class, resource, resource.request(APPLICATION_JSON_TYPE)).getRepositories();
        } catch (DockerException | InterruptedException e) {
            throw new DockerRegistryException(e);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Builder() {
        }

        private URI uri;
        private String username;
        private String password;

        URI uri() {
            return uri;
        }

        public Builder uri(String uri) {
            return uri(URI.create(uri));
        }

        public Builder uri(URI uri) {
            this.uri = uri;
            return this;
        }

        String username() {
            return this.username;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }


        String password() {
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
