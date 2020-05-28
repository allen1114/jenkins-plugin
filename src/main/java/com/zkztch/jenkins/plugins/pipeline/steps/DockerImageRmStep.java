package com.zkztch.jenkins.plugins.pipeline.steps;

import com.spotify.docker.client.DockerClient;
import hudson.Extension;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.PrintStream;

public class DockerImageRmStep extends AbstractDockerStep {
    public static final String STEP = "dockerImageRm";

    private String image;
    private boolean force;
    private boolean noPrune;

    @DataBoundConstructor
    public DockerImageRmStep(String image, boolean force, boolean noPrune) {
        this.image = image;
        this.force = force;
        this.noPrune = noPrune;
    }

    @Override
    protected Object doStart(StepContext context, PrintStream logger, DockerClient dockerClient) throws Exception {
        dockerClient.removeImage(image, force, noPrune);
        return null;
    }

    @Extension
    public static class Descriptor extends AbstractDockerStep.DockerStepDescriptor {
        @Override
        public String getFunctionName() {
            return STEP;
        }
    }
}
