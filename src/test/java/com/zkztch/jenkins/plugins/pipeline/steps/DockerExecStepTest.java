package com.zkztch.jenkins.plugins.pipeline.steps;

import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.zkztch.test.Docker;
import com.zkztch.test.Jenkins;
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

import java.util.UUID;

@Slf4j
public class DockerExecStepTest {

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
        Docker.client.startContainer(containerId);
    }

    @After
    public void clean() throws Exception {
        log.info("clean");
        Docker.client.stopContainer(containerId, 60);
        Docker.client.removeContainer(containerId);
    }

    @Test
    public void copyTest() throws Exception {
        String cmd = "ls -la";
        String format = "pipeline {\n" +
                "    agent any\n" +
                "    stages {\n" +
                "        stage(\"start\") {\n" +
                "            steps {\n" +
                "               script {\n" +
                "                   def exitcode = dockerExec cmd:'%s', container:'%s', dockerHost:'%s', dockerCertPath:'%s', registryUrl:'%s', registryUsername:'%s', registryPassword:'%s'\n" +
                "                   echo 'exitcode='+exitcode\n" +
                "               }\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}";
        String script = String.format(format, cmd, containerId, Docker.DOCKER_HOST, Docker.DOCKER_CERT_PATH,
                Docker.DOCKER_REPO_HOST, Docker.DOCKER_REPO_USERNAME, Docker.DOCKER_REPO_PASSWORD);
        log.info("script = " + script);
        WorkflowJob job = jenkinsRule.createProject(WorkflowJob.class, name);
        job.setDefinition(new CpsFlowDefinition(script, true));
        WorkflowRun run = jenkinsRule.assertBuildStatusSuccess(job.scheduleBuild2(0));
        jenkinsRule.waitUntilNoActivity();
        jenkinsRule.assertLogContains("exitcode=0", run);
    }
}
