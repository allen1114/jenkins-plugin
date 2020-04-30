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

public abstract class GitlabBaseStep extends Step {

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

    @Override
    public final StepExecution start(StepContext context) throws Exception {
        return new Execution<>(context, this);
    }

    public abstract Object doStart(StepContext context, PrintStream logger, GitLabApi gitLabApi, Project project) throws Exception;

    public static class Execution<T extends GitlabBaseStep> extends StepExecution {

        private T step;

        public Execution(@Nonnull StepContext context, T step) {
            super(context);
            this.step = step;
        }

        @Override
        public boolean start() throws Exception {
            do {
                EnvVars env = getContext().get(EnvVars.class);
                String host = step.getHost() != null ? step.getHost() : env.get(GitlabConsts.GITLAB_HOST);
                String token = step.getToken() != null ? step.getToken() : env.get(GitlabConsts.GITLAB_TOKEN);
                String namespace = step.getNamespace() != null ? step.getNamespace() : env.get(GitlabConsts.GITLAB_NAMESPACE);
                String project = step.getProject() != null ? step.getProject() : env.get(GitlabConsts.GITLAB_PROJECT);

                TaskListener listener = getContext().get(TaskListener.class);

                if (listener == null || StringUtils.isBlank(host) || StringUtils.isBlank(token) || StringUtils.isBlank(namespace) ||
                        StringUtils.isBlank(project)) {
                    getContext().onFailure(new IllegalStateException("inappropriate context"));
                    break;
                }

                GitLabApi gitLabApi = new GitLabApi(host, token);
                gitLabApi.setIgnoreCertificateErrors(true);

                try {
                    getContext().onSuccess(step.doStart(getContext(), listener.getLogger(), gitLabApi,
                            gitLabApi.getProjectApi().getProject(namespace, project)));
                } catch (Exception e) {
                    getContext().onFailure(e);
                }
            } while (false);
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
