# 17 — CI/CD (Continuous Integration & Continuous Delivery)

## 1. Definition

**Continuous Integration (CI):** Developers merge code to a shared branch frequently (multiple times/day). Each merge triggers automated build + tests to catch issues early.

**Continuous Delivery (CD):** Every code change that passes CI is automatically deployable to production. Deployment may require manual approval.

**Continuous Deployment:** Extends CD — every passing change is automatically deployed to production without manual intervention.

```
Code Commit → Build → Unit Tests → Integration Tests → Artifact → Deploy to Staging → Deploy to Prod
     CI ─────────────────────────────────────────┘     CD ──────────────────────────────────────────┘
```

---

## 2. Why This Is Needed

| Problem Without CI/CD | How CI/CD Solves It |
|---|---|
| "Works on my machine" syndrome | Consistent build environment |
| Integration hell (merge conflicts after weeks) | Small, frequent merges caught early |
| Manual deployments = human error | Automated, repeatable deployments |
| Slow feedback loop (bugs found in QA weeks later) | Immediate feedback on every commit |
| Fear of releasing | Confidence through automated testing |
| Inconsistent environments | Infrastructure as Code + pipeline-managed deployments |

---

## 3. How It Works Internally

### Jenkins Pipeline Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Jenkins Controller                      │
│  ┌─────────┐  ┌──────────┐  ┌────────────────────────┐  │
│  │ Scheduler│  │ Queue    │  │ Plugin Manager         │  │
│  └─────────┘  └──────────┘  └────────────────────────┘  │
└───────────────────────┬─────────────────────────────────┘
                        │ Distributes jobs
        ┌───────────────┼───────────────┐
        ▼               ▼               ▼
┌──────────────┐ ┌──────────────┐ ┌──────────────┐
│   Agent 1    │ │   Agent 2    │ │   Agent 3    │
│ (Linux/Java) │ │ (Docker)     │ │ (K8s Pod)    │
└──────────────┘ └──────────────┘ └──────────────┘
```

**Jenkins execution flow:**
1. SCM webhook triggers pipeline (or poll SCM)
2. Controller loads `Jenkinsfile` from repo
3. Pipeline parsed → stages created → jobs queued
4. Agent allocated (label matching) → workspace created
5. Each stage executes sequentially (or parallel if declared)
6. Artifacts archived, test results published
7. Post-actions run (notify, cleanup, deploy)

### GitHub Actions Architecture

```
┌──────────────────────────────────────────────────┐
│                  GitHub Event                      │
│  (push, pull_request, schedule, workflow_dispatch) │
└──────────────────────┬───────────────────────────┘
                       ▼
┌──────────────────────────────────────────────────┐
│              Workflow (.yml in .github/workflows/) │
│  ┌─────────┐  ┌─────────┐  ┌─────────┐         │
│  │  Job 1  │  │  Job 2  │  │  Job 3  │         │
│  │(build)  │→ │ (test)  │→ │(deploy) │         │
│  └─────────┘  └─────────┘  └─────────┘         │
└──────────────────────────────────────────────────┘
                       ▼
