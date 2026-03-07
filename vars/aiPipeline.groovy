/*
AI-Driven Jenkins Shared Library Pipeline
Analyzes repositories and generates CI/CD pipelines using AI.
*/

def call() {

    pipeline {

        agent any

        stages {

            // Clone application repository
            stage('Checkout') {
                steps {
                    git url: 'https://github.com/dkasha14/JavaSpringBoot.git', branch: 'master'
                }
            }

            // Detect application language
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

            // Install AI dependencies inside Python virtual environment
            stage('Install AI Dependencies') {
                steps {
                    sh '''
                    LIB=$(ls -d $WORKSPACE@libs/* | head -1)

                    python3 -m venv ai_venv

                    ai_venv/bin/pip install --upgrade pip

                    ai_venv/bin/pip install -r $LIB/requirements.txt
                    '''
                }
            }

            // Run AI repository analysis
            stage('AI Repository Analysis') {
                steps {

                    withCredentials([string(credentialsId: 'groq-api-key', variable: 'GROQ_API_KEY')]) {

                        sh '''
                        LIB=$(ls -d $WORKSPACE@libs/* | head -1)

                        export PYTHONPATH=$LIB

                        ai_venv/bin/python $LIB/ai_engine/repo_analyzer.py
                        '''
                    }
                }
            }

            // Generate pipeline using AI
            stage('AI Pipeline Generation') {
                steps {

                    withCredentials([string(credentialsId: 'groq-api-key', variable: 'GROQ_API_KEY')]) {

                        sh '''
                        LIB=$(ls -d $WORKSPACE@libs/* | head -1)

                        export PYTHONPATH=$LIB

                        ai_venv/bin/python $LIB/ai_engine/pipeline_generator.py
                        '''
                    }
                }
            }

            // Build application based on detected type
            stage('Build') {
                steps {

                    script {

                        if (env.APP_TYPE == "python") {

                            sh 'ai_venv/bin/pip install -r requirements.txt'

                        }

                        else if (env.APP_TYPE == "java") {

                            sh 'mvn -B -Dstyle.color=never clean package'

                        }

                        else if (env.APP_TYPE == "node") {

                            sh '''
                            npm install --silent
                            '''

                        }

                        else {

                            echo "No supported build file found"

                        }

                    }
                }
            }

            // Execute AI generated CI/CD pipeline
            stage('Execute AI Generated Pipeline') {
                steps {
                    sh '''
                    #!/bin/bash

                    ai_venv/bin/pip install pytest

                    export PATH="$WORKSPACE/ai_venv/bin:$PATH"

                    if [ -f generated_pipeline.sh ]; then

                        chmod +x generated_pipeline.sh

                        ./generated_pipeline.sh 2>&1 | tee failure.log

                        EXIT_CODE=${PIPESTATUS[0]}

                        if [ "$EXIT_CODE" = "5" ]; then
                            echo "No tests found — continuing pipeline"
                            exit 0
                        fi

                        exit $EXIT_CODE

                    else

                        echo "No generated pipeline found"
                        exit 1

                    fi
                    '''
                }
            }

            // Placeholder test stage
            stage('Test') {
                steps {
                    echo "Running tests..."
                }
            }

            // Placeholder deployment stage
            stage('Deploy') {
                steps {
                    echo "Deploy stage..."
                }
            }

        }

        // Run AI failure analyzer
        post {

            failure {

                echo "Pipeline failed. Running AI failure analysis."

                withCredentials([string(credentialsId: 'groq-api-key', variable: 'GROQ_API_KEY')]) {

                    sh '''
                    LIB=$(ls -d $WORKSPACE@libs/* | head -1)

                    export PYTHONPATH=$LIB

                    if [ -f failure.log ]; then
                        ai_venv/bin/python $LIB/ai_engine/log_analyzer.py failure.log
                    else
                        echo "failure.log not found"
                    fi
                    '''

                }

            }

        }

    }

}