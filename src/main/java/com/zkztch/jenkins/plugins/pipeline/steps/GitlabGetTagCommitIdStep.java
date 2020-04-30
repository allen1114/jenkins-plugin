package com.zkztch.jenkins.plugins.pipeline.steps;

import hudson.Extension;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.models.Project;
import org.gitlab4j.api.models.Tag;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.PrintStream;
import java.util.List;

public class GitlabGetTagCommitIdStep extends GitlabBaseStep {
    public static final String STEP = "gitlabGetTagCommitId";

    private String tag;

    @DataBoundConstructor
    public GitlabGetTagCommitIdStep(String tag) {
        this.tag = tag;
    }

    @Override
    public Object doStart(StepContext context, PrintStream logger, GitLabApi gitLabApi, Project project) throws Exception {
        List<Tag> tags = gitLabApi.getTagsApi().getTags(project);
        for (Tag tag : tags) {
            if (tag.getName().equals(this.tag)) {
                return tag.getCommit().getId();
            }
        }
        return null;
    }

    @Extension
    public static class Descriptor extends GitlabStepDescriptor {
        @Override
        public String getFunctionName() {
            return STEP;
        }
    }
}
