pipeline {
    agent any

    stages {
        stage('Checkout Code') {
            steps {
                // Pulls the fresh code from your GitHub develop branch
                checkout scm
            }
        }

        stage('Build Spring Boot JAR') {
            steps {
                echo 'Compiling and packaging the Spring Boot application with Gradle...'
                // Grant execution permissions to the gradle wrapper, then compile the JAR file
                sh 'chmod +x gradlew'
                sh './gradlew clean bootJar -x test'
            }
        }

        stage('Deploy Locally') {
            steps {
                echo 'Deploying Spring Boot application jar...'

                // Copy the newly generated JAR file over to your shared volume path
                sh 'cp build/libs/*.jar /var/www/my-backend-app/app.jar'

                echo 'Application JAR updated successfully in /var/www/my-backend-app/app.jar!'
            }
        }
    }
}