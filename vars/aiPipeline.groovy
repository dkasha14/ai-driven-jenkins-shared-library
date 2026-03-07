def call() {

    pipeline {

        agent any

        stages {

            stage('Checkout') {
                steps {
                    checkout scm
                }
            }

            stage('AI Repository Analysis') {
                steps {
                    sh '''
                    python ai_engine/repo_analyzer.py
                    '''
                }
            }

            stage('AI Pipeline Generation') {
                steps {
                    sh '''
                    python -m ai_engine.pipeline_generator
                    '''
                }
            }

            stage('Execute Generated Pipeline') {
                steps {
                    sh '''
                    chmod +x generated_pipeline.sh
                    ./generated_pipeline.sh
                    '''
                }
            }

        }

    }

}