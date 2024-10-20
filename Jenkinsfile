pipeline { 
    agent any

    environment {
        APP_NAME = 'spring-boot-test-jenkins' // Application name
        DOCKER_REGISTRY = 'ivangorbunovv' // Docker registry
        K8S_DEPLOYMENT_NAME = 'spring-boot-test-deployment'
        K8S_NAMESPACE = 'default' // Kubernetes namespace for deployment
        KUBECONFIG = 'C:/Users/gorbu/.kube/config'
    }

    stages {

        stage('Test') {
            steps {
                // Clean and run unit tests
                bat "mvn clean test"
            }
        }

        stage('Build') {
            steps {
                // Package the Spring Boot app
                bat "mvn package -DskipTests"
            }
        }

        stage('Docker Build Image') {
            steps {
                script {
                    // Get the short Git commit hash
                    def commitHash = bat(script: "git rev-parse HEAD", returnStdout: true).trim().split("\n")[-1].trim().substring(0, 7).trim()
                    
                    // Docker image name with the commit hash in the tag
                    def imageTag = "${DOCKER_REGISTRY}/${APP_NAME}:${commitHash}"
                    
                    // Use Jenkins credentials for Docker login and then build and tag Docker image
                    withCredentials([usernamePassword(credentialsId: 'docker-hub-credentials', usernameVariable: 'DOCKER_USERNAME', passwordVariable: 'DOCKER_PASSWORD')]) {
                        bat "docker login -u $DOCKER_USERNAME -p $DOCKER_PASSWORD"
						bat "docker build -t ${imageTag} ."
                    }
                }
            }
        }

        stage('Docker Push to Docker Hub') {
            steps {
                script {
                    // Get the short Git commit hash
                    def commitHash = bat(script: "git rev-parse HEAD", returnStdout: true).trim().split("\n")[-1].trim().substring(0, 7).trim()
                    
                    // Docker image name with the commit hash in the tag
                    def imageTag = "${DOCKER_REGISTRY}/${APP_NAME}:${commitHash}"

                    // Use Jenkins credentials for Docker login and then build and tag Docker image
                    withCredentials([usernamePassword(credentialsId: 'docker-hub-credentials', usernameVariable: 'DOCKER_USERNAME', passwordVariable: 'DOCKER_PASSWORD')]) {
                        bat "docker login -u $DOCKER_USERNAME -p $DOCKER_PASSWORD"
						bat "docker push ${imageTag}"
                    }
                }
            }
        }

        stage('Kubernetes Deploy') {
            steps {
                script {
                    // Deploy the Docker image to the Kubernetes cluster using kubectl
					
                    def commitHash = bat(script: "git rev-parse HEAD", returnStdout: true).trim().split("\n")[-1].trim().substring(0, 7).trim()
                    def imageTag = "${DOCKER_REGISTRY}/${APP_NAME}:${commitHash}"

                    // Check if the deployment already exists
                    def deploymentExists = bat(
                        script: "kubectl get deployment ${K8S_DEPLOYMENT_NAME} -n ${K8S_NAMESPACE}",
                        returnStatus: true
                    )
                    
                    if (deploymentExists != 0) {
                        echo 'deployment does not exist, creating new'
                        bat "kubectl apply -f k8s/deploy-and-service-for-spring-boot-test.yaml -n ${K8S_NAMESPACE}"
                    } else {
                        echo 'deployment exists, updating its image in runtime'
                    }
                    
                    // Replace the image of deployment in runtime
                    bat """
                        kubectl set image deployment/${K8S_DEPLOYMENT_NAME} spring-boot-test-container=${imageTag} -n ${K8S_NAMESPACE}
                        kubectl rollout status deployment/${K8S_DEPLOYMENT_NAME} -n ${K8S_NAMESPACE}
                        """
                }
            }
        }
    }

    post {
        always {
            // Clean workspace
            cleanWs()
        }
        success {
            // Notify success (e.g., email)
            echo "Deployment of ${APP_NAME} succeeded!"
        }
        failure {
            // Notify failure
            echo "Deployment of ${APP_NAME} failed!"
        }
    }
}
