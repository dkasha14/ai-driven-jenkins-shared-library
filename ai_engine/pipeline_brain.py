import json


def decide_pipeline(analysis):

    stages = []

    languages = analysis.get("Language", [])

    docker_enabled = analysis.get("docker", False)
    terraform_enabled = analysis.get("terraform", False)
    kubernetes_enabled = analysis.get("kubernetes", False)
    ansible_enabled = analysis.get("ansible", False)

    # Python
    if "Python" in languages:
        stages.append("install_dependencies")
        stages.append("run_tests")
        stages.append("security_scan")

    # Java
    if "Java" in languages:
        stages.append("maven_build")
        stages.append("security_scan")

    # Node
    if "Node" in languages:
        stages.append("npm_build")
        stages.append("security_scan")

    # .NET
    if ".NET" in languages or "DotNet" in languages:
        stages.append("dotnet_restore")
        stages.append("dotnet_build")
        stages.append("dotnet_test")

    # Docker
    if docker_enabled:
        stages.append("docker_build")
        stages.append("docker_push")

    # Terraform
    if terraform_enabled:
        stages.append("terraform_apply")

    # Kubernetes
    if kubernetes_enabled:
        stages.append("kubernetes_deploy")

    # Ansible
    if ansible_enabled:
        stages.append("ansible_deploy")

    return stages


if __name__ == "__main__":

    with open("analysis.json") as f:
        analysis = json.load(f)

    stages = decide_pipeline(analysis)

    print("AI Pipeline Decision:")
    print(stages)