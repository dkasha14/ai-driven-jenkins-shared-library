stage('Setup Python Environment') {
    steps {
        sh '''
        python3 -m venv venv
        . venv/bin/activate
        venv/bin/pip install --upgrade pip
        venv/bin/python ai_engine/repo_analyzer.py
        '''
    }
}