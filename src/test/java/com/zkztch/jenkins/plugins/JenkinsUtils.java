package com.zkztch.jenkins.plugins;

import org.junit.ClassRule;
import org.jvnet.hudson.test.BuildWatcher;
import org.jvnet.hudson.test.JenkinsRule;

public class JenkinsUtils {

    @ClassRule public static final BuildWatcher buildWatcher = new BuildWatcher();
    @ClassRule public static final JenkinsRule jenkinsRule = new JenkinsRule();
}
