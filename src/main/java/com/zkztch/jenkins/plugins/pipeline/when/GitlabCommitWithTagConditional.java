package com.zkztch.jenkins.plugins.pipeline.when;

import hudson.Extension;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.groovy.ast.expr.Expression;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Project;
import org.gitlab4j.api.models.Tag;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTWhenContent;
import org.jenkinsci.plugins.pipeline.modeldefinition.parser.ASTParserUtils;
import org.jenkinsci.plugins.pipeline.modeldefinition.when.DeclarativeStageConditional;
import org.jenkinsci.plugins.pipeline.modeldefinition.when.DeclarativeStageConditionalDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.List;

public class GitlabCommitWithTagConditional extends DeclarativeStageConditional<GitlabCommitWithTagConditional> {

    private String pattern;
    private String host;
    private String token;
    private String namespace;
    private String project;

    @DataBoundConstructor
    public GitlabCommitWithTagConditional(String pattern) {
        this.pattern = pattern;
    }

    @DataBoundSetter
    public void setHost(String host) {
        this.host = host;
    }

    @DataBoundSetter
    public void setToken(String token) {
        this.token = token;
    }

    @DataBoundSetter
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    @DataBoundSetter
    public void setProject(String project) {
        this.project = project;
    }


    public String getHost() {
        return host;
    }

    public String getToken() {
        return token;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getProject() {
        return project;
    }

    public String getPattern() {
        return pattern;
    }

    public boolean match(String ehost, String etoken, String enamespace, String eproject, String commit) throws GitLabApiException {

        String host = this.host == null ? ehost : this.host;
        String token = this.token == null ? etoken : this.token;
        String namespace = this.namespace == null ? enamespace : this.namespace;
        String project = this.project == null ? eproject : this.project;

        GitLabApi gitLabApi = new GitLabApi(host, token);
        gitLabApi.setIgnoreCertificateErrors(true);
        Project pro = gitLabApi.getProjectApi().getProject(namespace, project);
        List<Tag> tags = gitLabApi.getTagsApi().getTags(pro);
        for (Tag tag : tags) {
            if (StringUtils.equalsIgnoreCase(tag.getCommit().getId(), commit) && tag.getName().matches(pattern)) {
                return true;
            }
        }
        return false;

    }

    @Extension
    @Symbol("gitlabCommitWithTag")
    public static class DescriptorImpl extends DeclarativeStageConditionalDescriptor<GitlabCommitWithTagConditional> {

        @Nonnull
        @Override
        public String getDisplayName() {
            return "GIT_COMMIT with tag match the given patten";
        }

        @Override
        public Expression transformToRuntimeAST(@CheckForNull ModelASTWhenContent original) {
            return ASTParserUtils.transformWhenContentToRuntimeAST(original);
        }
    }
}
