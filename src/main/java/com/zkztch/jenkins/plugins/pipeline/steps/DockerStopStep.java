package com.zkztch.jenkins.plugins.pipeline.steps;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.Container;
import hudson.Extension;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

public class DockerStopStep extends AbstractDockerContainerStep {

    public static final String STEP = "dockerStop";

    @DataBoundConstructor
    public DockerStopStep() {

    }

    private int secondsToWait = 60;

    @DataBoundSetter
    public void setSecondsToWait(int secondsToWait) {
        this.secondsToWait = secondsToWait;
    }


    @Override
    protected Object doStep(StepContext context, DockerClient dockerClient, Container container) throws DockerException, InterruptedException {
        dockerClient.stopContainer(container.id(), secondsToWait);
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
