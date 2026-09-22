# Ejecución con Docker

Para probar la interfaz localmente con usuarios `ADMIN`, `CAJERO` y `ALMACENERO`,
datos de inventario y alertas preparadas, consulta
[`GUIA_PRUEBAS_DOCKER.md`](GUIA_PRUEBAS_DOCKER.md). Ese flujo usa el proyecto
Compose aislado `botica-demo` y no debe utilizarse en EC2.

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

   Mantén `DEMO_DATA_ENABLED=false` en EC2. El perfil de producción no crea
   ventas ni productos ficticios.

   Para una base nueva, crea el primer administrador de forma controlada:

   1. Define temporalmente `BOOTSTRAP_ADMIN_ENABLED=true`,
      `BOOTSTRAP_ADMIN_USERNAME`, `BOOTSTRAP_ADMIN_PASSWORD` y
      `BOOTSTRAP_ADMIN_FULL_NAME` en `.env`.
   2. Inicia el stack y verifica que puedas ingresar.
   3. Cambia `BOOTSTRAP_ADMIN_ENABLED=false`, elimina el valor de
      `BOOTSTRAP_ADMIN_PASSWORD` y recrea solo el backend con
      `docker compose up -d --force-recreate backend-api`.

   El aprovisionamiento solo crea el usuario cuando la tabla de usuarios está
   vacía y aplica la misma política de contraseña fuerte que la aplicación.

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

## Migraciones y actualización de una instalación existente

El backend usa Flyway y `spring.jpa.hibernate.ddl-auto=validate`. Al iniciar, aplica
las migraciones pendientes en orden y luego comprueba que las entidades coincidan
con PostgreSQL. La migración V3 agrega recetas, arqueos, devoluciones, recepción de
compras, documentos electrónicos, Resúmenes Diarios SUNAT y reportes DIGEMID.

Antes de actualizar producción, crea un respaldo y conserva el volumen actual:

```bash
docker compose exec -T postgres-db sh -c 'pg_dump -U "$POSTGRES_USER" -d "$POSTGRES_DB" -Fc' > botica-antes-v3.dump
docker compose build backend-api
docker compose build frontend-angular
docker compose up -d --wait --wait-timeout 180
docker compose logs backend-api --tail 150
```

No uses `docker compose down --volumes` durante una actualización: esa opción borra
la base de datos persistente.

## Flujo operativo incorporado

- Una orden de compra no incrementa stock. El stock cambia al confirmar una
  recepción con lote, vencimiento, cantidad y costo; admite recepciones parciales.
- La devolución a proveedor descuenta el lote exacto y registra su movimiento de
  kardex. No permite devolver más de lo recibido ni más de lo físicamente disponible.
- Una venta con productos `CON_RECETA_MEDICA` o
  `CON_RECETA_MEDICA_RETENIDA` necesita una receta vigente y con saldo suficiente.
- Una devolución de venta repone el lote originalmente vendido, revierte el saldo
  dispensado de la receta, registra reembolsos por medio de pago y genera la nota
  de crédito cuando corresponde.
- La anulación total requiere rol `ADMIN`. Los arqueos cuentan denominaciones PEN y
  comparan solo efectivo físico: fondo inicial + cobros en efectivo + ingresos -
  egresos. Yape, Plin y tarjeta se muestran separados.

## Configuración SUNAT

La integración inicia deshabilitada. Configura primero el entorno BETA y nunca
guardes el certificado ni la Clave SOL en Git:

```dotenv
SUNAT_ENABLED=true
SUNAT_MODE=BETA
SUNAT_RUC=20123456789
SUNAT_SOL_USER=USUARIO_SOL
SUNAT_SOL_PASSWORD=CLAVE_SOL
SUNAT_CERTIFICATE_BASE64=CONTENIDO_PKCS12_EN_BASE64
SUNAT_CERTIFICATE_PASSWORD=CLAVE_DEL_CERTIFICADO
SUNAT_RAZON_SOCIAL=BOTICA EJEMPLO S.A.C.
SUNAT_NOMBRE_COMERCIAL=BOTICA EJEMPLO
SUNAT_UBIGEO=150101
SUNAT_DIRECCION=AV. EJEMPLO 123
SUNAT_DEPARTAMENTO=LIMA
SUNAT_PROVINCIA=LIMA
SUNAT_DISTRITO=LIMA
```

El sistema genera y firma XML, conserva su hash y ejecuta reintentos con espera
progresiva. Las facturas y notas vinculadas a facturas usan `sendBill`; las boletas
y sus notas se agrupan por fecha en un Resumen Diario, usan `sendSummary` y luego
consultan el ticket con `getStatus`. Una nota espera primero la aceptación del
comprobante original. Solo una CDR real puede cambiar el estado a `ACEPTADO` u
`OBSERVADO`.

Completar estas variables no reemplaza el alta como emisor electrónico, la vigencia
del certificado ni las pruebas en BETA. Cambia a `SUNAT_MODE=PRODUCCION` únicamente
después de validar los casos tributarios de la botica con su contador o integrador.

## Reporte de precios DIGEMID

La pantalla **SUNAT / DIGEMID** genera un snapshot mensual de productos activos con
registro sanitario, precio público, stock, fecha de vigencia y SHA-256. El CSV usa
UTF-8 y conserva los valores usados en el reporte. El sistema no marca el reporte
como enviado o aceptado por sí mismo: un administrador debe registrar la constancia
obtenida en el portal SNIPPF/Observatorio y, si existe, sus observaciones.

El archivo es una salida de control y conciliación. Antes de usarlo como archivo de
carga masiva, confirma con DIGEMID o con el gestor de tu jurisdicción la plantilla
vigente asignada al establecimiento; el proyecto no inventa una aceptación ni una
respuesta de un servicio externo que no haya sido configurado.
