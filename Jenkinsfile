pipeline {
    agent any

    tools {
        maven 'Maven 3.8.1'
        jdk 'JDK 21'
    }

    environment {
        // Docker registry credentials
        DOCKER_REGISTRY = 'docker.io'
        DOCKER_REPO = 'bank'
        DOCKER_CREDENTIALS_ID = 'docker-hub-credentials'

        // Kubernetes cluster credentials
        KUBECONFIG_CREDENTIALS_ID = 'kubeconfig-credentials'

        // Helm settings
        HELM_RELEASE_NAME = 'bank-app'
        HELM_NAMESPACE = 'bank-app'

        // Secret credentials
        SECRET_CREDENTIALS_ID = 'bank-app-secrets'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                script {
                    echo 'Building Java microservices...'

                    // Build all microservices
                    sh 'mvn clean package -DskipTests'
                }
            }
        }

        stage('Test') {
            steps {
                script {
                    echo 'Running unit and integration tests...'

                    // Run tests for all microservices
                    sh 'mvn test'
                }

                post {
                    always {
                        // Publish test results
                        junit '**/target/surefire-reports/*.xml'
                    }
                }
            }
        }

        stage('Build Docker Images') {
            steps {
                script {
                    echo 'Building Docker images for all microservices...'

                    // Build Docker images
                    sh './build-images.sh'
                }
            }
        }

        stage('Push Docker Images') {
            steps {
                script {
                    echo 'Pushing Docker images to registry...'

                    // Login to Docker registry
                    withCredentials([usernamePassword(credentialsId: "${DOCKER_CREDENTIALS_ID}",
                                                      usernameVariable: 'DOCKER_USER',
                                                      passwordVariable: 'DOCKER_PASS')]) {
                        sh 'docker login -u $DOCKER_USER -p $DOCKER_PASS'
                    }

                    // Tag and push images
                    def services = ['front-ui', 'gateway', 'accounts-service', 'cash-service', 'transfer-service', 'notifications-service']
                    services.each { service ->
                        def imageTag = "${DOCKER_REPO}/${service}:latest"
                        def imageWithRegistry = "${DOCKER_REGISTRY}/${imageTag}"

                        sh "docker tag ${imageTag} ${imageWithRegistry}"
                        sh "docker push ${imageWithRegistry}"
                    }
                }
            }
        }

        stage('Helm Lint') {
            steps {
                script {
                    echo 'Linting Helm charts...'

                    // Lint Helm chart
                    sh 'helm lint helm-charts/bank-app-chart'
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                script {
                    echo 'Deploying to Kubernetes cluster...'

                    // Setup kubeconfig
                    withCredentials([file(credentialsId: "${KUBECONFIG_CREDENTIALS_ID}",
                                          variable: 'KUBECONFIG_FILE')]) {
                        sh 'mkdir -p ~/.kube'
                        sh 'cp $KUBECONFIG_FILE ~/.kube/config'
                    }

                    // Add Helm repositories
                    sh 'helm repo add bitnami https://charts.bitnami.com/bitnami'
                    sh 'helm repo update'

                    // Deploy application with secrets
                    sh '''
                        helm upgrade --install ${HELM_RELEASE_NAME} helm-charts/bank-app-chart \\
                          --namespace ${HELM_NAMESPACE} \\
                          --create-namespace \\
                          --set front-ui.ingress.enabled=true \\
                          --set front-ui.ingress.hosts[0].host=bank.local \\
                          --set front-ui.ingress.hosts[0].paths[0].path=/ \\
                          --set front-ui.ingress.hosts[0].paths[0].pathType=Prefix \\
                          --set gateway.ingress.enabled=true \\
                          --set gateway.ingress.hosts[0].host=api.bank.local \\
                          --set gateway.ingress.hosts[0].paths[0].path=/api \\
                          --set gateway.ingress.hosts[0].paths[0].pathType=Prefix \\
                          --set global.postgresql.password="" \\
                          --set global.keycloak.adminPassword="" \\
                          --set postgresql.auth.password="" \\
                          --set postgresql.auth.postgresPassword="" \\
                          --set keycloak.auth.adminPassword="" \\
                          --set keycloak.externalDatabase.password="" \\
                          --set accounts-service.env.SPRING_DATASOURCE_PASSWORD="" \\
                          --set cash-service.env.SPRING_DATASOURCE_PASSWORD="" \\
                          --set transfer-service.env.SPRING_DATASOURCE_PASSWORD="" \\
                          --set notifications-service.env.SPRING_DATASOURCE_PASSWORD="" \\
                          --set keycloak.env.KC_DB_PASSWORD="" \\
                          --set keycloak.env.KEYCLOAK_ADMIN_PASSWORD=""
                    '''
                }
            }
        }

        stage('Helm Test') {
            steps {
                script {
                    echo 'Running Helm tests...'

                    // Run Helm tests
                    sh 'helm test ${HELM_RELEASE_NAME} --namespace ${HELM_NAMESPACE}'
                }
            }
        }

        stage('Verify Deployment') {
            steps {
                script {
                    echo 'Verifying deployment...'

                    // Check if all pods are running
                    sh 'kubectl get pods -n ${HELM_NAMESPACE}'

                    // Wait for all pods to be ready
                    sh '''
                        kubectl wait --for=condition=ready pod -l app.kubernetes.io/instance=${HELM_RELEASE_NAME} \
                          --timeout=300s -n ${HELM_NAMESPACE} || exit 1
                    '''
                }
            }
        }
    }

    post {
        always {
            script {
                echo 'Cleaning up...'

                // Clean up Docker images locally to save space
                sh 'docker system prune -f' || true

                // Remove kubeconfig
                sh 'rm -f ~/.kube/config' || true
            }
        }

        success {
            script {
                echo 'Pipeline completed successfully!'

                // Send notification on success
                emailext (
                    subject: "SUCCESS: ${currentBuild.fullDisplayName}",
                    body: """Build successful!

Project: ${env.JOB_NAME}
Build Number: ${env.BUILD_NUMBER}
URL: ${env.BUILD_URL}

Pipeline completed successfully.""",
                    to: 'dev-team@example.com',
                    recipientProviders: [[$class: 'DevelopersRecipientProvider']]
                )
            }
        }

        failure {
            script {
                echo 'Pipeline failed!'

                // Send notification on failure
                emailext (
                    subject: "FAILED: ${currentBuild.fullDisplayName}",
                    body: """Build failed!

Project: ${env.JOB_NAME}
Build Number: ${env.BUILD_NUMBER}
URL: ${env.BUILD_URL}

Check the console output for details.""",
                    to: 'dev-team@example.com',
                    recipientProviders: [[$class: 'DevelopersRecipientProvider']]
                )
            }
        }
    }
}