# Ejecución con Docker

1. Crea la configuración local:

   ```powershell
   Copy-Item .env.example .env
   ```

   En Linux/EC2:

   ```bash
   cp .env.example .env
   ```

2. Edita `.env`, define `POSTGRES_PASSWORD` y genera `JWT_SECRET` en Base64.
   En PowerShell puedes generar una clave JWT segura con:

   ```powershell
   $bytes = New-Object byte[] 48
   [Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
   [Convert]::ToBase64String($bytes)
   ```

   En Linux/EC2:

   ```bash
   openssl rand -base64 48
   ```

3. Construye e inicia todos los servicios:

   ```powershell
   docker compose up --build -d
   docker compose ps
   ```

La aplicación queda disponible en `http://localhost` por defecto. Nginx sirve
Angular y reenvía `/api` al backend dentro de la red privada de Compose. Los
puertos de PostgreSQL y Spring solo se publican en `127.0.0.1`; desde Internet
se debe exponer únicamente el puerto del frontend.

En una instancia EC2 de 1 GB, crea al menos 2 GB de swap antes de construir las
imágenes y haz los builds de uno en uno para evitar que el kernel mate Maven o
Angular por falta de memoria:

```bash
docker compose build backend-api
docker compose build frontend-angular
docker compose up -d --wait --wait-timeout 180
docker compose ps
```

Para detenerla sin borrar la base de datos:

```powershell
docker compose down
```

Para borrar también el volumen de PostgreSQL, usa `docker compose down --volumes`.
