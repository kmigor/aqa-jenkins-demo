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
        choice(name: 'TEST_SCOPE', choices: ['all','api','ui','smoke'])
        booleanParam(name: 'HEADLESS', defaultValue: true)
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Clean') {
            steps {
                sh 'mvn clean'
            }
        }

        stage('Tests') {
            parallel {

                stage('API') {
                    steps {
                        sh "mvn test -Dgroups=api"
                    }
                }

                stage('UI') {
                    environment {
                        SELENIUM_REMOTE_URL = 'http://selenium:4444/wd/hub'
                    }
                    steps {
                        sh "mvn test -Dgroups=ui -Dheadless=${params.HEADLESS}"
                    }
                }
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
             archiveArtifacts artifacts: 'target/**/*.log', fingerprint: true, allowEmptyArchive: true
        }
    }
}