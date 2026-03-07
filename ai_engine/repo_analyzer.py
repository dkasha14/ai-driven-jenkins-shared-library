import os
import json

from ai_engine.llm_client import ask_llm


def detect_infrastructure(repo_path):

    docker = False
    terraform = False
    kubernetes = False
    ansible = False
    helm = False
    dotnet = False

    for root, dirs, files in os.walk(repo_path):

        dirs[:] = [d for d in dirs if d not in ['.git', '__pycache__', '.venv', 'venv']]

        for name in files:

            if name == "Dockerfile":
                docker = True

            if name.endswith(".tf"):
                terraform = True

            if name.endswith((".yaml", ".yml")):
                kubernetes = True

            if name.endswith(".csproj") or name.endswith(".sln"):
                dotnet = True

            if "playbook" in name or "ansible" in root:
                ansible = True

            if name == "Chart.yaml":
                helm = True

    return {
        "docker": docker,
        "terraform": terraform,
        "kubernetes": kubernetes,
        "ansible": ansible,
        "helm": helm,
        "dotnet": dotnet
    }


def analyze_repo(repo_path="."):

    files = []

    for root, dirs, filenames in os.walk(repo_path):

        dirs[:] = [d for d in dirs if d not in ['.git', '__pycache__', '.venv', 'venv']]

        for name in filenames:

            if name.endswith(('.py', '.groovy', '.java', '.js', '.yaml', '.yml', '.tf', '.json')):
                files.append(os.path.join(root, name))

                if len(files) >= 50:
                    break

        if len(files) >= 50:
            break

    prompt = f"""
Analyze the following repository files and determine:

Language
Framework
Build tool
Test tool
Deployment

Files:
{files}

Return JSON only.
"""

    response = ask_llm(prompt)

    response = response.replace("```json", "").replace("```", "").strip()

    try:
        analysis = json.loads(response)
    except Exception as e:
        print("LLM JSON parse failed:", e)

        analysis = {
            "Language": [],
            "Framework": "Unknown",
            "Build tool": "Unknown",
            "Test tool": "Unknown",
            "Deployment": "Unknown"
        }

    infra = detect_infrastructure(repo_path)

    analysis.update(infra)

    with open("analysis.json", "w") as f:
        json.dump(analysis, f, indent=2)

    return analysis


if __name__ == "__main__":

    print("Running AI repository analysis...")

    result = analyze_repo()

    print(result)