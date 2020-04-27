package com.zkztch.jenkins.plugins.pipeline.steps;

import com.google.common.collect.ImmutableSet;
import hudson.Extension;
import hudson.model.TaskListener;
import lombok.ToString;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.models.AccessLevel;
import org.gitlab4j.api.models.Project;
import org.gitlab4j.api.models.ProtectedBranch;
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
    public GitlabProtectBranchStep(String host, String token, String namespace, String project, String branch, String level) {
        super(host, token);
        this.namespace = namespace;
        this.project = project;
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

    @DataBoundSetter
    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getLevel() {
        return level;
    }

    @DataBoundSetter
    public void setLevel(String level) {
        this.level = level;
    }

    @Override
    public void doStart(StepContext context, PrintStream logger, GitLabApi gitLabApi) throws Exception {

        AccessLevel accessLevel = AccessLevel.valueOf(level);
        Project pro = gitLabApi.getProjectApi().getProject(namespace, project);
        for (ProtectedBranch protectedBranch : gitLabApi.getProtectedBranchesApi().getProtectedBranches(pro)) {
            if (protectedBranch.getName().equals(branch)) {
                gitLabApi.getProtectedBranchesApi().unprotectBranch(pro, branch);
                break;
            }
        }
        gitLabApi.getProtectedBranchesApi().protectBranch(pro, branch, accessLevel, accessLevel);
    }

    @Extension
    public static class DescriptorImpl extends StepDescriptor {

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return ImmutableSet.of(TaskListener.class);
        }

        @Override
        public String getFunctionName() {
            return STEP;
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return getFunctionName();
        }
    }
}
