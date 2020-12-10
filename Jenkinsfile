pipeline {
    agent any
    tools {
        jdk "jdk11"
        dockerTool "docker"
    }
    environment {
        registry = "cjvogel1972/kube-dashboard"
        registryCredential = 'dockerhub'
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
                }
            }
        }

        stage('Build Docker image') {
            steps {
                script {
                    docker.build("cjvogel1972/kube-dashboard:0.0.1").push()
                }
            }
        }
    }
}