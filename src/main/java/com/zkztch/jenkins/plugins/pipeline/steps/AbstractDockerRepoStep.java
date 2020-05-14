package com.zkztch.jenkins.plugins.pipeline.steps;

import com.google.common.collect.ImmutableSet;
import com.zkztch.docker.registry.DefaultDockerRegistryClient;
import com.zkztch.docker.registry.DockerRegistryClient;
import com.zkztch.jenkins.plugins.pipeline.DockerConsts;
import hudson.EnvVars;
import hudson.model.TaskListener;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import java.io.PrintStream;
import java.util.Set;

@Getter
public abstract class AbstractDockerRepoStep extends Step {

    private String url;
    private String username;
    private String password;

    @DataBoundSetter
    public void setUrl(String url) {
        this.url = url;
    }

    @DataBoundSetter
    public void setUsername(String username) {
        this.username = username;
    }

    @DataBoundSetter
    public void setPassword(String password) {
        this.password = password;
    }

    public void loadEnv(EnvVars env) {
        url = StringUtils.isNotBlank(url) ? url : env.expand(env.get(DockerConsts.DOCKER_REPO_URL));
        username = StringUtils.isNotBlank(username) ? username : env.expand(env.get(DockerConsts.DOCKER_REPO_USERNAME));
        password = StringUtils.isNotBlank(password) ? password : env.expand(env.get(DockerConsts.DOCKER_REPO_PASSWORD));
    }

    protected abstract Object doStart(StepContext context, PrintStream logger, DockerRegistryClient dockerRegistryClient) throws Exception;

    @Override
    public StepExecution start(StepContext context) throws Exception {
        this.loadEnv(context.get(EnvVars.class));
        return new AbstractDockerRepoStep.Execution<>(context, this);
    }

    public static class Execution<T extends AbstractDockerRepoStep> extends StepExecution {
        private T step;

        public Execution(@Nonnull StepContext context, T step) {
            super(context);
            this.step = step;
        }

        @Override
        public boolean start() throws Exception {
            DockerRegistryClient dockerRegistryClient =
                    DefaultDockerRegistryClient.builder().uri(step.getUrl()).username(step.getUsername()).password(step.getPassword())
                            .build();
            try {
                getContext().onSuccess(step.doStart(getContext(), getContext().get(TaskListener.class).getLogger(), dockerRegistryClient));
            } catch (Exception e) {
                getContext().onFailure(e);
            }
            return true;
        }
    }

    public static abstract class DockerRegistryStepDescriptor extends StepDescriptor {
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
