package com.zkztch.jenkins.plugins.pipeline.steps;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import com.zkztch.jenkins.plugins.pipeline.ZKZTCHConsts;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.TaskListener;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Set;

@Getter
public class ZkztchLoadEnvStep extends Step {
    public static final String STEP = "zkztchLoadEnv";

    private String addr;
    private String path;

    @DataBoundConstructor
    public ZkztchLoadEnvStep(String addr, String path) {
        this.addr = addr;
        this.path = path;
    }

    public void loadEnv(EnvVars env) {
        addr = StringUtils.isNotBlank(addr) ? addr : env.expand(env.get(ZKZTCHConsts.ZKZTCH_ENV_ADDR));
        path = StringUtils.isNotBlank(path) ? path : env.expand(env.get(ZKZTCHConsts.ZKZTCH_ENV_PATH));
    }

    @Override
    public StepExecution start(StepContext context) throws Exception {
        this.loadEnv(context.get(EnvVars.class));
        return new Execution(context, this);
    }

    public static class Execution extends StepExecution {
        private transient ZkztchLoadEnvStep step;

        public Execution(@Nonnull StepContext context, ZkztchLoadEnvStep step) {
            super(context);
            this.step = step;
        }

        @Override
        public boolean start() throws Exception {
            StringBuilder uri = new StringBuilder("");
            if (!StringUtils.startsWith(step.getAddr(), "http://") && !StringUtils.startsWith(step.getAddr(), "https://")) {
                uri.append("http://");
            }
            if (!StringUtils.endsWith(step.getAddr(), "/")) {
                uri.append("/");
            }
            uri.append(step.getAddr()).append("env");
            if (!StringUtils.startsWith(step.getPath(), "/")) {
                uri.append("/");
            }
            uri.append(step.getPath());
            HttpGet httpGet = new HttpGet(uri.toString());
            try (CloseableHttpClient httpClient = HttpClientBuilder.create().build();
                 CloseableHttpResponse response = httpClient.execute(httpGet)) {
                if (response.getStatusLine().getStatusCode() != 200) {
                    getContext().onFailure(new IllegalStateException("load env fail"));
                } else {
                    String json = EntityUtils.toString(response.getEntity());
                    Map<String, String> env = new ObjectMapper().readValue(json, new TypeReference<Map<String, String>>() {
                    });
                    getContext().onSuccess(env);
                }
            }
            return true;
        }
    }

    @Extension
    public static class Descriptor extends StepDescriptor {
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
