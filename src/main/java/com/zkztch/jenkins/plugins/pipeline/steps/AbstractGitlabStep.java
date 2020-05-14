package com.zkztch.jenkins.plugins.pipeline.steps;

import com.google.common.collect.ImmutableSet;
import com.zkztch.jenkins.plugins.pipeline.GitlabConsts;
import hudson.EnvVars;
import hudson.model.TaskListener;
import org.apache.commons.lang3.StringUtils;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.models.Project;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import java.io.PrintStream;
import java.util.Set;

public abstract class AbstractGitlabStep extends Step {

    private String host;
    private String token;
    private String namespace;
    private String project;

    public String getHost() {
        return host;
    }

    @DataBoundSetter
    public void setHost(String host) {
        this.host = host;
    }

    public String getToken() {
        return token;
    }

    @DataBoundSetter
    public void setToken(String token) {
        this.token = token;
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

    public void loadEnv(EnvVars env) {
        host = StringUtils.isNotBlank(host) ? host : env.expand(env.get(GitlabConsts.GITLAB_URL));
        token = StringUtils.isNotBlank(token) ? token : env.expand(env.get(GitlabConsts.GITLAB_TOKEN));
        namespace = StringUtils.isNotBlank(namespace) ? namespace : env.expand(env.get(GitlabConsts.GITLAB_NAMESPACE));
        project = StringUtils.isNotBlank(project) ? project : env.expand(env.get(GitlabConsts.GITLAB_PROJECT));
    }

    @Override
    public final StepExecution start(StepContext context) throws Exception {
        this.loadEnv(context.get(EnvVars.class));
        return new Execution<>(context, this);
    }

    public abstract Object doStart(StepContext context, PrintStream logger, GitLabApi gitLabApi, Project project) throws Exception;

    public static class Execution<T extends AbstractGitlabStep> extends StepExecution {

        private T step;

        public Execution(@Nonnull StepContext context, T step) {
            super(context);
            this.step = step;
        }

        @Override
        public boolean start() throws Exception {

            GitLabApi gitLabApi = new GitLabApi(step.getHost(), step.getToken());
            gitLabApi.setIgnoreCertificateErrors(true);

            try {
                getContext().onSuccess(step.doStart(getContext(), getContext().get(TaskListener.class).getLogger(), gitLabApi,
                        gitLabApi.getProjectApi().getProject(step.getNamespace(), step.getProject())));
            } catch (Exception e) {
                getContext().onFailure(e);
            }
            return true;
        }
    }

    public static abstract class GitlabStepDescriptor extends StepDescriptor {
        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return ImmutableSet.of(TaskListener.class);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return getFunctionName();
        }
    }
}
