package com.zkztch.jenkins.plugins.pipeline.steps;

import com.spotify.docker.client.exceptions.DockerException;
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

import java.util.UUID;

@Slf4j
public class DockerImageLsStepTest {
    @ClassRule public static final BuildWatcher buildWatcher = Jenkins.buildWatcher;
    @ClassRule public static final JenkinsRule jenkinsRule = Jenkins.jenkinsRule;

    private static final String image = TestPropertiesUtils.getProperty("docker.test.push.image");

    @Before
    public void setup() throws DockerException, InterruptedException {
        log.info("setup");
        Docker.client.tag(Docker.DOCKER_TEST_BASEIMAGE, image, true);
    }

    @After
    public void clean() throws DockerException, InterruptedException {
        log.info("clean");
        try {
            Docker.client.removeImage(image);
        } catch (Exception ignored) {

        }
    }

    @Test
    public void lsByReference() throws Exception {
        String filter = "reference=" + image;
        String format = "pipeline {\n" +
                "    agent any\n" +
                "    stages {\n" +
                "        stage(\"start\") {\n" +
                "            steps {\n" +
                "               script {\n" +
                "                   def images = dockerImageLs filter:'%s', dockerHost:'%s', dockerCertPath:'%s', registryUrl:'%s', registryUsername:'%s', registryPassword:'%s'\n" +
                "                   images.each{\n" +
                "                       echo 'imageid='+it \n" +
                "                   }\n" +
                "               }\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}";
        String script = String.format(format, filter, Docker.DOCKER_HOST, Docker.DOCKER_CERT_PATH, Docker.DOCKER_REPO_HOST,
                Docker.DOCKER_REPO_USERNAME, Docker.DOCKER_REPO_PASSWORD);

        log.info("script = " + script);
        WorkflowJob job = jenkinsRule.createProject(WorkflowJob.class, UUID.randomUUID().toString());
        job.setDefinition(new CpsFlowDefinition(script, true));
        WorkflowRun run = jenkinsRule.assertBuildStatusSuccess(job.scheduleBuild2(0));
        jenkinsRule.waitUntilNoActivity();
        jenkinsRule.assertLogContains("imageid=", run);
    }

}
