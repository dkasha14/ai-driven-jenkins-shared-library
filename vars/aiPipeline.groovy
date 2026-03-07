def call() {

    pipeline {

        agent any

        environment {
            LIB_PATH = "${WORKSPACE}@libs"
        }

        stages {

            stage('Checkout') {
                steps {

                    // Java example repository
                    git url: 'https://github.com/dkasha14/JavaSpringBoot.git', branch: 'master'

                    // Node example
                    // git url: 'https://github.com/dkasha14/nodeJsApplication.git', branch: 'master'
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

            stage('AI Repository Analysis') {
                steps {

                    script {

                        sh """
                        python3 ${LIB_PATH}/*/ai_engine/repo_analyzer.py
                        """
                    }
                }
            }

            stage('AI Pipeline Generation') {
                steps {

                    script {

                        sh """
                        python3 ${LIB_PATH}/*/ai_engine/pipeline_generator.py
                        """
                    }
                }
            }

            stage('Build') {
                steps {

                    script {

                        if (env.APP_TYPE == "python") {

                            sh 'pip3 install -r requirements.txt'
                        }

                        else if (env.APP_TYPE == "java") {

                            sh 'mvn clean package'
                        }

                        else if (env.APP_TYPE == "node") {

                            sh '''
                            npm install --silent
                            npm audit --audit-level=high --json > audit-report.json || true
                            '''

                            echo "Node dependency security scan completed"
                        }

                        else {

                            echo "No supported build file found"
                        }

                    }
                }
            }

            stage('Execute AI Generated Pipeline') {
                steps {

                    script {

                        sh '''
                        if [ -f generated_pipeline.sh ]; then
                            chmod +x generated_pipeline.sh
                            ./generated_pipeline.sh
                        else
                            echo "No generated pipeline found"
                        fi
                        '''
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

        post {

            failure {

                echo "Pipeline failed. Running AI failure analysis."

                sh """
                python3 ${LIB_PATH}/*/ai_engine/log_analyzer.py failure.log || true
                """
            }
        }
    }
}