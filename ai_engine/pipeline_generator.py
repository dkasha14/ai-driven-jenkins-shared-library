import json
from ai_engine.llm_client import ask_llm
from ai_engine.pipeline_brain import decide_pipeline


def clean_commands(commands):
    commands = commands.replace("```bash", "")
    commands = commands.replace("```", "")
    return commands.strip()


def load_analysis():
    with open("analysis.json", "r") as f:
        return json.load(f)


def generate_pipeline():

    analysis = load_analysis()

    stages = decide_pipeline(analysis)

    prompt = f"""
You are a Senior DevOps Engineer.

Repository analysis:
{analysis}

Pipeline stages decided by AI:
{stages}

Generate bash commands for these stages.

Return only executable bash commands.
"""

    response = ask_llm(prompt)

    cleaned = clean_commands(response)

    with open("generated_pipeline.sh", "w") as f:
        f.write("#!/bin/bash\n\n")
        f.write(cleaned + "\n")

    print("Pipeline generated successfully.")


if __name__ == "__main__":

    print("Generating pipeline using AI...")

    generate_pipeline()