#!/bin/bash
# Deployment script for Chat Application to Kubernetes

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
IMAGE_REGISTRY="${IMAGE_REGISTRY:-docker.io}"
IMAGE_NAME="${IMAGE_NAME:-stratagmvx/chat-application}"
IMAGE_TAG="${IMAGE_TAG:-latest}"
NAMESPACE="${NAMESPACE:-default}"
KUBECONFIG="${KUBECONFIG:-$HOME/.kube/config}"

echo -e "${YELLOW}=== Chat Application Deployment ===${NC}"

# Check prerequisites
check_prerequisites() {
    echo -e "${YELLOW}Checking prerequisites...${NC}"
    
    if ! command -v kubectl &> /dev/null; then
        echo -e "${RED}kubectl is not installed${NC}"
        exit 1
    fi
    
    if ! command -v docker &> /dev/null; then
        echo -e "${RED}Docker is not installed${NC}"
        exit 1
    fi
    
    if [ ! -f "$KUBECONFIG" ]; then
        echo -e "${RED}Kubeconfig file not found at $KUBECONFIG${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}✓ All prerequisites met${NC}"
}

# Build Docker image
build_image() {
    echo -e "${YELLOW}Building Docker image...${NC}"
    docker build -t "${IMAGE_REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}" \
                 -t "${IMAGE_REGISTRY}/${IMAGE_NAME}:latest" .
    echo -e "${GREEN}✓ Image built successfully${NC}"
}

# Push Docker image
push_image() {
    echo -e "${YELLOW}Pushing Docker image to registry...${NC}"
    docker push "${IMAGE_REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}"
    docker push "${IMAGE_REGISTRY}/${IMAGE_NAME}:latest"
    echo -e "${GREEN}✓ Image pushed successfully${NC}"
}

# Deploy to Kubernetes
deploy_to_kubernetes() {
    echo -e "${YELLOW}Deploying to Kubernetes...${NC}"
    
    # Update image in manifest
    sed "s|image: .*|image: ${IMAGE_REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}|g" kubernetes-manifest.yaml | \
    kubectl apply -f -
    
    echo -e "${GREEN}✓ Manifest applied${NC}"
}

# Wait for deployment to be ready
wait_for_deployment() {
    echo -e "${YELLOW}Waiting for deployment to be ready...${NC}"
    kubectl rollout status deployment/chat-application -n "${NAMESPACE}" --timeout=5m
    echo -e "${GREEN}✓ Deployment is ready${NC}"
}

# Verify deployment
verify_deployment() {
    echo -e "${YELLOW}Verifying deployment...${NC}"
    
    echo -e "\n${YELLOW}Pods:${NC}"
    kubectl get pods -l app=chat-application -n "${NAMESPACE}"
    
    echo -e "\n${YELLOW}Services:${NC}"
    kubectl get svc chat-application-service -n "${NAMESPACE}"
    
    echo -e "\n${YELLOW}Deployment Status:${NC}"
    kubectl describe deployment chat-application -n "${NAMESPACE}"
    
    echo -e "${GREEN}✓ Verification complete${NC}"
}

# Main execution
main() {
    check_prerequisites
    build_image
    push_image
    deploy_to_kubernetes
    wait_for_deployment
    verify_deployment
    
    echo -e "\n${GREEN}=== Deployment Complete ===${NC}"
    echo -e "${GREEN}Chat Application is running at:${NC}"
    kubectl get svc chat-application-service -n "${NAMESPACE}" -o wide
}

main "$@"
