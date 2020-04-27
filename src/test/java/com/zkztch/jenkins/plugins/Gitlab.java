package com.zkztch.jenkins.plugins;

import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Project;

import java.util.UUID;

public class Gitlab {

    public static final String DEFAULT_BRANCH = "master";
    public static final String host;
    public static final String token;
    public static final GitLabApi api;


    static {
        host = TestPropertiesUtils.getProperty("gitlab.host");
        token = TestPropertiesUtils.getProperty("gitlab.token");
        api = new GitLabApi(host, token);
        api.setIgnoreCertificateErrors(true);
    }

    public static Project createTmpProject() throws GitLabApiException {

        Project projectSettings = new Project()
                .withName(UUID.randomUUID().toString())
                .withDefaultBranch(Gitlab.DEFAULT_BRANCH)
                .withPublic(true)
                .withInitializeWithReadme(true)
                .withRequestAccessEnabled(false);

        return api.getProjectApi().createProject(projectSettings);
    }

    public static void deleteProject(Project project) throws GitLabApiException {
        if (project != null) {
            api.getProjectApi().deleteProject(project);
        }
    }

}
