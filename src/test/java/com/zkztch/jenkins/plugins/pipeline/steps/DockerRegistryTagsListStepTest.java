package com.zkztch.jenkins.plugins.pipeline.steps;

import com.spotify.docker.client.exceptions.DockerException;
import com.zkztch.jenkins.test.Docker;
import com.zkztch.jenkins.test.Jenkins;
import com.zkztch.jenkins.test.TestPropertiesUtils;
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

@Slf4j
public class DockerRegistryTagsListStepTest {

    @ClassRule public static final BuildWatcher buildWatcher = Jenkins.buildWatcher;
    @ClassRule public static final JenkinsRule jenkinsRule = Jenkins.jenkinsRule;

    private static final String imageName = TestPropertiesUtils.getProperty("docker.registry.test.image");
    private static final String tagName = TestPropertiesUtils.getProperty("docker.registry.test.tag");
    private static final String image = Docker.DOCKER_REPO_HOST + "/" + imageName + ":" + tagName;

    @Before
    public void setup() throws DockerException, InterruptedException {
        log.info("setup");
        Docker.client.tag(Docker.DOCKER_TEST_BASEIMAGE, image, true);
        Docker.client.push(image);
    }

    @After
    public void clean() throws DockerException, InterruptedException {
        log.info("clean");
        Docker.client.removeImage(image);
    }

    @Test
    public void listTest() throws Exception {
        String format = "pipeline {\n" +
                "    agent any\n" +
                "    stages {\n" +
                "        stage(\"start\") {\n" +
                "            steps {\n" +
                "               script {\n" +
                "                   def list = dockerRegistryTagsList image:'%s', registryUrl:'%s', registryUsername:'%s', registryPassword:'%s'\n" +
                "                   for(String tag : list){echo tag}\n" +
                "               }\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}";
        String script = String.format(format, imageName,TestPropertiesUtils.getProperty("docker.registory.host"), Docker.DOCKER_REPO_USERNAME,
                Docker.DOCKER_REPO_PASSWORD);

        log.info("script = " + script);
        WorkflowJob job = jenkinsRule.createProject(WorkflowJob.class, "pushTest");
        job.setDefinition(new CpsFlowDefinition(script, true));
        WorkflowRun run = jenkinsRule.assertBuildStatusSuccess(job.scheduleBuild2(0));

        jenkinsRule.waitUntilNoActivity();

    }


}
