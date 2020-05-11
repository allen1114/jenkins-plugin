package com.zkztch.jenkins.plugins.pipeline.steps;

import com.spotify.docker.client.DockerClient;
import hudson.Extension;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.PrintStream;

public class DockerStopStep extends DockerBaseStep {

    public static final String STEP = "dockerStop";

    private String id;
    private int secondsToWait = 60;

    @DataBoundConstructor
    public DockerStopStep(String id) {
        this.id = id;
    }

    @DataBoundSetter
    public void setSecondsToWait(int secondsToWait) {
        this.secondsToWait = secondsToWait;
    }

    @Override
    protected Object doStart(StepContext context, PrintStream logger, DockerClient dockerClient) throws Exception {
        dockerClient.stopContainer(id, secondsToWait);
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
