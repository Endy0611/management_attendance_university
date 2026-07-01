pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                // Explicitly syncs and pulls the develop branch on every webhook push
                checkout([$class: 'GitSCM',
                    branches: [[name: 'develop']],
                    userRemoteConfigs: scm.userRemoteConfigs
                ])
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