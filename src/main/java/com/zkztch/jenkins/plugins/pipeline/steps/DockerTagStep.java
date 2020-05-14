package com.zkztch.jenkins.plugins.pipeline.steps;

import com.spotify.docker.client.DockerClient;
import hudson.Extension;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.PrintStream;

public class DockerTagStep extends AbstractDockerStep {

    public static final String STEP = "dockerTag";

    private String image;
    private String tag;
    private boolean force;

    @DataBoundConstructor
    public DockerTagStep(String image, String tag, boolean force) {
        this.image = image;
        this.tag = tag;
        this.force = force;
    }

    @Override
    protected Object doStart(StepContext context, PrintStream logger, DockerClient dockerClient) throws Exception {
        dockerClient.tag(image, tag, force);
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
