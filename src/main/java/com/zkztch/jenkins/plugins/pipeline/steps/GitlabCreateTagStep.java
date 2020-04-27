package com.zkztch.jenkins.plugins.pipeline.steps;

import com.google.common.collect.ImmutableSet;
import hudson.Extension;
import hudson.model.TaskListener;
import org.apache.commons.lang3.StringUtils;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.models.Project;
import org.gitlab4j.api.models.Tag;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import java.io.PrintStream;
import java.util.List;
import java.util.Set;

public class GitlabCreateTagStep extends GitlabBaseStep {
    public static final String STEP = "gitlabCreateTag";

    private String namespace;
    private String project;
    private String tag;
    private String ref;
    private Boolean force;

    @DataBoundConstructor
    public GitlabCreateTagStep(String host, String token, String namespace, String project, String tag, String ref, Boolean force) {
        super(host, token);
        this.namespace = namespace;
        this.project = project;
        this.tag = tag;
        this.ref = ref;
        this.force = force;
    }

    public String getNamespace() {
        return namespace;
    }

    @DataBoundSetter
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getProject() {
        return project;
    }

    @DataBoundSetter
    public void setProject(String project) {
        this.project = project;
    }

    public String getTag() {
        return tag;
    }

    @DataBoundSetter
    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getRef() {
        return ref;
    }

    @DataBoundSetter
    public void setRef(String ref) {
        this.ref = ref;
    }

    public Boolean getForce() {
        return force;
    }

    @DataBoundSetter
    public void setForce(Boolean force) {
        this.force = force;
    }

    @Override
    public void doStart(StepContext context, PrintStream logger, GitLabApi gitLabApi) throws Exception {
        Project pro = gitLabApi.getProjectApi().getProject(namespace, project);
        List<Tag> tags = gitLabApi.getTagsApi().getTags(pro);
        for (Tag t : tags) {
            if (StringUtils.equals(t.getName(), tag)) {
                if (force) {
                    gitLabApi.getTagsApi().deleteTag(pro, tag);
                } else {
                    throw new IllegalStateException("tag exist");
                }
                break;
            }
        }
        gitLabApi.getTagsApi().createTag(pro, tag, ref);
    }

    @Extension
    public static class DescriptorImpl extends StepDescriptor {

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
