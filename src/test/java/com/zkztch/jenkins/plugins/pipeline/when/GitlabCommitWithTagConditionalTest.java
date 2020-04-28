package com.zkztch.jenkins.plugins.pipeline.when;

import com.zkztch.jenkins.test.Gitlab;
import com.zkztch.jenkins.test.Jenkins;
import lombok.extern.slf4j.Slf4j;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Project;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.BuildWatcher;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.UUID;

@Slf4j
public class GitlabCommitWithTagConditionalTest {

    @ClassRule public static final BuildWatcher buildWatcher = Jenkins.buildWatcher;
    @ClassRule public static final JenkinsRule jenkinsRule = Jenkins.jenkinsRule;
    private Project project;
    private String commit;
    private String tag;

    @Before
    public void setup() throws GitLabApiException {
        log.info("setup");
        project = Gitlab.createTmpProject();
        tag = UUID.randomUUID().toString();
        Gitlab.api.getTagsApi().createTag(project, tag, Gitlab.DEFAULT_BRANCH);
        commit = Gitlab.api.getTagsApi().getTag(project, tag).getCommit().getId();
    }

    @After
    public void clean() throws GitLabApiException {
        log.info("clean");
        Gitlab.deleteProject(project);
    }

    @Test
    public void passWhenTagMatchWithEnv() throws Exception {

        String format = "pipeline {\n" +
                "    agent any\n" +
                "    environment {\n" +
                "        GITLAB_HOST = \"%s\"\n" +
                "        GITLAB_TOKEN = \"%s\"\n" +
                "        GITLAB_NAMESPACE = \"%s\"\n" +
                "        GITLAB_PROJECT = \"%s\"\n" +
                "        GIT_COMMIT = \"%s\"\n" +
                "    }\n" +
                "    stages {\n" +
                "        stage(\"start\") {\n" +
                "            when {\n" +
                "                gitlabCommitWithTag \"%s\"\n" +
                "            }\n" +
                "            steps {\n" +
                "                echo \"${GIT_COMMIT} matched\"\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}";
        String script =
                String.format(format, Gitlab.host, Gitlab.token, project.getNamespace().getPath(), project.getPath(), commit, tag);

        log.info("script = " + script);
        WorkflowJob job = jenkinsRule.createProject(WorkflowJob.class, project.getName());
        job.setDefinition(new CpsFlowDefinition(script, true));

        jenkinsRule.assertLogContains(commit + " matched", jenkinsRule.assertBuildStatusSuccess(job.scheduleBuild2(0)));
        jenkinsRule.waitUntilNoActivity();

    }

    @Test
    public void passWhenTagMatchWithParam() throws Exception {

        String format = "pipeline {\n" +
                "    agent any\n" +
                "    environment {\n" +
                "        GIT_COMMIT = \"%s\"\n" +
                "    }\n" +
                "    stages {\n" +
                "        stage(\"start\") {\n" +
                "            when {\n" +
                "                gitlabCommitWithTag host: '%s', token: '%s', namespace: '%s', project: '%s', pattern: '%s'\n" +
                "            }\n" +
                "            steps {\n" +
                "                echo \"${GIT_COMMIT} matched\"\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}\n";
        String script =
                String.format(format, commit, Gitlab.host, Gitlab.token, project.getNamespace().getPath(), project.getPath(), tag);

        log.info("script = " + script);
        WorkflowJob job = jenkinsRule.createProject(WorkflowJob.class, project.getName());
        job.setDefinition(new CpsFlowDefinition(script, true));

        jenkinsRule.assertLogContains(commit + " matched", jenkinsRule.assertBuildStatusSuccess(job.scheduleBuild2(0)));
        jenkinsRule.waitUntilNoActivity();

    }
}
