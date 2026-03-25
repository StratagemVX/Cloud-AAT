# Deployment Pipeline Guide

## Overview
This deployment pipeline uses GitHub Actions to automate building, testing, and deploying your Spring Boot chat application to Kubernetes.

## Pipeline Stages

### 1. Build and Test (`build-and-test`)
- Checks out code
- Sets up Java 21
- Runs unit tests with Maven
- Builds JAR artifact
- Uploads test reports as artifacts

### 2. Security Scan (`security-scan`)
- Scans codebase with Trivy for vulnerabilities
- Uploads results to GitHub Security tab

### 3. Build and Push Image (`build-and-push-image`)
- Triggered only on `main` and `develop` branches
- Builds optimized multi-stage Docker image
- Pushes to Docker Hub with tags:
  - `latest`
  - Branch-specific tag
  - Commit SHA tag
- Uses Docker build cache for faster subsequent builds

### 4. Deploy to Kubernetes (`deploy-to-kubernetes`)
- Triggered only on `main` branch after image push
- Configures kubectl with cluster credentials
- Updates image reference in Kubernetes manifest
- Performs rolling update with health checks
- Verifies rollout status
- Sends Slack notifications on success/failure

## Prerequisites

### GitHub Secrets (Required)
1. **DOCKER_USERNAME** - Docker Hub username
2. **DOCKER_PASSWORD** - Docker Hub personal access token
3. **KUBE_CONFIG** - Base64-encoded kubeconfig file
4. **SLACK_WEBHOOK_URL** - (Optional) Slack webhook for notifications

#### Generate kubeconfig secret:
```bash
cat ~/.kube/config | base64 | pbcopy  # macOS
cat ~/.kube/config | base64 -w 0 | xclip -selection clipboard  # Linux
```

### Local Prerequisites
- Docker installed
- kubectl configured
- Maven 3.9+
- Java 21+

## Deployment Methods

### Method 1: Automated GitHub Actions (Recommended)
1. Push to `main` branch → automatic deployment
2. Create pull request → runs tests only (no deployment)
3. Merge to `main` → full pipeline executes

### Method 2: Manual Deployment Script
```bash
chmod +x deploy.sh
./deploy.sh
```

Or with custom settings:
```bash
IMAGE_TAG=v1.2.3 NAMESPACE=production ./deploy.sh
```

### Method 3: Manual kubectl
```bash
kubectl apply -f kubernetes-manifest.yaml
kubectl set image deployment/chat-application \
  chat-application=stratagmvx/chat-application:latest
kubectl rollout status deployment/chat-application
```

## Local Development with Docker Compose

### Run locally with auto-rebuild
```bash
docker compose up
```

### Access application
- Chat App: http://localhost:5000
- H2 Console: http://localhost:8082

### Run with volume mounts for hot reload
```bash
docker compose up --pull always
```

## Kubernetes Resources Deployed

1. **Deployment** - 3 replicas with rolling updates
2. **Service** - LoadBalancer exposing port 80 → 5000
3. **ServiceAccount** - For RBAC policies
4. **ClusterRole** - Minimal permissions (pods, services read-only)
5. **HorizontalPodAutoscaler** - Auto-scales 3-10 replicas based on CPU/memory
6. **PodDisruptionBudget** - Ensures minimum availability during cluster operations

## Configuration

### Environment Variables
Set in Kubernetes manifest or via ConfigMap:
```yaml
env:
  - name: SPRING_APPLICATION_NAME
    value: chat-application
  - name: SERVER_PORT
    value: "5000"
  - name: JAVA_OPTS
    value: "-XX:+UseG1GC -XX:MaxGCPauseMillis=200"
```

### Resource Limits
- **Requests**: 256Mi memory, 250m CPU
- **Limits**: 512Mi memory, 500m CPU

Adjust in `kubernetes-manifest.yaml` based on workload:
```yaml
resources:
  requests:
    memory: "512Mi"
    cpu: "500m"
  limits:
    memory: "1Gi"
    cpu: "1000m"
```

## Monitoring & Troubleshooting

### View deployment logs
```bash
kubectl logs -f deployment/chat-application
kubectl logs -f pod/chat-application-xxxxx
```

### Check pod status
```bash
kubectl describe pod chat-application-xxxxx
kubectl get pods -o wide -l app=chat-application
```

### View service endpoint
```bash
kubectl get svc chat-application-service
kubectl port-forward svc/chat-application-service 8080:80
```

### HPA metrics
```bash
kubectl get hpa chat-application-hpa --watch
kubectl describe hpa chat-application-hpa
```

### Rollback failed deployment
```bash
kubectl rollout history deployment/chat-application
kubectl rollout undo deployment/chat-application --to-revision=1
```

## CI/CD Pipeline Workflow

```
Code Push
    ↓
├─ Build & Test (all branches)
├─ Security Scan (all branches)
    ↓
├─ IF main/develop branch:
│  └─ Build & Push Image
│       ↓
│  ├─ IF main branch:
│  │  └─ Deploy to Kubernetes
│  │     ├─ Rolling Update
│  │     ├─ Health Check Verification
│  │     └─ Slack Notification
│  │
│  └─ IF develop branch:
│     └─ Image pushed (no auto-deploy)
```

## Performance Tips

1. **Build Cache**: Leverage Docker layer caching
   - Keep stable dependencies at top of Dockerfile
   - Use `.dockerignore` to exclude unnecessary files

2. **Multi-stage Builds**: Reduces final image size
   - Build stage: Maven build (1.5GB+)
   - Runtime stage: JRE Alpine (100-200MB)

3. **Parallel Pipeline Jobs**: Tests and security scans run in parallel
   - Faster feedback loop
   - Earlier detection of issues

4. **Rolling Updates**: Zero-downtime deployments
   - Gradual pod replacement
   - Health checks ensure readiness

## Security Best Practices

1. **Image Scanning**: Trivy scans for CVEs before deployment
2. **Non-root User**: Container runs as UID 1000
3. **Read-only Filesystem**: Prevents unauthorized modifications
4. **Security Context**: Dropped all Linux capabilities
5. **RBAC**: Minimal permissions via ServiceAccount
6. **Secrets Management**: Use GitHub Secrets for credentials

## Network Configuration

### Service Types
- **LoadBalancer**: External IP for ingress traffic
- **ClusterIP**: (Alternative) Internal-only access

### Session Affinity
Configured as ClientIP to maintain session stickiness across pod replicas.

### DNS
- Internal: `chat-application-service.default.svc.cluster.local`
- Pod domain: `<pod-ip>.default.pod.cluster.local`

## Disaster Recovery

### PodDisruptionBudget
Ensures minimum 1 pod remains available during:
- Node maintenance
- Cluster upgrades
- Forced pod evictions

### Backup Strategy
```bash
# Backup manifest
kubectl get deployment chat-application -o yaml > backup-deployment.yaml
kubectl get service chat-application-service -o yaml > backup-service.yaml

# Restore from backup
kubectl apply -f backup-deployment.yaml
kubectl apply -f backup-service.yaml
```

## Costs Optimization

1. **HPA limits**: 10 max replicas prevents cost runaway
2. **Resource requests**: Proper sizing enables bin-packing
3. **Image caching**: Reduces transfer and storage costs
4. **Graceful termination**: 30s grace period for clean shutdown

## Additional Resources

- [Kubernetes Deployment Docs](https://kubernetes.io/docs/concepts/workloads/controllers/deployment/)
- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Docker Best Practices](https://docs.docker.com/develop/dev-best-practices/)
- [Spring Boot in Kubernetes](https://spring.io/guides/gs/spring-boot-kubernetes/)
