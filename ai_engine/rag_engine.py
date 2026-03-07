import os


def search_knowledge(query):

    kb_file = "knowledge_base/devops_fixes.txt"

    if not os.path.exists(kb_file):
        return "No knowledge base found."

    with open(kb_file) as f:
        data = f.read()

    if query.lower() in data.lower():
        return data

    return "No known fix found in knowledge base."


if __name__ == "__main__":

    query = "docker command not found"

    result = search_knowledge(query)

    print("Knowledge Base Result:\n")
    print(result)
