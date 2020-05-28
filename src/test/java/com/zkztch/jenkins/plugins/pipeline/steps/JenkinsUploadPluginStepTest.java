package com.zkztch.jenkins.plugins.pipeline.steps;

import com.google.common.io.Resources;
import com.zkztch.test.Jenkins;
import com.zkztch.test.TestPropertiesUtils;
import lombok.extern.slf4j.Slf4j;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.BuildWatcher;
import org.jvnet.hudson.test.JenkinsRule;

@Slf4j
public class JenkinsUploadPluginStepTest {


    @ClassRule public static final BuildWatcher buildWatcher = Jenkins.buildWatcher;
    @ClassRule public static final JenkinsRule jenkinsRule = Jenkins.jenkinsRule;

    private String url = TestPropertiesUtils.getProperty("jenkins.url");
    private String file = Resources.getResource(TestPropertiesUtils.getProperty("jenkins.plugin.file")).getPath();
    private String user = TestPropertiesUtils.getProperty("jenkins.user.name");
    private String token = TestPropertiesUtils.getProperty("jenkins.user.token");

    @Test
    public void test() throws Exception {
        String script = String.format("pipeline {\n" +
                        "    agent any\n" +
                        "    stages {\n" +
                        "        stage(\"start\") {\n" +
                        "            steps{\n" +
                        "                jenkinsUploadPlugin url:'%s', file:'%s', user:'%s', token:'%s', restart: true \n" +
                        "            }\n" +
                        "        }\n" +
                        "    }\n" +
                        "}",
                url, file, user, token);
        log.info("script = " + script);
        WorkflowJob job = jenkinsRule.createProject(WorkflowJob.class, "jenkinsUploadPlugin");
        job.setDefinition(new CpsFlowDefinition(script, true));
        jenkinsRule.assertBuildStatusSuccess(job.scheduleBuild2(0));
        jenkinsRule.waitUntilNoActivity();

    }
}
