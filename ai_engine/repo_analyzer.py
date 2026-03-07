import os
import json

from llm_client import ask_llm

def scan_repository():

    repo_files = []

    for root, dirs, files in os.walk("."):
        for file in files:
            path = os.path.join(root, file)

            # ignore git and venv noise
            if ".git" in path or ".venv" in path:
                continue

            repo_files.append(path)

    return repo_files


def analyze_with_llm(files):

    file_list = "\n".join(files)

    prompt = f"""
You are an AI DevOps system.

Analyze the following repository structure and determine:

1. programming language
2. framework
3. build tool
4. test framework
5. deployment type (docker/kubernetes/none)

Return ONLY JSON like this:

{{
 "language": "",
 "framework": "",
 "build_tool": "",
 "test_tool": "",
 "deployment": ""
}}

Repository files:

{file_list}
"""

    response = ask_llm(prompt)

    return response


def save_analysis(data):

    with open("analysis.json", "w") as f:
        f.write(data)


if __name__ == "__main__":

    print("Running AI repository analysis...")

    files = scan_repository()

    ai_result = analyze_with_llm(files)

    save_analysis(ai_result)

    print("AI analysis completed.")
    print(ai_result)
