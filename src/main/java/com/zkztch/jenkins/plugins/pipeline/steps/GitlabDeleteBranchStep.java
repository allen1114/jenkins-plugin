package com.zkztch.jenkins.plugins.pipeline.steps;

import hudson.Extension;
import org.apache.commons.lang3.StringUtils;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.models.Branch;
import org.gitlab4j.api.models.Project;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.PrintStream;
import java.util.List;

public class GitlabDeleteBranchStep extends GitlabBaseStep {
    public static final String STEP = "gitlabDeleteBranch";

    private String branch;

    @DataBoundConstructor
    public GitlabDeleteBranchStep(String branch) {
        this.branch = branch;
    }

    @Override
    public Object doStart(StepContext context, PrintStream logger, GitLabApi gitLabApi, Project project) throws Exception {
        List<Branch> branchList = gitLabApi.getRepositoryApi().getBranches(project);
        for (Branch b : branchList) {
            if (StringUtils.equals(b.getName(), branch)) {
                try {
                    gitLabApi.getProtectedBranchesApi().unprotectBranch(project, branch);
                } catch (Exception ignored) {

                }
                gitLabApi.getRepositoryApi().deleteBranch(project, branch);
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
