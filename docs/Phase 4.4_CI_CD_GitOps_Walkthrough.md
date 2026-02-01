# Phase 4.4: CI/CD & GitOps Walkthrough

This phase automated the entire delivery lifecycle, from code commit to Kubernetes deployment, using **GitHub Actions** and **ArgoCD**.

## 1. CI Pipeline (`.github/workflows/ci.yml`) 🛠️

**Trigger**: Pull Requests to `main`.

### Key Features
- **Fast Feedback**: Runs on every PR to catch issues early.
- **Environment**: Sets up `JDK 21` and caches `~/.m2/repository` for speed.
- **Verification**: Runs `mvn clean verify`, which executes:
    - Unit Tests
    - Integration Tests (via Testcontainers)
    - Checkstyle/formatting checks
- **Reporting**: Publishes JUnit test reports for easy debugging.

## 2. CD Pipeline (`.github/workflows/cd.yml`) 📦

**Trigger**: Push to `main`.

### Key Features
- **Matrix Strategy**: Builds all 10 services in parallel for efficiency.
- **Container Registry**: Publishes images to **GitHub Container Registry (ghcr.io)**.
- **Versioning**: Tags images with:
    - `latest` (for development)
    - `sha-<commit_hash>` (for immutable history)
- **Security**: Uses `GITHUB_TOKEN` for password-less authentication.

## 3. GitOps with ArgoCD 🐙

We implemented a declarative GitOps approach where the Git repository is the "Source of Truth" for the Kubernetes state.

### Infrastructure (`infrastructure/argocd`)

#### `common.yaml`
Manages the deployment of the Helm Library Chart (if standalone, though usually it's just a dependency).

#### `services-appset.yaml`
A powerful **ApplicationSet** that dynamically generates ArgoCD Applications for all 10 microservices.
- **Source**: `infrastructure/helm/services/{{name}}`
- **Destination**: `pizzaflow` namespace.
- **Sync Policy**:
    - **Automated**: Changes to Helm charts in Git are automatically applied.
    - **Self-Heal**: Configuration drift (manual changes in K8s) is automatically corrected.
    - **Prune**: Resources deleted from Git are deleted from K8s.

## 4. Deployment Flow 🚀

1.  **Developer** pushes code to `main`.
2.  **GitHub Actions** builds and publishes new Docker image `ghcr.io/malkooleh/pizzaflow/order-service:sha-xyz`.
3.  **Developer** updates the Helm chart `values.yaml` in Git with the new image tag.
4.  **ArgoCD** detects the change in the Helm chart.
5.  **ArgoCD** syncs the state, performing a rolling update on the Kubernetes cluster.
