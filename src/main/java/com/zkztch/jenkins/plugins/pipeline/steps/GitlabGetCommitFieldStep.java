package com.zkztch.jenkins.plugins.pipeline.steps;

import hudson.Extension;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.models.Commit;
import org.gitlab4j.api.models.Project;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.PrintStream;
import java.lang.reflect.Field;

public class GitlabGetCommitFieldStep extends GitlabBaseStep {

    public static final String STEP = "gitlabGetCommitField";

    private String commit;
    private String field;

    @DataBoundConstructor
    public GitlabGetCommitFieldStep(String commit, String field) {
        this.commit = commit;
        this.field = field;
    }

    @Override
    public Object doStart(StepContext context, PrintStream logger, GitLabApi gitLabApi, Project project) throws Exception {
        try {
            Commit commit = gitLabApi.getCommitsApi().getCommit(project, this.commit);
            String[] fileds = this.field.split("\\.");
            Object ret = commit;
            for (String f : fileds) {
                Field field = FieldUtils.getDeclaredField(ret.getClass(), f, true);
                ret = field.get(ret);
            }
            return ret;
        } catch (Exception e) {
            e.printStackTrace(logger);
            return null;
        }
    }

    @Extension
    public static class Descriptor extends GitlabStepDescriptor {
        @Override
        public String getFunctionName() {
            return STEP;
        }
    }
}
