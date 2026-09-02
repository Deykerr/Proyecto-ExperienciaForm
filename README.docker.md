# Ejecución con Docker

1. Crea la configuración local:

   ```powershell
   Copy-Item .env.example .env
   ```

2. Edita `.env`, define `POSTGRES_PASSWORD` y genera `JWT_SECRET` en Base64.
   En PowerShell puedes generar una clave JWT segura con:

   ```powershell
   $bytes = New-Object byte[] 48
   [Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
   [Convert]::ToBase64String($bytes)
   ```

3. Construye e inicia todos los servicios:

   ```powershell
   docker compose up --build -d
   docker compose ps
   ```

La aplicación queda disponible en `http://localhost` por defecto. Nginx sirve
Angular y reenvía `/api` al backend dentro de la red privada de Compose.

Para detenerla sin borrar la base de datos:

```powershell
docker compose down
```

Para borrar también el volumen de PostgreSQL, usa `docker compose down --volumes`.
