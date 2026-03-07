import sys
import logging

from ai_engine.llm_client import ask_llm


logging.basicConfig(level=logging.ERROR)


def load_log(logfile: str):

    try:
        with open(logfile, "r") as f:
            return f.read()
    except FileNotFoundError:
        print("Log file not found:", logfile)
        sys.exit(1)


def clean_response(text: str):

    text = text.replace("```bash", "")
    text = text.replace("```", "")
    return text.strip()


def build_prompt(log_text: str):

    return f"""
You are a Senior DevOps Engineer.

Analyze the CI/CD pipeline failure logs.

Logs:
{log_text}

Return ONLY:

Root Cause:
Fix:
Command:
"""


def analyze_failure(log_text: str):

    prompt = build_prompt(log_text)

    response = ask_llm(prompt)

    return clean_response(response)


def main():

    if len(sys.argv) < 2:
        print("Usage: python -m ai_engine.log_analyzer <logfile>")
        sys.exit(1)

    logfile = sys.argv[1]

    logs = load_log(logfile)

    result = analyze_failure(logs)

    print("\n=== AI Failure Analysis ===\n")
    print(result)


if __name__ == "__main__":
    main()