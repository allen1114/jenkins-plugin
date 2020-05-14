package com.zkztch.test;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerCertificates;
import com.spotify.docker.client.DockerCertificatesStore;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.auth.FixedRegistryAuthSupplier;
import com.spotify.docker.client.messages.RegistryAuth;
import com.spotify.docker.client.messages.RegistryConfigs;
import com.spotify.docker.client.shaded.com.google.common.base.Optional;
import com.zkztch.docker.registry.DefaultDockerRegistryClient;
import com.zkztch.docker.registry.DockerRegistryClient;

import java.net.URI;
import java.nio.file.Paths;

public class Docker {
    public static final String DOCKER_CERT_PATH;
    public static final String DOCKER_HOST;
    public static final String DOCKER_REPO_HOST;
    public static final String DOCKER_REPO_URL;
    public static final String DOCKER_REPO_USERNAME;
    public static final String DOCKER_REPO_PASSWORD;
    public static final String DOCKER_TEST_BASEIMAGE;

    public static final DockerClient client;
    public static final DockerRegistryClient registryClient;

    static {
        try {

            URI certUri = Resources.getResource("docker_certs").toURI();
            DOCKER_CERT_PATH = certUri.getPath();
            DOCKER_HOST = TestPropertiesUtils.getProperty("docker.host");
            DOCKER_REPO_HOST = TestPropertiesUtils.getProperty("docker.registory.host");
            DOCKER_REPO_URL = TestPropertiesUtils.getProperty("docker.registory.url");
            DOCKER_REPO_USERNAME = TestPropertiesUtils.getProperty("docker.registory.username");
            DOCKER_REPO_PASSWORD = TestPropertiesUtils.getProperty("docker.registory.password");
            DOCKER_TEST_BASEIMAGE = TestPropertiesUtils.getProperty("dockat.test.base.image");

            DefaultDockerClient.Builder builder = null;

            builder = DefaultDockerClient.fromEnv().readTimeoutMillis(0);
            builder.uri(DOCKER_HOST);

            Optional<DockerCertificatesStore> certs = null;
            certs = DockerCertificates.builder().dockerCertPath(Paths.get(certUri)).build();
            if (certs.isPresent()) {
                builder.dockerCertificates(certs.get());
            }

            RegistryAuth.Builder registryAuthBuilder = RegistryAuth.builder();
            registryAuthBuilder.serverAddress(DOCKER_REPO_HOST);
            registryAuthBuilder.username(DOCKER_REPO_USERNAME);
            registryAuthBuilder.password(DOCKER_REPO_PASSWORD);

            RegistryAuth registryAuth = registryAuthBuilder.build();

            RegistryConfigs configsForBuild = RegistryConfigs.create(ImmutableMap.of(
                    registryAuth.serverAddress(), registryAuth
            ));
            builder.registryAuthSupplier(new FixedRegistryAuthSupplier(registryAuth, configsForBuild));
            client = builder.build();


            registryClient = DefaultDockerRegistryClient.builder().uri(Docker.DOCKER_REPO_URL).username(Docker.DOCKER_REPO_USERNAME)
                    .password(Docker.DOCKER_REPO_PASSWORD).build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
