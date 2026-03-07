def call() {

    pipeline {

        agent any

        stages {

            stage('Checkout') {
                steps {
                    // git url: 'https://github.com/dkasha14/JavaSpringBoot.git', branch: 'master'
                    git url: 'https://github.com/dkasha14/nodeJsApplication.git', branch: 'master'
                }
            }

            stage('Detect Application Type') {
                steps {
                    script {

                        if (fileExists('requirements.txt')) {
                            env.APP_TYPE = "python"
                        } 
                        else if (fileExists('pom.xml')) {
                            env.APP_TYPE = "java"
                        } 
                        else if (fileExists('package.json')) {
                            env.APP_TYPE = "node"
                        } 
                        else {
                            env.APP_TYPE = "unknown"
                        }

                        echo "Detected application type: ${env.APP_TYPE}"

                    }
                }
            }

            stage('Build') {
                steps {
                    script {

                        if (env.APP_TYPE == "python") {
                            sh 'pip3 install -r requirements.txt'
                        }

                        if (env.APP_TYPE == "java") {
                            sh 'mvn clean package'
                        }

                        if (env.APP_TYPE == "node") {
                            sh 'npm install'
                        }

                        if (env.APP_TYPE == "unknown") {
                            echo "No supported build file found"
                        }

                    }
                }
            }

            stage('Test') {
                steps {
                    echo "Running tests..."
                }
            }

            stage('Deploy') {
                steps {
                    echo "Deploy stage..."
                }
            }

        }
    }
}