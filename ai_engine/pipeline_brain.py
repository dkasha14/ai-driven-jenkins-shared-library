# ai_engine/pipeline_brain.py

import json


def normalize_languages(language_field):
    # Convert language field into a normalized list for reliable detection

    if isinstance(language_field, list):
        return [lang.lower() for lang in language_field]

    if isinstance(language_field, str):
        text = language_field.lower()

        languages = []

        if "python" in text:
            languages.append("python")

        if "java" in text:
            languages.append("java")

        if "node" in text or "javascript" in text:
            languages.append("node")

        if ".net" in text or "dotnet" in text:
            languages.append("dotnet")

        return languages

    return []


def decide_pipeline(analysis):

    stages = []

    # Normalize languages so pipeline logic works reliably
    languages = normalize_languages(analysis.get("Language", []))

    docker_enabled = analysis.get("docker", False)
    terraform_enabled = analysis.get("terraform", False)
    kubernetes_enabled = analysis.get("kubernetes", False)
    ansible_enabled = analysis.get("ansible", False)

    # Python pipeline
    if "python" in languages:
        stages.append("install_dependencies")
        stages.append("run_tests")
        stages.append("security_scan")

    # Java pipeline
    if "java" in languages:
        stages.append("maven_build")
        stages.append("security_scan")

    # Node pipeline
    if "node" in languages:
        stages.append("npm_build")
        stages.append("security_scan")

    # .NET pipeline
    if "dotnet" in languages:
        stages.append("dotnet_restore")
        stages.append("dotnet_build")
        stages.append("dotnet_test")

    # Docker pipeline
    if docker_enabled:
        stages.append("docker_build")
        stages.append("docker_push")

    # Terraform pipeline
    if terraform_enabled:
        stages.append("terraform_apply")

    # Kubernetes deployment
    if kubernetes_enabled:
        stages.append("kubernetes_deploy")

    # Ansible deployment
    if ansible_enabled:
        stages.append("ansible_deploy")

    # Remove duplicates while preserving order
    stages = list(dict.fromkeys(stages))

    return stages


if __name__ == "__main__":

    with open("analysis.json") as f:
        analysis = json.load(f)

    stages = decide_pipeline(analysis)

    print("AI Pipeline Decision:")
    print(stages)