package com.zkztch.jenkins.plugins.pipeline.steps;

import com.google.common.collect.ImmutableSet;
import com.zkztch.docker.DefaultDockerRegistryClient;
import com.zkztch.docker.DockerRegistryClient;
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
public abstract class AbstractDockerRegistryStep extends Step {

    private String registryUrl;
    private String registryUsername;
    private String registryPassword;

    @DataBoundSetter
    public void setRegistryUrl(String registryUrl) {
        this.registryUrl = registryUrl;
    }

    @DataBoundSetter
    public void setRegistryUsername(String registryUsername) {
        this.registryUsername = registryUsername;
    }

    @DataBoundSetter
    public void setRegistryPassword(String registryPassword) {
        this.registryPassword = registryPassword;
    }

    public void loadEnv(EnvVars env) {
        registryUrl = StringUtils.isNotBlank(registryUrl) ? registryUrl : env.expand(env.get(DockerConsts.DOCKER_REPO_HOST));
        registryUsername =
                StringUtils.isNotBlank(registryUsername) ? registryUsername : env.expand(env.get(DockerConsts.DOCKER_REPO_USERNAME));
        registryPassword =
                StringUtils.isNotBlank(registryPassword) ? registryPassword : env.expand(env.get(DockerConsts.DOCKER_REPO_PASSWORD));
    }

    protected abstract Object doStart(StepContext context, PrintStream logger, DockerRegistryClient dockerRegistryClient) throws Exception;

    @Override
    public StepExecution start(StepContext context) throws Exception {
        this.loadEnv(context.get(EnvVars.class));
        return new AbstractDockerRegistryStep.Execution<>(context, this);
    }

    public static class Execution<T extends AbstractDockerRegistryStep> extends StepExecution {
        private T step;

        public Execution(@Nonnull StepContext context, T step) {
            super(context);
            this.step = step;
        }

        @Override
        public boolean start() throws Exception {
            DockerRegistryClient dockerRegistryClient =
                    DefaultDockerRegistryClient.builder().uri(step.getRegistryUrl()).username(step.getRegistryUsername())
                            .password(step.getRegistryPassword()).build();
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
