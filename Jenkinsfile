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
                echo 'Building backend image and restarting container...'
                
                // Build the new image using the Dockerfile in the project root
                sh 'docker build -t attendance-backend:latest .'
                
                // Stop and remove the old running container if it exists (ignores error if it doesn't)
                sh 'docker stop backend || true'
                sh 'docker rm backend || true'
                
                // Spin up the new container connected to your network
                sh 'docker run -d --name backend --network management_attendance_university_default -p 8080:8080 attendance-backend:latest'

                echo 'Deployment successful! Your live container has been updated.'
            }
        }
    }
}
