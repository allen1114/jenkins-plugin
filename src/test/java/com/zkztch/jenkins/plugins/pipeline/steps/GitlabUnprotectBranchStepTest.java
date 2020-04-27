package com.zkztch.jenkins.plugins.pipeline.steps;

import com.zkztch.jenkins.test.Gitlab;
import com.zkztch.jenkins.test.Jenkins;
import lombok.extern.slf4j.Slf4j;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.AccessLevel;
import org.gitlab4j.api.models.Project;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.*;
import org.jvnet.hudson.test.BuildWatcher;
import org.jvnet.hudson.test.JenkinsRule;

@Slf4j
public class GitlabUnprotectBranchStepTest {

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
    public void unprotectedWhenProtected() throws Exception {

        if (Gitlab.api.getProtectedBranchesApi().getProtectedBranches(project).size() == 0) {
            try {
                Gitlab.api.getProtectedBranchesApi()
                        .protectBranch(project, Gitlab.DEFAULT_BRANCH, AccessLevel.MAINTAINER, AccessLevel.MAINTAINER);
            } catch (Exception ignored) {
            }
        }
        String script = String.format(
                "gitlabUnprotectBranch host: '%s', token: '%s', namespace: '%s', project: '%s', branch: '%s'\n",
                Gitlab.host, Gitlab.token, project.getNamespace().getPath(), project.getPath(), Gitlab.DEFAULT_BRANCH);
        log.info("script = " + script);
        WorkflowJob job = jenkinsRule.createProject(WorkflowJob.class, project.getName());
        job.setDefinition(new CpsFlowDefinition(script, true));
        jenkinsRule.assertBuildStatusSuccess(job.scheduleBuild2(0));
        jenkinsRule.waitUntilNoActivity();

        Assert.assertEquals(0, Gitlab.api.getProtectedBranchesApi().getProtectedBranches(project).size());
    }

}
