package com.zkztch.jenkins.plugins.pipeline.steps;

import com.google.common.collect.ImmutableSet;
import com.zkztch.jenkins.plugins.pipeline.GitlabConsts;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.TaskListener;
import lombok.ToString;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.models.AccessLevel;
import org.gitlab4j.api.models.Project;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import java.io.PrintStream;
import java.util.Set;

@ToString
public class GitlabProtectBranchStep extends GitlabBaseStep {

    public static final String STEP = "gitlabProtectBranch";

    private String namespace;
    private String project;
    private String branch;
    private String level;

    @DataBoundConstructor
    public GitlabProtectBranchStep(String branch, String level) {
        this.branch = branch;
        this.level = level;
    }

    public String getNamespace() {
        return namespace;
    }

    @DataBoundSetter
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getProject() {
        return project;
    }

    @DataBoundSetter
    public void setProject(String project) {
        this.project = project;
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