┌──────────────────────────────────────────────────┐
│                   Runner                          │
│  (GitHub-hosted or self-hosted)                   │
│  Each job gets a fresh VM instance                │
└──────────────────────────────────────────────────┘
```

---

## 4. Real-World Example — Ericsson 5G Microservice Pipeline

```groovy
// Jenkinsfile — Declarative Pipeline
pipeline {
    agent { label 'docker-java17' }
    
    environment {
        REGISTRY = 'harbor.ericsson.se'
        IMAGE = "${REGISTRY}/5g-nef/capif-service"
        VERSION = "${env.BUILD_NUMBER}-${env.GIT_COMMIT.take(7)}"
    }
    
    stages {
        stage('Build') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }
        
        stage('Unit Tests') {
            steps {
                sh 'mvn test'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                    jacoco execPattern: '**/target/jacoco.exec'
                }
            }
        }
        
        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('sonar-server') {
                    sh 'mvn sonar:sonar'
                }
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
        
        stage('Docker Build & Push') {
            steps {
                sh """
                    docker build -t ${IMAGE}:${VERSION} .
                    docker push ${IMAGE}:${VERSION}
                """
            }
        }
        
        stage('Deploy to Staging') {
            steps {
                sh """
                    helm upgrade --install capif-service ./helm-chart \
                        --namespace staging \
                        --set image.tag=${VERSION} \
                        --wait --timeout 300s
                """
            }
        }
        
        stage('Integration Tests') {
            steps {
                sh 'mvn verify -Pintegration-tests -Dbase.url=http://capif-staging.internal'
            }
        }
        
        stage('Deploy to Production') {
            when { branch 'main' }
            input { message 'Deploy to production?' }
            steps {
                sh """
                    helm upgrade --install capif-service ./helm-chart \
                        --namespace production \
                        --set image.tag=${VERSION} \
                        --set replicas=3 \
                        --wait --timeout 300s
                """
            }
        }
    }
    
    post {
        failure {
            slackSend channel: '#5g-alerts', message: "Pipeline FAILED: ${env.JOB_NAME} #${env.BUILD_NUMBER}"
        }
        success {
            slackSend channel: '#5g-deployments', message: "Deployed ${VERSION} to staging"
        }
    }
}
```

---

## 5. Common Interview Questions

### Q1: Declarative vs Scripted Pipeline in Jenkins?

| Aspect | Declarative | Scripted |
|--------|-------------|----------|
| Syntax | Structured (`pipeline { }`) | Groovy script (`node { }`) |
| Learning curve | Lower | Higher (full Groovy) |
| Validation | Pre-validated before execution | Runtime errors only |
| Flexibility | Limited (predefined structure) | Unlimited (any Groovy code) |
| Error handling | `post { }` blocks | `try/catch/finally` |
| Restart | Can restart from specific stage | Cannot |
| Best for | 90% of pipelines | Complex logic, dynamic stages |

```groovy
// Declarative
pipeline {
    agent any
    stages {
        stage('Build') { steps { sh 'mvn package' } }
    }
    post { failure { mail to: 'team@company.com' } }
}

// Scripted
node {
    try {
        stage('Build') { sh 'mvn package' }
    } catch (e) {
        mail to: 'team@company.com', subject: "FAILED"
        throw e
    }
}
```

### Q2: What are Jenkins Shared Libraries?

Reusable pipeline code stored in a separate Git repo, loaded into any pipeline.

```
(shared-library-repo)
├── vars/           ← Global functions (callable as steps)
│   ├── buildJava.groovy
│   └── deployHelm.groovy
├── src/            ← Groovy classes (OOP logic)
│   └── com/company/Pipeline.groovy
└── resources/      ← Non-Groovy files (configs, templates)
```

```groovy
// vars/buildJava.groovy
def call(Map config = [:]) {
    def javaVersion = config.javaVersion ?: '17'
    def skipTests = config.skipTests ?: false
    
    stage('Build') {
        sh "mvn clean package ${skipTests ? '-DskipTests' : ''}"
    }
    stage('Test') {
        if (!skipTests) {
            sh 'mvn test'
            junit '**/surefire-reports/*.xml'
        }
    }
}

// Usage in Jenkinsfile
@Library('my-shared-lib') _
buildJava(javaVersion: '17', skipTests: false)
```

### Q3: GitHub Actions — How do matrix builds work?

Matrix strategy runs a job across multiple combinations of variables:

```yaml
name: CI
on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    strategy:
      matrix:
        java-version: [11, 17, 21]
        os: [ubuntu-latest, windows-latest]
      fail-fast: false  # Don't cancel other jobs if one fails
    
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: ${{ matrix.java-version }}
          distribution: 'temurin'
      - run: mvn test
