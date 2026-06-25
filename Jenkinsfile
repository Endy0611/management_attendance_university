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

        stage('Deploy Locally') {
            steps {
                echo 'Deploying Spring Boot application to active Docker container...'

                // 1. Copy the newly generated JAR file over to your shared volume path
                sh 'cp build/libs/*.jar /var/www/my-backend-app/app.jar'

                // 2. Head to your project directory and restart the backend service
                sh '''
                    cd /home/hrdeventhub/management_attendance_university
                    sudo docker compose restart backend
                '''
                echo 'Deployment successful! Container updated.'
            }
        }
    }
}