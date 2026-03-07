import json
import logging

from ai_engine.llm_client import ask_llm
from ai_engine.pipeline_brain import decide_pipeline


logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(levelname)s - %(message)s"
)


def clean_commands(commands: str) -> str:
    """Remove markdown formatting from LLM output"""
    commands = commands.replace("```bash", "")
    commands = commands.replace("```", "")
    return commands.strip()


def load_analysis() -> dict:
    """Load repository analysis"""
    try:
        with open("analysis.json", "r") as f:
            return json.load(f)
    except FileNotFoundError:
        logging.error("analysis.json not found. Run repo_analyzer first.")
        raise


def build_prompt(analysis: dict, stages: list) -> str:
    """Create the AI prompt used to generate the pipeline"""

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
docker_build → docker build -t ai-devops-app:latest .
docker_push → docker push ai-devops-app:latest
terraform_apply → terraform init && terraform apply -auto-approve
kubernetes_deploy → kubectl apply -f k8s/
ansible_deploy → ansible-playbook deploy.yml

Return bash commands only.
"""

    return prompt


def write_pipeline(commands: str):
    """Write pipeline script"""

    with open("generated_pipeline.sh", "w") as f:
        f.write("#!/bin/bash\n\n")
        f.write("set -e\n\n")
        f.write(commands + "\n")

    logging.info("Pipeline script written to generated_pipeline.sh")


def generate_pipeline():
    """Main pipeline generation flow"""

    logging.info("Loading repository analysis")

    analysis = load_analysis()

    logging.info("Deciding pipeline stages")

    stages = decide_pipeline(analysis)

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

    logging.info("Starting AI pipeline generation")

    generate_pipeline()