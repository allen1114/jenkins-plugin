package com.zkztch.jenkins.plugins.pipeline.steps;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.Image;
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
import java.util.UUID;

@Slf4j
public class DockerTagStepTest {

    @ClassRule public static final BuildWatcher buildWatcher = Jenkins.buildWatcher;
    @ClassRule public static final JenkinsRule jenkinsRule = Jenkins.jenkinsRule;

    private String tag;

    @Before
    public void setup() {
        log.info("setup");
        tag = UUID.randomUUID().toString() + ":test";
    }

    @After
    public void clean() {
        log.info("clean");
        try {
            Docker.client.removeImage(tag);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void tagTest() throws Exception {
        String format = "pipeline {\n" +
                "    agent any\n" +
                "    stages {\n" +
                "        stage(\"start\") {\n" +
                "            steps {\n" +
                "               script {\n" +
                "                   dockerTag image:'%s', tag: '%s', dockerHost:'%s', dockerCertPath:'%s', registryUrl:'%s', registryUsername:'%s', registryPassword:'%s'\n" +
                "               }\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}";
        String script = String.format(format, Docker.DOCKER_TEST_BASEIMAGE, tag, Docker.DOCKER_HOST, Docker.DOCKER_CERT_PATH, Docker.DOCKER_REPO_HOST,
                Docker.DOCKER_REPO_USERNAME, Docker.DOCKER_REPO_PASSWORD);
        log.info("script = " + script);
        WorkflowJob job = jenkinsRule.createProject(WorkflowJob.class, "tagTest");
        job.setDefinition(new CpsFlowDefinition(script, true));
        WorkflowRun run = jenkinsRule.assertBuildStatusSuccess(job.scheduleBuild2(0));
        jenkinsRule.waitUntilNoActivity();

        boolean imageFound = false;
        List<Image> imageList = Docker.client.listImages(DockerClient.ListImagesParam.allImages());
        for (Image i : imageList) {
            for (String s : i.repoTags()) {
                if (s.equals(tag)) {
                    imageFound = true;
                    break;
                }
            }
        }
        Assert.assertTrue(imageFound);

    }
}
