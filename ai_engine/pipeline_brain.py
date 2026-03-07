import json


def decide_pipeline(analysis):

    stages = []

    project_type = analysis.get("project_type", "")

    docker_enabled = analysis.get("docker", False)
    terraform_enabled = analysis.get("terraform", False)
    kubernetes_enabled = analysis.get("kubernetes", False)

    # Language based stages
    if project_type == "python":
        stages.append("install_dependencies")
        stages.append("run_tests")
        stages.append("security_scan")

    elif project_type == "java":
        stages.append("maven_build")
        stages.append("security_scan")

    elif project_type == "node":
        stages.append("npm_build")
        stages.append("security_scan")

    # Docker stages
    if docker_enabled:
        stages.append("docker_build")
        stages.append("docker_push")

    # Terraform stages
    if terraform_enabled:
        stages.append("terraform_apply")

    # Kubernetes stages
    if kubernetes_enabled:
        stages.append("kubernetes_deploy")

    return stages


if __name__ == "__main__":

    with open("analysis.json") as f:
        analysis = json.load(f)

    stages = decide_pipeline(analysis)

    print("AI Pipeline Decision:")
    print(stages)