package com.zkztch.jenkins.plugins.pipeline.steps;

import com.zkztch.jenkins.test.Gitlab;
import com.zkztch.jenkins.test.Jenkins;
import lombok.extern.slf4j.Slf4j;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.AccessLevel;
import org.gitlab4j.api.models.BranchAccessLevel;
import org.gitlab4j.api.models.Project;
import org.gitlab4j.api.models.ProtectedBranch;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.*;
import org.jvnet.hudson.test.BuildWatcher;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.List;

@Slf4j
public class GitlabProtectBranchStepTest {

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
    public void pretectChangedWhenProtected() throws Exception {

        String accessLevel = "DEVELOPER";

        String script = String.format(
                "gitlabProtectBranch host: '%s', token: '%s', namespace: '%s', project: '%s', branch: '%s',level: '%s' \n",
                Gitlab.host, Gitlab.token, project.getNamespace().getPath(), project.getPath(), Gitlab.DEFAULT_BRANCH, accessLevel);
        log.info("script = " + script);
        WorkflowJob job = jenkinsRule.createProject(WorkflowJob.class, project.getName());
        job.setDefinition(new CpsFlowDefinition(script, true));
        jenkinsRule.assertBuildStatusSuccess(job.scheduleBuild2(0));
        jenkinsRule.waitUntilNoActivity();

        List<ProtectedBranch> protectedBranches = Gitlab.api.getProtectedBranchesApi().getProtectedBranches(project);

        Assert.assertEquals(1, protectedBranches.size());
        ProtectedBranch protectedBranch = protectedBranches.get(0);

        List<BranchAccessLevel> branchAccessLevels = protectedBranch.getPushAccessLevels();
        Assert.assertEquals(1, branchAccessLevels.size());

        BranchAccessLevel branchAccessLevel = branchAccessLevels.get(0);
        Assert.assertEquals(AccessLevel.valueOf(accessLevel), branchAccessLevel.getAccessLevel());

    }

}
