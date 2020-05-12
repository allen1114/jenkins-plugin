package com.zkztch.jenkins.plugins.pipeline.steps;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.Container;
import hudson.Extension;
import org.kohsuke.stapler.DataBoundConstructor;

public class DockerStateStep extends DockerContainerStep {

    public static final String STEP = "dockerState";
    @DataBoundConstructor
    public DockerStateStep() {
    }

    @Override
    protected Object doStep(DockerClient dockerClient, Container container) throws DockerException, InterruptedException {
        return container.state();
    }


    @Extension
    public static class Descriptor extends DockerStepDescriptor {
        @Override
        public String getFunctionName() {
            return STEP;
        }
    }
}
