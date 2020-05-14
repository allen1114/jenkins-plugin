package com.zkztch.jenkins.plugins.pipeline.steps;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerCertificates;
import com.spotify.docker.client.DockerCertificatesStore;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.auth.FixedRegistryAuthSupplier;
import com.spotify.docker.client.auth.MultiRegistryAuthSupplier;
import com.spotify.docker.client.auth.RegistryAuthSupplier;
import com.spotify.docker.client.messages.RegistryAuth;
import com.spotify.docker.client.messages.RegistryConfigs;
import com.spotify.docker.client.shaded.com.google.common.base.Optional;
import com.zkztch.jenkins.plugins.pipeline.DockerConsts;
import hudson.EnvVars;
import hudson.FilePath;
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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter
public abstract class AbstractDockerStep extends Step {
    private String dockerHost;
    private String dockerCertPath;
    private String registryUrl;
    private String registryUsername;
    private String registryPassword;

    @DataBoundSetter
    public void setDockerHost(String dockerHost) {
        this.dockerHost = dockerHost;
    }

    @DataBoundSetter
    public void setDockerCertPath(String dockerCertPath) {
        this.dockerCertPath = dockerCertPath;
    }

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
        dockerHost = StringUtils.isNotBlank(dockerHost) ? dockerHost : env.expand(env.get(DockerConsts.DOCKER_HOST));
        dockerCertPath = StringUtils.isNotBlank(dockerCertPath) ? dockerCertPath : env.expand(env.get(DockerConsts.DOCKER_CERT_PATH));
        registryUrl = StringUtils.isNotBlank(registryUrl) ? registryUrl : env.expand(env.get(DockerConsts.DOCKER_REPO_HOST));
        registryUsername =
                StringUtils.isNotBlank(registryUsername) ? registryUsername : env.expand(env.get(DockerConsts.DOCKER_REPO_USERNAME));
        registryPassword =
                StringUtils.isNotBlank(registryPassword) ? registryPassword : env.expand(env.get(DockerConsts.DOCKER_REPO_PASSWORD));
    }

    @Override
    public final StepExecution start(StepContext context) throws Exception {
        this.loadEnv(context.get(EnvVars.class));
        return new AbstractDockerStep.Execution<>(context, this);
    }

    public static class Execution<T extends AbstractDockerStep> extends StepExecution {
        private T step;

        public Execution(@Nonnull StepContext context, T step) {
            super(context);
            this.step = step;
        }

        @Override
        public boolean start() throws Exception {
            DockerClient dockerClient = buildDockerClient();
            try {
                getContext().onSuccess(step.doStart(getContext(), getContext().get(TaskListener.class).getLogger(), dockerClient));
            } catch (Exception e) {
                getContext().onFailure(e);
            }
            return true;
        }

        private DockerClient buildDockerClient() throws Exception {
            TaskListener listener = getContext().get(TaskListener.class);

            listener.getLogger().println("dockerHost:" + step.getDockerHost());
            listener.getLogger().println("dockerCertPath:" + step.getDockerCertPath());
            DefaultDockerClient.Builder builder = DefaultDockerClient.fromEnv().readTimeoutMillis(0);

            if (StringUtils.isNoneBlank(step.getDockerHost())) {
                builder.uri(step.getDockerHost());
            }

            if (StringUtils.isNotBlank(step.getDockerCertPath())) {
                FilePath workspace = getContext().get(FilePath.class);
                FilePath certDir = workspace.child(step.getDockerCertPath());
                if (certDir.exists()) {
                    Optional<DockerCertificatesStore> certs =
                            DockerCertificates.builder().dockerCertPath(Paths.get(certDir.toURI())).build();
                    if (certs.isPresent()) {
                        builder.dockerCertificates(certs.get());
                    }
                }
            }
            builder.registryAuthSupplier(registryAuth());
            return builder.build();
        }

        private RegistryAuthSupplier registryAuth() {

            List<RegistryAuthSupplier> suppliers = new ArrayList<>();
            if (StringUtils.isNotBlank(step.getRegistryUrl())) {

                RegistryAuth.Builder registryAuthBuilder = RegistryAuth.builder();
                registryAuthBuilder.serverAddress(step.getRegistryUrl());
                registryAuthBuilder.username(step.getRegistryUsername());
                registryAuthBuilder.password(step.getRegistryPassword());
                RegistryAuth registryAuth = registryAuthBuilder.build();

                RegistryConfigs registryConfigs = RegistryConfigs.create(ImmutableMap.of(registryAuth.serverAddress(), registryAuth));
                suppliers.add(new FixedRegistryAuthSupplier(registryAuth, registryConfigs));
            }
            return new MultiRegistryAuthSupplier(suppliers);

        }
    }

    protected abstract Object doStart(StepContext context, PrintStream logger, DockerClient dockerClient) throws Exception;

    public static abstract class DockerStepDescriptor extends StepDescriptor {
        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return ImmutableSet.of(TaskListener.class, FilePath.class);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return getFunctionName();
        }
    }
}
