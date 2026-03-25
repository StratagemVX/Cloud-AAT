## Deployment Instructions

### Prerequisites
- Docker Desktop installed
- Kubernetes cluster (minikube, Docker Desktop K8s, or cloud provider)
- kubectl CLI configured

### Step 1: Push Docker Image to Registry

For **local testing** with Docker Desktop:
```bash
# No push needed — image exists locally
docker images | findstr chat-application
```

For **production** (Docker Hub):
```bash
# Tag image
docker tag chat-application:1.0 your-username/chat-application:1.0

# Log in to Docker Hub
docker login

# Push image
docker push your-username/chat-application:1.0

# Update kubernetes-manifest.yaml: change image to your-username/chat-application:1.0
```

For **private registry**:
```bash
docker tag chat-application:1.0 registry.example.com/chat-application:1.0
docker push registry.example.com/chat-application:1.0
```

### Step 2: Deploy to Kubernetes

**Using kubectl:**
```bash
# Apply manifest
kubectl apply -f kubernetes-manifest.yaml

# Verify deployment
kubectl get deployments
kubectl get pods
kubectl get services
```

**Check deployment status:**
```bash
# Watch rollout
kubectl rollout status deployment/chat-application

# View pod logs
kubectl logs -f deployment/chat-application

# Describe deployment (troubleshooting)
kubectl describe deployment/chat-application
```

### Step 3: Access Application

**Local (Docker Desktop K8s):**
```bash
# Port-forward to localhost
kubectl port-forward svc/chat-application-service 5000:80

# Access at http://localhost:5000
```

**With LoadBalancer (cloud provider):**
```bash
# Get external IP
kubectl get services

# Access at http://<EXTERNAL-IP>:80
```

### Deployment Configuration

**Replicas:** 3 instances
- Auto-restarts on failure
- Pod anti-affinity (spread across nodes)
- Resource requests: 256Mi memory, 250m CPU
- Resource limits: 512Mi memory, 500m CPU
- Liveness probe: `/health` (30s interval)
- Readiness probe: `/health` (5s interval)

**Service Type:** LoadBalancer
- Port 80 → Container port 5000
- Session affinity: ClientIP

### Cleanup

```bash
# Delete deployment
kubectl delete -f kubernetes-manifest.yaml

# Verify removal
kubectl get all
```

### Troubleshooting

| Issue | Solution |
|---|---|
| ImagePullBackOff | Change `imagePullPolicy` to `IfNotPresent` or push to accessible registry |
| CrashLoopBackOff | Check logs: `kubectl logs <pod-name>` |
| Pending | Check resources: `kubectl describe pod <pod-name>` |
| Can't access app | Verify service: `kubectl get svc` and port-forward |
