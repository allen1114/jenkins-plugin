package com.zkztch.jenkins.plugins.pipeline.steps;

import com.zkztch.jenkins.plugins.Gitlab;
import com.zkztch.jenkins.plugins.JenkinsUtils;
import hudson.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Branch;
import org.gitlab4j.api.models.Project;
import org.gitlab4j.api.models.RepositoryFile;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.*;
import org.jvnet.hudson.test.BuildWatcher;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.List;

@Slf4j
public class GitlabCreateBranchStepTest {

    @ClassRule public static final BuildWatcher buildWatcher = JenkinsUtils.buildWatcher;
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
    public void createdWhenBranchNotExist() throws Exception {

        String script = String.format(
                "gitlabCreateBranch host: '%s', token: '%s', namespace: '%s', project: '%s', branch: '%s', ref: '%s',force: '%b' \n",
                Gitlab.host, Gitlab.token, project.getNamespace().getPath(), project.getPath(), branch, Gitlab.DEFAULT_BRANCH, false);

        log.info("script = " + script);
        WorkflowJob job = jenkinsRule.createProject(WorkflowJob.class, project.getName());
        job.setDefinition(new CpsFlowDefinition(script, true));
        jenkinsRule.assertBuildStatusSuccess(job.scheduleBuild2(0));
        jenkinsRule.waitUntilNoActivity();

        List<Branch> branches = Gitlab.api.getRepositoryApi().getBranches(project);
        Assert.assertEquals(2, branches.size());

        boolean branchFound = false;
        for (Branch b : branches) {
            if (StringUtils.equals(b.getName(), branch)) {
                branchFound = true;
                break;
            }
        }
        Assert.assertTrue(branchFound);
    }

    @Test
    public void failWhenBranchExistAndNotForce() throws Exception {
        Gitlab.api.getRepositoryApi().createBranch(project, branch, Gitlab.DEFAULT_BRANCH);

        RepositoryFile file = new RepositoryFile();
        file.setFilePath("test.txt");
        file.encodeAndSetContent("test");
        Gitlab.api.getRepositoryFileApi().createFile(project, file, branch, "tt");

        String script = String.format(
                "gitlabCreateBranch host: '%s', token: '%s', namespace: '%s', project: '%s', branch: '%s', ref: '%s',force: %b \n",
                Gitlab.host, Gitlab.token, project.getNamespace().getPath(), project.getPath(), branch, Gitlab.DEFAULT_BRANCH, false);
        log.info("script = " + script);
        WorkflowJob job = jenkinsRule.createProject(WorkflowJob.class, project.getName());
        job.setDefinition(new CpsFlowDefinition(script, true));
        jenkinsRule.assertBuildStatus(Result.FAILURE, job.scheduleBuild2(0));
        jenkinsRule.waitUntilNoActivity();

        List<Branch> branches = Gitlab.api.getRepositoryApi().getBranches(project);
        Assert.assertEquals(2, branches.size());

        boolean branchFound = false;
        for (Branch b : branches) {
            if (StringUtils.equals(b.getName(), branch)) {
                branchFound = true;
                break;
            }
        }
        Assert.assertTrue(branchFound);
        try {
            file = Gitlab.api.getRepositoryFileApi().getFile(project, file.getFilePath(), branch);
        } catch (Exception e) {
            file = null;
        }
        Assert.assertNotNull(file);
        Assert.assertEquals(file.getDecodedContentAsString(), "test");
    }

    @Test
    public void coveredWhenBranchExistAndForce() throws Exception {
        Gitlab.api.getRepositoryApi().createBranch(project, branch, Gitlab.DEFAULT_BRANCH);

        RepositoryFile file = new RepositoryFile();
        file.setFilePath("test.txt");
        file.encodeAndSetContent("t1");
        Gitlab.api.getRepositoryFileApi().createFile(project, file, branch, "tt");

        RepositoryFile mfile = new RepositoryFile();
        mfile.setFilePath("test.txt");
        mfile.encodeAndSetContent("t2");
        Gitlab.api.getRepositoryFileApi().createFile(project, mfile, Gitlab.DEFAULT_BRANCH, "tt");

        String script = String.format(
                "gitlabCreateBranch host: '%s', token: '%s', namespace: '%s', project: '%s', branch: '%s', ref: '%s',force: %b \n",
                Gitlab.host, Gitlab.token, project.getNamespace().getPath(), project.getPath(), branch, Gitlab.DEFAULT_BRANCH, true);
        log.info("script = " + script);
        WorkflowJob job = jenkinsRule.createProject(WorkflowJob.class, project.getName());
        job.setDefinition(new CpsFlowDefinition(script, true));
        jenkinsRule.assertBuildStatusSuccess(job.scheduleBuild2(0));
        jenkinsRule.waitUntilNoActivity();

        List<Branch> branches = Gitlab.api.getRepositoryApi().getBranches(project);
        Assert.assertEquals(2, branches.size());

        boolean branchFound = false;
        for (Branch b : branches) {
            if (StringUtils.equals(b.getName(), branch)) {
                branchFound = true;
                break;
            }
        }
        Assert.assertTrue(branchFound);
        try {
            file = Gitlab.api.getRepositoryFileApi().getFile(project, file.getFilePath(), branch);
        } catch (Exception e) {
            file = null;
        }
        Assert.assertNotNull(file);
        Assert.assertEquals(file.getDecodedContentAsString(), "t2");
    }
}
