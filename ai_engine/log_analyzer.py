import sys
from ai_engine.llm_client import ask_llm


def analyze_failure(log_text):

    prompt = f"""
You are a Senior DevOps engineer.

Analyze the CI/CD pipeline failure logs below and suggest the root cause and fix.

Logs:
{log_text}

Return:

Root Cause:
Fix:
"""

    response = ask_llm(prompt)

    return response


if __name__ == "__main__":

    if len(sys.argv) < 2:
        print("Usage: python log_analyzer.py <logfile>")
        sys.exit(1)

    logfile = sys.argv[1]

    with open(logfile, "r") as f:
        logs = f.read()

    result = analyze_failure(logs)

    print("\nAI Failure Analysis:\n")
    print(result)
