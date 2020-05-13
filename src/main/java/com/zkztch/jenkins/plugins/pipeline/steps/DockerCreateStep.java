package com.zkztch.jenkins.plugins.pipeline.steps;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.ObjectMapperProvider;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.shaded.com.fasterxml.jackson.core.JsonParseException;
import com.spotify.docker.client.shaded.com.fasterxml.jackson.databind.JsonMappingException;
import com.spotify.docker.client.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.PrintStream;

public class DockerCreateStep extends DockerBaseStep {

    public static final String STEP = "dockerCreate";

    private static final ObjectMapper objectMapper = ObjectMapperProvider.objectMapper();

    private String name;
    private String config;

    @DataBoundConstructor
    public DockerCreateStep() {
    }

    @DataBoundSetter
    public void setName(String name) {
        this.name = name;
    }

    @DataBoundSetter
    public void setConfig(String config) {
        this.config = config;
    }

    @Override
    protected Object doStart(StepContext context, PrintStream logger, DockerClient dockerClient)
            throws Exception {

        ContainerConfig containerConfig = null;
        try {
            containerConfig = objectMapper.readValue(config, ContainerConfig.class);
        } catch (JsonParseException | JsonMappingException e) {
            logger.println("config seem not a correct json string! try resolve as a file path.");
            FilePath workspace = context.get(FilePath.class);
            FilePath configFile = workspace.child(config);
            if (configFile.exists()) {
                String configJson = context.get(EnvVars.class).expand(configFile.readToString().trim());
                containerConfig = objectMapper.readValue(configJson, ContainerConfig.class);
            }
        }
        return dockerClient.createContainer(containerConfig, name).id();
    }

    @Extension
    public static class Descriptor extends DockerStepDescriptor {
        @Override
        public String getFunctionName() {
            return STEP;
        }
    }
}
