package com.zkztch.jenkins.plugins.pipeline.steps;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.Container;
import hudson.Extension;
import hudson.FilePath;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.kohsuke.stapler.DataBoundConstructor;

import java.nio.file.Paths;

public class DockerCopyStep extends AbstractDockerContainerStep {
    public static final String STEP = "dockerCopy";

    private String fromDir;
    private String tarToSend;
    private String toPath;

    @DataBoundConstructor
    public DockerCopyStep(String fromDir, String tarToSend, String toPath) {
        this.fromDir = fromDir;
        this.tarToSend = tarToSend;
        this.toPath = toPath;
    }

    @Override
    protected Object doStep(StepContext context, DockerClient dockerClient, Container container) throws Exception {
        FilePath workspace = context.get(FilePath.class);
        if (StringUtils.isNotBlank(fromDir)) {
            FilePath formPath = workspace.child(fromDir);
            dockerClient.copyToContainer(Paths.get(formPath.toURI()), container.id(), toPath);
        }
        if (StringUtils.isNotBlank(tarToSend)) {
            FilePath tarPath = workspace.child(tarToSend);
            dockerClient.copyToContainer(tarPath.read(), container.id(), toPath);
        }
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
