package com.zkztch.jenkins.plugins.pipeline.steps;

import com.zkztch.test.Jenkins;
import lombok.extern.slf4j.Slf4j;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.BuildWatcher;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.UUID;

@Slf4j
public class EnvExpendStepTest {

    @ClassRule public static final BuildWatcher buildWatcher = Jenkins.buildWatcher;
    @ClassRule public static final JenkinsRule jenkinsRule = Jenkins.jenkinsRule;

    private String testenv;

    @Before
    public void setup() {
        testenv = UUID.randomUUID().toString();
    }

    @Test
    public void test() throws Exception {
        String format = "pipeline {\n" +
                "    agent any\n" +
                "    environment {\n" +
                "        TEST_ENV = \"%s\"\n" +
                "    }\n" +
                "    stages {\n" +
                "        stage(\"start\") {\n" +
                "            steps {\n" +
                "                script {\n" +
                "                   def txt = envExpand content: '\\$' + '{TEST_ENV}'\n" +
                "                   echo txt \n" +
                "               }\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}";
        String script = String.format(format, testenv);
        log.info("script = " + script);
        WorkflowJob job = jenkinsRule.createProject(WorkflowJob.class, "LoadZkztchEnvStepTest");
        job.setDefinition(new CpsFlowDefinition(script, true));
        WorkflowRun run = jenkinsRule.assertBuildStatusSuccess(job.scheduleBuild2(0));
        jenkinsRule.waitUntilNoActivity();
        jenkinsRule.assertLogContains(testenv, run);

    }
}
