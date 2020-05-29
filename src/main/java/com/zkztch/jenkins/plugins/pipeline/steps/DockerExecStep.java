package com.zkztch.jenkins.plugins.pipeline.steps;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ExecCreation;
import com.spotify.docker.client.messages.ExecState;
import hudson.Extension;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.kohsuke.stapler.DataBoundConstructor;

public class DockerExecStep extends AbstractDockerContainerStep {
    public static final String STEP = "dockerExec";

    private String cmd;

    @DataBoundConstructor
    public DockerExecStep(String cmd) {
        this.cmd = cmd;
    }

    @Override
    protected Object doStep(StepContext context, DockerClient dockerClient, Container container) throws Exception {
        DockerClient.ExecCreateParam[] createParams = new DockerClient.ExecCreateParam[]{DockerClient.ExecCreateParam.attachStdout(),
                DockerClient.ExecCreateParam.attachStderr(),
                DockerClient.ExecCreateParam.attachStdin(),
                DockerClient.ExecCreateParam.tty()};

        ExecCreation execCreation = dockerClient.execCreate(container.id(), new String[]{"sh", "-c", cmd}, createParams);

        try (LogStream stream = dockerClient.execStart(execCreation.id())) {
            String output = stream.readFully();
            ExecState state = dockerClient.execInspect(execCreation.id());
            if (state.exitCode() != 0) {
                throw new IllegalStateException(output);
            }
            return output;
        }
    }

    @Extension
    public static class Descriptor extends DockerStepDescriptor {
        @Override
        public String getFunctionName() {
            return STEP;
        }
    }

}
