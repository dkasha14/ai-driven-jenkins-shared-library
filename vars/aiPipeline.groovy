/*
-------------------------------------------------------------
AI DRIVEN JENKINS SHARED LIBRARY PIPELINE
-------------------------------------------------------------

This pipeline performs the following high level workflow:

1. Clone the application repository
2. Detect application type automatically
3. Install AI engine dependencies
4. Run AI repository analysis
5. Generate CI/CD pipeline using LLM
6. Build the application
7. Execute the AI generated pipeline
8. Handle failures using AI log analysis

-------------------------------------------------------------
*/

def call() {

    pipeline {

        /* 
        -----------------------------------------------------
        Run pipeline on any available Jenkins agent
        -----------------------------------------------------
        */
        agent any

        stages {

            /*
            -----------------------------------------------------
            Stage 1 : Checkout Application Source Code
            -----------------------------------------------------
            This stage clones the target repository that the AI
            system will analyze and build.
            -----------------------------------------------------
            */
            stage('Checkout') {
                steps {
                    git url: 'https://github.com/dkasha14/JavaSpringBoot.git', branch: 'master'
                }
            }

            /*
            -----------------------------------------------------
            Stage 2 : Detect Application Type Automatically
            -----------------------------------------------------
            The pipeline checks for common build files to detect
            the programming language and ecosystem.

            Python  -> requirements.txt
            Java    -> pom.xml
            NodeJS  -> package.json
            -----------------------------------------------------
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
            -----------------------------------------------------
            Stage 3 : Install AI Engine Dependencies
            -----------------------------------------------------
            The AI engine requires Python dependencies such as:

            - Groq SDK
            - LangChain
            - LangGraph
            - Requests
            - Python dotenv

            A virtual environment is created to avoid conflicts
            with system Python (PEP 668 restriction).
            -----------------------------------------------------
            */
            stage('Install AI Dependencies') {
                steps {
                    sh '''
                    # Locate the shared library directory inside Jenkins workspace
                    LIB=$(ls -d $WORKSPACE@libs/* | head -1)

                    # Create isolated Python environment
                    python3 -m venv ai_venv

                    # Upgrade pip inside the virtual environment
                    ai_venv/bin/pip install --upgrade pip

                    # Install required AI dependencies
                    ai_venv/bin/pip install -r $LIB/requirements.txt
                    '''
                }
            }

            /*
            -----------------------------------------------------
            Stage 4 : AI Repository Analysis
            -----------------------------------------------------
            This stage runs the AI analyzer which scans the
            repository to detect:

            - programming language
            - frameworks
            - docker usage
            - terraform usage
            - kubernetes usage
            - CI/CD requirements

            Groq LLM API key is securely injected from Jenkins
            credentials.
            -----------------------------------------------------
            */
            stage('AI Repository Analysis') {
                steps {

                    withCredentials([string(credentialsId: 'groq-api-key', variable: 'GROQ_API_KEY')]) {

                        sh '''
                        LIB=$(ls -d $WORKSPACE@libs/* | head -1)

                        # Allow Python to locate shared library modules
                        export PYTHONPATH=$LIB

                        # Run repository analyzer
                        ai_venv/bin/python $LIB/ai_engine/repo_analyzer.py
                        '''

                    }
                }
            }

            /*
            -----------------------------------------------------
            Stage 5 : AI Pipeline Generation
            -----------------------------------------------------
            The AI engine sends repository metadata to the LLM
            and dynamically generates a CI/CD pipeline script.

            Output file generated:

            generated_pipeline.sh
            -----------------------------------------------------
            */
            stage('AI Pipeline Generation') {
                steps {

                    withCredentials([string(credentialsId: 'groq-api-key', variable: 'GROQ_API_KEY')]) {

                        sh '''
                        LIB=$(ls -d $WORKSPACE@libs/* | head -1)

                        export PYTHONPATH=$LIB

                        # Generate CI/CD pipeline using AI
                        ai_venv/bin/python $LIB/ai_engine/pipeline_generator.py
                        '''

                    }
                }
            }

            /*
            -----------------------------------------------------
            Stage 6 : Build Application
            -----------------------------------------------------
            Based on detected application type, the correct
            build command is executed.

            Python -> pip install dependencies
            Java   -> Maven build
            Node   -> npm install + security audit
            -----------------------------------------------------
            */
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

            /*
            -----------------------------------------------------
            Stage 7 : Execute AI Generated Pipeline
            -----------------------------------------------------
            The generated script may include additional DevOps
            steps such as:

            - Docker build
            - Docker push
            - Kubernetes deployment
            - Security scanning

            The virtual environment PATH is injected to prevent
            PEP 668 Python installation issues.
            -----------------------------------------------------
            */
            stage('Execute AI Generated Pipeline') {
                steps {
                    sh '''
                    if [ -f generated_pipeline.sh ]; then

                        chmod +x generated_pipeline.sh

                        # Ensure generated script uses the virtual environment Python
                        export PATH=$WORKSPACE/ai_venv/bin:$PATH

                        ./generated_pipeline.sh

                    else
                        echo "No generated pipeline found"
                    fi
                    '''
                }
            }

            /*
            -----------------------------------------------------
            Stage 8 : Test Stage
            -----------------------------------------------------
            Placeholder stage where test execution could be added
            depending on project type.
            -----------------------------------------------------
            */
            stage('Test') {
                steps {
                    echo "Running tests..."
                }
            }

            /*
            -----------------------------------------------------
            Stage 9 : Deployment Stage
            -----------------------------------------------------
            This stage would typically perform:

            - Kubernetes deployment
            - Helm release
            - Infrastructure provisioning
            -----------------------------------------------------
            */
            stage('Deploy') {
                steps {
                    echo "Deploy stage..."
                }
            }

        }

        /*
        -----------------------------------------------------
        Post Actions : AI Failure Analysis
        -----------------------------------------------------

        If the pipeline fails, the AI log analyzer will attempt
        to detect root cause and suggest fixes automatically.

        Input:
        failure.log

        Output:
        Root cause + recommended solution
        -----------------------------------------------------
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