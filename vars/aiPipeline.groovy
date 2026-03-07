/*
AI-Driven Jenkins Shared Library Pipeline 
Optimized for Ubuntu/Debian (PEP 668 Compliant)
*/

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
                        } else if (fileExists('pom.xml')) {
                            env.APP_TYPE = "java"
                        } else if (fileExists('package.json')) {
                            env.APP_TYPE = "node"
                        } else {
                            env.APP_TYPE = "unknown"
                        }
                        echo "Detected application type: ${env.APP_TYPE}"
                    }
                }
            }

            stage('Install AI Dependencies') {
                steps {
                    sh '''
                    # Locate the Shared Library path
                    LIB=$(ls -d $WORKSPACE@libs/* | head -1)

                    # Create virtual environment if it doesn't exist
                    python3 -m venv ai_venv
                    
                    # Use the VENV's pip directly to avoid 'externally-managed-environment' errors
                    ai_venv/bin/pip install --upgrade pip
                    ai_venv/bin/pip install -r $LIB/requirements.txt
                    '''
                }
            }

            stage('AI Repository Analysis') {
                steps {
                    withCredentials([string(credentialsId: 'groq-api-key', variable: 'GROQ_API_KEY')]) {
                        sh '''
                        LIB=$(ls -d $WORKSPACE@libs/* | head -1)
                        export PYTHONPATH=$LIB
                        # Use venv python
                        ai_venv/bin/python $LIB/ai_engine/repo_analyzer.py
                        '''
                    }
                }
            }

            stage('AI Pipeline Generation') {
                steps {
                    withCredentials([string(credentialsId: 'groq-api-key', variable: 'GROQ_API_KEY')]) {
                        sh '''
                        LIB=$(ls -d $WORKSPACE@libs/* | head -1)
                        export PYTHONPATH=$LIB
                        # Use venv python
                        ai_venv/bin/python $LIB/ai_engine/pipeline_generator.py
                        '''
                    }
                }
            }

            stage('Build') {
                steps {
                    script {
                        if (env.APP_TYPE == "python") {
                            // Ubuntu Fix: Install app requirements into the same venv
                            sh 'ai_venv/bin/pip install -r requirements.txt'
                        } else if (env.APP_TYPE == "java") {
                            sh 'mvn clean package'
                        } else if (env.APP_TYPE == "node") {
                            sh '''
                            npm install --silent
                            npm audit --audit-level=high --json > audit-report.json || true
                            '''
                        } else {
                            echo "No supported build file found"
                        }
                    }
                }
            }

            stage('Execute AI Generated Pipeline') {
                steps {
                    sh '''
                    # Ubuntu Fix: Install test tools into the venv, not the system
                    ai_venv/bin/pip install pytest bandit

                    if [ -f generated_pipeline.sh ]; then
                        chmod +x generated_pipeline.sh
                        
                        # Add venv to PATH so 'pytest' command is found inside the script
                        export PATH="$WORKSPACE/ai_venv/bin:$PATH"
                        
                        ./generated_pipeline.sh
                    else
                        echo "No generated pipeline found"
                        exit 1
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