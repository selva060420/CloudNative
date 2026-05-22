# 16 — Docker & Kubernetes

## 1. Definition

**Docker** is a containerization platform that packages applications with their dependencies into lightweight, portable containers. **Kubernetes (K8s)** is a container orchestration system that automates deployment, scaling, and management of containerized applications.

Together they solve: "It works on my machine" → "It works everywhere, at any scale."

---

## 2. Why This Is Needed

| Problem | Docker/K8s Solves It By |
|---------|------------------------|
| Environment inconsistency | Containers bundle app + deps identically |
| Slow deployments | Immutable images, rolling updates |
| Manual scaling | HPA auto-scales based on metrics |
| Single point of failure | ReplicaSets ensure desired pod count |
| Resource waste | Bin-packing, resource limits |
| Service discovery complexity | K8s DNS + Service abstraction |
| Secret management | K8s Secrets + external vaults |
| Zero-downtime deploys | Rolling updates, readiness probes |

---

## 3. How It Works Internally

### 3.1 Docker Architecture

```
┌─────────────────────────────────────────┐
│              Docker Host                 │
│  ┌─────────┐  ┌─────────┐  ┌────────┐  │
│  │Container│  │Container│  │Container│  │
│  │  App A  │  │  App B  │  │  App C  │  │
│  └────┬────┘  └────┬────┘  └────┬───┘  │
│       └─────────────┼────────────┘      │
│              Docker Engine               │
│         (containerd + runc)              │
└─────────────────┬───────────────────────┘
                  │
          Host OS Kernel (shared)
```

**Key concepts:**
- **Image:** Read-only template (layered filesystem via UnionFS)
- **Container:** Running instance of an image (isolated process)
- **Dockerfile:** Build instructions for creating images
- **Registry:** Image storage (Docker Hub, ECR, Harbor)

### 3.2 Docker Image Layers

```dockerfile
FROM openjdk:17-slim          # Layer 1: Base OS + JDK (~200MB)
COPY build/libs/app.jar /app/ # Layer 2: Application JAR (~50MB)
EXPOSE 8080                   # Metadata only (no layer)
ENTRYPOINT ["java","-jar","/app/app.jar"]
```

Each instruction creates a cached layer. Layers are shared across images — if 10 services use `openjdk:17-slim`, the base layer is stored once.

### 3.3 Kubernetes Architecture

```
┌─────────────────── Control Plane ───────────────────┐
│  ┌──────────┐  ┌───────────┐  ┌──────────────────┐ │
│  │API Server│  │ Scheduler │  │Controller Manager │ │
│  └─────┬────┘  └─────┬─────┘  └────────┬─────────┘ │
│        └──────────────┼─────────────────┘           │
│                    ┌──┴──┐                          │
│                    │etcd │ (cluster state store)     │
│                    └─────┘                          │
└─────────────────────────────────────────────────────┘
         │                    │                │
┌────────┴───┐      ┌────────┴───┐    ┌──────┴─────┐
│  Worker 1  │      │  Worker 2  │    │  Worker 3  │
│ ┌────────┐ │      │ ┌────────┐ │    │ ┌────────┐ │
│ │kubelet │ │      │ │kubelet │ │    │ │kubelet │ │
│ │kube-prx│ │      │ │kube-prx│ │    │ │kube-prx│ │
│ │ Pods   │ │      │ │ Pods   │ │    │ │ Pods   │ │
│ └────────┘ │      │ └────────┘ │    │ └────────┘ │
└────────────┘      └────────────┘    └────────────┘
```

**Control Plane components:**
| Component | Role |
|-----------|------|
| API Server | REST gateway — all communication goes through it |
| etcd | Distributed key-value store (cluster state) |
| Scheduler | Assigns pods to nodes (resource-aware) |
| Controller Manager | Runs control loops (ReplicaSet, Deployment, Job controllers) |

**Worker Node components:**
| Component | Role |
|-----------|------|
| kubelet | Ensures containers run as specified in PodSpec |
| kube-proxy | Network rules (iptables/IPVS) for Service routing |
| Container Runtime | containerd / CRI-O (runs containers) |

---

## 4. Key Kubernetes Objects

### 4.1 Pod
Smallest deployable unit. One or more containers sharing network/storage.

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: my-app
spec:
  containers:
  - name: app
    image: my-app:1.0
    ports:
    - containerPort: 8080
    resources:
      requests:
        cpu: "250m"
        memory: "256Mi"
      limits:
        cpu: "500m"
        memory: "512Mi"
    livenessProbe:
      httpGet:
        path: /health
        port: 8080
      initialDelaySeconds: 30
      periodSeconds: 10
    readinessProbe:
      httpGet:
        path: /ready
        port: 8080
      initialDelaySeconds: 5
      periodSeconds: 5
