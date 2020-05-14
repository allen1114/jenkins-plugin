package com.zkztch.jenkins.plugins.pipeline.steps;

import com.spotify.docker.client.DockerClient;
import hudson.Extension;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.PrintStream;

public class DockerPushStep extends AbstractDockerStep {

    public static final String STEP = "dockerPush";

    private String image;

    @DataBoundConstructor
    public DockerPushStep(String image) {
        this.image = image;
    }

    @Override
    protected Object doStart(StepContext context, PrintStream logger, DockerClient dockerClient) throws Exception {
        dockerClient.push(image);
        return null;
    }

    @Extension
    public static class Descriptor extends DockerStepDescriptor {
        @Override
        public String getFunctionName() {
            return STEP;
        }
    }
}
