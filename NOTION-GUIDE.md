# Guide Notion — Projet Final DevOps G2 (Product API)

> Ce fichier te sert de template pour rédiger ton rapport Notion.
> Copie chaque section dans Notion et remplace les `[CAPTURE]` par tes screenshots.

---

## 📌 Informations Générales

- **Groupe** : G2
- **Projet** : Mini Product Inventory API
- **Étudiant** : [TON NOM]
- **Infrastructure** : 2 instances EC2 AWS
- **Repo GitLab** : [LIEN GITLAB]
- **Repo GitHub** : https://github.com/SatoshiJr1/product-api
- **Docker Hub** : https://hub.docker.com/r/satoshijr/product-api

---

## 1️⃣ Partie Kubernetes — Déploiement Spring Boot + MySQL

### 1.1 Namespace `devops-l3gl`

```bash
kubectl get namespaces
```
[CAPTURE : résultat montrant devops-l3gl]

### 1.2 MySQL — Deployment + Service

**Fichier `k8s/mysql.yaml` :**
```yaml
# Coller le contenu complet de k8s/mysql.yaml ici
```

```bash
kubectl get pods -n devops-l3gl
kubectl get svc -n devops-l3gl
```
[CAPTURE : pods MySQL running]
[CAPTURE : service MySQL ClusterIP]

### 1.3 Application product-api — ConfigMap + Deployment + Service

**Fichier `k8s/l3gl-devops.yaml` :**
```yaml
# Coller le contenu complet de k8s/l3gl-devops.yaml ici
```

```bash
kubectl get configmap -n devops-l3gl
kubectl get pods -n devops-l3gl
kubectl get svc -n devops-l3gl
kubectl describe pod <product-api-pod> -n devops-l3gl
```
[CAPTURE : ConfigMap créée]
[CAPTURE : pod product-api Running]
[CAPTURE : service product-api ClusterIP port 8086]
[CAPTURE : describe pod montrant les probes + limites resources]

### 1.4 Ingress HTTP

```bash
kubectl get ingress -n devops-l3gl
```
[CAPTURE : ingress product-api-ingress]

### 1.5 Reverse Proxy Nginx

**Fichier `k8s/nginx.conf` :**
```nginx
# Coller le contenu de nginx.conf ici
```

