package com.zkztch.jenkins.plugins.pipeline.steps;

import hudson.Extension;
import org.apache.commons.lang3.StringUtils;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.models.Project;
import org.gitlab4j.api.models.Tag;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.PrintStream;
import java.util.List;

public class GitlabDeleteTagStep extends AbstractGitlabStep {

    public static final String STEP = "gitlabDeleteTag";

    private String tag;

    @DataBoundConstructor
    public GitlabDeleteTagStep(String tag) {
        this.tag = tag;
    }

    @Override
    public Object doStart(StepContext context, PrintStream logger, GitLabApi gitLabApi, Project project) throws Exception {
        List<Tag> tags = gitLabApi.getTagsApi().getTags(project);
        for (Tag t : tags) {
            if (StringUtils.equals(t.getName(), tag)) {
                gitLabApi.getTagsApi().deleteTag(project, tag);
                break;
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
