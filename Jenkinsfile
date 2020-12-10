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
                    withSonarQubeEnv("sonar") {
                        sh './gradlew build sonarqube --no-daemon'
                        sh 'docker build . --file src/main/docker/Dockerfile --tag cjvogel1972/kube-dashboard:0.0.1'
                    }
            }
        }
    }
}