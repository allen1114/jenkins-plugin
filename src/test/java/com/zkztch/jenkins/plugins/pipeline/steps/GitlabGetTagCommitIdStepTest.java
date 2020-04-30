package com.zkztch.jenkins.plugins.pipeline.steps;

import com.zkztch.jenkins.test.Gitlab;
import com.zkztch.jenkins.test.Jenkins;
import lombok.extern.slf4j.Slf4j;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Project;
import org.gitlab4j.api.models.Tag;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.BuildWatcher;
import org.jvnet.hudson.test.JenkinsRule;

@Slf4j
public class GitlabGetTagCommitIdStepTest {

    @ClassRule public static final BuildWatcher buildWatcher = Jenkins.buildWatcher;
    @ClassRule public static final JenkinsRule jenkinsRule = Jenkins.jenkinsRule;

    private Project project;
    private final String tag = "t1";

    @Before
    public void setup() throws GitLabApiException {
        log.info("setup");
        project = Gitlab.createTmpProject();
    }

    @After
    public void clean() throws GitLabApiException {
        log.info("clean");
        Gitlab.deleteProject(project);
    }

    @Test
    public void getWhenHas() throws Exception {
        Gitlab.api.getTagsApi().createTag(project, tag, Gitlab.DEFAULT_BRANCH);
        Tag t = Gitlab.api.getTagsApi().getTag(project, tag);
        String script = String.format(
                "def c = gitlabGetTagCommitId host: '%s', token: '%s', namespace: '%s', project: '%s', tag: '%s' \n" +
                        "if (c == \"%s\") \n" +
                        "{echo c }\n",
                Gitlab.host, Gitlab.token, project.getNamespace().getPath(), project.getPath(), tag, t.getCommit().getId());

        log.info("script = " + script);
        WorkflowJob job = jenkinsRule.createProject(WorkflowJob.class, project.getName());
        job.setDefinition(new CpsFlowDefinition(script, true));
        jenkinsRule.assertLogContains(t.getCommit().getId(), jenkinsRule.assertBuildStatusSuccess(job.scheduleBuild2(0)));
        jenkinsRule.waitUntilNoActivity();

    }
}
