pipeline {
    agent none

    tools {
        allure 'Allure'
    }

    parameters {
        choice(name: 'TEST_SCOPE',
                choices: ['all', 'api', 'ui', 'smoke'],
                description: 'Which tests to run')

        booleanParam(name: 'HEADLESS',
                defaultValue: true,
                description: 'Run UI tests in headless mode')
    }

    triggers {
        cron('H 2 * * *')
    }

    stages {

        stage('Checkout') {
            agent any
            steps {
                checkout scm
            }
        }

        stage('Clean') {
            agent {
                docker {
                    image 'maven:3.9-eclipse-temurin-17'
                    args '-v /var/jenkins_home/.m2:/root/.m2'
                    reuseNode true
                }
            }
            steps {
                sh 'mvn clean'
            }
        }

        stage('Tests') {
            parallel {

                stage('API Tests') {
                    when {
                        expression {
                            params.TEST_SCOPE == 'all'
                            params.TEST_SCOPE == 'api'
                            params.TEST_SCOPE == 'smoke'
                        }
                    }
                    agent {
                        docker {
                            image 'maven:3.9-eclipse-temurin-17'
                            args '-v /var/jenkins_home/.m2:/root/.m2'
                            reuseNode true
                        }
                    }
                    steps {
                        retry(1) {
                            sh "mvn test -Dgroups=${params.TEST_SCOPE}"
                        }
                    }
                }

                stage('UI Tests') {
                    when {
                        expression {
                            params.TEST_SCOPE == 'all'
                            params.TEST_SCOPE == 'ui'
                            params.TEST_SCOPE == 'smoke'
                        }
                    }
                    agent {
                        docker {
                            image 'maven:3.9-eclipse-temurin-17'
                            args '--network jenkins-docker_default -v /var/jenkins_home/.m2:/root/.m2'
                            reuseNode true
                        }
                    }
                    environment {
                        SELENIUM_REMOTE_URL = 'http://selenium:4444/wd/hub'
                    }
                    steps {
                        retry(1) {
                            sh """
                                mvn test \
                                -Dgroups=${params.TEST_SCOPE} \
                                -Dheadless=${params.HEADLESS}
                            """
                        }
                    }
                }
            }
        }

        stage('Allure Report') {
            agent any
            steps {
                allure includeProperties: false,
                       results: [[path: 'target/allure-results']]
            }
        }

        stage('Archive Results') {
            agent any
            steps {
                junit 'target/surefire-reports/*.xml'
                archiveArtifacts artifacts: 'target/**/*.log', fingerprint: true
            }
        }
    }

    post {
        failure {
            echo "Build failed"
        }
    }
}