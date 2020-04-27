package com.zkztch.jenkins.plugins.pipeline.steps;

import com.zkztch.jenkins.test.Gitlab;
import com.zkztch.jenkins.test.Jenkins;
import lombok.extern.slf4j.Slf4j;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Project;
import org.gitlab4j.api.models.Tag;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.*;
import org.jvnet.hudson.test.BuildWatcher;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.List;

@Slf4j
public class GitlabDeleteTagStepTest {

    @ClassRule public static final BuildWatcher buildWatcher  = Jenkins.buildWatcher;
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
    public void deleteWhenTagExist() throws Exception {
        Gitlab.api.getTagsApi().createTag(project, tag, Gitlab.DEFAULT_BRANCH);
        String script = String.format(
                "gitlabDeleteTag host: '%s', token: '%s', namespace: '%s', project: '%s', tag: '%s' \n",
                Gitlab.host, Gitlab.token, project.getNamespace().getPath(), project.getPath(), tag);

        log.info("script = " + script);
        WorkflowJob job = jenkinsRule.createProject(WorkflowJob.class, project.getName());
        job.setDefinition(new CpsFlowDefinition(script, true));
        jenkinsRule.assertBuildStatusSuccess(job.scheduleBuild2(0));
        jenkinsRule.waitUntilNoActivity();

        List<Tag> tags = Gitlab.api.getTagsApi().getTags(project);
        Assert.assertEquals(0, tags.size());
    }

}
