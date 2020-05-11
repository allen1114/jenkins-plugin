package com.zkztch.jenkins.plugins.pipeline.steps;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import hudson.Extension;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.Map;

public class DockerCreateStep extends DockerBaseStep {

    public static final String STEP = "dockerCreate";

    private String name;
    private Map<String, Object> params;

    @DataBoundConstructor
    public DockerCreateStep(String name, Map<String, Object> params) {
        this.name = name;
        this.params = params;
    }

    @Override
    protected Object doStart(StepContext context, PrintStream logger, DockerClient dockerClient)
            throws Exception {
        ContainerConfig.Builder containerConfigBuiler = ContainerConfig.builder();
        if (params != null) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                String key = entry.getKey();
                Object val = entry.getValue();
                if (val != null) {
                    Method method = MethodUtils.getMatchingAccessibleMethod(ContainerConfig.Builder.class, key, val.getClass());
                    if (method != null) {
                        method.invoke(containerConfigBuiler, val);
                    }
                }
            }
        }
        ContainerCreation creation = dockerClient.createContainer(containerConfigBuiler.build(), name);
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