```

### 4.2 Deployment
Manages ReplicaSets, enables rolling updates and rollbacks.

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: my-app
spec:
  replicas: 3
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1        # 1 extra pod during update
      maxUnavailable: 0  # zero downtime
  selector:
    matchLabels:
      app: my-app
  template:
    metadata:
      labels:
        app: my-app
    spec:
      containers:
      - name: app
        image: my-app:2.0
```

### 4.3 Service
Stable network endpoint for a set of pods.

| Type | Scope | Use Case |
|------|-------|----------|
| ClusterIP | Internal only | Service-to-service communication |
| NodePort | External via node IP:port | Dev/testing |
| LoadBalancer | External via cloud LB | Production ingress |
| Headless | No cluster IP, DNS returns pod IPs | StatefulSets, Cassandra |

```yaml
apiVersion: v1
kind: Service
metadata:
  name: my-app-svc
spec:
  type: ClusterIP
  selector:
    app: my-app
  ports:
  - port: 80
    targetPort: 8080
```

### 4.4 ConfigMap & Secret

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: app-config
data:
  DATABASE_URL: "jdbc:postgresql://db:5432/mydb"
  LOG_LEVEL: "INFO"
---
apiVersion: v1
kind: Secret
metadata:
  name: app-secrets
type: Opaque
data:
  DB_PASSWORD: cGFzc3dvcmQxMjM=  # base64 encoded
```

**Mounting in pods:**
```yaml
envFrom:
- configMapRef:
    name: app-config
- secretRef:
    name: app-secrets
```

### 4.5 Ingress
L7 routing rules for external HTTP/HTTPS traffic.

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: my-ingress
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /
spec:
  rules:
  - host: api.example.com
    http:
      paths:
      - path: /users
        pathType: Prefix
        backend:
          service:
            name: user-svc
            port:
              number: 80
      - path: /orders
        pathType: Prefix
        backend:
          service:
            name: order-svc
            port:
              number: 80
```

---

## 5. Dockerfile Best Practices

### 5.1 Multi-Stage Build (Critical for Java)

```dockerfile
# Stage 1: Build
FROM gradle:8.5-jdk17 AS builder
WORKDIR /app
COPY build.gradle settings.gradle ./
COPY src ./src
RUN gradle build --no-daemon -x test

# Stage 2: Runtime (minimal image)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
```

**Why multi-stage?**
- Build image: ~800MB (JDK + Gradle + source)
- Runtime image: ~150MB (JRE + JAR only)
- No build tools or source code in production image

### 5.2 Layer Optimization

```dockerfile
# BAD — cache busted on every code change
COPY . .
RUN gradle build

# GOOD — dependencies cached separately
COPY build.gradle settings.gradle ./
RUN gradle dependencies --no-daemon    # cached unless build.gradle changes
COPY src ./src
RUN gradle build --no-daemon -x test   # only rebuilds app code
```

### 5.3 Security Best Practices

| Practice | Why |
|----------|-----|
| Use specific tags (`openjdk:17.0.2-slim`) not `latest` | Reproducible builds |
| Run as non-root user | Limit blast radius |
| Use `.dockerignore` | Exclude secrets, .git, build artifacts |
| Scan images (`trivy image my-app:1.0`) | Find CVEs |
| Use distroless/alpine base | Smaller attack surface |
| No secrets in image layers | Use build secrets or runtime injection |

---

## 6. Helm

Helm is the package manager for Kubernetes — templated YAML + versioned releases.

### 6.1 Chart Structure

```
my-chart/
├── Chart.yaml          # Metadata (name, version, dependencies)
├── values.yaml         # Default configuration values
├── templates/
│   ├── deployment.yaml # Templated K8s manifests
│   ├── service.yaml
│   ├── ingress.yaml
│   ├── configmap.yaml
│   ├── hpa.yaml
│   └── _helpers.tpl    # Template helper functions
└── charts/             # Sub-chart dependencies
```

### 6.2 Templating Example

```yaml
# templates/deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: {{ include "my-chart.fullname" . }}
spec:
  replicas: {{ .Values.replicaCount }}
  template:
    spec:
      containers:
      - name: {{ .Chart.Name }}
        image: "{{ .Values.image.repository }}:{{ .Values.image.tag }}"
        resources:
          {{- toYaml .Values.resources | nindent 12 }}
```

### 6.3 Key Commands

```bash
helm install my-release ./my-chart -f custom-values.yaml
helm upgrade my-release ./my-chart --set image.tag=2.0
helm rollback my-release 1          # rollback to revision 1
helm list -n my-namespace            # list releases
helm template ./my-chart             # render without installing (debug)
```

---

## 7. Istio Service Mesh

### 7.1 Architecture

