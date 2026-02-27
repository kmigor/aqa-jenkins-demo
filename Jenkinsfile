pipeline {
    agent none
    tools {
        maven 'Maven'
    }
    stages {

        stage('Checkout') {
            agent any
            steps {
                checkout scm
            }
        }
        stage('Run API Tests') {
            agent any
            steps {
                sh 'mvn -Dtest=ApiTest clean test'
            }
        }
        stage('Run UI Tests') {
            agent {
                docker {
                    image 'selenium/standalone-chrome:latest'
                    args '-v /var/jenkins_home/.m2:/root/.m2'
                    reuseNode true
                }
            }
            steps {
                sh 'mvn -Dtest=UITest clean test'
            }
        }
        stage('Allure Report') {
            agent any
            steps {
                allure includeProperties: false, jdk: '', results: [[path: 'target/allure-results']]
            }
        }
    }
}