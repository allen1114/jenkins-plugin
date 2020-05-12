package com.zkztch.jenkins.plugins.pipeline.steps;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.Image;
import com.zkztch.jenkins.test.Docker;
import com.zkztch.jenkins.test.Jenkins;
import com.zkztch.jenkins.test.TestPropertiesUtils;
import lombok.extern.slf4j.Slf4j;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.*;
import org.jvnet.hudson.test.BuildWatcher;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.List;

@Slf4j
public class DockerPullStepTest {

    @ClassRule public static final BuildWatcher buildWatcher = Jenkins.buildWatcher;
    @ClassRule public static final JenkinsRule jenkinsRule = Jenkins.jenkinsRule;

    private static final String image = TestPropertiesUtils.getProperty("docker.test.pull.image");

    @Before
    public void setup() throws DockerException, InterruptedException {
        log.info("setup");

        Docker.client.tag(Docker.DOCKER_TEST_BASEIMAGE, image, true);
        Docker.client.push(image);

        try {
            Docker.client.removeImage(image);
        } catch (Exception ignored) {

        }
    }

    @After
    public void clean() {
        log.info("clean");
        try {
            Docker.client.removeImage(image);
        } catch (Exception ignored) {

        }
    }

    @Test
    public void pullTest() throws Exception {

        String script = String.format(
                "script {\n" +
                        "dockerPull image:'%s', dockerHost:'%s', dockerCertPath:'%s', registryUrl:'%s', registryUsername:'%s', registryPassword:'%s'\n" +
                        "\n}",
                image, Docker.DOCKER_HOST, Docker.DOCKER_CERT_PATH, Docker.DOCKER_REGISTRY_URL, Docker.DOCKER_REGISTRY_USERNAME,
                Docker.DOCKER_REGISTRY_PASSWORD);

        log.info("script = " + script);
        WorkflowJob job = jenkinsRule.createProject(WorkflowJob.class, "pullTest");
        job.setDefinition(new CpsFlowDefinition(script, true));
        WorkflowRun run = jenkinsRule.assertBuildStatusSuccess(job.scheduleBuild2(0));

        jenkinsRule.waitUntilNoActivity();

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
