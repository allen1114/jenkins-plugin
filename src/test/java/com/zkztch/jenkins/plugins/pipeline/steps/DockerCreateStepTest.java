package com.zkztch.jenkins.plugins.pipeline.steps;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.Container;
import com.zkztch.jenkins.test.Docker;
import com.zkztch.jenkins.test.Jenkins;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.*;
import org.jvnet.hudson.test.BuildWatcher;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.List;
import java.util.UUID;

@Slf4j
public class DockerCreateStepTest {

    @ClassRule public static final BuildWatcher buildWatcher = Jenkins.buildWatcher;
    @ClassRule public static final JenkinsRule jenkinsRule = Jenkins.jenkinsRule;

    private String containerName;
    private String containerId;

    @Before
    public void setup() {
        log.info("setup");
        containerName = UUID.randomUUID().toString();
        containerId = null;
    }

    @After
    public void clean() throws Exception {
        log.info("clean");
        if (StringUtils.isNotBlank(containerId)) {
            Docker.client.removeContainer(containerId);
        }
    }

    @Test
    public void createContainerTest() throws Exception {

        String script = String.format(
                "script {\n" +
                        "def containerId = dockerCreate name:'%s', params:['image':'%s'], dockerHost:'%s', dockerCertPath:'%s'\n" +
                        "echo containerId \n" +
                        "\n}",
                containerName, Docker.DOCKER_TEST_BASEIMAGE, Docker.DOCKER_HOST, Docker.DOCKER_CERTS_PATH);
        log.info("script = " + script);
        WorkflowJob job = jenkinsRule.createProject(WorkflowJob.class, containerName);
        job.setDefinition(new CpsFlowDefinition(script, true));
        WorkflowRun run = jenkinsRule.assertBuildStatusSuccess(job.scheduleBuild2(0));
        jenkinsRule.waitUntilNoActivity();

        List<Container> containers = Docker.client.listContainers(DockerClient.ListContainersParam.allContainers());
        for (Container c : containers) {
            for (String n : c.names()) {
                if (n.contains(containerName)) {
                    containerId = c.id();
                    break;
                }
            }
        }
        Assert.assertNotNull(containerId);
        jenkinsRule.assertLogContains(containerId, run);

    }
}
