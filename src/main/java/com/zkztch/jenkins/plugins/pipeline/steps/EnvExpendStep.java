package com.zkztch.jenkins.plugins.pipeline.steps;

import com.google.common.collect.ImmutableSet;
import hudson.EnvVars;
import hudson.Extension;
import hudson.model.TaskListener;
import lombok.Getter;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.util.Set;

@Getter
public class EnvExpendStep extends Step {
    public static final String STEP = "envExpand";

    private String content;

    @DataBoundConstructor
    public EnvExpendStep(String content) {
        this.content = content;
    }

    @Override
    public StepExecution start(StepContext context) throws Exception {
        return new Execution(context, this);
    }

    public static class Execution extends StepExecution {

        private static final long serialVersionUID = -7607022083139454749L;
        private transient EnvExpendStep step;

        public Execution(@Nonnull StepContext context, EnvExpendStep step) {
            super(context);
            this.step = step;
        }

        @Override
        public boolean start() throws Exception {
            EnvVars envVars = getContext().get(EnvVars.class);
            getContext().onSuccess(envVars.expand(step.getContent()));
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