```
┌─────────────────────────────────────────┐
│            Istio Control Plane           │
│  ┌───────┐  ┌───────┐  ┌────────────┐  │
│  │Pilot  │  │Citadel│  │  Galley    │  │
│  │(config)│ │(certs)│  │(validation)│  │
│  └───────┘  └───────┘  └────────────┘  │
└─────────────────────────────────────────┘
         │ (xDS push config)
┌────────┴────────────────────────────────┐
│  Pod                                     │
│  ┌──────────┐    ┌──────────────────┐   │
│  │   App    │◄──►│  Envoy Sidecar   │   │
│  │Container │    │  (proxy all I/O) │   │
│  └──────────┘    └──────────────────┘   │
└─────────────────────────────────────────┘
```

**What Istio provides:**
- **mTLS:** Automatic mutual TLS between services (zero-trust)
- **Traffic management:** Canary deployments, traffic splitting, retries
- **Observability:** Distributed tracing, metrics, access logs
- **Authorization:** Fine-grained RBAC policies

### 7.2 Traffic Management

```yaml
# VirtualService — route 90% to v1, 10% to v2 (canary)
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: my-app
spec:
  hosts:
  - my-app
  http:
  - route:
    - destination:
        host: my-app
        subset: v1
      weight: 90
    - destination:
        host: my-app
        subset: v2
      weight: 10
```

---

## 8. Probes (Health Checks)

| Probe | Purpose | Failure Action |
|-------|---------|----------------|
| **Liveness** | Is the container alive? | Restart container |
| **Readiness** | Can it serve traffic? | Remove from Service endpoints |
| **Startup** | Has it finished starting? | Delay liveness/readiness checks |

**Probe types:**
```yaml
# HTTP check
livenessProbe:
  httpGet:
    path: /health
    port: 8080
  initialDelaySeconds: 30
  periodSeconds: 10
  failureThreshold: 3

# TCP check (for non-HTTP services)
readinessProbe:
  tcpSocket:
    port: 5432
  periodSeconds: 5

# Command check
livenessProbe:
  exec:
    command: ["cat", "/tmp/healthy"]
```

**Common mistakes:**
- Liveness probe too aggressive → restart loops
- No startup probe for slow-starting apps (Java) → killed before ready
- Readiness probe hitting external dependency → cascading failures

---

## 9. Horizontal Pod Autoscaler (HPA)

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: my-app-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: my-app
  minReplicas: 2
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
  behavior:
    scaleDown:
      stabilizationWindowSeconds: 300  # wait 5min before scaling down
```

**Scaling flow:**
```
Metrics Server → HPA Controller → checks every 15s →
  current CPU > target? → scale up (immediate)
  current CPU < target? → wait stabilization window → scale down
```

**Requirements:** `resources.requests` MUST be set on containers (HPA calculates % of request).

---

## 10. Resource Management

### 10.1 Requests vs Limits

| | Requests | Limits |
|--|----------|--------|
| **Meaning** | Guaranteed minimum | Maximum allowed |
| **Scheduling** | Used by scheduler for placement | Not considered for scheduling |
| **Enforcement** | Always available | CPU: throttled; Memory: OOMKilled |

```yaml
resources:
  requests:
    cpu: "250m"      # 0.25 CPU cores guaranteed
    memory: "512Mi"  # 512MB guaranteed
  limits:
    cpu: "1000m"     # max 1 CPU core (throttled beyond)
    memory: "1Gi"    # max 1GB (OOMKilled beyond)
```

### 10.2 QoS Classes

| Class | Condition | Eviction Priority |
|-------|-----------|-------------------|
| Guaranteed | requests == limits (all containers) | Last to evict |
| Burstable | requests < limits | Middle |
| BestEffort | No requests or limits set | First to evict |

**Production rule:** Always set requests. Set limits for memory (prevent OOM). CPU limits are debatable (throttling can cause latency spikes).

---

## 11. Interview Questions

### Q1: Your Java app takes 60s to start. Pods keep getting killed. Fix it.

**Answer:**
```yaml
startupProbe:
  httpGet:
    path: /health
    port: 8080
  failureThreshold: 30    # 30 × 10s = 300s max startup time
  periodSeconds: 10
livenessProbe:
  httpGet:
    path: /health
    port: 8080
  initialDelaySeconds: 0  # starts after startup probe succeeds
  periodSeconds: 10
```

Without startup probe, liveness probe fires during startup → kills pod → restart loop.

### Q2: Explain the difference between `kubectl apply` and `kubectl create`.

| | `create` | `apply` |
|--|----------|---------|
| Approach | Imperative (create new) | Declarative (converge to desired state) |
| If exists | Error | Update (3-way merge) |
| Tracks changes | No | Yes (via `last-applied-configuration` annotation) |
| Production use | Rarely | Always |

### Q3: How do you achieve zero-downtime deployment?

1. **Rolling update strategy** with `maxUnavailable: 0`
2. **Readiness probe** — new pod only receives traffic when ready
3. **PreStop hook** — graceful shutdown (drain connections)
4. **PodDisruptionBudget** — prevent too many pods down during node drain

```yaml
lifecycle:
  preStop:
    exec:
      command: ["sh", "-c", "sleep 10"]  # allow LB to deregister
