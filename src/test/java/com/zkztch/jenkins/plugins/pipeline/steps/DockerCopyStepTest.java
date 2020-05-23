package com.zkztch.jenkins.plugins.pipeline.steps;

import com.google.common.io.Resources;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.zkztch.test.Docker;
import com.zkztch.test.Jenkins;
import com.zkztch.test.TestPropertiesUtils;
import lombok.extern.slf4j.Slf4j;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.BuildWatcher;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.IOException;
import java.util.UUID;

@Slf4j
public class DockerCopyStepTest {

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
    public void copyTest() throws Exception {
        String fromDir = Resources.getResource("docker_certs").getPath();
        String tarToSend = Resources.getResource("dockerCopyToContainer.tar.gz").getPath();
        String toPath = TestPropertiesUtils.getProperty("docker.copy.test.to");
        String format = "pipeline {\n" +
                "    agent any\n" +
                "    stages {\n" +
                "        stage(\"start\") {\n" +
                "            steps {\n" +
                "               script {\n" +
                "                   dockerCopy fromDir:'%s',tarToSend:'%s', toPath:'%s', container:'%s', dockerHost:'%s', dockerCertPath:'%s', registryUrl:'%s', registryUsername:'%s', registryPassword:'%s'\n" +
                "               }\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}";
        String script = String.format(format, fromDir, tarToSend, toPath, containerId, Docker.DOCKER_HOST, Docker.DOCKER_CERT_PATH,
                Docker.DOCKER_REPO_HOST,
                Docker.DOCKER_REPO_USERNAME, Docker.DOCKER_REPO_PASSWORD);
        log.info("script = " + script);
        WorkflowJob job = jenkinsRule.createProject(WorkflowJob.class, name);
        job.setDefinition(new CpsFlowDefinition(script, true));
        WorkflowRun run = jenkinsRule.assertBuildStatusSuccess(job.scheduleBuild2(0));
        jenkinsRule.waitUntilNoActivity();

    }
}
