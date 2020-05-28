pipeline {
    agent any

    environment {
        JENKINS_URL = "http://jenkins.dev.zkztch.com"
        PLUGIN_FILE = "target/zkztch-jenkins-plugin.hpi"
        JENKINS_USER = "admin"
        JENKINS_TOKEN = "119199ddc3aa38663d777c3b03d468da04"
    }

    stages {
        stage("build") {
            steps {
                checkout scm
                sh "chmod u+x mvnw"
                sh "./mvnw -s settings.xml -DskipTests clean package"
                jenkinsUploadPlugin url: "${JENKINS_URL}", file: "${PLUGIN_FILE}", user: "${JENKINS_USER}", token: "${JENKINS_TOKEN}", restart: true
            }

            post {
                success {
                    emailext subject: "${JOB_NAME}构建成功",
                            attachLog: true,
                            body: "${JOB_NAME}构建成功",
                            to: "caizl@zkztch.com"
                }

                unsuccessful {
                    emailext subject: "${JOB_NAME}构建失败",
                            attachLog: true,
                            body: "${JOB_NAME}构建失败",
                            to: "caizl@zkztch.com"
                }
            }
        }
    }
}
