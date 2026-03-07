import os
import json

from ai_engine.llm_client import ask_llm

def analyze_repo(repo_path="."):

    files = []

    for root, dirs, filenames in os.walk(repo_path):

        # ignore unnecessary folders
        dirs[:] = [d for d in dirs if d not in ['.git', '__pycache__', '.venv', 'venv']]

        for name in filenames:

            # only important project files
            if name.endswith(('.py', '.groovy', '.java', '.js', '.yaml', '.yml', '.tf', '.json')):
                files.append(os.path.join(root, name))

                # stop scanning after 50 files
                if len(files) >= 50:
                    break

        # break outer loop also if limit reached
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

    # clean markdown if LLM returns it
    response = response.replace("```json", "").replace("```", "").strip()

    try:
        analysis = json.loads(response)
    except Exception as e:
        print("LLM JSON parse failed:", e)
        print("LLM response was:", response)

        analysis = {
            "language": "unknown",
            "framework": "unknown",
            "build_tool": "unknown",
            "test_tool": "unknown",
            "deployment": "unknown"
        }

    with open("analysis.json", "w") as f:
        json.dump(analysis, f, indent=2)

    return analysis


if __name__ == "__main__":

    print("Running AI repository analysis...")

    result = analyze_repo()

    print(result)