package com.zkztch.jenkins.plugins.pipeline.steps;

import com.zkztch.jenkins.test.Docker;
import com.zkztch.jenkins.test.TestPropertiesUtils;
import org.junit.Test;

public class DockerClientTest {

    private static final String pullimage = TestPropertiesUtils.getProperty("docker.pull.test.image");

    @Test
    public void test() throws Exception {
        Docker.client.pull(pullimage);
    }

}
