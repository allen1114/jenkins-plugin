package com.zkztch.jenkins.plugins.pipeline.steps;

import com.google.common.collect.ImmutableSet;
import hudson.Extension;
import hudson.model.TaskListener;
import org.apache.commons.lang3.StringUtils;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.models.Branch;
import org.gitlab4j.api.models.Project;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import java.io.PrintStream;
import java.util.List;
import java.util.Set;

public class GitlabCreateBranchStep extends GitlabBaseStep {

    public static final String STEP = "gitlabCreateBranch";

    private String namespace;
    private String project;
    private String branch;
    private String ref;
    private Boolean force;

    @DataBoundConstructor
    public GitlabCreateBranchStep(String host, String token, String namespace, String project, String branch, String ref,
                                  Boolean force) {
        super(host, token);
        this.namespace = namespace;
        this.project = project;
        this.branch = branch;
        this.ref = ref;
        this.force = force;
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

    public String getRef() {
        return ref;
    }

    @DataBoundSetter
    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getBranch() {
        return branch;
    }

    @DataBoundSetter
    public void setBranch(String branch) {
        this.branch = branch;
    }

    public Boolean getForce() {
        return force;
    }

    @DataBoundSetter
    public void setForce(Boolean force) {
        this.force = force;
    }

    @Override
    public void doStart(StepContext context, PrintStream logger, GitLabApi gitLabApi) throws Exception {
        Project pro = gitLabApi.getProjectApi().getProject(namespace, project);
        List<Branch> branchList = gitLabApi.getRepositoryApi().getBranches(pro);
        for (Branch b : branchList) {
            if (StringUtils.equals(b.getName(), branch)) {
                if (force) {
                    gitLabApi.getRepositoryApi().deleteBranch(pro, branch);
                    break;
                } else {
                    throw new IllegalStateException("branch exist");
                }
            }
        }

        gitLabApi.getRepositoryApi().createBranch(pro, branch, ref);
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
