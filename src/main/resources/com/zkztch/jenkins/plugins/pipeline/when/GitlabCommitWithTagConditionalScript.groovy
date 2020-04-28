package com.zkztch.jenkins.plugins.pipeline.when

import org.jenkinsci.plugins.pipeline.modeldefinition.when.DeclarativeStageConditionalScript
import org.jenkinsci.plugins.workflow.cps.CpsScript

class GitlabCommitWithTagConditionalScript extends DeclarativeStageConditionalScript<GitlabCommitWithTagConditional> {

    GitlabCommitWithTagConditionalScript(CpsScript s, GitlabCommitWithTagConditional c) {
        super(s, c)
    }

    @Override
    boolean evaluate() {
        Object env = script.getProperty("env");
        return describable.match(env.getProperty("GITLAB_HOST"), env.getProperty("GITLAB_TOKEN"), env.getProperty("GITLAB_NAMESPACE"), env.getProperty("GITLAB_PROJECT"), env.getProperty("GIT_COMMIT"))
    }
}
