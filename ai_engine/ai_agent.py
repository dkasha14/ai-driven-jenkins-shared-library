import json
from ai_engine.repo_analyzer import analyze_repo
from ai_engine.pipeline_brain import decide_pipeline
from ai_engine.pipeline_generator import generate_pipeline
from ai_engine.log_analyzer import analyze_failure
from ai_engine.rag_engine import search_knowledge


def run_ai_devops():

    print("AI DevOps Agent Starting")

    # Step 1 Repo analysis
    analysis = analyze_repo(".")
    print("Repository Analysis:", analysis)

    # Step 2 Decide pipeline stages
    stages = decide_pipeline(analysis)
    print("Pipeline Stages:", stages)

    # Step 3 Generate pipeline
    generate_pipeline()
    print("Pipeline generated.")

    print("AI DevOps Agent Completed")


if __name__ == "__main__":
    run_ai_devops()
