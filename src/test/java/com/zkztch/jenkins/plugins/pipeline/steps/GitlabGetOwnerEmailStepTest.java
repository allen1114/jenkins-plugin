package com.zkztch.jenkins.plugins.pipeline.steps;

import com.zkztch.test.Gitlab;
import com.zkztch.test.Jenkins;
import lombok.extern.slf4j.Slf4j;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Project;
import org.gitlab4j.api.models.User;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.BuildWatcher;
import org.jvnet.hudson.test.JenkinsRule;

@Slf4j
public class GitlabGetOwnerEmailStepTest {


    @ClassRule public static final BuildWatcher buildWatcher = Jenkins.buildWatcher;
    @ClassRule public static final JenkinsRule jenkinsRule = Jenkins.jenkinsRule;

    private Project project;

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
    public void test() throws Exception {
        User user = Gitlab.api.getUserApi().getUser(project.getOwner().getName());

        String script = String.format(
                "def e = gitlabGetOwnerEmail host: '%s', token: '%s', namespace: '%s', project: '%s'\n" +
                        "echo e",
                Gitlab.host, Gitlab.token, project.getNamespace().getPath(), project.getPath());
        log.info("script = " + script);
        WorkflowJob job = jenkinsRule.createProject(WorkflowJob.class, project.getName());
        job.setDefinition(new CpsFlowDefinition(script, true));
        jenkinsRule.assertLogContains(user.getEmail(), jenkinsRule.assertBuildStatusSuccess(job.scheduleBuild2(0)));
        jenkinsRule.waitUntilNoActivity();
    }
}
