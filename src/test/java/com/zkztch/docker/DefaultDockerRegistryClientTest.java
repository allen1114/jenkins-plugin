package com.zkztch.docker;

import com.spotify.docker.client.DockerClient;
import org.junit.Test;

public class DefaultDockerRegistryClientTest {

    @Test
    public void test() {
        DockerRegistryClient client =
                DefaultDockerRegistryClient.builder().uri("http://docker.repo.sit.zkztch.com").username("admin").password("admin.hunter")
                        .build();
        TagsList tagsList = null;
        try {
            tagsList = client.tagsList("com.zkztch.ci/ci-demo");
            System.out.println(tagsList);
        } catch (DockerRegistryRequestException e) {
            e.printStackTrace();
            System.out.println(e.getStatus());
        }


    }
}
