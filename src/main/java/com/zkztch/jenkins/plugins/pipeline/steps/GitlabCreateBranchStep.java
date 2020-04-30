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

public class GitlabCreateBranchStep extends GitlabBaseStep {

    public static final String STEP = "gitlabCreateBranch";
    private String branch;
    private String ref;
    private Boolean force;

    @DataBoundConstructor
    public GitlabCreateBranchStep(String branch, String ref, Boolean force) {
        this.branch = branch;
        this.ref = ref;
        this.force = force;
    }

    public String getBranch() {
        return branch;
    }

    public String getRef() {
        return ref;
    }

    public Boolean getForce() {
        return force;
    }

    @Override
    public Object doStart(StepContext context, PrintStream logger, GitLabApi gitLabApi, Project project) throws Exception {

        List<Branch> branchList = gitLabApi.getRepositoryApi().getBranches(project);
        for (Branch b : branchList) {
            if (StringUtils.equals(b.getName(), branch)) {
                if (force) {
                    try {
                        gitLabApi.getProtectedBranchesApi().unprotectBranch(project, branch);
                    } catch (Exception ignored) {

                    }
                    gitLabApi.getRepositoryApi().deleteBranch(project, branch);
                    break;
                } else {
                    throw new IllegalStateException("branch exist");
                }
            }
        }

        gitLabApi.getRepositoryApi().createBranch(project, branch, ref);
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
