# Guide d'Authentification - Digital Banking API

## Vue d'ensemble

L'API Digital Banking utilise maintenant **Spring Security** avec **JWT (JSON Web Token)** pour l'authentification et l'autorisation basée sur les rôles.

## Rôles Disponibles

### 🔑 ADMIN
- Accès complet à toutes les fonctionnalités
- Gestion des utilisateurs
- Accès à tous les endpoints

### 👤 CUSTOMER  
- Accès aux opérations bancaires
- Gestion de ses propres comptes
- Accès limité aux endpoints clients

## Comptes de Test

### Administrateur
- **Username**: `admin`
- **Password**: `admin123`
- **Email**: `admin@digbank.com`
- **Rôle**: ADMIN

### Clients
- **Username**: `abdelkrim` | **Password**: `password123`
- **Username**: `soufiane` | **Password**: `password123`  
- **Username**: `mohamed` | **Password**: `password123`
- **Rôle**: CUSTOMER

## Endpoints d'Authentification

### 🔐 Connexion
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

**Réponse:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "admin",
  "email": "admin@digbank.com",
  "role": "ADMIN",
  "message": "Authentication successful"
}
```

### 📝 Inscription
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "newuser",
  "email": "newuser@example.com",
  "password": "password123",
  "role": "CUSTOMER",
  "name": "Nouveau Client",
  "phone": "0123456789",
  "address": "123 Rue Example"
}
```

## Utilisation du Token JWT

### Dans les Headers HTTP
```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Dans Swagger UI
1. Cliquez sur le bouton **"Authorize"** 🔒
2. Entrez: `Bearer YOUR_JWT_TOKEN`
3. Cliquez sur **"Authorize"**

## Endpoints Sécurisés

### 🔒 Accès ADMIN uniquement
- `GET /api/admin/users` - Liste tous les utilisateurs
- `GET /api/admin/customers` - Liste tous les clients
- `GET /api/admin/users/role/{role}` - Utilisateurs par rôle
- `PUT /api/admin/users/{userId}/status` - Activer/désactiver un utilisateur

### 🔒 Accès ADMIN + CUSTOMER
- `GET /api/customers` - Liste des clients
- `POST /api/customers` - Créer un client
- `GET /api/accounts` - Comptes bancaires
- Toutes les opérations bancaires

### 🌐 Accès Public
- `POST /api/auth/login` - Connexion
- `POST /api/auth/register` - Inscription
- `/swagger-ui/**` - Documentation Swagger
- `/h2-console/**` - Console H2 (développement)

## Configuration JWT

- **Durée de validité**: 24 heures (86400000 ms)
- **Algorithme**: HS256
- **Secret**: Configuré dans `application.properties`

## Exemples d'Utilisation

### 1. Se connecter en tant qu'Admin
```bash
curl -X POST http://localhost:8085/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

### 2. Accéder aux utilisateurs (avec token)
```bash
curl -X GET http://localhost:8085/api/admin/users \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 3. Créer un nouveau client
```bash
curl -X POST http://localhost:8085/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "client1",
    "email": "client1@example.com", 
    "password": "password123",
    "role": "CUSTOMER",
    "name": "Client Test",
    "phone": "0123456789"
  }'
```

## Sécurité

- ✅ Mots de passe chiffrés avec BCrypt
- ✅ Tokens JWT sécurisés
- ✅ Autorisation basée sur les rôles
- ✅ Protection CORS configurée
- ✅ Endpoints publics limités

## Démarrage Rapide

1. **Démarrer l'application**
   ```bash
   mvn spring-boot:run
   ```

2. **Accéder à Swagger UI**
   ```
   http://localhost:8085/swagger-ui.html
   ```

3. **Se connecter avec un compte de test**
4. **Copier le token JWT**
5. **Autoriser dans Swagger avec le token**
6. **Tester les endpoints sécurisés**

---

🎯 **L'implémentation est simple, efficace et compréhensible comme demandé !**
