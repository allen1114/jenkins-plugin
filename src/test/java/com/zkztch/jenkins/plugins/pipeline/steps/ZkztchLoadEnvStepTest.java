package com.zkztch.jenkins.plugins.pipeline.steps;

import com.zkztch.test.Jenkins;
import lombok.extern.slf4j.Slf4j;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.BuildWatcher;
import org.jvnet.hudson.test.JenkinsRule;

@Slf4j
public class ZkztchLoadEnvStepTest {

    @ClassRule public static final BuildWatcher buildWatcher = Jenkins.buildWatcher;
    @ClassRule public static final JenkinsRule jenkinsRule = Jenkins.jenkinsRule;

    @Test
    public void test() throws Exception {
        String script = String.format("def envMap = zkztchLoadEnv addr: '%s', path: '%s'\n" +
                        "envMap.each {\n" +
                        "echo it.key\n" +
                        "echo it.value\n" +
                        "}",
                "http://192.168.1.130:8003/", "ci-demo/dev");
        log.info("script = " + script);
        WorkflowJob job = jenkinsRule.createProject(WorkflowJob.class, "LoadZkztchEnvStepTest");
        job.setDefinition(new CpsFlowDefinition(script, true));
        jenkinsRule.assertBuildStatusSuccess(job.scheduleBuild2(0));
        jenkinsRule.waitUntilNoActivity();

    }
}
