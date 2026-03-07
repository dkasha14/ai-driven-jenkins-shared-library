import json
import logging

# Import LLM client to communicate with Groq AI model
from ai_engine.llm_client import ask_llm

# Import pipeline decision engine
from ai_engine.pipeline_brain import decide_pipeline


# Configure logging format for pipeline generation process
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(levelname)s - %(message)s"
)


def clean_commands(commands: str) -> str:
    # Remove markdown formatting returned by LLM to keep only executable commands
    commands = commands.replace("```bash", "")
    commands = commands.replace("```", "")
    return commands.strip()


def load_analysis() -> dict:
    # Load repository analysis results produced by repo_analyzer
    try:
        with open("analysis.json", "r") as f:
            return json.load(f)
    except FileNotFoundError:
        logging.error("analysis.json not found. Run repo_analyzer first.")
        raise


def select_valid_stages(analysis: dict, stages: list) -> list:
    # Filter pipeline stages based on detected technologies in the repository

    valid_stages = []

    language = analysis.get("Language", "").lower()

    for stage in stages:

        # Allow Python dependency installation only for Python repositories
        if stage == "install_dependencies" and "python" not in language:
            continue

        # Allow Maven build only for Java repositories
        if stage == "maven_build" and "java" not in language:
            continue

        # Allow docker steps only if docker is detected
        if stage in ["docker_build", "docker_push"] and not analysis.get("docker"):
            continue

        # Allow Kubernetes deployment only if Kubernetes is detected
        if stage == "kubernetes_deploy" and not analysis.get("kubernetes"):
            continue

        # Allow Terraform only if Terraform is detected
        if stage == "terraform_apply" and not analysis.get("terraform"):
            continue

        # Allow Ansible only if Ansible is detected
        if stage == "ansible_deploy" and not analysis.get("ansible"):
            continue

        valid_stages.append(stage)

    # Remove duplicate stages while preserving order
    return list(dict.fromkeys(valid_stages))


def build_prompt(analysis: dict, stages: list) -> str:
    # Construct the DevOps prompt used to request pipeline commands from the LLM

    prompt = f"""
You are a Senior DevOps Engineer.

Repository analysis:
{json.dumps(analysis, indent=2)}

Pipeline stages decided by the AI engine:
{stages}

Generate a bash CI/CD pipeline.

Rules:
- Output ONLY executable bash commands
- No explanations
- Commands must work in Linux CI environments
- Follow the order of stages

Stage guidelines:

install_dependencies → pip install -r requirements.txt
run_tests → pytest
security_scan → bandit -r .
maven_build → mvn clean package
docker_build → docker build -t ai-devops-app:latest .
docker_push → docker push ai-devops-app:latest
terraform_apply → terraform init && terraform apply -auto-approve
kubernetes_deploy → kubectl apply -f k8s/
ansible_deploy → ansible-playbook deploy.yml

Return bash commands only.
"""

    return prompt


def write_pipeline(commands: str):
    # Write generated pipeline commands into executable bash script

    with open("generated_pipeline.sh", "w") as f:
        f.write("#!/bin/bash\n\n")
        f.write("set -e\n\n")
        f.write(commands + "\n")

    logging.info("Pipeline script written to generated_pipeline.sh")


def generate_pipeline():
    # Main workflow that generates CI/CD pipeline using AI

    logging.info("Loading repository analysis")

    analysis = load_analysis()

    logging.info("Deciding pipeline stages")

    stages = decide_pipeline(analysis)

    # Filter stages based on repository technologies
    stages = select_valid_stages(analysis, stages)

    if not stages:
        logging.warning("No pipeline stages detected. Exiting.")
        return

    logging.info(f"Stages selected: {stages}")

    logging.info("Building AI prompt")

    prompt = build_prompt(analysis, stages)

    logging.info("Requesting pipeline from LLM")

    response = ask_llm(prompt)

    commands = clean_commands(response)

    write_pipeline(commands)

    logging.info("Pipeline generation completed")


if __name__ == "__main__":

    # Entry point for running pipeline generator module
    logging.info("Starting AI pipeline generation")

    generate_pipeline()