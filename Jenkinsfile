pipeline {
    agent {
        docker {
            image 'maven:3.9-eclipse-temurin-17'
            args '--network jenkins-docker_default -v /var/jenkins_home/.m2:/root/.m2'
        }
    }

    tools {
        allure 'Allure'
    }

    parameters {
        choice(name: 'RUN_TYPE',
               choices: ['pr', 'nightly', 'manual'])
        booleanParam(name: 'HEADLESS', defaultValue: true)
    }

    triggers {
        cron('H 2 * * *')
    }

    stages {

        stage('Checkout') {
            steps { checkout scm }
        }

        stage('Build') {
            steps { sh 'mvn clean install -DskipTests' }
        }

        stage('PR Tests') {
            when { expression { params.RUN_TYPE == 'pr' } }
            parallel {

                stage('API Smoke') {
                    steps { sh 'mvn test -Dgroups=api,smoke' }
                }

                stage('UI Smoke') {
                    environment {
                        SELENIUM_REMOTE_URL = 'http://selenium:4444/wd/hub'
                    }
                    steps {
                        sh "mvn test -Dgroups=ui,smoke -Dheadless=${params.HEADLESS}"
                    }
                }
            }
        }

        stage('Nightly UI Regression') {
            when { triggeredBy 'TimerTrigger' }
            environment {
                SELENIUM_REMOTE_URL = 'http://selenium:4444/wd/hub'
            }
            steps {
                sh "mvn test -Dgroups=ui,regression"
            }
        }

        stage('Manual E2E') {
            when { expression { params.RUN_TYPE == 'manual' } }
            environment {
                SELENIUM_REMOTE_URL = 'http://selenium:4444/wd/hub'
            }
            steps {
                sh "mvn test -Dgroups=e2e"
            }
        }

        stage('Allure Report') {
            steps {
                allure includeProperties: false,
                       results: [[path: 'target/allure-results']]
            }
        }
    }

    post {
        always {
            junit 'target/surefire-reports/*.xml'
        }
    }
}