pipeline {
    agent any
    tools {
        jdk "JDK11"
        gradle "Gradle5.4"
    }
    stages {
        stage('run sonar') {
            steps {
                 withSonarQubeEnv('Sonar7.7') {
                     sh 'gradle sonarqube'
                 }
           }
        }
    }
}