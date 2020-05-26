package com.zkztch.jenkins.plugins.pipeline.steps;

import com.zkztch.jenkins.plugins.pipeline.GitlabConsts;
import com.zkztch.test.Gitlab;
import com.zkztch.test.Jenkins;
import lombok.extern.slf4j.Slf4j;
import org.gitlab4j.api.Constants;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Project;
import org.gitlab4j.api.models.RepositoryFile;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.BuildWatcher;
import org.jvnet.hudson.test.JenkinsRule;

@Slf4j
public class GitlabCheckoutStepTest {

    @ClassRule public static final BuildWatcher buildWatcher = Jenkins.buildWatcher;
    @ClassRule public static final JenkinsRule jenkinsRule = Jenkins.jenkinsRule;

    private Project project;
    private String dir = "dir";
    private String fileName = "test.txt";
    private String fileContent = "test";

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
    public void checkoutTest() throws Exception {
        RepositoryFile file = new RepositoryFile();
        file.setFilePath(fileName);
        file.setEncoding(Constants.Encoding.TEXT);
        file.encodeAndSetContent(fileContent);
        Gitlab.api.getRepositoryFileApi().createFile(project, file, Gitlab.DEFAULT_BRANCH, "tt");

        String format = "pipeline {\n" +
                "    agent any\n" +
                "    environment {\n" +
                "        %s = \"%s\"\n" +
                "        %s = \"%s\"\n" +
                "        %s = \"%s\"\n" +
                "        %s = \"%s\"\n" +
                "    }\n" +
                "    stages {\n" +
                "        stage(\"start\") {\n" +
                "            steps {\n" +
                "                script {\n" +
                "                   gitlabCheckout dir: '%s', ref: '%s' \n" +
                "                   def txt = readFile encoding: 'utf-8', file: '%s/%s' \n" +
                "                   echo txt \n" +
                "               }\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}";
        String script =
                String.format(format, GitlabConsts.GITLAB_URL, Gitlab.host, GitlabConsts.GITLAB_TOKEN, Gitlab.token,
                        GitlabConsts.GITLAB_NAMESPACE, project.getNamespace().getPath(),
                        GitlabConsts.GITLAB_PROJECT, project.getPath(), dir, Gitlab.DEFAULT_BRANCH, dir, fileName);
        log.info("script = " + script);
        WorkflowJob job = jenkinsRule.createProject(WorkflowJob.class, project.getName());
        job.setDefinition(new CpsFlowDefinition(script, true));
        WorkflowRun run = jenkinsRule.assertBuildStatusSuccess(job.scheduleBuild2(0));
        jenkinsRule.waitUntilNoActivity();
        jenkinsRule.assertLogContains(fileContent, run);
    }

}
