package com.zkztch.jenkins.plugins.pipeline.steps;

import com.spotify.docker.client.DockerClient;
import hudson.Extension;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.PrintStream;

public class DockerRemoveStep extends DockerBaseStep {
    public static final String STEP = "dockerRemove";

    private String id;

    @DataBoundConstructor
    public DockerRemoveStep(String id) {
        this.id = id;
    }

    @Override
    protected Object doStart(StepContext context, PrintStream logger, DockerClient dockerClient) throws Exception {
        dockerClient.removeContainer(id);
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