```

This creates 6 parallel jobs (3 Java versions × 2 OS).

### Q4: GitFlow vs Trunk-Based Development?

| Aspect | GitFlow | Trunk-Based |
|--------|---------|-------------|
| Branches | main, develop, feature/*, release/*, hotfix/* | main + short-lived feature branches |
| Merge frequency | Weekly/sprint-end | Multiple times per day |
| Release process | Release branch → testing → merge to main | Feature flags + deploy from main |
| Complexity | High (many long-lived branches) | Low (1 main branch) |
| CI/CD fit | Harder (multiple branches to build) | Natural fit (always deployable main) |
| Team size | Large teams with release managers | Any size, requires discipline |
| Rollback | Revert merge or hotfix branch | Feature flag toggle (instant) |
| Best for | Versioned software (mobile apps, SDKs) | SaaS, microservices, web apps |

**At Ericsson:** We use trunk-based for microservices (short feature branches, merge to main daily, deploy via pipeline). For SDK releases to external partners, we use release branches.

### Q5: How do you handle secrets in CI/CD pipelines?

```groovy
// Jenkins — Credentials Plugin
pipeline {
    environment {
        DB_CREDS = credentials('prod-db-credentials')  // Injects _USR and _PSW
        AWS_KEY = credentials('aws-access-key')
    }
    stages {
        stage('Deploy') {
            steps {
                // Masked in logs automatically
                sh 'helm upgrade --set db.password=${DB_CREDS_PSW}'
            }
        }
    }
}
```

```yaml
# GitHub Actions — Secrets
jobs:
  deploy:
    steps:
      - name: Deploy
        env:
          AWS_ACCESS_KEY_ID: ${{ secrets.AWS_ACCESS_KEY_ID }}
          AWS_SECRET_ACCESS_KEY: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
        run: |
          aws eks update-kubeconfig --name production
          helm upgrade --install my-app ./chart
```

**Best practices:**
- Never hardcode secrets in Jenkinsfile/workflow files
- Use vault integration (HashiCorp Vault, AWS Secrets Manager)
- Rotate credentials regularly
- Limit secret scope (job-level, not global)
- Audit secret access

---

## 6. Tricky Edge Cases & Pitfalls

### Pitfall 1: Flaky Tests Breaking the Pipeline

```groovy
// BAD: Retry the whole stage
stage('Test') {
    retry(3) { sh 'mvn test' }  // Hides real failures
}

// BETTER: Quarantine flaky tests + track them
stage('Test') {
    steps {
        sh 'mvn test -Dgroups="!flaky"'  // Exclude flaky tests
    }
}
stage('Flaky Tests (Non-blocking)') {
    steps {
        sh 'mvn test -Dgroups="flaky"'
    }
    post {
        failure { echo 'Flaky tests failed — tracked in JIRA' }
    }
}
```

### Pitfall 2: Docker Layer Cache Invalidation

```dockerfile
# BAD: Cache busted on every build (COPY . before dependency install)
COPY . /app
RUN mvn package

# GOOD: Dependencies cached separately
COPY pom.xml /app/
RUN mvn dependency:go-offline
COPY src/ /app/src/
RUN mvn package -o
```

### Pitfall 3: Pipeline Timeout Without Cleanup

```groovy
// BAD: Timeout kills pipeline, leaves resources dangling
timeout(time: 30, unit: 'MINUTES') {
    sh 'helm install ...'
}

// GOOD: Always cleanup
stage('Deploy') {
    options { timeout(time: 30, unit: 'MINUTES') }
    steps { sh 'helm install ...' }
    post {
        failure { sh 'helm rollback my-release' }
        aborted { sh 'helm uninstall my-release --namespace staging' }
    }
}
```

### Pitfall 4: Race Condition in Parallel Deployments

```groovy
// BAD: Two pipelines deploy to same environment simultaneously
stage('Deploy') { sh 'helm upgrade ...' }

// GOOD: Use locks
stage('Deploy') {
    options { lock('staging-deploy') }  // Only one pipeline at a time
    steps { sh 'helm upgrade ...' }
}
```

### Pitfall 5: GitHub Actions — Workflow Concurrency

```yaml
# Without this, multiple pushes trigger overlapping deployments
concurrency:
  group: deploy-${{ github.ref }}
  cancel-in-progress: true  # Cancel older runs for same branch
