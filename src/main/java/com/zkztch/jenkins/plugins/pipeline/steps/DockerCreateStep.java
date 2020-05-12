package com.zkztch.jenkins.plugins.pipeline.steps;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.ObjectMapperProvider;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import hudson.Extension;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.PrintStream;

public class DockerCreateStep extends DockerBaseStep {

    public static final String STEP = "dockerCreate";

    private String name;
    private String config = "{}";

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
        ObjectMapper objectMapper = ObjectMapperProvider.objectMapper();
        ContainerConfig containerConfig = objectMapper.readValue(config, ContainerConfig.class);
//
//        ContainerConfig.Builder containerConfigBuiler = ContainerConfig.builder();
//        if (params != null) {
//            for (Map.Entry<String, Object> entry : params.entrySet()) {
//                String key = entry.getKey();
//                Object val = entry.getValue();
//                if (val != null) {
//                    Method method = MethodUtils.getMatchingAccessibleMethod(ContainerConfig.Builder.class, key, val.getClass());
//                    if (method != null) {
//                        method.invoke(containerConfigBuiler, val);
//                    }
//                }
//            }
//        }
//        ContainerCreation creation = dockerClient.createContainer(containerConfigBuiler.build(), name);
        ContainerCreation creation = dockerClient.createContainer(containerConfig, name);
        return creation.id();
    }

    @Extension
    public static class Descriptor extends DockerStepDescriptor {
        @Override
        public String getFunctionName() {
            return STEP;
        }
    }
}
