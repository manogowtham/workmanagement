# Railway Deployment Guide

## Prerequisites
- GitHub account
- Railway account (sign up at railway.app)

## Environment Variables Required in Railway:
- `SPRING_PROFILES_ACTIVE=prod`
- `DATABASE_URL` (auto-set by Railway MySQL service)
- `DB_USERNAME` (auto-set by Railway MySQL service)  
- `DB_PASSWORD` (auto-set by Railway MySQL service)

## Deployment Steps:
1. Push code to GitHub repository
2. Connect GitHub repo to Railway
3. Add MySQL database service
4. Set environment variables
5. Deploy!

## Local Testing:
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

## Build for Production:
```bash
./mvnw clean package -DskipTests
```

## Important Files:
- `application-prod.properties` - Production configuration
- `system.properties` - Java version specification
- `Procfile` - Railway startup command