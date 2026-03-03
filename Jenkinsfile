pipeline {

    agent none

    tools {
        allure 'Allure'
    }

    parameters {
        booleanParam(name: 'RUN_E2E', defaultValue: false, description: 'Run E2E manually')
        booleanParam(name: 'HEADLESS', defaultValue: true, description: 'Headless mode for UI')
    }

    triggers {
        cron('H 2 * * *') // Nightly UI regression
    }

    stages {

        stage('Checkout') {
            agent any
            steps {
                dir('fakeApp') {
                    git url: 'https://github.com/kmigor/fakeApp.git', branch: 'main'
                }
                dir('aqa') {
                    checkout scm
                }
                stash name: 'source', includes: '/*'
            }
        }

        stage('Build App') {
            agent {
                docker {
                    image 'maven:3.9-eclipse-temurin-17'
                    args '-v /var/jenkins_home/.m2:/root/.m2'
                    reuseNode true
                }
            }
            steps {
                unstash 'source'
                dir('fakeApp') {
                    sh 'mvn clean package -DskipTests'
                }
                stash name: 'app-build', includes: 'fakeApp/target/'
            }
        }

        stage('Unit Tests') {
            agent {
                docker {
                    image 'maven:3.9-eclipse-temurin-17'
                    args '-v /var/jenkins_home/.m2:/root/.m2'
                    reuseNode true
                }
            }
            steps {
                unstash 'source'
                unstash 'app-build'
                dir('fakeApp') {
                    sh 'mvn test'
                }
            }
        }

        stage('PR Tests') {
            when { changeRequest() }
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
                        dir('aqa') {
                            sh 'mvn test -Dgroups=api,smoke'
                        }
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
                        timeout(time: 30, unit: 'MINUTES') {
                            retry(2) {
                                dir('aqa') {
                                    sh "mvn test -Dgroups=ui,smoke -Dheadless=${params.HEADLESS}"
                                }
                            }
                        }
                    }
                }
            }
        }
        stage('UI Regression (Nightly)') {
                    when { triggeredBy 'TimerTrigger' }
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
                        timeout(time: 30, unit: 'MINUTES') {
                            retry(2) {
                                dir('aqa') {
                                    sh 'mvn test -Dgroups=ui,regression'
                                }
                            }
                        }
                    }
                }

                stage('E2E (Manual)') {
                    when {
                        allOf {
                            expression { params.RUN_E2E }
                            not { triggeredBy 'TimerTrigger' }
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
                        dir('aqa') {
                            sh 'mvn test -Dgroups=e2e'
                        }
                    }
                }
            }

            post {
                always {

                    junit 'fakeApp/target/surefire-reports/*.xml'
                    junit 'aqa/target/surefire-reports/*.xml'

                    allure includeProperties: false,
                           results: [[path: 'aqa/target/allure-results']]

                    archiveArtifacts artifacts: '/target//*.log', fingerprint: true

                    cleanWs()
                }
            }
        }