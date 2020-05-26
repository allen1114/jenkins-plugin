package com.zkztch.jenkins.plugins.pipeline.steps;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;
import com.jcraft.jsch.Session;
import hudson.Extension;
import hudson.FilePath;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.util.FS;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.models.Project;
import org.gitlab4j.api.models.SshKey;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class GitlabCheckoutStep extends AbstractGitlabStep {

    public static final String STEP = "gitlabCheckout";

    private static final String SSH_KEY_TITLE = "TmpkeyForGitlabCheckout";

    private String ref;
    private String dir;

    @DataBoundConstructor
    public GitlabCheckoutStep(String ref, String dir) {
        this.ref = ref;
        this.dir = dir;
    }

    @Override
    public Object doStart(StepContext context, PrintStream logger, GitLabApi gitLabApi, Project project) throws Exception {

        FilePath workspace = context.get(FilePath.class);
        FilePath gitdir = workspace.child(dir);
        CloneCommand cmd = Git.cloneRepository();
        JSch jsch = new JSch();
        JSch.setConfig("ssh-rsa", JSch.getConfig("signature.rsa"));
        JSch.setConfig("ssh-dss", JSch.getConfig("signature.dss"));
        KeyPair keyPair = KeyPair.genKeyPair(jsch, KeyPair.RSA);
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        keyPair.writePrivateKey(o);
        byte[] prikey = o.toByteArray();
        jsch.addIdentity(SSH_KEY_TITLE, prikey, keyPair.getPublicKeyBlob(), null);

        ByteArrayOutputStream op = new ByteArrayOutputStream();
        keyPair.writePublicKey(op, SSH_KEY_TITLE);
        SshKey sshKey = gitLabApi.getUserApi().addSshKey(SSH_KEY_TITLE, new String(op.toByteArray(), StandardCharsets.UTF_8));
        SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() {
            @Override
            protected void configure(OpenSshConfig.Host host, Session session) {
                session.setConfig("StrictHostKeyChecking", "no");
            }

            @Override
            protected JSch createDefaultJSch(FS fs) throws JSchException {
                return jsch;
            }
        };
        cmd.setURI(project.getSshUrlToRepo())
                .setDirectory(new File(gitdir.toURI()))
                .setBranch(ref)
                .setTransportConfigCallback(
                        transport -> {
                            if (transport instanceof SshTransport) {
                                SshTransport sshTransport = (SshTransport) transport;
                                sshTransport.setSshSessionFactory(sshSessionFactory);
                            }
                        });
        Git git = cmd.call();
        git.close();
        gitLabApi.getUserApi().deleteSshKey(sshKey.getId());
        return null;
    }

    @Extension
    public static class Descriptor extends GitlabStepDescriptor {
        @Override
        public String getFunctionName() {
            return STEP;
        }
    }
}
