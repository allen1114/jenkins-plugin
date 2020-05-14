package com.zkztch.docker.registry;

import com.zkztch.test.Docker;
import org.junit.Test;

import java.util.List;

public class DefaultDockerRegistryClientTest {

    @Test
    public void test() {
        DockerRegistryClient registryClient =
                DefaultDockerRegistryClient.builder().uri(Docker.DOCKER_REPO_URL).username(Docker.DOCKER_REPO_USERNAME)
                        .password(Docker.DOCKER_REPO_PASSWORD).build();
        try {
            List<String> repos = registryClient.listRepositories();
            for (String s : repos) {
                System.out.println(s);
                List<String> tagsList = registryClient.listTags(s);
                for (String ss : tagsList) {
                    System.out.println("--" + ss);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