```bash
curl http://<IP-VM1>/l3gl/api/products
```
[CAPTURE : réponse JSON depuis l'extérieur via /l3gl/]

### 1.6 Probes Actuator

```bash
curl http://<IP-VM1>/l3gl/actuator/health/readiness
curl http://<IP-VM1>/l3gl/actuator/health/liveness
```
[CAPTURE : réponses {"status":"UP"}]

### 1.7 Limites de ressources

```bash
kubectl describe pod <product-api-pod> -n devops-l3gl | grep -A 5 "Limits\|Requests"
```
[CAPTURE : CPU 200m/400m, Memory 250Mi/500Mi]

---

## 2️⃣ Partie Monitoring — Prometheus, Grafana, Loki

### 2.1 Configuration Spring Boot

**Dépendances ajoutées dans `pom.xml` :**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
<dependency>
    <groupId>com.github.loki4j</groupId>
    <artifactId>loki-logback-appender</artifactId>
    <version>1.4.1</version>
</dependency>
```

**`application.properties` (extrait) :**
```properties
management.endpoints.web.exposure.include=health,info,prometheus,metrics
management.endpoint.prometheus.enabled=true
management.endpoint.health.probes.enabled=true
management.health.livenessState.enabled=true
management.health.readinessState.enabled=true
```

**`logback-spring.xml` :**
```xml
# Coller le contenu de logback-spring.xml ici
```

```bash
curl http://<IP-VM1>/l3gl/actuator/prometheus | head -20
```
[CAPTURE : métriques Prometheus exposées]

### 2.2 Prometheus

**Fichier `observability/prometheus.yml` :**
```yaml
global:
  scrape_interval: 10s

scrape_configs:
  - job_name: 'product-api'
    metrics_path: '/l3gl/actuator/prometheus'
    static_configs:
      - targets: ['<IP-VM1>:80']
```

[CAPTURE : Prometheus UI (http://<IP-VM2>:9090) → Status → Targets → product-api UP]

### 2.3 Grafana — Datasources

[CAPTURE : Grafana → Configuration → Data Sources → Prometheus configuré]
[CAPTURE : Grafana → Configuration → Data Sources → Loki configuré]

### 2.4 Dashboard GET /api/products

Requête PromQL :
```
rate(http_server_requests_seconds_count{method="GET",uri="/api/products"}[5m])
```

[CAPTURE : Dashboard Grafana montrant le graphe GET]

### 2.5 Dashboard POST /api/products

Requête PromQL :
```
rate(http_server_requests_seconds_count{method="POST",uri="/api/products"}[5m])
```

[CAPTURE : Dashboard Grafana montrant le graphe POST]

### 2.6 Logs dans Grafana (Loki)

[CAPTURE : Grafana → Explore → Loki → logs de product-api visibles]

### 2.7 Docker Compose (VM2)

**Fichier `observability/docker-compose.yml` :**
```yaml
# Coller le contenu de docker-compose.yml ici
```

```bash
docker ps
```
[CAPTURE : conteneurs prometheus + grafana + loki running sur VM2]

---

## 3️⃣ Partie GitLab CI/CD

### 3.1 Fichier `.gitlab-ci.yml`

```yaml
# Coller le contenu complet de .gitlab-ci.yml ici
```

### 3.2 Variables CI/CD

[CAPTURE : GitLab → Settings → CI/CD → Variables (DOCKER_USERNAME, DOCKER_PASSWORD, DOCKER_IMAGE)]

### 3.3 Pipeline exécuté

[CAPTURE : GitLab → CI/CD → Pipelines → pipeline vert (test ✅ → build ✅ → docker ✅)]
[CAPTURE : Détail du job `test`]
[CAPTURE : Détail du job `build`]
[CAPTURE : Détail du job `docker` (push réussi)]

---

## 4️⃣ Tests API en direct

```bash
# Depuis la VM1 ou depuis l'extérieur

# Créer un produit
curl -X POST http://<IP-VM1>/l3gl/api/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Laptop","description":"PC portable","price":999.99,"quantity":10}'

# Lister les produits
curl http://<IP-VM1>/l3gl/api/products

# Mettre à jour la quantité
curl -X PUT http://<IP-VM1>/l3gl/api/products/1 \
  -H "Content-Type: application/json" \
  -d '{"quantity": 3}'

# Compter les produits en stock faible
curl http://<IP-VM1>/l3gl/api/products/low-stock/count
```

[CAPTURE : résultat de chaque curl]

---

## 📹 Vidéo de démonstration (7-15 min)

### Plan de la vidéo (dans cet ordre) :

1. **Kubernetes (3-5 min)**
   - Montrer `kubectl get all -n devops-l3gl`
   - Montrer l'accès via `curl http://<IP>/l3gl/api/products`
   - Montrer les probes, limites resources
   - Montrer la config Nginx

2. **Monitoring (3-5 min)**
   - Montrer Prometheus targets UP
   - Montrer les 2 dashboards Grafana (GET + POST)
   - Montrer les logs dans Grafana via Loki
   - Faire un curl POST en live et voir le graphe bouger

3. **GitLab CI/CD (2-3 min)**
   - Montrer le `.gitlab-ci.yml`
   - Montrer le pipeline passé en vert
   - Montrer les 3 jobs (test, build, docker)

---

## Checklist captures à prendre

- [ ] `kubectl get namespaces` → devops-l3gl
- [ ] `kubectl get pods -n devops-l3gl` → MySQL + product-api Running
- [ ] `kubectl get svc -n devops-l3gl` → ClusterIP MySQL + product-api
- [ ] `kubectl get ingress -n devops-l3gl`
- [ ] `kubectl describe pod` → probes + resources
- [ ] `curl /l3gl/api/products` via IP publique
- [ ] `curl /l3gl/actuator/health/readiness`
- [ ] `curl /l3gl/actuator/prometheus` (extrait)
- [ ] Prometheus UI → Targets UP
- [ ] Grafana → datasource Prometheus
- [ ] Grafana → datasource Loki
- [ ] Grafana → Dashboard GET
- [ ] Grafana → Dashboard POST
- [ ] Grafana → Explore → Logs Loki
- [ ] `docker ps` sur VM2 (prometheus + grafana + loki)
- [ ] GitLab → Variables CI/CD
- [ ] GitLab → Pipeline vert
- [ ] GitLab → Job test
- [ ] GitLab → Job build
- [ ] GitLab → Job docker
- [ ] curl POST /api/products
- [ ] curl GET /api/products
- [ ] curl PUT /api/products/1
- [ ] curl GET /api/products/low-stock/count