```

---

## 7. Comparison with Related Concepts

### Jenkins vs GitHub Actions vs Bamboo

| Feature | Jenkins | GitHub Actions | Bamboo |
|---------|---------|----------------|--------|
| Hosting | Self-hosted (or CloudBees) | GitHub-hosted + self-hosted runners | Atlassian Cloud or Server |
| Config | Jenkinsfile (Groovy) | YAML workflows | UI + YAML (Bamboo Specs) |
| Plugins | 1800+ plugins | Marketplace actions | Limited plugins |
| Scalability | Agent-based (K8s pods) | Auto-scaling runners | Remote agents |
| Cost | Free (infra cost) | Free tier + paid minutes | Per-agent licensing |
| Git integration | Any SCM | GitHub-native | Bitbucket-native |
| Container support | Docker agent, K8s plugin | Native (each job = container) | Docker tasks |
| Secrets | Credentials plugin + Vault | Built-in secrets | Shared credentials |
| Parallelism | `parallel { }` block | Matrix strategy + job dependencies | Parallel stages |
| Maintenance | High (plugin updates, security) | Low (managed by GitHub) | Medium |

### Deployment Strategies

| Strategy | Downtime | Risk | Rollback Speed | Resource Cost |
|----------|----------|------|----------------|---------------|
| **Recreate** | Yes (brief) | High | Slow (redeploy) | Low (1x) |
| **Rolling Update** | No | Medium | Medium (rollback) | Low (1x + buffer) |
| **Blue-Green** | No | Low | Instant (switch) | High (2x) |
| **Canary** | No | Very Low | Fast (route change) | Medium (1x + small) |
| **A/B Testing** | No | Low | Fast | Medium |

```yaml
# Kubernetes Rolling Update
spec:
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 25%        # Max pods above desired count
      maxUnavailable: 25%  # Max pods that can be down
```

```yaml
# Blue-Green with Istio
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
spec:
  http:
    - route:
        - destination:
            host: my-service
            subset: green   # Switch from blue to green
          weight: 100
```

```yaml
# Canary with Istio (10% traffic to new version)
spec:
  http:
    - route:
        - destination:
            host: my-service
            subset: stable
          weight: 90
        - destination:
            host: my-service
            subset: canary
          weight: 10
```

---

## 8. Performance Impact

| Factor | Impact | Optimization |
|--------|--------|-------------|
| Build time | Slow builds = slow feedback = developer frustration | Parallel stages, caching, incremental builds |
| Test execution | Full test suite can take 30+ min | Test parallelization, test impact analysis |
| Docker builds | Layer rebuilds waste time | Multi-stage builds, layer caching, BuildKit |
| Artifact upload/download | Network bottleneck | Local artifact cache, registry mirrors |
| Agent provisioning | Cold start for K8s agents: 30-60s | Pre-warmed agent pools, persistent agents |
| Pipeline overhead | Groovy CPS transformation in Jenkins | Limit Groovy complexity, use `@NonCPS` |

### Build Optimization Techniques

```groovy
// 1. Parallel test execution
stage('Tests') {
    parallel {
        stage('Unit Tests') { steps { sh 'mvn test -pl module-a' } }
        stage('Integration Tests') { steps { sh 'mvn verify -pl module-b' } }
        stage('Contract Tests') { steps { sh 'mvn test -Pcontract' } }
    }
}

// 2. Maven build cache
sh 'mvn package -Dmaven.repo.local=.m2/repository'  // Local cache in workspace

// 3. Skip unchanged modules (Maven incremental)
sh 'mvn package -pl :changed-module -am'  // Only build affected modules
```

```yaml
# GitHub Actions — Dependency caching
- uses: actions/cache@v4
  with:
    path: ~/.m2/repository
    key: ${{ runner.os }}-maven-${{ hashFiles('**/pom.xml') }}
    restore-keys: ${{ runner.os }}-maven-
