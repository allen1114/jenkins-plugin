package com.zkztch.jenkins.plugins.pipeline.steps;

import org.gitlab4j.api.GitLabApi;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.PrintStream;

public abstract class GitlabBaseStep extends Step {

    private String host;
    private String token;

    public GitlabBaseStep(String host, String token) {
        this.host = host;
        this.token = token;
    }

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

    @Override
    public final StepExecution start(StepContext context) throws Exception {
        return new GitlabStepExecution<>(context, this);
    }

    public abstract void doStart(StepContext context, PrintStream logger, GitLabApi gitLabApi) throws Exception;
}
