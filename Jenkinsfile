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
                // Ensure the Gradle wrapper has execution permissions, then build the JAR
                sh 'chmod +x gradlew'
                sh './gradlew clean bootJar -x test'
            }
        }

        stage('Deploy Locally') {
            when {
                branch 'develop'
            }
            steps {
                echo 'Deploying Spring Boot application locally to /var/www...'

                // 1. Gradle outputs the built jar into the build/libs/ directory.
                // We copy it straight over to our deployment folder.
                sh 'cp build/libs/*.jar /var/www/my-backend-app/app.jar'

                // 2. Safely stop the old instance and spin up the new one in the background
                sh '''
                    cd /var/www/my-backend-app/

                    # Kill any existing application running on your backend port (e.g. 8080)
                    sudo fuser -k 8080/tcp || true

                    # Start the fresh Spring Boot JAR quietly in the background
                    nohup java -jar app.jar > spring-boot.log 2>&1 &

                    echo "Spring Boot application updated and running!"
                '''
            }
        }
    }
}