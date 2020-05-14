package com.zkztch.jenkins.plugins.pipeline.steps;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerConfig;
import com.zkztch.jenkins.test.Docker;
import com.zkztch.jenkins.test.Jenkins;
import hudson.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.*;
import org.jvnet.hudson.test.BuildWatcher;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.List;
import java.util.UUID;

@Slf4j
public class DockerStartStepTest {

    @ClassRule public static final BuildWatcher buildWatcher = Jenkins.buildWatcher;
    @ClassRule public static final JenkinsRule jenkinsRule = Jenkins.jenkinsRule;

    private String containerId;
    private String name;

    @Before
    public void setup() throws DockerException, InterruptedException {
        log.info("setup");
        name = UUID.randomUUID().toString();
        ContainerConfig containerConfig = ContainerConfig.builder().image(Docker.DOCKER_TEST_BASEIMAGE).build();
        containerId = Docker.client.createContainer(containerConfig, name).id();
    }

    @After
    public void clean() throws Exception {
        log.info("clean");
        Docker.client.removeContainer(containerId);
    }

    @Test
    public void startTest() throws Exception {

        String format = "pipeline {\n" +
                "    agent any\n" +
                "    stages {\n" +
                "        stage(\"start\") {\n" +
                "            steps {\n" +
                "               script {\n" +
                "                   dockerStart container:'%s', dockerHost:'%s', dockerCertPath:'%s', registryUrl:'%s', registryUsername:'%s', registryPassword:'%s'\n" +
                "               }\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}";
        String script = String.format(format, containerId, Docker.DOCKER_HOST, Docker.DOCKER_CERT_PATH, Docker.DOCKER_REPO_HOST,
                Docker.DOCKER_REPO_USERNAME, Docker.DOCKER_REPO_PASSWORD);
        log.info("script = " + script);
        WorkflowJob job = jenkinsRule.createProject(WorkflowJob.class, name);
        job.setDefinition(new CpsFlowDefinition(script, true));
        WorkflowRun run = jenkinsRule.assertBuildStatusSuccess(job.scheduleBuild2(0));
        jenkinsRule.waitUntilNoActivity();

        List<Container> containers = Docker.client.listContainers(DockerClient.ListContainersParam.allContainers());
        for (Container c : containers) {
            if (c.id().equals(containerId)) {
                Assert.assertEquals(c.state(), "running");
                Docker.client.stopContainer(containerId, 30);
                break;
            }
        }
    }

    @Test
    public void startTestByName() throws Exception {
        String format = "pipeline {\n" +
                "    agent any\n" +
                "    stages {\n" +
                "        stage(\"start\") {\n" +
                "            steps {\n" +
                "               script {\n" +
                "                   dockerStart container:'%s', dockerHost:'%s', dockerCertPath:'%s', registryUrl:'%s', registryUsername:'%s', registryPassword:'%s'\n" +
                "               }\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}";
        String script = String.format(format, name, Docker.DOCKER_HOST, Docker.DOCKER_CERT_PATH, Docker.DOCKER_REPO_HOST,
                Docker.DOCKER_REPO_USERNAME, Docker.DOCKER_REPO_PASSWORD);
        log.info("script = " + script);
        WorkflowJob job = jenkinsRule.createProject(WorkflowJob.class, name);
        job.setDefinition(new CpsFlowDefinition(script, true));
        WorkflowRun run = jenkinsRule.assertBuildStatusSuccess(job.scheduleBuild2(0));
        jenkinsRule.waitUntilNoActivity();

        List<Container> containers = Docker.client.listContainers(DockerClient.ListContainersParam.allContainers());
        for (Container c : containers) {
            if (c.id().equals(containerId)) {
                Assert.assertEquals(c.state(), "running");
                Docker.client.stopContainer(containerId, 30);
                break;
            }
        }
    }

    @Test
    public void failOnNotFound() throws Exception {
        String format = "pipeline {\n" +
                "    agent any\n" +
                "    stages {\n" +
                "        stage(\"start\") {\n" +
                "            steps {\n" +
                "               script {\n" +
                "                   dockerStart container:'%s',failWhenNotFound: true, dockerHost:'%s', dockerCertPath:'%s', registryUrl:'%s', registryUsername:'%s', registryPassword:'%s'\n" +
                "               }\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}";
        String script = String.format(format, UUID.randomUUID().toString(), Docker.DOCKER_HOST, Docker.DOCKER_CERT_PATH, Docker.DOCKER_REPO_HOST,
                Docker.DOCKER_REPO_USERNAME, Docker.DOCKER_REPO_PASSWORD);
        log.info("script = " + script);
        WorkflowJob job = jenkinsRule.createProject(WorkflowJob.class, name);
        job.setDefinition(new CpsFlowDefinition(script, true));
        jenkinsRule.assertBuildStatus(Result.FAILURE, job.scheduleBuild2(0));
        jenkinsRule.waitUntilNoActivity();

        List<Container> containers = Docker.client.listContainers(DockerClient.ListContainersParam.allContainers());
        for (Container c : containers) {
            if (c.id().equals(containerId)) {
                Assert.assertNotEquals(c.state(), "running");
                break;
            }
        }
    }
}
