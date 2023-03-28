# Kubernetes related information

This folder contains information related to Kubernetes.


Kubernetes guide
=================

## Contents
- [Prerequisite](#prerequisite)
- [Installation](#installation)
- [Setup](#setup)
- [Tools](#tools)


## Prerequisite:

- Copy all the scripts provided in this repository. 
- Check if you have enough cpu, memory and storage in your machine.
- Go, sudo access, git and bash/sh are some of the prerequisites.

## Installation:

- Do the below commands to set up local Kubernetes cluster:

```
chmod +x k8s_docker_tools_install.sh
./k8s_docker_tools_install.sh
source ~/.bashrc

```

- If it is a `KAAS` (Kubernetes as a Service) cluster, get access from your team wherein they add your credentials to the clusterrole (managed by rancher).

- Download the kubeconfig file with certificate, key and CA for authentication and clusterrole (rbac) permission for authorization.

### Tip:

If you have multiple clusters instead of having separate kubeconfig file, manage it in single file like below

```
apiVersion: v1
clusters:
- cluster:
    certificate-authority-data: DATA+OMITTED
    server: https://<cluster-1>:6443
  name: <cluster-1>
- cluster:
    certificate-authority-data: DATA+OMITTED
    server: https://<cluster-2>:6443
  name: <cluster-1>
contexts:
- context:
    cluster: <cluster-1>
    namespace: default
    user: <user-1>
  name: <user-1>@<cluster-1>
- context:
    cluster: <cluster-2>
    namespace: default
    user: <user-1>
  name: <user-1>@<cluster-2>
current-context: <user-1>@<cluster-1>
kind: Config
preferences:
  colors: true
users:
- name: <user-1>
  user:
    client-certificate-data: REDACTED
    client-key-data: REDACTED
```

## Setup:

- Paste the contents of `bash_aliases.sh` file in `~/.bashrc` or `~/.bash_aliases` file. Do `source ~/.bash_aliases` and then `source ~/.bashrc` .

- Check if bash completion is installed by typing: `type _init_completion`. If it is not installed, do the following:

```
sudo apt install bash-completion
source /usr/share/bash-completion/bash_completion
source ~/.bashrc
```

- To enable *kubectl* bash completion with alias:

```
kubectl completion bash | sudo tee /etc/bash_completion.d/kubectl > /dev/null
echo 'complete -o default -F __start_kubectl ks' >>~/.bashrc
source ~/.bashrc

```

- To enable *helm* bash completion with alias:

```
helm completion bash | sudo tee /etc/bash_completion.d/helm > /dev/null
echo 'complete -o default -F __start_helm helm' >>~/.bashrc
source ~/.bashrc

```

**Note**: Below are the config folders after doing installations of several packages:

```
.krew/
.kube/
.fzf/
```

## Tools:

- Below are the list of useful kubectl plugins from krew

```
cert-manager                    Manage cert-manager resources inside your cluster
cost                            View cluster cost information
df-pv                           Show disk usage (like unix df) for pvc   ---> very useful 
graph                           Visualize Kubernetes resources and relationships. ---> very useful
oomd                            Show recently OOMKilled pods           ---> very useful 
pod-dive                        Shows a pod's workload tree and info inside a node       
pod-lens                        Show pod-related resources         ---> very useful             
rbac-lookup                     Reverse lookup for RBAC                             
rbac-tool                       Plugin to analyze RBAC permissions and generate
ttsum                           Visualize taints and tolerations       
view-cert                       View certificate information stored in secrets      
view-secret                     Decode Kubernetes secrets
```

- Following will be installed using `k8s_docker_tools_install.sh` script:

```
docker-ce-cli
kubectl
helm
krew
crictl
kind or minikube
k9s
kubectx
kubens
fzf
```

**Note**: Open `favourites.html` file provided in this repository and refer kubernetes category for important links.