```

---

## 9. Trade-offs

| Decision | When to Use | When NOT to Use |
|----------|-------------|-----------------|
| Jenkins | Complex pipelines, on-prem requirements, existing investment | Small teams, GitHub-only repos |
| GitHub Actions | GitHub repos, simple-to-medium pipelines, open source | Air-gapped environments, non-GitHub SCM |
| Trunk-based dev | Microservices, SaaS, frequent deploys | Regulated industries needing release branches |
| GitFlow | Versioned products, multiple supported versions | Fast-moving web services |
| Blue-green deploy | Zero-downtime required, instant rollback needed | Cost-sensitive (2x infrastructure) |
| Canary deploy | High-traffic services, gradual risk reduction | Simple internal tools |
| Monorepo pipeline | Shared libraries, atomic cross-service changes | Independent team ownership |
| Multi-repo pipeline | Team autonomy, independent deploy cycles | Tight cross-service coupling |

---

## 10. 30–60 Second Interview Answers

### "Explain your CI/CD pipeline"

> "We use Jenkins with declarative pipelines for our 5G microservices at Ericsson. On every push, the pipeline runs: Maven build, unit tests with JaCoCo coverage, SonarQube quality gate, Docker image build pushed to Harbor registry, Helm deployment to staging K8s cluster, integration tests against staging, then manual approval for production. We use shared libraries for common steps across 15+ microservices. Deployment is canary-based using Istio — 10% traffic to new version, monitor error rates for 5 minutes, then full rollout. Rollback is automatic if error rate exceeds threshold."

### "How do you handle a failed deployment?"

> "We have automated rollback built into the pipeline. If health checks fail after deployment, Helm automatically rolls back to the previous release. For canary deployments, Istio routes traffic back to the stable version. We also have a manual rollback runbook: `helm rollback <release> <revision>`. Post-incident, we add the failure scenario as a test case to prevent regression. All rollbacks trigger a Slack alert to the on-call team."

### "Jenkins vs GitHub Actions — when would you choose each?"

> "Jenkins when you need complex pipeline logic, on-prem hosting, or have heavy plugin ecosystem investment. GitHub Actions when your code is on GitHub and you want minimal maintenance — it's managed, has great marketplace actions, and matrix builds are trivial. At Ericsson we use Jenkins because we need on-prem agents for security compliance and have 50+ shared library functions. For open-source side projects, I use GitHub Actions because it's zero-maintenance and free for public repos."

---

## 11. Real Production Scenario

### The Broken Friday Deploy

**Context:** Ericsson 5G NEF service, 12 microservices, Jenkins pipeline.

**What happened:**
1. Developer merged a feature branch Friday 4 PM
2. Pipeline passed all tests (unit + integration)
3. Deployed to production via canary (10% traffic)
4. Monitoring showed 0.1% increase in 5xx errors — below alert threshold
5. Full rollout proceeded automatically
6. Monday morning: 15% of API calls failing for one specific 3GPP endpoint

**Root cause:** Integration tests used mocked external dependencies. The new code changed the request format to a downstream 3GPP service that wasn't covered by contract tests.

**Fix applied to pipeline:**
```groovy
stage('Contract Tests') {
    steps {
        // Pact contract tests against real provider stubs
        sh 'mvn test -Pcontract-tests'
    }
}

stage('Canary Validation') {
    steps {
        sh '''
            # Extended canary window: 30 min instead of 5 min
            # Check per-endpoint error rates, not just aggregate
            ./scripts/canary-validate.sh \
                --duration 30m \
                --threshold 0.01 \
                --per-endpoint true
        '''
    }
}
```

**Lessons:**
- Never deploy Friday without extended canary monitoring
- Contract tests catch integration issues mocks miss
- Per-endpoint monitoring > aggregate error rates
- Canary duration should match traffic patterns (5 min misses low-traffic endpoints)

---

## 12. If This Fails, How to Debug

| Symptom | Root Cause | Fix |
|---------|-----------|-----|
| Pipeline stuck "waiting for agent" | No agents with matching label, or agents offline | Check agent labels, increase K8s pod limits, check agent connectivity |
| "Permission denied" in pipeline | Credentials expired or missing | Rotate credentials, check credential scope (folder vs global) |
| Tests pass locally, fail in CI | Environment difference (Java version, timezone, locale) | Pin versions in pipeline, use Docker agent for consistency |
| Docker build fails with "no space" | Agent disk full from old images/workspaces | Add `docker system prune` step, configure workspace cleanup |
| Pipeline passes but deployment fails | Helm values mismatch, K8s resource limits, image pull error | Check `helm diff`, verify image exists in registry, check K8s events |
| Flaky test failures | Race conditions, external dependency, time-dependent tests | Quarantine flaky tests, add retries for external calls, mock time |
| Slow pipeline (>30 min) | Sequential stages, no caching, full test suite every time | Parallelize, add Maven/Docker cache, test impact analysis |
| GitHub Actions: "Resource not accessible by integration" | Insufficient token permissions | Add `permissions:` block to workflow |

### Debug Commands

```bash
# Jenkins
# Check agent status
curl -s http://jenkins/computer/api/json | jq '.computer[] | {name: .displayName, offline: .offline}'

