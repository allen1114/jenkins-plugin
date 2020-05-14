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

public class GitlabCreateTagStep extends AbstractGitlabStep {
    public static final String STEP = "gitlabCreateTag";

    private String tag;
    private String ref;
    private Boolean force;

    @DataBoundConstructor
    public GitlabCreateTagStep(String tag, String ref, Boolean force) {
        this.tag = tag;
        this.ref = ref;
        this.force = force;
    }

    @Override
    public Object doStart(StepContext context, PrintStream logger, GitLabApi gitLabApi, Project project) throws Exception {
        List<Tag> tags = gitLabApi.getTagsApi().getTags(project);
        for (Tag t : tags) {
            if (StringUtils.equals(t.getName(), tag)) {
                if (force) {
                    gitLabApi.getTagsApi().deleteTag(project, tag);
                } else {
                    throw new IllegalStateException("tag exist");
                }
                break;
            }
        }
        gitLabApi.getTagsApi().createTag(project, tag, ref);

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
