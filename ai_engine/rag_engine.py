import os

KB_PATH = "knowledge_base/devops_fixes.txt"


def load_knowledge():

    if not os.path.exists(KB_PATH):
        return ""

    with open(KB_PATH, "r") as f:
        return f.read()


def search_knowledge(query: str):

    knowledge = load_knowledge()

    if not knowledge:
        return "Knowledge base not available."

    sections = knowledge.strip().split("\n\n")

    query = query.lower()

    for section in sections:
        if query in section.lower():
            return section.strip()

    return "No relevant fix found in knowledge base."


if __name__ == "__main__":

    test_query = "docker command not found"

    result = search_knowledge(test_query)

    print("\n=== Knowledge Base Result ===\n")
    print(result)