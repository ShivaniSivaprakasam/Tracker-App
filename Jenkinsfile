pipeline {
    agent any

    environment {
        DOCKER_IMAGE = "shivaniavani/tracker-app"
        DOCKER_TAG = "${BUILD_NUMBER}"
        PROJECT_DIR = "/workspace/tracker-app"
    }

    stages {

        stage('Build Docker Image') {
            steps {
                script {
                    docker.build(
                        "${DOCKER_IMAGE}:${DOCKER_TAG}",
                        "${PROJECT_DIR}"
                    )
                }
            }
        }

        stage('Push to Docker Hub') {
            steps {
                script {
                    docker.withRegistry(
                        'https://registry.hub.docker.com',
                        'dockerhub-credentials-new'
                    ) {
                        def image = docker.image(
                            "${DOCKER_IMAGE}:${DOCKER_TAG}"
                        )

                        image.push()
                        image.push("latest")
                    }
                }
            }
        }

        stage('Deploy') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'aiven-db-credentials',
                        usernameVariable: 'DB_USERNAME',
                        passwordVariable: 'DB_PASSWORD'
                    ),
                    usernamePassword(
                        credentialsId: 'gmail-credentials',
                        usernameVariable: 'MAIL_USERNAME',
                        passwordVariable: 'MAIL_PASSWORD'
                    )
                ]) {
                    sh '''
                        docker pull ${DOCKER_IMAGE}:${DOCKER_TAG}

                        docker stop tracker-app || true
                        docker rm tracker-app || true

                        docker run -d \
                          --name tracker-app \
                          --restart unless-stopped \
                          -p 8080:8080 \
                          -e DB_USERNAME="${DB_USERNAME}" \
                          -e DB_PASSWORD="${DB_PASSWORD}" \
                          -e MAIL_USERNAME="${MAIL_USERNAME}" \
                          -e MAIL_PASSWORD="${MAIL_PASSWORD}" \
                          -e APP_BASE_URL="http://localhost:8080" \
                          -e SPRING_DATASOURCE_URL="jdbc:mysql://mysql-21a4c9d3-tracker-app.b.aivencloud.com:23616/tracker_db?useSSL=true&allowPublicKeyRetrieval=true&serverTimezone=UTC" \
                          ${DOCKER_IMAGE}:${DOCKER_TAG}
                    '''
                }
            }
        }
    }

    post {
        always {
            sh "docker rmi ${DOCKER_IMAGE}:${DOCKER_TAG} || true"
        }

        success {
            echo "Build ${DOCKER_TAG} built, pushed, and deployed successfully."
        }

        failure {
            echo "Build failed — check the console output above for details."
        }
    }
}
