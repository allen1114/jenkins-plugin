package com.zkztch.jenkins.plugins.pipeline.steps;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.Image;

import com.zkztch.test.Docker;
import com.zkztch.test.Jenkins;
import com.zkztch.test.TestPropertiesUtils;
import lombok.extern.slf4j.Slf4j;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.*;
import org.jvnet.hudson.test.BuildWatcher;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.List;

@Slf4j
public class DockerPushStepTest {

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
    public void pushTest() throws Exception {

        String format = "pipeline {\n" +
                "    agent any\n" +
                "    stages {\n" +
                "        stage(\"start\") {\n" +
                "            steps {\n" +
                "               script {\n" +
                "                   dockerPush image:'%s', dockerHost:'%s', dockerCertPath:'%s', registryUrl:'%s', registryUsername:'%s', registryPassword:'%s'\n" +
                "               }\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}";
        String script = String.format(format, image, Docker.DOCKER_HOST, Docker.DOCKER_CERT_PATH, Docker.DOCKER_REPO_HOST,
                Docker.DOCKER_REPO_USERNAME, Docker.DOCKER_REPO_PASSWORD);

        log.info("script = " + script);
        WorkflowJob job = jenkinsRule.createProject(WorkflowJob.class, "pushTest");
        job.setDefinition(new CpsFlowDefinition(script, true));
        WorkflowRun run = jenkinsRule.assertBuildStatusSuccess(job.scheduleBuild2(0));

        jenkinsRule.waitUntilNoActivity();

        Docker.client.removeImage(image);
        Docker.client.pull(image);

        boolean imageFound = false;
        List<Image> imageList = Docker.client.listImages(DockerClient.ListImagesParam.allImages());
        for (Image i : imageList) {
            for (String s : i.repoTags()) {
                if (s.equals(image)) {
                    imageFound = true;
                    break;
                }
            }
        }
        Assert.assertTrue(imageFound);

    }

}
