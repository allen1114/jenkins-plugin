package com.zkztch.jenkins.test;

import com.google.common.collect.ImmutableMap;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerCertificates;
import com.spotify.docker.client.DockerCertificatesStore;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.auth.FixedRegistryAuthSupplier;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.messages.RegistryAuth;
import com.spotify.docker.client.messages.RegistryConfigs;
import com.spotify.docker.client.shaded.com.google.common.base.Optional;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.nio.file.Paths;

public class Docker {
    private static final String dockerCertsDir = "docker_certs";
    public static final String DOCKER_CERT_PATH;
    public static final String DOCKER_HOST = TestPropertiesUtils.getProperty("docker.host");
    public static final String DOCKER_REGISTRY_URL = TestPropertiesUtils.getProperty("docker.registory.url");
    public static final String DOCKER_REGISTRY_USERNAME = TestPropertiesUtils.getProperty("docker.registory.username");
    public static final String DOCKER_REGISTRY_PASSWORD = TestPropertiesUtils.getProperty("docker.registory.password");

    public static final String DOCKER_TEST_BASEIMAGE = TestPropertiesUtils.getProperty("dockat.test.base.image");

    public static final DockerClient client;

    static {

        String basedir = (String) System.getProperties().get("basedir");

        if (basedir != null && (basedir.endsWith("target/checkout") || basedir.endsWith("target\\checkout"))) {
            basedir = basedir.substring(0, basedir.length() - 15);
        }

        File dir = new File(basedir, "src/test/resources/" + dockerCertsDir);
        if (dir.exists()) {
            DOCKER_CERT_PATH = dir.getAbsolutePath().replaceAll("\\\\", "/");

        } else {
            dir = new File((String) System.getProperties().get("user.home"), dockerCertsDir);
            if (dir.exists()) {
                DOCKER_CERT_PATH = dir.getAbsolutePath().replaceAll("\\\\", "/");
                ;
            } else {
                DOCKER_CERT_PATH = null;
            }
        }

        DefaultDockerClient.Builder builder = null;
        try {
            builder = DefaultDockerClient.fromEnv().readTimeoutMillis(0);
        } catch (DockerCertificateException e) {
            throw new RuntimeException(e);
        }

        builder.uri(TestPropertiesUtils.getProperty("docker.host"));


        Optional<DockerCertificatesStore> certs = null;
        try {
            certs = DockerCertificates.builder().dockerCertPath(Paths.get(DOCKER_CERT_PATH)).build();
        } catch (DockerCertificateException e) {
            throw new RuntimeException(e);
        }
        if (certs.isPresent()) {
            builder.dockerCertificates(certs.get());
        }

        registryAuth(builder);

        client = builder.build();

        try {
            client.pull(TestPropertiesUtils.getProperty("dockat.test.base.image"));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void registryAuth(DefaultDockerClient.Builder builder) {
        String registryUrl = TestPropertiesUtils.getProperty("docker.registory.url");

        if (StringUtils.isNotBlank(registryUrl)) {
            RegistryAuth.Builder registryAuthBuilder = RegistryAuth.builder();
            registryAuthBuilder.serverAddress(registryUrl);

            String registryUsername = TestPropertiesUtils.getProperty("docker.registory.username");
            String registryPassword = TestPropertiesUtils.getProperty("docker.registory.password");

            if (StringUtils.isNotBlank(registryUsername)) {
                registryAuthBuilder.username(registryUsername);
            }
            if (StringUtils.isNotBlank(registryPassword)) {
                registryAuthBuilder.password(registryPassword);
            }

            RegistryAuth registryAuth = registryAuthBuilder.build();

            RegistryConfigs configsForBuild = RegistryConfigs.create(ImmutableMap.of(
                    registryAuth.serverAddress(), registryAuth
            ));
            builder.registryAuthSupplier(new FixedRegistryAuthSupplier(registryAuth, configsForBuild));
        }
    }
}
