pipeline {

    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '20'))
    }

    tools {
        allure 'Allure'
    }

    parameters {
        booleanParam(name: 'RUN_E2E', defaultValue: false, description: 'Run E2E manually')
        booleanParam(name: 'RUN_SMOKE_FROM_APP', defaultValue: false, description: 'Triggered from fakeApp PR')
        booleanParam(name: 'HEADLESS', defaultValue: true, description: 'Headless mode for UI')
    }

    triggers {
        cron('*/2 * * * *')
    }

    stages {

        stage('Checkout') {
            agent any
            steps {
                checkout scm
                stash name: 'source', includes: '**/*'
            }
        }

        stage('Smoke Tests') {
            when {
                anyOf {
                    triggeredBy 'TimerTrigger'
                    expression { params.RUN_SMOKE_FROM_APP }
                }
            }

            parallel {

                stage('API Smoke') {
                    agent {
                        docker {
                            image 'maven:3.9-eclipse-temurin-17'
                            args '-v /var/jenkins_home/.m2:/root/.m2'
                            reuseNode true
                        }
                    }
                    steps {
                        unstash 'source'
                        sh 'mvn -B test -Dgroups=ApiSmoke'
                    }
                }

                stage('UI Smoke') {
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
                        unstash 'source'
                        sh "curl http://selenium:4444/status"
                        sh "echo Remote = http://selenium:4444/wd/hub"
                        sh "mvn -version"
                        timeout(time: 30, unit: 'MINUTES') {
                            retry(2) {
                                sh """
                                mvn -B test \
                                -Dgroups=UISmoke \
                                -Dselenide.remote=$SELENIUM_REMOTE_URL \
                                -Dheadless=${params.HEADLESS}
                                """
                            }
                        }
                    }
                }
            }
        }

        stage('UI Regression (Midnight)') {
            when {
                triggeredBy 'TimerTrigger'
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
                unstash 'source'
                sh "curl http://selenium:4444/status"
                sh "echo Remote = http://selenium:4444/wd/hub"
                sh "mvn -version"
                timeout(time: 45, unit: 'MINUTES') {
                    retry(2) {
                        sh """
                        mvn -B test \
                        -Dgroups=regression \
                        -Dselenide.remote=$SELENIUM_REMOTE_URL
                        """
                    }
                }
            }
        }

        stage('E2E (Manual)') {
            when {
                expression { params.RUN_E2E }
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
                unstash 'source'
                sh "curl http://selenium:4444/status"
                sh "echo Remote = http://selenium:4444/wd/hub"
                sh "mvn -version"
                sh """
                mvn -B test \
                -Dgroups=e2e \
                -Dselenide.remote=$SELENIUM_REMOTE_URL
                """
            }
        }
    }

    post {
        always {
            junit 'target/surefire-reports/*.xml'
            allure includeProperties: false,
                   results: [[path: 'target/allure-results']]
            archiveArtifacts artifacts: 'target/**/*.log', allowEmptyArchive: true
            cleanWs()
        }
    }
}