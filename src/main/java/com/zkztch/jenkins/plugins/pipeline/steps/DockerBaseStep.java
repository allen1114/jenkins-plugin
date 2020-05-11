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
public abstract class DockerBaseStep extends Step {
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

    @Override
    public final StepExecution start(StepContext context) throws Exception {
        return new DockerBaseStep.Execution<>(context, this);
    }

    public static class Execution<T extends DockerBaseStep> extends StepExecution {
        private T step;

        public Execution(@Nonnull StepContext context, T step) {
            super(context);
            this.step = step;
        }

        @Override
        public boolean start() throws Exception {
            DockerClient dockerClient = buildDockerClient();
            TaskListener listener = getContext().get(TaskListener.class);
            try {
                getContext().onSuccess(step.doStart(getContext(), listener.getLogger(), dockerClient));
            } catch (Exception e) {
                getContext().onFailure(e);
            }
            return true;
        }

        private DockerClient buildDockerClient() throws Exception {

            EnvVars env = getContext().get(EnvVars.class);
            String dockerHost = step.getDockerHost() != null ? step.getDockerHost() : env.expand(env.get(DockerConsts.DOCKER_HOST));
            String dockerCertPath =
                    step.getDockerCertPath() != null ? step.getDockerCertPath() : env.expand(env.get(DockerConsts.DOCKER_CERT_PATH));

            DefaultDockerClient.Builder builder = DefaultDockerClient.fromEnv().readTimeoutMillis(0);
            if (StringUtils.isNoneBlank(dockerHost)) {
                builder.uri(dockerHost);
            }

            Optional<DockerCertificatesStore> certs = DockerCertificates.builder().dockerCertPath(Paths.get(dockerCertPath)).build();
            if (certs.isPresent()) {
                builder.dockerCertificates(certs.get());
            }

            registryAuth(env, builder);

            return builder.build();
        }

        private void registryAuth(EnvVars env, DefaultDockerClient.Builder builder) {

            List<RegistryAuthSupplier> suppliers = new ArrayList<>();
            String registryUrl =
                    step.getRegistryUrl() != null ? step.getRegistryUrl() : env.expand(env.get(DockerConsts.DOCKER_REGISTRY_URL));

            if (StringUtils.isNotBlank(registryUrl)) {
                RegistryAuth.Builder registryAuthBuilder = RegistryAuth.builder();
                registryAuthBuilder.serverAddress(registryUrl);

                String registryUsername = step.getRegistryUsername() != null ? step.getRegistryUsername() :
                        env.expand(env.get(DockerConsts.DOCKER_REGISTRY_USERNAME));
                String registryPassword = step.getRegistryPassword() != null ? step.getRegistryPassword() :
                        env.expand(env.get(DockerConsts.DOCKER_REGISTRY_PASSWORD));

                if (StringUtils.isNotBlank(registryUsername)) {
                    registryAuthBuilder.username(registryUsername);
                }
                if (StringUtils.isNotBlank(registryPassword)) {
                    registryAuthBuilder.password(registryPassword);
                }

                RegistryAuth registryAuth = registryAuthBuilder.build();

                RegistryConfigs configsForBuild = RegistryConfigs.create(ImmutableMap.of(
                        registryAuth.serverAddress(), registryAuth
                ));
                suppliers.add(new FixedRegistryAuthSupplier(registryAuth, configsForBuild));
            }
            builder.registryAuthSupplier(new MultiRegistryAuthSupplier(suppliers));
        }
    }

    protected abstract Object doStart(StepContext context, PrintStream logger, DockerClient dockerClient) throws Exception;

    public static abstract class DockerStepDescriptor extends StepDescriptor {
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
