# Kubernetes V3 Deployment Steps (PV + PVC + ConfigMap + Secret)

This guide demonstrates deploying a Spring Boot application and MySQL database on Kubernetes using:

- Namespace
- ConfigMap
- Secret
- Persistent Volume (PV)
- Persistent Volume Claim (PVC)
- MySQL Deployment & Service
- Spring Boot Deployment & Service

---

# Prerequisites

Verify the required tools are installed.

```bash
docker --version
kubectl version --client
minikube version
gradle --version
```

Start Minikube.

```bash
minikube start
```

Verify the cluster.

```bash
kubectl get nodes
```

Expected:

```text
NAME       STATUS   ROLES           AGE   VERSION
minikube   Ready    control-plane   ...   ...
```

---

# Step 1: Build the Spring Boot Application

```bash
gradle clean build
```

Verify the generated JAR.

```bash
ls build/libs
```

---

# Step 2: Build Docker Image

```bash
docker build -t piyushpingale23/lms-springboot-api:3.0 .
```

Verify:

```bash
docker images
```

Expected:

```text
REPOSITORY                          TAG
piyushpingale23/lms-springboot-api  3.0
```

---

# Step 3: Push Docker Image

Login to Docker Hub.

```bash
docker login
```

Push the image.

```bash
docker push piyushpingale23/lms-springboot-api:3.0
```

---

# Step 4: Verify Image on Docker Hub

(Optional)

```text
https://hub.docker.com/r/piyushpingale23/lms-springboot-api
```

---

# Step 5: (Optional) Delete Existing Kubernetes Resources

Run these commands only if you want a fresh deployment.

```bash
kubectl delete deployment lms-api -n lms

kubectl delete deployment lms-mysql-db -n lms

kubectl delete service lms-api-service -n lms

kubectl delete service lms-mysql-db -n lms

kubectl delete pvc mysql-pvc -n lms

kubectl delete pv mysql-pv
```

---

# Step 6: Create Namespace

```bash
kubectl apply -f common/namespace.yaml
```

Verify:

```bash
kubectl get namespaces
```

---

# Step 7: Create Persistent Volume

```bash
kubectl apply -f common/pv.yaml
```

Verify:

```bash
kubectl get pv
```

---

# Step 8: Create Persistent Volume Claim

```bash
kubectl apply -f common/pvc.yaml
```

Verify:

```bash
kubectl get pvc -n lms
```

Expected:

```text
NAME        STATUS   VOLUME
mysql-pvc   Bound    mysql-pv
```

---

# Step 9: Create ConfigMap

```bash
kubectl apply -f common/configMap.yaml
```

Verify:

```bash
kubectl get configmap -n lms
```

Describe ConfigMap.

```bash
kubectl describe configmap lms-config -n lms
```

---

# Step 10: Create Secret

```bash
kubectl apply -f common/secret.yaml
```

Verify:

```bash
kubectl get secret -n lms
```

Describe Secret.

```bash
kubectl describe secret lms-secret -n lms
```

---

# Step 11: Deploy MySQL

```bash
kubectl apply -f v3/mysql-deployment-service.yaml
```

Verify Deployment.

```bash
kubectl get deployment -n lms
```

Verify Pods.

```bash
kubectl get pods -n lms
```

View logs.

```bash
kubectl logs deployment/lms-mysql-db -n lms
```

---

# Step 12: Deploy Spring Boot

```bash
kubectl apply -f v3/springboot-deployment-service.yaml
```

Verify Deployment.

```bash
kubectl get deployment -n lms
```

Verify Pods.

```bash
kubectl get pods -n lms
```

View logs.

```bash
kubectl logs deployment/lms-api -n lms
```

---

# Step 13: Verify Complete Deployment

```bash
kubectl get all -n lms
```

Expected:

- MySQL Deployment
- Spring Boot Deployment
- MySQL Service
- Spring Boot Service
- Pods
- ReplicaSets

---

# Step 14: Verify PV and PVC

```bash
kubectl get pv
```

```bash
kubectl get pvc -n lms
```

Expected:

```text
PV

mysql-pv      Bound

PVC

mysql-pvc     Bound
```

---

# Step 15: Access Spring Boot Application

Get the NodePort URL.

```bash
minikube service lms-api-service -n lms --url
```

Example:

```text
http://192.168.49.2:30893
```

Access the API.

```text
http://192.168.49.2:30893/api/learners
```

---

# Step 16: Open Kubernetes Dashboard

Get the Dashboard URL.

```bash
minikube dashboard --url
```

Open the generated URL in your browser.

Example:

```text
http://127.0.0.1:39241/api/v1/namespaces/kubernetes-dashboard/services/http:kubernetes-dashboard:/proxy/
```

Switch the namespace from **default** to **lms**.

Verify:

- Deployments
- Pods
- ReplicaSets
- Services
- ConfigMaps
- Secrets
- PersistentVolumeClaims

---

# Step 17: Verify MySQL Database

Connect to the MySQL Pod.

```bash
kubectl exec -it deployment/lms-mysql-db -n lms -- \
mysql -h 127.0.0.1 -u root -p
```

Password:

```text
Root@123
```

Run:

```sql
SHOW DATABASES;

USE lms;

SHOW TABLES;
```

---

# Step 18: Useful Commands

Get all resources.

```bash
kubectl get all -n lms
```

Describe Deployment.

```bash
kubectl describe deployment lms-api -n lms
```

Describe Pod.

```bash
kubectl describe pod <pod-name> -n lms
```

View Spring Boot logs.

```bash
kubectl logs deployment/lms-api -n lms
```

View MySQL logs.

```bash
kubectl logs deployment/lms-mysql-db -n lms
```

Delete all resources.

```bash
kubectl delete -f v3/
kubectl delete -f common/
```

---

# Complete Deployment Commands

```bash
# Start Minikube
minikube start

# Verify Cluster
kubectl get nodes

# Build Spring Boot
gradle clean build

# Build Docker Image
docker build -t piyushpingale23/lms-springboot-api:3.0 .

# Push Docker Image
docker login
docker push piyushpingale23/lms-springboot-api:3.0

# Create Kubernetes Resources
kubectl apply -f common/namespace.yaml
kubectl apply -f common/pv.yaml
kubectl apply -f common/pvc.yaml
kubectl apply -f common/configMap.yaml
kubectl apply -f common/secret.yaml
kubectl apply -f v3/mysql-deployment-service.yaml
kubectl apply -f v3/springboot-deployment-service.yaml

# Verify
kubectl get all -n lms
kubectl get pv
kubectl get pvc -n lms

# Spring Boot URL
minikube service lms-api-service -n lms --url

# Kubernetes Dashboard
minikube dashboard --url
```

---

## Project Structure

```text
kubernetes/
│
├── common/
│   ├── namespace.yaml
│   ├── pv.yaml
│   ├── pvc.yaml
│   ├── configMap.yaml
│   └── secret.yaml
│
├── v1/
│
├── v2/
│
└── v3/
    ├── mysql-deployment-service.yaml
    ├── springboot-deployment-service.yaml
    └── deployment-service-steps.md
```