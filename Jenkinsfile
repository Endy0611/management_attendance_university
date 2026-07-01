pipeline {
    agent any

    stages {
        stage('Deploy') {
            steps {
                echo 'Triggering host deployment script...'
                sh 'sudo -u hrdeventhub /home/hrdeventhub/management_attendance_university/deploy.sh'
            }
        }
    }

    post {
        success { echo 'Deployed!' }
        failure { echo 'Failed!!' }
    }
}