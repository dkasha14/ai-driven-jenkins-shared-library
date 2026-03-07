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

            // Install AI dependencies in virtual environment
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

            // Run repository analysis using AI engine
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

            // Generate dynamic CI/CD pipeline
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

            // Build application
            stage('Build') {
                steps {

                    script {

                        if (env.APP_TYPE == "python") {
                            sh 'ai_venv/bin/pip install -r requirements.txt'
                        }

                        else if (env.APP_TYPE == "java") {
                            sh 'mvn clean package'
                        }

                        else if (env.APP_TYPE == "node") {
                            sh '''
                            npm install --silent
                            npm audit --audit-level=high --json > audit-report.json || true
                            '''
                        }

                        else {
                            echo "No supported build file found"
                        }

                    }
                }
            }

            // Execute AI generated pipeline
            stage('Execute AI Generated Pipeline') {
                steps {
                    sh '''

                    ai_venv/bin/pip install pytest bandit

                    if [ -f generated_pipeline.sh ]; then

                        chmod +x generated_pipeline.sh

                        export PATH=$WORKSPACE/ai_venv/bin:$PATH

                        ./generated_pipeline.sh

                    else
                        echo "No generated pipeline found"
                        exit 1
                    fi
                    '''
                }
            }

            // Test placeholder stage
            stage('Test') {
                steps {
                    echo "Running tests..."
                }
            }

            // Deployment placeholder stage
            stage('Deploy') {
                steps {
                    echo "Deploy stage..."
                }
            }

        }

        // Run AI log analysis if pipeline fails
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