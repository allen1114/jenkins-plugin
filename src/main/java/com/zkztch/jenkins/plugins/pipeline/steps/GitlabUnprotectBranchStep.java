package com.zkztch.jenkins.plugins.pipeline.steps;

import hudson.Extension;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.models.Project;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.PrintStream;

public class GitlabUnprotectBranchStep extends AbstractGitlabStep {

    public static final String STEP = "gitlabUnprotectBranch";

    private String branch;

    @DataBoundConstructor
    public GitlabUnprotectBranchStep(String branch) {
        this.branch = branch;
    }

    @Override
    public Object doStart(StepContext context, PrintStream logger, GitLabApi gitLabApi, Project project) throws Exception {
        try {
            gitLabApi.getProtectedBranchesApi().unprotectBranch(project, branch);
        } catch (Exception ignored) {

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
