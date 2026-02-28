pipeline {
    agent none

    tools {
        allure 'Allure'
    }

    stages {

        stage('Checkout') {
            agent any
            steps {
                checkout scm
            }
        }

        stage('Build') {
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

        stage('Run API Tests') {
            agent {
                docker {
                    image 'maven:3.9-eclipse-temurin-17'
                    args '-v /var/jenkins_home/.m2:/root/.m2'
                    reuseNode true
                }
            }
            steps {
                sh 'mvn -Dtest=ApiTest test'
            }
        }

        stage('Run UI Tests') {
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
                sh 'mvn -Dtest=UITest test'
            }
        }

        stage('Allure Report') {
            agent any
            steps {
                allure includeProperties: false, results: [[path: 'target/allure-results']]
            }
        }
    }
}