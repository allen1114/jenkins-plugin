package com.zkztch.jenkins.plugins.pipeline.steps;

import hudson.Extension;
import lombok.ToString;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.models.AccessLevel;
import org.gitlab4j.api.models.Project;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.PrintStream;

@ToString
public class GitlabProtectBranchStep extends AbstractGitlabStep {

    public static final String STEP = "gitlabProtectBranch";

    private String branch;
    private String level;

    @DataBoundConstructor
    public GitlabProtectBranchStep(String branch, String level) {
        this.branch = branch;
        this.level = level;
    }

    public String getBranch() {
        return branch;
    }

    public String getLevel() {
        return level;
    }

    @Override
    public Object doStart(StepContext context, PrintStream logger, GitLabApi gitLabApi, Project project) throws Exception {

        AccessLevel accessLevel = AccessLevel.valueOf(level);
        try {
            gitLabApi.getProtectedBranchesApi().unprotectBranch(project, branch);
        } catch (Exception ignored) {

        }
        gitLabApi.getProtectedBranchesApi().protectBranch(project, branch, accessLevel, accessLevel);

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