# Replay pipeline with debug logging
# Jenkins UI → Build → Replay → Add: echo "DEBUG: ${variable}"

# View pipeline logs for specific stage
curl "http://jenkins/job/my-job/123/wfapi/describe"

# GitHub Actions
# Re-run with debug logging
# Settings → Secrets → Add ACTIONS_RUNNER_DEBUG=true

# Download workflow logs via API
gh run view <run-id> --log

# Helm deployment debug
helm history my-release                    # See revision history
helm get values my-release                 # Current values
helm diff upgrade my-release ./chart       # Preview changes
kubectl describe pod <pod-name>            # Check events
kubectl logs <pod-name> --previous         # Crashed container logs
```

---

## Follow-Up Interview Questions

### Q1: "How would you design a CI/CD pipeline for a monorepo with 20 microservices?"

**Answer:**

```yaml
# Detect which services changed
changes:
  - uses: dorny/paths-filter@v3
    id: filter
    with:
      filters: |
        service-a: 'services/service-a/**'
        service-b: 'services/service-b/**'
        shared-lib: 'libs/shared/**'

# Only build/deploy changed services
build-service-a:
  if: needs.changes.outputs.service-a == 'true' || needs.changes.outputs.shared-lib == 'true'
```

Key design decisions:
1. **Change detection** — Only build/test/deploy services that changed (or depend on changed shared libs)
2. **Shared library changes** — Trigger all dependent services
3. **Parallel builds** — Each service builds independently
4. **Independent deployability** — Each service has its own Helm chart and can deploy independently
5. **Shared pipeline templates** — Common build/test/deploy logic in shared library
6. **Dependency graph** — If service-A depends on service-B's API, run contract tests

### Q2: "How do you ensure zero-downtime deployments?"

**Answer:**

1. **Rolling updates** with proper readiness probes (don't route traffic until ready)
2. **Graceful shutdown** — Handle SIGTERM, drain connections, complete in-flight requests
3. **Database migrations** — Backward-compatible only (expand-contract pattern)
4. **Feature flags** — Deploy code dark, enable via flag (no deployment needed to activate)
5. **Health checks** — Liveness + readiness + startup probes configured correctly
6. **Connection draining** — `preStop` hook with sleep to allow LB to deregister

```yaml
spec:
  containers:
    - lifecycle:
        preStop:
          exec:
            command: ["sh", "-c", "sleep 15"]  # Allow LB to deregister
      readinessProbe:
        httpGet:
          path: /actuator/health/readiness
          port: 8080
        initialDelaySeconds: 10
        periodSeconds: 5
```

```java
// Graceful shutdown in Spring Boot
@PreDestroy
public void onShutdown() {
    log.info("Shutting down — draining connections...");
    // Complete in-flight requests (Spring Boot handles this with server.shutdown=graceful)
}
```

```properties
# application.properties
server.shutdown=graceful
spring.lifecycle.timeout-per-shutdown-phase=30s
```

---

## Practice Task

**Design a complete CI/CD pipeline for a Spring Boot microservice that:**

1. Builds on every push to any branch
2. Runs unit tests + generates coverage report
3. Runs SonarQube quality gate (fails if coverage < 80%)
4. Builds Docker image with proper tagging (branch-commit for feature, semver for main)
5. Deploys to staging on merge to `main`
6. Runs integration tests against staging
7. Canary deployment to production (10% → 50% → 100%) with automated rollback
8. Sends Slack notifications on failure
9. Uses shared library for common steps
10. Handles secrets securely

Write both a `Jenkinsfile` and equivalent `GitHub Actions workflow` for comparison.

**Bonus:** Add a step that prevents deployment if another deployment is in progress (deployment lock).
