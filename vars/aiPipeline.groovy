/*
AI-Driven Jenkins Shared Library Pipeline that analyzes repositories, generates CI/CD pipelines using AI, and executes them automatically.
*/

def call() {

    pipeline {

        // Run the pipeline on any available Jenkins build agent
        agent any

        stages {

            // Clone the target application repository that will be analyzed and built
            stage('Checkout') {
                steps {
                    git url: 'https://github.com/dkasha14/JavaSpringBoot.git', branch: 'master'
                }
            }

            // Detect the application technology stack by checking common build files
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

            // Create Python virtual environment and install AI engine dependencies required for repository analysis
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

            // Run the AI repository analyzer to detect languages, frameworks, and infrastructure components
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

            // Generate a dynamic CI/CD pipeline script using the LLM based on repository analysis results
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

            // Build the application using the appropriate build tool depending on detected project type
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

            // Execute the AI-generated CI/CD pipeline while ensuring required test tools are available
            stage('Execute AI Generated Pipeline') {
                steps {
                    sh '''

                    pip install pytest bandit || true

                    if [ -f generated_pipeline.sh ]; then

                        chmod +x generated_pipeline.sh

                        export PATH=$WORKSPACE/ai_venv/bin:$PATH

                        ./generated_pipeline.sh

                    else
                        echo "No generated pipeline found"
                    fi
                    '''
                }
            }

            // Placeholder stage where project specific automated tests can be executed
            stage('Test') {
                steps {
                    echo "Running tests..."
                }
            }

            // Placeholder stage for deployment actions such as Kubernetes or infrastructure provisioning
            stage('Deploy') {
                steps {
                    echo "Deploy stage..."
                }
            }

        }

        // Run AI log analysis after pipeline failure to help diagnose root cause automatically
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