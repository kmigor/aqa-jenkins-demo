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
        cron('''
            H */2 * * *
            H 0 * * *
        ''')
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
                        sh 'mvn -B test -Dgroups=api,smoke'
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
                        timeout(time: 30, unit: 'MINUTES') {
                            retry(2) {
                                sh """
                                mvn -B test \
                                -Dgroups=ui,smoke \
                                -Dselenide.remote=http://selenium:4444/wd/hub \
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
                allOf {
                    triggeredBy 'TimerTrigger'
                    expression {
                        def hour = new Date().format("H", TimeZone.getTimeZone('UTC'))
                        hour == "0"
                    }
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
                unstash 'source'
                timeout(time: 45, unit: 'MINUTES') {
                    retry(2) {
                        sh """
                        mvn -B test \
                        -Dgroups=ui,regression \
                        -Dselenide.remote=http://selenium:4444/wd/hub
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
                sh """
                mvn -B test \
                -Dgroups=e2e \
                -Dselenide.remote=http://selenium:4444/wd/hub
                """
            }
        }
    }

    post {
        always {
            junit 'target/surefire-reports/*.xml'

            allure includeProperties: false,
                                       results: [[path: 'target/allure-results']]

            archiveArtifacts artifacts: 'target/*.log', fingerprint: true

            cleanWs()
        }
    }
}