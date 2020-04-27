package com.zkztch.jenkins.plugins.pipeline.steps;

import hudson.model.TaskListener;
import org.apache.commons.lang3.StringUtils;
import org.gitlab4j.api.GitLabApi;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepExecution;

import javax.annotation.Nonnull;

public class GitlabStepExecution<T extends GitlabBaseStep> extends StepExecution {

    private T step;

    public GitlabStepExecution(@Nonnull StepContext context, T step) {
        super(context);
        this.step = step;
    }

    @Override
    public boolean start() throws Exception {
        do {
            TaskListener listener = getContext().get(TaskListener.class);

            if (listener == null || StringUtils.isBlank(step.getHost()) || StringUtils.isBlank(step.getToken())) {
                getContext().onFailure(new IllegalStateException("inappropriate context"));
                break;
            }

            GitLabApi gitLabApi = new GitLabApi(step.getHost(), step.getToken());
            gitLabApi.setIgnoreCertificateErrors(true);

            try {
                step.doStart(getContext(), listener.getLogger(), gitLabApi);
                getContext().onSuccess(null);
            } catch (Exception e) {
                getContext().onFailure(e);
            }
        } while (false);
        return true;
    }
}
