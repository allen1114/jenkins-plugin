package com.zkztch.jenkins.plugins.pipeline.steps;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.Container;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.PrintStream;
import java.util.List;

public abstract class AbstractDockerContainerStep extends AbstractDockerStep {

    private String container;
    private boolean failWhenNotFound = false;

    @DataBoundSetter
    public void setContainer(String container) {
        this.container = container;
    }

    @DataBoundSetter
    public void setFailWhenNotFound(boolean failWhenNotFound) {
        this.failWhenNotFound = failWhenNotFound;
    }

    @Override
    protected Object doStart(StepContext context, PrintStream logger, DockerClient dockerClient) throws Exception {
        String clt = container.toLowerCase().trim();
        List<Container> containers = dockerClient.listContainers(DockerClient.ListContainersParam.allContainers());
        for (Container c : containers) {
            String idlt = c.id().toLowerCase().trim();
            if ((StringUtils.equals(idlt, clt)) || (StringUtils.length(clt) >= 12 && StringUtils.startsWith(idlt, clt))) {
                return doStep(context, dockerClient, c);
            }
            for (String name : c.names()) {
                if ((StringUtils.startsWith(name, "/") && StringUtils.equals(StringUtils.substringAfter(name, "/"), container))
                        || StringUtils.equals(name, container)) {
                    return doStep(context, dockerClient, c);
                }
            }
        }
        if (failWhenNotFound) {
            throw new IllegalStateException("container not found");
        }
        return null;
    }

    protected abstract Object doStep(StepContext context, DockerClient dockerClient, Container container)
            throws Exception;

}
