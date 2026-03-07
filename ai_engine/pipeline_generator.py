import json
import logging
from ai_engine.llm_client import ask_llm
from ai_engine.pipeline_brain import decide_pipeline


logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(levelname)s - %(message)s"
)


def clean_commands(commands: str) -> str:
    """
    Remove markdown formatting from LLM output
    """
    commands = commands.replace("```bash", "")
    commands = commands.replace("```", "")
    return commands.strip()


def load_analysis() -> dict:
    """
    Load repository analysis results
    """
    try:
        with open("analysis.json", "r") as f:
            return json.load(f)
    except FileNotFoundError:
        logging.error("analysis.json not found. Run repo analyzer first.")
        raise


def build_prompt(analysis: dict, stages: list) -> str:
    """
    Construct the LLM prompt for pipeline generation
    """

    prompt = f"""
You are an expert DevOps automation system.

Your job is to generate a CI/CD pipeline script based on repository analysis.

Repository analysis result:
{json.dumps(analysis, indent=2)}

Pipeline stages decided by AI:
{stages}

Rules:

1. Generate only executable bash commands.
2. Do not include explanations.
3. Commands must run in CI environments.
4. Use safe DevOps practices.

Possible stages may include:
- dependency installation
- build
- testing
- security scanning
- docker build
- docker push
- terraform init/plan/apply
- kubectl deployment
- ansible playbook execution

Return only bash commands.
"""

    return prompt


def write_pipeline(commands: str):
    """
    Write generated pipeline script
    """

    with open("generated_pipeline.sh", "w") as f:
        f.write("#!/bin/bash\n\n")
        f.write("set -e\n\n")
        f.write(commands + "\n")

    logging.info("Pipeline script written to generated_pipeline.sh")


def generate_pipeline():
    """
    Main pipeline generation workflow
    """

    logging.info("Loading repository analysis")

    analysis = load_analysis()

    logging.info("Deciding pipeline stages")

    stages = decide_pipeline(analysis)

    logging.info("Building LLM prompt")

    prompt = build_prompt(analysis, stages)

    logging.info("Requesting pipeline from AI engine")

    response = ask_llm(prompt)

    cleaned_commands = clean_commands(response)

    write_pipeline(cleaned_commands)

    logging.info("Pipeline generated successfully")


if __name__ == "__main__":

    logging.info("Starting AI pipeline generation")

    generate_pipeline()