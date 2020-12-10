pipeline {
    agent any
    tools {
        jdk "jdk11"
        dockerTool "docker"
    }
    environment {
        registry = "cjvogel1972/kube-dashboard"
        registryCredential = 'dockerhub'
        image = ''
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
                    image = docker.build(registry + ":0.0.1")
                    docker.withRegistry( '', registryCredential ) {
                        image.push()
                    }
                }
            }
        }
    }
    post {
        always {
            options {​​​​
                office365ConnectorWebhooks([[
                        startNotification: true,
                        notifyBackToNormal: true,
                        notifyFailure: true,
                        notifyRepeatedFailure: true,
                        notifySuccess: true,
                        notifyUnstable: true,
                        url: 'https://outlook.office.com/webhook/bcfd3775-a771-49e6-b79d-22ff707a4c40@06ad24ba-31eb-40d8-948c-a29338f7d041/JenkinsCI/1f4551481bd14b8baa0908e8e8b28a0a/09999f24-0c10-4d71-97cb-d2a7c5d96b2b'
                    ]]
                )
            }
        }
    }
}