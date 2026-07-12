# Kubernetes V3 Deployment Steps (PV + PVC + ConfigMap + Secret)

This guide deploys the following components:

- Namespace
- Persistent Volume (PV)
- Persistent Volume Claim (PVC)
- ConfigMap
- Secret
- MySQL Deployment & Service
- Spring Boot Deployment & Service

---

## Step 1: Build the Spring Boot Application

```bash
gradle clean build
```

---

## Step 2: Build Docker Image

```bash
docker build -t piyushpingale23/lms-springboot-api:3.0 .
```

---

## Step 3: Push Docker Image to Docker Hub

```bash
docker login

docker push piyushpingale23/lms-springboot-api:3.0
```

---

## Step 4: (Optional) Delete Existing Kubernetes Resources

> Use this step only when deploying from scratch or recreating the database.

```bash
kubectl delete deployment lms-api -n lms

kubectl delete deployment lms-mysql-db -n lms

kubectl delete service lms-api-service -n lms

kubectl delete service lms-mysql-db -n lms

kubectl delete pvc mysql-pvc -n lms

kubectl delete pv mysql-pv
```

---

## Step 5: Create Kubernetes Resources

### Create Namespace

```bash
kubectl apply -f common/namespace.yaml
```

### Create Persistent Volume

```bash
kubectl apply -f common/pv.yaml
```

### Create Persistent Volume Claim

```bash
kubectl apply -f common/pvc.yaml
```

### Create ConfigMap

```bash
kubectl apply -f common/configMap.yaml
```

### Create Secret

```bash
kubectl apply -f common/secret.yaml
```

### Deploy MySQL

```bash
kubectl apply -f v3/mysql-deployment-service.yaml
```

### Deploy Spring Boot

```bash
kubectl apply -f v3/springboot-deployment-service.yaml
```

---

## Step 6: Verify Deployment

```bash
kubectl get all -n lms
```

Verify Persistent Volume:

```bash
kubectl get pv
```

Verify Persistent Volume Claim:

```bash
kubectl get pvc -n lms
```

Verify Pods:

```bash
kubectl get pods -n lms
```

Verify Services:

```bash
kubectl get svc -n lms
```

---

## Step 7: Access Spring Boot Application

```bash
minikube service lms-api-service -n lms --url
```

Example:

```
http://192.168.49.2:30893
```

API:

```
http://192.168.49.2:30893/api/learners
```

---

## Step 8: Open Kubernetes Dashboard

```bash
minikube dashboard --url
```

Open the generated URL in your browser.

Switch the namespace from **default** to **lms** to view:

- Deployments
- Pods
- ReplicaSets
- Services
- ConfigMaps
- Secrets
- PersistentVolumeClaims

---

## Step 9: Verify MySQL Database

Connect to the MySQL Pod:

```bash
kubectl exec -it deployment/lms-mysql-db -n lms -- \
mysql -h 127.0.0.1 -u root -p
```

Password:

```
Root@123
```

Run:

```sql
SHOW DATABASES;

USE lms;

SHOW TABLES;
```

---

# Complete Deployment Commands

```bash
# Build Spring Boot
gradle clean build

# Build Docker Image
docker build -t piyushpingale23/lms-springboot-api:3.0 .

# Push Docker Image
docker login
docker push piyushpingale23/lms-springboot-api:3.0

# (Optional) Delete existing resources
kubectl delete deployment lms-api -n lms
kubectl delete deployment lms-mysql-db -n lms
kubectl delete service lms-api-service -n lms
kubectl delete service lms-mysql-db -n lms
kubectl delete pvc mysql-pvc -n lms
kubectl delete pv mysql-pv

# Create Kubernetes resources
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

# Access Spring Boot Application
minikube service lms-api-service -n lms --url

# Open Kubernetes Dashboard
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