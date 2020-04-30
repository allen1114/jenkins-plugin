package com.zkztch.jenkins.plugins.pipeline.steps;

import hudson.Extension;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.models.Project;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.PrintStream;

public class GitlabGetAuthorEmailStep extends GitlabBaseStep {
    public static final String STEP = "gitlabGetAuthorEmail";

    private String commit;

    @DataBoundConstructor
    public GitlabGetAuthorEmailStep(String commit) {
        this.commit = commit;
    }

    @Override
    public Object doStart(StepContext context, PrintStream logger, GitLabApi gitLabApi, Project project) throws Exception {
        try {
            return gitLabApi.getCommitsApi().getCommit(project, this.commit).getAuthorEmail();
        } catch (Exception e) {
            e.printStackTrace(logger);
            return null;
        }
    }

    @Extension
    public static class Descriptor extends GitlabStepDescriptor {
        @Override
        public String getFunctionName() {
            return STEP;
        }
    }
}
