package com.zkztch.jenkins.plugins.pipeline.steps;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.Image;
import hudson.Extension;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

public class DockerImageLsStep extends AbstractDockerStep {
    public static final String STEP = "dockerImageLs";

    private String filter;

    @DataBoundConstructor
    public DockerImageLsStep(String filter) {
        this.filter = filter;
    }

    @Override
    protected Object doStart(StepContext context, PrintStream logger, DockerClient dockerClient) throws Exception {
        String[] filters = StringUtils.split(filter, "=");
        if (filters == null || filters.length != 2) {
            throw new IllegalStateException("error filter");
        }
        DockerClient.ListImagesParam listImagesParam = null;
        switch (filters[0]) {
            case "before":
            case "since":
            case "dangling":
            case "label":
            case "reference":
                listImagesParam = DockerClient.ListImagesParam.filter(filters[0], filters[1]);
                break;
            default:
                throw new IllegalStateException("error filter");
        }
        Set<String> images = new HashSet<>();
        for (Image image : dockerClient.listImages(listImagesParam)) {

            if (image.repoTags() != null) {
                images.addAll(image.repoTags());
            } else if (image.repoDigests() != null) {
                images.addAll(image.repoDigests());
            }
        }
        return images;
    }

    @Extension
    public static class Descriptor extends DockerStepDescriptor {
        @Override
        public String getFunctionName() {
            return STEP;
        }
    }
}
