from llm_client import ask_llm

import os
def load_analysis():
    """
    Load repository analysis produced by repo_analyzer.py
    """

    with open("analysis.json", "r") as f:
        data = f.read()

    return data


def generate_pipeline_with_ai(analysis):
    """
    Ask the LLM to generate CI/CD pipeline commands
    """

    prompt = f"""
You are a Senior AI DevOps Platform Engineer.

Your job is to design a CI/CD pipeline based on repository analysis.

Repository analysis:
{analysis}

Determine the appropriate pipeline stages and return ONLY the shell commands
required to execute the pipeline.

Possible pipeline capabilities include:

• dependency installation
• application build
• automated testing
• code quality analysis (SonarQube)
• containerization (Docker build)
• container registry push
• infrastructure provisioning (Terraform)
• application deployment (Kubernetes / Helm)

Rules:

1. Return ONLY executable shell commands.
2. Do NOT include explanations.
3. Do NOT include markdown formatting.
4. Only include stages relevant to the repository.
5. Commands must be executable in a Linux CI/CD runner.

Example output:

pip install -r requirements.txt
python -m unittest discover -s tests -p 'test_*.py'
sonar-scanner
docker build -t app:latest .
docker push registry/app:latest
kubectl apply -f deployment.yaml
"""

    response = ask_llm(prompt)

    return response


import os

def clean_commands(commands):

    commands = commands.replace("```bash", "")
    commands = commands.replace("```", "")

    lines = commands.splitlines()
    valid_commands = []

    for line in lines:
        line = line.strip()

        if not line:
            continue

        line = line.lstrip(".")

        # Skip sonar if scanner not installed
        if "sonar-scanner" in line and not os.system("which sonar-scanner > /dev/null 2>&1") == 0:
            continue

               # Skip kubectl if not installed
        if "kubectl" in line and not os.system("which kubectl > /dev/null 2>&1") == 0:
            continue
                # Skip docker build if Dockerfile missing
        if "docker build" in line and not os.path.exists("Dockerfile"):
            continue

        # Skip docker push if Dockerfile missing
        if "docker push" in line and not os.path.exists("Dockerfile"):
            continue
                # Force tests directory instead of scanning entire repo
        if "python -m unittest discover" in line:
            line = "python -m unittest discover -s tests -p 'test_*.py'"
                # Skip docker tag if Dockerfile missing
        if "docker tag" in line and not os.path.exists("Dockerfile"):
            continue
        valid_commands.append(line)

    return "\n".join(valid_commands)

def save_pipeline(commands):
    """
    Save validated pipeline commands to generated script
    """

    cleaned = clean_commands(commands)

    with open("generated_pipeline.sh", "w", encoding="utf-8") as f:

        f.write("#!/bin/bash\n\n")
        f.write(cleaned + "\n")


if __name__ == "__main__":

    print("Generating pipeline using AI...")

    analysis = load_analysis()

    pipeline_commands = generate_pipeline_with_ai(analysis)

    save_pipeline(pipeline_commands)

    print("Pipeline generated successfully.")
    print(clean_commands(pipeline_commands))