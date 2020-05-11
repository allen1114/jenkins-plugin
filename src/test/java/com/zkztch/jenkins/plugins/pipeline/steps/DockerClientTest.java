package com.zkztch.jenkins.plugins.pipeline.steps;

import com.zkztch.jenkins.test.Docker;
import com.zkztch.jenkins.test.TestPropertiesUtils;
import org.junit.Test;

public class DockerClientTest {

    @Test
    public void test() throws Exception {
        Docker.client.pull(Docker.DOCKER_TEST_BASEIMAGE);
    }

}
