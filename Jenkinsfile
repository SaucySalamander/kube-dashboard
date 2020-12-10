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
        office365ConnectorWebhooks([[
                startNotification: true,
                notifyBackToNormal: true,
                notifyFailure: true,
                notifyRepeatedFailure: true,
                notifySuccess: true,
                notifyUnstable: true,
                url: 'https://outlook.office.com/webhook/bcfd3775-a771-49e6-b79d-22ff707a4c40@06ad24ba-31eb-40d8-948c-a29338f7d041/JenkinsCI/b4f440e92bf14772a85f9c3e6b8bfa20/4dc936cc-4cbc-4246-8c07-35ff70e9a0e4'
            ]]
        )
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
                    sh './gradlew build jTR sonarqube --no-daemon'
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

        stage('Kick off deployment pipleline') {
            steps {
                script {
                    AUTHOR_NAME = sh (
                          script: "git show -s --format='%%an' HEAD",
                          returnStdout: true
                      ).split('\r\n')[2].trim()
                    cloudBeesFlowRunPipeline addParam: '{"pipeline":{"pipelineName":"K8s_Pipeline","parameters":"[{\\"parameterName\\": \\"authorName\\", \\"parameterValue\\": \\"${AUTHOR_NAME}\\"},{\\"parameterName\\": \\"chartName\\", \\"parameterValue\\": \\"kube-dashboard\\"}, {\\"parameterName\\": \\"namespace\\", \\"parameterValue\\": \\"kube-dashboard\\"}, {\\"parameterName\\": \\"releaseName\\", \\"parameterValue\\": \\"kube-dashboard\\"}, {\\"parameterName\\": \\"repoName\\", \\"parameterValue\\": \\"codefest\\"}, {\\"parameterName\\": \\"repoUrl\\", \\"parameterValue\\": \\"https://dbrande99.github.io/helm-chart/\\"}, {\\"parameterName\\": \\"valuesFile\\", \\"parameterValue\\": \\"\\"}, {\\"parameterName\\": \\"webhookUrl\\", \\"parameterValue\\": \\"https://outlook.office.com/webhook/bcfd3775-a771-49e6-b79d-22ff707a4c40@06ad24ba-31eb-40d8-948c-a29338f7d041/IncomingWebhook/0cd223e391824897a4ddeb6ad990a670/4dc936cc-4cbc-4246-8c07-35ff70e9a0e4\\"}]"}}', configuration: 'Codefest', pipelineName: 'K8s_Pipeline', projectName: 'Codefest'
                }
            }
        }
    }
}