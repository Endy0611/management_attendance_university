pipeline {
    agent any

    stages {
        stage('Checkout Code') {
            steps {
                checkout scm
            }
        }

        stage('Build Spring Boot JAR') {
            steps {
                echo 'Compiling and packaging the Spring Boot application with Gradle...'
                sh 'chmod +x gradlew'
                sh './gradlew clean bootJar -x test'
            }
        }

        stage('Deploy Natively to Docker') {
            steps {
                echo 'Triggering Docker Compose to rebuild and restart the backend container...'
                sh 'docker-compose up -d --build backend'
                echo 'Deployment successful! Your live container has been updated.'
            }
        }
    }
}
