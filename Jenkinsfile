pipeline {
    agent any

    stages {
        stage("build") {
            steps {
                checkout scm
                sh "chmod u+x mvnw"
                sh "./mvnw -s settings.xml -DskipTests clean deploy"
            }

            post {
                success {
                    emailext subject: "${JOB_NAME}构建成功",
                            attachLog: true,
                            body: "${JOB_NAME}构建成功",
                            attachmentsPattern: '**/zkztch-jenkins-plugin.hpi',
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
