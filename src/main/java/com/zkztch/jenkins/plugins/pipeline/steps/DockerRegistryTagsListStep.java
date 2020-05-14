package com.zkztch.jenkins.plugins.pipeline.steps;

import com.zkztch.docker.DockerRegistryClient;
import hudson.Extension;
import lombok.Data;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.PrintStream;
import java.util.List;

@Data
public class DockerRegistryTagsListStep extends AbstractDockerRegistryStep {

    public static final String STEP = "dockerRegistryTagsList";

    private String image;

    @DataBoundConstructor
    public DockerRegistryTagsListStep(String image) {
        this.image = image;
    }

    @Override
    protected Object doStart(StepContext context, PrintStream logger, DockerRegistryClient dockerRegistryClient) throws Exception {
        List<String> strings = dockerRegistryClient.tagsList(image).getTags();
        return strings;
    }

    @Extension
    public static class Descriptor extends DockerRegistryStepDescriptor {
        @Override
        public String getFunctionName() {
            return STEP;
        }
    }
}
