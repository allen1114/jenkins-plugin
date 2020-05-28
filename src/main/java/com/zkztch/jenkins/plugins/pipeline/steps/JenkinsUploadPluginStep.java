package com.zkztch.jenkins.plugins.pipeline.steps;

import com.google.common.collect.ImmutableSet;
import com.spotify.docker.client.shaded.org.glassfish.jersey.internal.util.Base64;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.TaskListener;
import lombok.Getter;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Set;

@Getter
public class JenkinsUploadPluginStep extends Step {
    public static final String STEP = "jenkinsUploadPlugin";

    private static final String API_UPLOADPLUGIN_PATH = "/pluginManager/uploadPlugin";
    private static final String API_SAFERESTART_PATH = "/updateCenter/safeRestart";
    private String url;
    private String file;
    private String user;
    private String token;
    private boolean restart;

    @DataBoundConstructor
    public JenkinsUploadPluginStep(String url, String file, String user, String token, boolean restart) {
        this.url = url;
        this.file = file;
        this.user = user;
        this.token = token;
        this.restart = restart;
    }

    private String getAuth() {
        return "Basic " + Base64.encodeAsString((user + ":" + token).getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public StepExecution start(StepContext context) throws Exception {
        return new Execution(context, this);
    }

    public static class Execution extends StepExecution {

        private static final long serialVersionUID = 9020343824971122150L;
        private transient JenkinsUploadPluginStep step;

        public Execution(@Nonnull StepContext context, JenkinsUploadPluginStep step) {
            super(context);
            this.step = step;
        }

        @Override
        public boolean start() throws Exception {
            try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
                HttpPost upload = new HttpPost(step.getUrl() + API_UPLOADPLUGIN_PATH);
                upload.addHeader("Authorization", step.getAuth());
                FilePath workspace = getContext().get(FilePath.class);
                FileBody plugin = new FileBody(new File(workspace.child(step.getFile()).toURI()));
                HttpEntity reqEntity = MultipartEntityBuilder.create().addPart("name", plugin).build();
                upload.setEntity(reqEntity);
                try (final CloseableHttpResponse response = httpclient.execute(upload)) {
                    if (response.getStatusLine().getStatusCode() != 302) {
                        throw new IllegalStateException(EntityUtils.toString(response.getEntity()));
                    }
                }
                if (step.isRestart()) {
                    HttpPost restart = new HttpPost(step.getUrl() + API_SAFERESTART_PATH);
                    restart.addHeader("Authorization", step.getAuth());
                    try (final CloseableHttpResponse response = httpclient.execute(restart)) {
                        if (response.getStatusLine().getStatusCode() != 302) {
                            throw new IllegalStateException(EntityUtils.toString(response.getEntity()));
                        }
                    }
                }
            }
            getContext().onSuccess(null);
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
