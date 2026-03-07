/*
AI-Driven Jenkins Shared Library Pipeline
- Optimized for Ubuntu (PEP 668 compliant)
- Clean Maven logging
- Automated AI failure analysis integration
*/

def call() {
    pipeline {
        agent any

        stages {
            // 1. Clone application repository
            stage('Checkout') {
                steps {
                    git url: 'https://github.com/dkasha14/JavaSpringBoot.git', branch: 'master'
                }
            }

            // 2. Detect application technology stack
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

            // 3. Setup isolated environment for AI tools
            stage('Install AI Dependencies') {
                steps {
                    sh '''
                    # Locate Shared Library path
                    LIB=$(ls -d $WORKSPACE@libs/* | head -1)
                    
                    # Create venv to bypass Ubuntu "externally-managed-environment" error
                    python3 -m venv ai_venv
                    ai_venv/bin/pip install --upgrade pip
                    ai_venv/bin/pip install -r $LIB/requirements.txt
                    '''
                }
            }

            // 4. AI-Powered Repository Analysis
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

            // 5. AI-Powered Pipeline Generation
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

            // 6. Build stage with optimized logging
            stage('Build') {
                steps {
                    script {
                        if (env.APP_TYPE == "python") {
                            sh 'ai_venv/bin/pip install -r requirements.txt'
                        } else if (env.APP_TYPE == "java") {
                            // Clean logs: Batch mode + no ANSI colors
                            sh 'mvn -B -Dstyle.color=never clean package'
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

            // 7. Execute generated script with error handling
            stage('Execute AI Generated Pipeline') {
                steps {
                    sh '''
                    # Ensure test tools are in the venv
                    ai_venv/bin/pip install pytest bandit
                    export PATH="$WORKSPACE/ai_venv/bin:$PATH"

                    if [ -f generated_pipeline.sh ]; then
                        chmod +x generated_pipeline.sh
                        
                        # Security Scan (ignoring failures so pipeline continues)
                        bandit -r . -x ai_venv,venv,.venv -ll || true

                        # Run pipeline & capture logs for AI analyzer
                        # PIPESTATUS[0] captures the script exit code, not 'tee'
                        ./generated_pipeline.sh 2>&1 | tee failure.log
                        EXIT_CODE=${PIPESTATUS[0]}

                        # Exit Code 5 = No tests found (Pytest). We treat this as success.
                        if [ "$EXIT_CODE" = "5" ]; then
                            echo "No tests collected, but build succeeded."
                            exit 0
                        fi

                        exit $EXIT_CODE
                    else
                        echo "Error: generated_pipeline.sh not found."
                        exit 1
                    fi
                    '''
                }
            }
        }

        // 8. Auto-Analysis on failure
        post {
            failure {
                echo "Pipeline failed. Starting AI diagnostics..."
                withCredentials([string(credentialsId: 'groq-api-key', variable: 'GROQ_API_KEY')]) {
                    sh '''
                    LIB=$(ls -d $WORKSPACE@libs/* | head -1)
                    export PYTHONPATH=$LIB
                    if [ -f failure.log ]; then
                        ai_venv/bin/python $LIB/ai_engine/log_analyzer.py failure.log
                    else
                        echo "Failure analysis skipped: failure.log not found"
                    fi
                    '''
                }
            }
        }
    }
}