Production Grade Cluster Information
=================

Information that are required while designing a production grade kubernetes cluster. For testing purpose, use k3d. Refer this link https://github.com/k3d-io/k3d/blob/main/README.md

## Contents
- [Prerequisite](#prerequisite)
- [Applications](#applications)
- [NativeResource](#nativeresource)
- [Others](#others)


## Prerequisite
Namespaces found in any production/development grade KAAS cluster:

```
    * cattle-fleet-system
    * cattle-impersonation-system
    * cattle-system
    * cis-operator-system
    * custom-application-crds 
    * ingress-nginx
    * kube-node-lease
    * kube-public
    * kube-system
```


## Applications

### kyverno/OPA GateKeeper:

```
https://devopslearning.medium.com/day-6-open-policy-agent-opa-vs-kyverno-7604b5ecddd9
```

### Certificate Manager : 

Cert-manager adds certificates and certificate issuers as resource types in Kubernetes clusters, and simplifies the process of obtaining, renewing and using those certificates.

It supports issuing certificates from a variety of sources, including Let's Encrypt (ACME), HashiCorp Vault, and Venafi TPP / TLS Protect Cloud, as well as local in-cluster issuance. cert-manager also ensures certificates remain valid and up to date, attempting to renew certificates at an appropriate time before expiry to reduce the risk of outages and remove toil.

#### CRDS deployed:

```
challenges.acme.cert-manager.io
certificaterequests.cert-manager.io
certificates.cert-manager.io
clusterissuers.cert-manager.io
issuers.cert-manager.io
orders.acme.cert-manager.io
```

#### Github link:
```
https://github.com/cert-manager/cert-manager
```

### Cattle/ Rancher:

Cattle is the orchestration engine that powers Rancher. Its primary role is meta data management and orchestration of external systems. Cattle in fact does no real work, but instead delegates to other components (agents) to do the actual work. You can look at Cattle as the middle management layer in Rancher.

Rancher is an open source container management platform built for organizations that deploy containers in production. Rancher makes it easy to run Kubernetes everywhere, meet IT requirements, and empower DevOps teams.

#### CRDS deployed:

##### Management related crds

```
apiservices.management.cattle.io
authconfigs.management.cattle.io
clusterregistrationtokens.management.cattle.io
clusters.management.cattle.io
features.management.cattle.io
groups.management.cattle.io
groupmembers.management.cattle.io
preferences.management.cattle.io
settings.management.cattle.io
tokens.management.cattle.io
userattributes.management.cattle.io
users.management.cattle.io
```
##### CIS(security scans) - CIS Operator
```
clusterscanbenchmarks.cis.cattle.io
clusterscanprofiles.cis.cattle.io
clusterscanreports.cis.cattle.io
clusterscans.cis.cattle.io
```
##### UI related crds
```
navlinks.ui.cattle.io
```

##### Catalog related crds
```
clusterrepos.catalog.cattle.io
operations.catalog.cattle.io
```

#### Github link:

```
https://github.com/rancher/cattle
https://github.com/rancher/fleet ---> deploy in cattle-fleet-system namespace
https://github.com/rancher/rancher/tree/release/v2.7/pkg/apis ----> crds for cattle
https://github.com/rancher/rancher/blob/68215bcdde090854ff28b43b01d1aa0b69611920/pkg/systemtemplate/template.go  ----> deploy in cattle-system
https://github.com/rancher/rancher/tree/68215bcdde090854ff28b43b01d1aa0b69611920/pkg/impersonation ---> deploy in cattle-impersonation-system namespace
https://github.com/rancher/cis-operator
```

### CoreOS - Prometheus operator:

The Prometheus Operator provides Kubernetes native deployment and management of Prometheus and related monitoring components. The purpose of this project is to simplify and automate the configuration of a Prometheus based monitoring stack for Kubernetes clusters.

The Prometheus operator includes, but is not limited to, the following features:

Kubernetes Custom Resources: Use Kubernetes custom resources to deploy and manage Prometheus, Alertmanager, and related components.

Simplified Deployment Configuration: Configure the fundamentals of Prometheus like versions, persistence, retention policies, and replicas from a native Kubernetes resource.

Prometheus Target Configuration: Automatically generate monitoring target configurations based on familiar Kubernetes label queries; no need to learn a Prometheus specific configuration language.

#### CRDS deployed:

```
prometheuses.monitoring.coreos.com
prometheusrules.monitoring.coreos.com
servicemonitors.monitoring.coreos.com
```

#### Github link:

```
https://github.com/prometheus-operator/prometheus-operator
```

### Strimzi - Apache Kafka operator:

Strimzi provides a way to run an Apache Kafka cluster on Kubernetes or OpenShift in various deployment configurations. See website for more details about the project.

#### CRDS deployed:

```
kafkabridges.kafka.strimzi.io
kafkaconnectors.kafka.strimzi.io
kafkaconnects.kafka.strimzi.io
kafkamirrormaker2s.kafka.strimzi.io
kafkamirrormakers.kafka.strimzi.io
kafkarebalances.kafka.strimzi.io
kafkas.kafka.strimzi.io
kafkatopics.kafka.strimzi.io
kafkausers.kafka.strimzi.io
strimzipodsets.core.strimzi.io
```

#### Github link:
```
https://github.com/strimzi/strimzi-kafka-operator
```

### Project Contour - Ingress controller

Contour is an Ingress controller for Kubernetes that works by deploying the Envoy proxy as a reverse proxy and load balancer. Contour supports dynamic configuration updates out of the box while maintaining a lightweight profile.

Contour also introduces a new ingress API (HTTPProxy) which is implemented via a Custom Resource Definition (CRD). Its goal is to expand upon the functionality of the Ingress API to allow for a richer user experience as well as solve shortcomings in the original design.

#### CRDS deployed:

```
extensionservices.projectcontour.io
httpproxies.projectcontour.io
tlscertificatedelegations.projectcontour.io
```

#### Github link:

```
https://github.com/projectcontour/contour
```

### Project Calico - networking and network security solution

Software Defined Networking: `Project Calico`

Calico is a widely adopted, battle-tested open source networking and network security solution for Kubernetes, virtual machines, and bare-metal workloads. Calico provides two major services for Cloud Native applications:

Network connectivity between workloads.
Network security policy enforcement between workloads.
Calico’s flexible architecture supports a wide range of deployment options, using modular components and technologies, including:

Choice of data plane technology, whether it be eBPF, standard Linux, Windows HNS or VPP
Enforcement of the full set of Kubernetes network policy features, plus for those needing a richer set of policy features, Calico network policies.
An optimized Kubernetes Service implementation using eBPF.
Kubernetes apiserver integration, for managing Calico configuration and Calico network policies.
Both non-overlay and overlay (via IPIP or VXLAN) networking options in either public cloud or on-prem deployments.
CNI plugins for Kubernetes to provide highly efficient pod networking and IP Address Management (IPAM).
A Neutron ML2 plugin to provide VM networking for OpenStack.
A BGP routing stack that can advertise routes for workload and service IP addresses to physical network infrastructure.

#### CRDS deployed:
```
bgpconfigurations.crd.projectcalico.org
bgppeers.crd.projectcalico.org
blockaffinities.crd.projectcalico.org
caliconodestatuses.crd.projectcalico.org
clusterinformations.crd.projectcalico.org
felixconfigurations.crd.projectcalico.org
globalnetworkpolicies.crd.projectcalico.org
globalnetworksets.crd.projectcalico.org
hostendpoints.crd.projectcalico.org
ipamblocks.crd.projectcalico.org
ipamconfigs.crd.projectcalico.org
ipamhandles.crd.projectcalico.org
ippools.crd.projectcalico.org
ipreservations.crd.projectcalico.org
kubecontrollersconfigurations.crd.projectcalico.org
networkpolicies.crd.projectcalico.org
networksets.crd.projectcalico.org
```

#### Github link:

```
https://github.com/projectcalico/calico/tree/4742bce16f45ea0bcd582ea6800053b7ac6cb4b3/libcalico-go/config/crd
```

### Datastax - Apache Cassandra Operator

DataStax Kubernetes Operator for Apache Cassandra® (Cass Operator) automates the process of deploying and managing open-source Apache Cassandra® or DataStax Enterprise (DSE) in a Kubernetes cluster.

Cass Operator distills the user-supplied information down to the number of nodes and cluster name to manage the lifecycle of individual Kubernetes resources. Additional options are available, but for starters, that's essentially all you'll need to specify. Now the process of managing the distributed Cassandra or DSE data platform is turnkey and much easier, which means your team is free to focus on the application layer and its functionality.

#### CRDS deployed:
```
cassandradatacenters.cassandra.datastax.com
```

#### Github link:
```
https://github.com/datastax/cass-operator
```

### Ceph CSI RBD: Network Block storage for persistence in cluster

Software Defined Storage: `Ceph`

It contains Ceph Container Storage Interface (CSI) driver for RBD, CephFS and Kubernetes sidecar deployment YAMLs to support CSI functionality: provisioner, attacher, resizer, driver-registrar and snapshotter.

Ceph CSI plugins implement an interface between a CSI-enabled Container Orchestrator (CO) and Ceph clusters. They enable dynamically provisioning Ceph volumes and attaching them to workloads.

Independent CSI plugins are provided to support RBD and CephFS backed volumes,

For details about configuration and deployment of RBD plugin, please refer rbd doc and for CephFS plugin configuration and deployment please refer cephFS doc.
For example usage of the RBD and CephFS CSI plugins, see examples in examples/.
Stale resource cleanup, please refer cleanup doc.

#### CRDS used:
```
csidrivers.storage.k8s.io
csinodes.storage.k8s.io
storageclasses.storage.k8s.io
volumeattachments.storage.k8s.io
csistoragecapacities.storage.k8s.io
```

#### Github link:
```
https://github.com/ceph/ceph-csi/tree/devel/deploy/rbd/kubernetes
https://github.com/ceph/ceph-csi/tree/devel/charts/ceph-csi-rbd
https://docs.ceph.com/en/latest/rbd/rbd-kubernetes/
```

## NativeResource:

### Api Services:

```
runtimeclasses.node.k8s.io
apiservices.apiregistration.k8s.io
flowschemas.flowcontrol.apiserver.k8s.io
prioritylevelconfigurations.flowcontrol.apiserver.k8s.io
mutatingwebhookconfigurations.admissionregistration.k8s.io
validatingwebhookconfigurations.admissionregistration.k8s.io
```

### Authorization:

```
serviceaccounts
clusterrolebindings.rbac.authorization.k8s.io
clusterroles.rbac.authorization.k8s.io
rolebindings.rbac.authorization.k8s.io
roles.rbac.authorization.k8s.io
```

### Scheduling:

```
priorityclasses.scheduling.k8s.io
```

### Certificate Management:

```
certificatesigningrequests.certificates.k8s.io (Used with cert-manager.io)
```

### Service discovery:

```
services
endpoints
endpointslices.discovery.k8s.io
```

### Network Management:

```
networkpolicies.networking.k8s.io
ingressclasses.networking.k8s.io
ingresses.networking.k8s.io
```

### Lease Management:

```
leases.coordination.k8s.io
```

### Metrics management:

```
nodes.metrics.k8s.io
pods.metrics.k8s.io
```

### Horizontal scaling:

```
horizontalpodautoscalers.autoscaling
```

### Pod related policies:

```
podsecuritypolicies.policy
poddisruptionbudgets.policy
```

### Other resources:
```
# batch
jobs.batch
cronjobs.batch

# apps
replicasets.apps
controllerrevisions.apps
daemonsets.apps
deployments.apps
statefulsets.apps

# others
componentstatuses
configmaps
limitranges
namespaces
nodes
persistentvolumeclaims
persistentvolumes
pods
podtemplates
replicationcontrollers
resourcequotas
secrets
```

## Others:

`Terraform` is used to do infrastructure automation and cluster provisioning. Network Function Manager does FCAP (Fault , Configuration, Accounting, Performance and Security).

Use dive tool to inspect docker images (ps axf) and use tree command to view file system.
 
### virtualized network function:
```
virtual machine engine: kvm or vmware
vm orchestration: openstack
```
### containerized network function:
```
container runtime: containerd
container orchestration: k8s or openshift or okd
```

**To-Do:** Deploy a master-slave k8 node in a aws using terraform/ansible and install operators, nignx, cattle, kubernetes on the vm within a vpc using ansible.
