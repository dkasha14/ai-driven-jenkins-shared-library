import json
import logging

from ai_engine.llm_client import ask_llm
from ai_engine.pipeline_brain import decide_pipeline


logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(levelname)s - %(message)s"
)


# Map stage names to real executable commands
STAGE_COMMAND_MAP = {
    "install_dependencies": "pip install -r requirements.txt || true",
    "run_tests": "pytest || true",
    "security_scan": "bandit -r . || true",
    "maven_build": "mvn clean package",
    "npm_build": "npm install",
    "docker_build": "docker build -t ai-devops-app:latest .",
    "docker_push": "docker push ai-devops-app:latest",
    "kubernetes_deploy": "kubectl apply -f k8s/ || true",
    "terraform_apply": "terraform init && terraform apply -auto-approve",
    "ansible_deploy": "ansible-playbook deploy.yml"
}


def clean_commands(commands: str) -> str:
    """Remove markdown formatting returned by the LLM."""
    commands = commands.replace("```bash", "")
    commands = commands.replace("```", "")
    return commands.strip()


def normalize_commands(commands: str) -> str:
    """Replace stage names with real commands if AI returns them."""
    lines = commands.split("\n")
    fixed_lines = []

    for line in lines:
        stripped = line.strip()

        if stripped in STAGE_COMMAND_MAP:
            fixed_lines.append(STAGE_COMMAND_MAP[stripped])
        else:
            fixed_lines.append(line)

    return "\n".join(fixed_lines)


def load_analysis() -> dict:
    """Load repository analysis file produced by repo analyzer."""
    with open("analysis.json", "r") as f:
        return json.load(f)


def build_prompt(analysis: dict, stages: list) -> str:
    """Create the AI prompt used to generate the pipeline."""
    return f"""
You are a senior DevOps engineer.

Repository analysis:
{json.dumps(analysis, indent=2)}

Pipeline stages:
{stages}

Generate a CI/CD pipeline as pure bash commands.

Rules:
- Output ONLY executable bash commands
- Do NOT output stage names
- Convert stage names into real commands

Example:
install_dependencies -> pip install -r requirements.txt
run_tests -> pytest
security_scan -> bandit -r .
maven_build -> mvn clean package

Return bash commands only.
"""


def write_pipeline(commands: str):
    """Write final pipeline script to disk."""
    with open("generated_pipeline.sh", "w") as f:
        f.write("#!/bin/bash\n\n")
        f.write("set -e\n\n")
        f.write(commands + "\n")

    logging.info("Pipeline script written to generated_pipeline.sh")


def generate_pipeline():
    """Main pipeline generation workflow."""

    logging.info("Loading repository analysis")
    analysis = load_analysis()

    logging.info("Deciding pipeline stages")
    stages = decide_pipeline(analysis)

    logging.info("Building AI prompt")
    prompt = build_prompt(analysis, stages)

    logging.info("Requesting pipeline from LLM")
    response = ask_llm(prompt)

    commands = clean_commands(response)

    # 🔧 THIS IS THE FIX
    commands = normalize_commands(commands)

    write_pipeline(commands)

    logging.info("Pipeline generation completed")


if __name__ == "__main__":
    generate_pipeline()