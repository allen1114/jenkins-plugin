package com.zkztch.jenkins.plugins.pipeline.steps;

import com.zkztch.jenkins.test.Gitlab;
import com.zkztch.jenkins.test.Jenkins;
import hudson.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Project;
import org.gitlab4j.api.models.RepositoryFile;
import org.gitlab4j.api.models.Tag;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.*;
import org.jvnet.hudson.test.BuildWatcher;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.List;

@Slf4j
public class GitlabCreateTagStepTest {

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
    public void createWhenTagNotExist() throws Exception {
        String script = String.format(
                "gitlabCreateTag host: '%s', token: '%s', namespace: '%s', project: '%s', tag: '%s', ref: '%s',force: '%b' \n",
                Gitlab.host, Gitlab.token, project.getNamespace().getPath(), project.getPath(), tag, Gitlab.DEFAULT_BRANCH, false);

        log.info("script = " + script);
        WorkflowJob job = jenkinsRule.createProject(WorkflowJob.class, project.getName());
        job.setDefinition(new CpsFlowDefinition(script, true));
        jenkinsRule.assertBuildStatusSuccess(job.scheduleBuild2(0));
        jenkinsRule.waitUntilNoActivity();

        List<Tag> tags = Gitlab.api.getTagsApi().getTags(project);
        Assert.assertEquals(1, tags.size());
        boolean tagFound = false;
        for (Tag t : tags) {
            if (StringUtils.equals(t.getName(), tag)) {
                tagFound = true;
                break;
            }
        }
        Assert.assertTrue(tagFound);
    }


    @Test
    public void failWhenTagExistAndNotForce() throws Exception {

        Gitlab.api.getTagsApi().createTag(project, tag, Gitlab.DEFAULT_BRANCH);

        RepositoryFile file = new RepositoryFile();
        file.setFilePath("test.txt");
        file.encodeAndSetContent("test");
        Gitlab.api.getRepositoryFileApi().createFile(project, file, Gitlab.DEFAULT_BRANCH, "tt");

        String script = String.format(
                "gitlabCreateTag host: '%s', token: '%s', namespace: '%s', project: '%s', tag: '%s', ref: '%s',force: '%b' \n",
                Gitlab.host, Gitlab.token, project.getNamespace().getPath(), project.getPath(), tag, Gitlab.DEFAULT_BRANCH, false);

        log.info("script = " + script);
        WorkflowJob job = jenkinsRule.createProject(WorkflowJob.class, project.getName());
        job.setDefinition(new CpsFlowDefinition(script, true));
        jenkinsRule.assertBuildStatus(Result.FAILURE, job.scheduleBuild2(0));
        jenkinsRule.waitUntilNoActivity();

        List<Tag> tags = Gitlab.api.getTagsApi().getTags(project);
        Assert.assertEquals(1, tags.size());
        boolean tagFound = false;
        for (Tag t : tags) {
            if (StringUtils.equals(t.getName(), tag)) {
                tagFound = true;
                break;
            }
        }
        Assert.assertTrue(tagFound);
        try {
            file = Gitlab.api.getRepositoryFileApi().getFile(project, file.getFilePath(), tag);
        } catch (Exception e) {
            file = null;
        }
        Assert.assertNull(file);

    }

    @Test
    public void coveredWhenTagExistAndForce() throws Exception {

        Gitlab.api.getTagsApi().createTag(project, tag, Gitlab.DEFAULT_BRANCH);

        RepositoryFile file = new RepositoryFile();
        file.setFilePath("test.txt");
        file.encodeAndSetContent("test");
        Gitlab.api.getRepositoryFileApi().createFile(project, file, Gitlab.DEFAULT_BRANCH, "tt");

        String script = String.format(
                "gitlabCreateTag host: '%s', token: '%s', namespace: '%s', project: '%s', tag: '%s', ref: '%s',force: '%b' \n",
                Gitlab.host, Gitlab.token, project.getNamespace().getPath(), project.getPath(), tag, Gitlab.DEFAULT_BRANCH, true);

        log.info("script = " + script);
        WorkflowJob job = jenkinsRule.createProject(WorkflowJob.class, project.getName());
        job.setDefinition(new CpsFlowDefinition(script, true));
        jenkinsRule.assertBuildStatusSuccess(job.scheduleBuild2(0));
        jenkinsRule.waitUntilNoActivity();

        List<Tag> tags = Gitlab.api.getTagsApi().getTags(project);
        Assert.assertEquals(1, tags.size());
        boolean tagFound = false;
        for (Tag t : tags) {
            if (StringUtils.equals(t.getName(), tag)) {
                tagFound = true;
                break;
            }
        }
        Assert.assertTrue(tagFound);
        try {
            file = Gitlab.api.getRepositoryFileApi().getFile(project, file.getFilePath(), tag);
        } catch (Exception e) {
            file = null;
        }
        Assert.assertNotNull(file);

    }
}
