package com.zkztch.jenkins.plugins.pipeline.steps;

import com.zkztch.docker.registry.DockerRegistryClient;
import hudson.Extension;
import lombok.Getter;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.PrintStream;

@Getter
public class DockerRepoListTagsStep extends AbstractDockerRepoStep {

    public static final String STEP = "dockerRepoListTags";

    private String repository;

    @DataBoundConstructor
    public DockerRepoListTagsStep(String repository) {
        this.repository = repository;
    }

    @Override
    protected Object doStart(StepContext context, PrintStream logger, DockerRegistryClient dockerRegistryClient) throws Exception {
        return dockerRegistryClient.listTags(repository);
    }

    @Extension
    public static class Descriptor extends DockerRegistryStepDescriptor {
        @Override
        public String getFunctionName() {
            return STEP;
        }
    }
}
