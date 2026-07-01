pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                // Pulls automatically via SCM but restricts the workspace strictly to the develop branch
                checkout([$class: 'GitSCM',
                    branches: [[name: '*/develop']],
                    userRemoteConfigs: scm.userRemoteConfigs
                ])
            }
        }

        stage('Build JAR') {
            steps {
                echo 'Compiling Boot application from develop...'
                sh '''
                    chmod +x gradlew
                    ./gradlew clean bootJar -x test
                '''
            }
        }

        stage('Deploy to Docker') {
            steps {
                echo 'Rebuilding and restarting attendance-backend container...'
                sh '''
                    # Build fresh image using the new JAR
                    docker build -t attendance-backend:latest .

                    # Force remove the old running container immediately
                    docker rm -f backend || true

                    # Spin up the fresh container on your network
                    docker run -d \
                      --name backend \
                      --network management_attendance_university_default \
                      -p 8080:8080 \
                      attendance-backend:latest
                '''
            }
        }
    }

    post {
        success { echo 'Deployed successfully!' }
        failure { echo 'Failed!!' }
    }
}