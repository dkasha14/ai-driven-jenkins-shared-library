import json

def decide_pipeline(analysis):

    stages = []

    language = analysis.get("language", "")

    if "Python" in language:
        stages.append("install_dependencies")
        stages.append("run_tests")

    if "Java" in language:
        stages.append("maven_build")

    if "Node" in language:
        stages.append("npm_build")

    stages.append("docker_build")
    stages.append("docker_push")
    stages.append("kubernetes_deploy")

    return stages


if __name__ == "__main__":

    with open("analysis.json") as f:
        analysis = json.load(f)

    stages = decide_pipeline(analysis)

    print("AI Pipeline Decision:")
    print(stages)
