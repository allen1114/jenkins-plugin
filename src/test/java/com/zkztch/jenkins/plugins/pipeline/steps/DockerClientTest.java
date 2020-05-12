package com.zkztch.jenkins.plugins.pipeline.steps;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.spotify.docker.client.ObjectMapperProvider;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import com.zkztch.jenkins.test.Docker;
import com.zkztch.jenkins.test.TestPropertiesUtils;
import org.junit.Test;

public class DockerClientTest {

    @Test
    public void test() throws Exception {
//        Docker.client.pull(Docker.DOCKER_TEST_BASEIMAGE);

        String json = Resources.toString(Resources.getResource("container_config.json"), Charsets.UTF_8).trim();
        json = json.replaceAll("\\s+", " ");
        System.out.println(json);
        ObjectMapper objectMapper = ObjectMapperProvider.objectMapper();
        ContainerConfig containerConfig = objectMapper.readValue(json, ContainerConfig.class);

        System.out.println(containerConfig);
    }


}
