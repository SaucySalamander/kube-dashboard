pipeline {
    agent any
    tools {
        jdk "jdk11"
    }
    options {
        skipDefaultCheckout()
        timestamps()
    }
    stages {
        stage('Initialize Pipeline') {
            steps {
                script {
                    properties([
                        pipelineTriggers([[$class: "GitHubPushTrigger"]])
                    ])
                }
            }
        }

        stage('Build Project') {
            steps {
                checkout scm
//                withEnv(["JAVA_HOME=${tool 'jdk11'}", "PATH=${tool 'jdk11'}/bin:${env.PATH}"]) {
                    withSonarQubeEnv("sonar") {
                        sh './gradlew build sonarqube --no-daemon'
                        sh 'docker build . --file src/main/docker/Dockerfile --tag cjvogel1972/kube-dashboard:0.0.1'
                    }
//                }
            }
        }
    }
}