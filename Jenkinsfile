pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build JAR') {
            steps {
                echo 'Compiling Boot application...'
                sh '''
                    chmod +x gradlew
                    ./gradlew clean bootJar -x test
                '''
            }
        }
    }

    post {
        success { echo 'Deployed!' }
        failure { echo 'Failed!!' }
    }
}