---
apiVersion: policy/v1
kind: PodDisruptionBudget
metadata:
  name: my-app-pdb
spec:
  minAvailable: 2
  selector:
    matchLabels:
      app: my-app
```

### Q4: Container uses 2GB memory but limit is 1GB. What happens?

- Container is **OOMKilled** (Out Of Memory)
- Pod status shows `OOMKilled` in `containerStatuses.lastState`
- If `restartPolicy: Always` → kubelet restarts it → may enter CrashLoopBackOff
- **Fix:** Increase memory limit OR fix memory leak (heap dump analysis)

### Q5: How does K8s Service discovery work?

```
Pod A wants to call "order-service":
1. DNS query: order-service.default.svc.cluster.local
2. CoreDNS resolves to ClusterIP (e.g., 10.96.45.12)
3. kube-proxy iptables/IPVS rules route to actual pod IP
4. Load balances across healthy pods (readiness probe passing)
```

**FQDN format:** `<service>.<namespace>.svc.cluster.local`

---

## 12. CKAD-Level Scenarios

### Scenario 1: Debug CrashLoopBackOff

```bash
kubectl describe pod <name>           # check Events section
kubectl logs <name> --previous        # logs from crashed container
kubectl get pod <name> -o yaml        # check exit code, OOMKilled
```

Common causes: missing ConfigMap/Secret, wrong image tag, OOM, failing health check, missing permissions.

### Scenario 2: Canary Deployment (without Istio)

```yaml
# Stable: 9 replicas with v1
# Canary: 1 replica with v2
# Both have same label selector → Service routes to both
# Monitor error rate → if OK, scale canary up, stable down
```

### Scenario 3: Pod can't reach another service

```bash
kubectl exec -it <pod> -- nslookup <service-name>   # DNS works?
kubectl get endpoints <service-name>                  # endpoints populated?
kubectl get networkpolicy -n <namespace>              # network policy blocking?
kubectl logs -n kube-system -l k8s-app=kube-dns      # CoreDNS issues?
```

---

## 13. Quick Reference — kubectl Commands

```bash
# Debugging
kubectl get pods -o wide                    # pod IPs + nodes
kubectl describe pod <name>                 # events + conditions
kubectl logs <name> -c <container> -f       # stream logs
kubectl exec -it <name> -- /bin/sh          # shell into container
kubectl top pods                            # CPU/memory usage

# Deployments
kubectl rollout status deployment/<name>    # watch rollout
kubectl rollout undo deployment/<name>      # rollback
kubectl scale deployment/<name> --replicas=5

# Config
kubectl create configmap <name> --from-file=config.yaml
kubectl create secret generic <name> --from-literal=key=value

# Troubleshooting
kubectl get events --sort-by='.lastTimestamp'
kubectl get pod <name> -o jsonpath='{.status.containerStatuses[0].state}'
```

---

## 14. Follow-Up Questions

### Q1: "Your cluster has 100 microservices. How do you manage Helm charts at scale?"

**Answer:**
- **Umbrella chart** — parent chart with all services as dependencies
- **Library chart** — shared templates (e.g., standard Deployment, Service) that service charts inherit
- **GitOps (ArgoCD/Flux)** — Git repo is source of truth, auto-sync to cluster
- **values per environment** — `values-dev.yaml`, `values-prod.yaml`
- **Helmfile** — declarative multi-chart orchestration

### Q2: "A node is running out of disk. What happens to pods?"

**Answer:**
- kubelet monitors disk pressure (eviction thresholds: `imagefs.available < 15%`)
- Node gets tainted: `node.kubernetes.io/disk-pressure:NoSchedule`
- kubelet evicts pods in order: BestEffort → Burstable → Guaranteed
- Evicted pods are rescheduled to healthy nodes (if resources available)
- **Prevention:** Set `ephemeral-storage` requests/limits, use PVCs for data

---

## 15. Practice Task

**Design a production-ready K8s deployment for a Spring Boot microservice:**

Requirements:
- 3 replicas minimum, auto-scale to 10 based on CPU
- Zero-downtime rolling updates
- External HTTPS access via Ingress
- Config from ConfigMap, secrets from Vault
- Resource limits appropriate for a Java app (heap = 75% of memory limit)
- Proper probes accounting for JVM startup time

Deliverables: Deployment, Service, HPA, Ingress, ConfigMap, PDB — all in one YAML.
