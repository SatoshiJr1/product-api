# Plan exécuté — Projet Final G2 (Product API)

## Récap de ce qui a été fait (fichiers créés/modifiés)

### Partie 1 : Kubernetes (10 pts)

| Critère du prof | Points | Fichier | Statut |
|---|---|---|---|
| Namespace `devops-l3gl` | 0.25 | `k8s/mysql.yaml` (ligne 1-4) | ✅ Créé |
| MySQL Deployment | 0.5 | `k8s/mysql.yaml` | ✅ Créé |
| MySQL Service ClusterIP | 0.5 | `k8s/mysql.yaml` | ✅ Créé |
| ConfigMap utilisé et monté | 1 | `k8s/l3gl-devops.yaml` (ConfigMap product-api-config) | ✅ Créé |
| Deployment app configuré | 2.5 | `k8s/l3gl-devops.yaml` (image satoshijr/product-api) | ✅ Créé |
| Service ClusterIP app | 1 | `k8s/l3gl-devops.yaml` (port 8086) | ✅ Créé |
| Ingress HTTP | 1 | `k8s/l3gl-devops.yaml` (ingressClassName nginx) | ✅ Créé |
| Reverse proxy Nginx + `/l3gl/*` | 1.75 | `k8s/nginx.conf` | ✅ Créé |
| ReadinessProbe + LivenessProbe | 0.5 | `k8s/l3gl-devops.yaml` (actuator health) | ✅ Créé |
| Limites CPU/Memory | 1 | `k8s/l3gl-devops.yaml` (200m/400m, 250Mi/500Mi) | ✅ Créé |

### Partie 2 : Monitoring & Observabilité (6 pts)

| Critère du prof | Points | Fichier | Statut |
|---|---|---|---|
| Spring Boot configuré (Prometheus + Loki) | 1 | `pom.xml` + `application.properties` + `logback-spring.xml` | ✅ Modifié/Créé |
| Prometheus configuré (scrape 10s) | 1 | `observability/prometheus.yml` | ✅ Créé |
| Grafana configuré (datasources) | 1 | `observability/docker-compose.yml` | ✅ Créé — config manuelle dans l'UI |
| Dashboard GET `/api/products` | 1 | — | 🔧 À faire dans Grafana UI |
| Dashboard POST `/api/products` | 1 | — | 🔧 À faire dans Grafana UI |
| Logs visibles dans Grafana | 1 | Via Loki datasource | 🔧 À vérifier après déploiement |

### Partie 3 : GitLab CI/CD (2 pts)

| Critère du prof | Points | Fichier | Statut |
|---|---|---|---|
| `.gitlab-ci.yml` présent et jobs configurés | 1 | `.gitlab-ci.yml` | ✅ Créé |
| Jobs test, build, docker exécutés | 1 | — | 🔧 À valider sur GitLab |

---

## Arborescence finale dans product-api/

```
product-api/
├── .gitlab-ci.yml                          ← Pipeline CI/CD
├── Dockerfile                              ← Image Docker (existant)
├── pom.xml                                 ← + actuator + micrometer + loki
├── k8s/
│   ├── mysql.yaml                          ← Namespace + Secret + ConfigMap + Deploy + Svc MySQL
│   ├── l3gl-devops.yaml                    ← ConfigMap app + Deploy + Svc + Ingress product-api
│   └── nginx.conf                          ← Reverse proxy /l3gl/* → Ingress
├── observability/
│   ├── prometheus.yml                      ← Scrape /l3gl/actuator/prometheus toutes les 10s
│   └── docker-compose.yml                  ← Prometheus + Grafana + Loki
└── src/main/resources/
    ├── application.properties              ← + actuator endpoints + probes
    └── logback-spring.xml                  ← Push logs → Loki sur VM2
```

---

## Plan de déploiement AWS (2 instances EC2)

### EC2 1 — Kubernetes (Application)

1. Lancer une instance Ubuntu (t3.medium minimum)
2. Installer Docker, Minikube, kubectl
3. `minikube start --driver=docker`
4. `minikube addons enable ingress`
5. Appliquer les manifestes K8s :
   ```bash
   kubectl apply -f k8s/mysql.yaml
   kubectl apply -f k8s/l3gl-devops.yaml
   ```
6. Installer Nginx sur l'hôte :
   ```bash
   sudo apt install nginx -y
   ```
7. Copier `k8s/nginx.conf` dans `/etc/nginx/sites-available/default`
8. Remplacer `<MINIKUBE_IP>` et `<INGRESS_PORT>` :
   ```bash
   minikube ip                   # → ex: 192.168.49.2
   kubectl get svc -n ingress-nginx  # → port NodePort
   ```
9. `sudo systemctl restart nginx`
10. Tester : `curl http://localhost/l3gl/api/products`

### EC2 2 — Monitoring

1. Lancer une instance Ubuntu (t3.small suffit)
2. Installer Docker + Docker Compose
3. Copier le dossier `observability/` sur cette VM
4. Remplacer `<ADRESSE-IP-VM1>` dans `prometheus.yml` par l'IP publique EC2 1
5. `docker compose up -d`
6. Accéder à Grafana : `http://<IP-VM2>:3000` (admin/admin)
7. Ajouter les datasources :
   - Prometheus → `http://prometheus:9090`
   - Loki → `http://loki:3100`
8. Créer les 2 dashboards

### Avant déploiement K8s — Rebuild de l'image Docker

⚠️ Remplacer `<ADRESSE-IP-VM2>` dans `logback-spring.xml` AVANT de build :
```bash
cd product-api
mvn clean package -DskipTests
docker build -t satoshijr/product-api:latest .
docker push satoshijr/product-api:latest
```

---

## Placeholders à remplacer

| Fichier | Placeholder | Quand |
|---|---|---|
| `logback-spring.xml` | `<ADRESSE-IP-VM2>` | Avant le build Docker |
| `observability/prometheus.yml` | `<ADRESSE-IP-VM1>` | Sur la VM2 avant docker compose up |
| `k8s/nginx.conf` | `<MINIKUBE_IP>:<INGRESS_PORT>` | Sur la VM1 après minikube start |

---

## GitLab CI/CD — Setup

1. Créer un projet sur GitLab (ou importer depuis GitHub)
2. Pousser le code sur la branche `develop`
3. Dans **Settings → CI/CD → Variables**, ajouter :
   - `DOCKER_USERNAME` = `satoshijr`
   - `DOCKER_PASSWORD` = *(ton mot de passe Docker Hub)*
   - `DOCKER_IMAGE` = `satoshijr/product-api:latest`
4. Le pipeline se déclenche automatiquement au push sur `develop`
