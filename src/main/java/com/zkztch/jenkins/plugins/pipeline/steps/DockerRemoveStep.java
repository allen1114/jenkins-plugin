package com.zkztch.jenkins.plugins.pipeline.steps;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.Container;
import hudson.Extension;
import org.kohsuke.stapler.DataBoundConstructor;

public class DockerRemoveStep extends DockerContainerStep {
    public static final String STEP = "dockerRemove";
    @DataBoundConstructor
    public DockerRemoveStep() {
    }

    @Override
    protected Object doStep(DockerClient dockerClient, Container container) throws DockerException, InterruptedException {
        dockerClient.removeContainer(container.id());
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
