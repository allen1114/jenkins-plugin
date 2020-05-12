package com.zkztch.jenkins.plugins.pipeline.steps;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerConfig;
import com.zkztch.jenkins.test.Docker;
import com.zkztch.jenkins.test.Jenkins;
import lombok.extern.slf4j.Slf4j;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.*;
import org.jvnet.hudson.test.BuildWatcher;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.List;

@Slf4j
public class DockerRemoveStepTest {

    @ClassRule public static final BuildWatcher buildWatcher = Jenkins.buildWatcher;
    @ClassRule public static final JenkinsRule jenkinsRule = Jenkins.jenkinsRule;

    private String containerId;

    @Before
    public void setup() throws DockerException, InterruptedException {
        log.info("setup");
        ContainerConfig containerConfig = ContainerConfig.builder().image(Docker.DOCKER_TEST_BASEIMAGE).build();
        containerId = Docker.client.createContainer(containerConfig).id();
    }

    @After
    public void clean() throws DockerException, InterruptedException {
        log.info("clean");
        if (containerId != null) {
            Docker.client.removeContainer(containerId);
        }
    }

    @Test
    public void removeTest() throws Exception {
        String script = String.format(
                "script {\n" +
                        "dockerRemove container:'%s', dockerHost:'%s', dockerCertPath:'%s', registryUrl:'%s', registryUsername:'%s', registryPassword:'%s'\n" +
                        "\n}",
                containerId, Docker.DOCKER_HOST, Docker.DOCKER_CERT_PATH, Docker.DOCKER_REGISTRY_URL, Docker.DOCKER_REGISTRY_USERNAME,
                Docker.DOCKER_REGISTRY_PASSWORD);

        log.info("script = " + script);
        WorkflowJob job = jenkinsRule.createProject(WorkflowJob.class, "removeTest");
        job.setDefinition(new CpsFlowDefinition(script, true));
        WorkflowRun run = jenkinsRule.assertBuildStatusSuccess(job.scheduleBuild2(0));
        jenkinsRule.waitUntilNoActivity();

        List<Container> containers = Docker.client.listContainers(DockerClient.ListContainersParam.allContainers());
        for (Container c : containers) {
            if (c.id().equals(containerId)) {
                Assert.fail("Container shoud be removed");
            }
        }
        containerId = null;
    }

}
