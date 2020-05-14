package com.zkztch.jenkins.plugins.pipeline.when

import com.zkztch.jenkins.plugins.pipeline.GitlabConsts
import hudson.plugins.git.GitSCM
import org.glassfish.jersey.inject.hk2.Hk2InjectionManagerFactory
import org.glassfish.jersey.internal.inject.InjectionManagerFactory
import org.jenkinsci.plugins.pipeline.modeldefinition.when.DeclarativeStageConditionalScript
import org.jenkinsci.plugins.workflow.cps.CpsScript

class GitlabCommitWithTagConditionalScript extends DeclarativeStageConditionalScript<GitlabCommitWithTagConditional> {

    GitlabCommitWithTagConditionalScript(CpsScript s, GitlabCommitWithTagConditional c) {
        super(s, c)
    }

    @Override
    boolean evaluate() {
        Object env = script.getProperty("env");
        return describable.match(env.getProperty(GitlabConsts.GITLAB_URL), env.getProperty(GitlabConsts.GITLAB_TOKEN), env.getProperty(GitlabConsts.GITLAB_NAMESPACE), env.getProperty(GitlabConsts.GITLAB_PROJECT), env.getProperty(GitSCM.GIT_COMMIT));
    }
}
