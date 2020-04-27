package com.zkztch.jenkins.plugins.pipeline.steps;

import com.zkztch.jenkins.plugins.Gitlab;
import com.zkztch.jenkins.plugins.JenkinsUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Branch;
import org.gitlab4j.api.models.Project;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.*;
import org.jvnet.hudson.test.BuildWatcher;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.List;

@Slf4j
public class GitlabDeleteBranchStepTest {
    @ClassRule public static final BuildWatcher buildWatcher  = JenkinsUtils.buildWatcher;
    @ClassRule public static final JenkinsRule jenkinsRule = JenkinsUtils.jenkinsRule;
    private Project project;
    private final String branch = "dev";

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
    public void deletedWhenBranchExist() throws Exception {
        Gitlab.api.getRepositoryApi().createBranch(project, branch, Gitlab.DEFAULT_BRANCH);

        String script = String.format(
                "gitlabDeleteBranch host: '%s', token: '%s', namespace: '%s', project: '%s', branch: '%s' \n",
                Gitlab.host, Gitlab.token, project.getNamespace().getPath(), project.getPath(), branch);
        log.info("script = " + script);
        WorkflowJob job = jenkinsRule.createProject(WorkflowJob.class, project.getName());
        job.setDefinition(new CpsFlowDefinition(script, true));
        jenkinsRule.assertBuildStatusSuccess(job.scheduleBuild2(0));
        jenkinsRule.waitUntilNoActivity();

        List<Branch> branches = Gitlab.api.getRepositoryApi().getBranches(project);
        Assert.assertEquals(1, branches.size());

        boolean branchFound = false;
        for (Branch b : branches) {
            if (StringUtils.equals(b.getName(), branch)) {
                branchFound = true;
                break;
            }
        }
        Assert.assertFalse(branchFound);
    }


}
