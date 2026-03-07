def call() {

    pipeline {

        agent any

        stages {

            stage('Checkout Repository') {
                steps {
                    echo "Checking out source code"
                    checkout scm
                }
            }

            stage('AI Repository Analysis') {
                steps {
                    echo "Running AI repository analyzer"
                    sh '.venv/bin/python ai_engine/repo_analyzer.py'
                }
            }

            stage('Generate Pipeline Plan') {
                steps {
                    echo "Generating pipeline plan using AI"
                    sh '.venv/bin/python ai_engine/pipeline_generator.py'
                }
            }

            stage('Execute Generated Pipeline') {
                steps {
                    echo "Executing generated pipeline steps"
                    sh 'bash generated_pipeline.sh'
                }
            }

        }

    }

}
