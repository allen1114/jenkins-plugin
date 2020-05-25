package com.zkztch.jenkins.plugins.pipeline.steps;

import hudson.Extension;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.models.Project;
import org.gitlab4j.api.models.Tag;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class GitlabGetTagListStep extends AbstractGitlabStep {

    public static final String STEP = "gitlabGetTagList";

    @DataBoundConstructor
    public GitlabGetTagListStep() {
    }

    @Override
    public Object doStart(StepContext context, PrintStream logger, GitLabApi gitLabApi, Project project) throws Exception {

        List<Tag> tagList = gitLabApi.getTagsApi().getTags(project);
        List<String> tags = new ArrayList<>();
        for (Tag t : tagList) {
            tags.add(t.getName());
        }
        return tags;
    }

    @Extension
    public static class Descriptor extends GitlabStepDescriptor {
        @Override
        public String getFunctionName() {
            return STEP;
        }
    }
}
