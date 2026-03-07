/*
AI-Driven Jenkins Shared Library Pipeline
Analyzes repositories and generates CI/CD pipelines using AI.
*/

def call() {

    pipeline {

        agent any

        stages {

            /*
            ------------------------------------------------
            Checkout Application Repository
            ------------------------------------------------
            */
            stage('Checkout') {
                steps {
                    git url: 'https://github.com/dkasha14/JavaSpringBoot.git', branch: 'master'
                }
            }

            /*
            ------------------------------------------------
            Detect Application Type
            ------------------------------------------------
            */
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

            /*
            ------------------------------------------------
            Install AI Engine Dependencies
            ------------------------------------------------
            */
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

            /*
            ------------------------------------------------
            Run AI Repository Analyzer
            ------------------------------------------------
            */
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

            /*
            ------------------------------------------------
            Generate Pipeline using AI
            ------------------------------------------------
            */
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

            /*
            ------------------------------------------------
            Build Application
            ------------------------------------------------
            */
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

                            echo "No supported build configuration found"

                        }

                    }
                }
            }

            /*
            ------------------------------------------------
            Execute AI Generated Pipeline
            ------------------------------------------------
            */
            stage('Execute AI Generated Pipeline') {
                steps {

                    sh '''

                    set +e

                    ai_venv/bin/pip install pytest

                    export PATH="$WORKSPACE/ai_venv/bin:$PATH"

                    if [ -f generated_pipeline.sh ]; then

                        chmod +x generated_pipeline.sh

                        ./generated_pipeline.sh 2>&1 | tee failure.log

                        EXIT_CODE=$?

                        if [ "$EXIT_CODE" -eq 5 ]; then
                            echo "No tests found. Continuing pipeline."
                            exit 0
                        fi

                        exit $EXIT_CODE

                    else

                        echo "generated_pipeline.sh not found"
                        exit 1

                    fi
                    '''
                }
            }

            /*
            ------------------------------------------------
            Test Stage (Placeholder)
            ------------------------------------------------
            */
            stage('Test') {
                steps {
                    echo "Test stage placeholder"
                }
            }

            /*
            ------------------------------------------------
            Deploy Stage (Placeholder)
            ------------------------------------------------
            */
            stage('Deploy') {
                steps {
                    echo "Deploy stage placeholder"
                }
            }

        }

        /*
        ------------------------------------------------
        AI Failure Analysis
        ------------------------------------------------
        */
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