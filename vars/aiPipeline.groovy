def call() {

    pipeline {

        agent any

        stages {

            stage('Checkout') {
                steps {
                    git url: 'https://github.com/dkasha14/JavaSpringBoot.git', branch: 'master'
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

            stage('Install AI Dependencies') {
                steps {
                    script {

                        sh '''
                        LIB=$(ls -d $WORKSPACE@libs/* | head -1)

                        python3 -m venv ai_venv

                        ai_venv/bin/pip install --upgrade pip
                        ai_venv/bin/pip install -r $LIB/requirements.txt
                        '''
                    }
                }
            }

            stage('AI Repository Analysis') {
                steps {
                    script {

                        sh '''
                        LIB=$(ls -d $WORKSPACE@libs/* | head -1)

                        export PYTHONPATH=$LIB

                        ai_venv/bin/python $LIB/ai_engine/repo_analyzer.py
                        '''
                    }
                }
            }

            stage('AI Pipeline Generation') {
                steps {
                    script {

                        sh '''
                        LIB=$(ls -d $WORKSPACE@libs/* | head -1)

                        export PYTHONPATH=$LIB

                        ai_venv/bin/python $LIB/ai_engine/pipeline_generator.py
                        '''
                    }
                }
            }

            stage('Build') {
                steps {
                    script {

                        if (env.APP_TYPE == "python") {

                            sh '''
                            ai_venv/bin/pip install -r requirements.txt
                            '''

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

                sh '''
                LIB=$(ls -d $WORKSPACE@libs/* | head -1)

                export PYTHONPATH=$LIB

                ai_venv/bin/python $LIB/ai_engine/log_analyzer.py failure.log || true
                '''

            }

        }

    }

}