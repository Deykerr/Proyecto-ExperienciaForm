# Guía de pruebas locales con Docker y VS Code

Esta modalidad usa el proyecto Compose `botica-demo`, un volumen separado y
credenciales conocidas únicamente para desarrollo. No modifica una instalación
Compose levantada con el nombre normal del repositorio.

> No uses `.env.local.example`, sus usuarios ni sus contraseñas en EC2 o producción.

## 1. Requisitos

- Docker Desktop iniciado.
- Visual Studio Code abierto en la carpeta raíz del proyecto.
- Docker Compose v2 y Buildx 0.17 o posterior.

En la terminal PowerShell integrada de VS Code comprueba:

```powershell
docker version
docker compose version
docker buildx version
```

Si `docker buildx version` no existe o es anterior a 0.17, actualiza Docker
Desktop. No descargues manualmente una etiqueta de imagen inventada para Buildx.

## 2. Construir y levantar el entorno demo

Desde la carpeta donde está `docker-compose.yml` ejecuta:

```powershell
docker compose -p botica-demo --env-file .env.local.example config
docker compose -p botica-demo --env-file .env.local.example up -d --build --wait --wait-timeout 240
docker compose -p botica-demo --env-file .env.local.example ps
```

El primer arranque puede tardar varios minutos porque compila Spring Boot y
Angular. Los tres servicios deben terminar con estado `healthy`.

Abre en el navegador:

```text
http://localhost:8080
```

El backend queda disponible solo desde la computadora local en el puerto 9091 y
PostgreSQL en el 5433, para no interferir normalmente con instalaciones locales.

## 3. Usuarios de prueba

| Usuario | Contraseña | Rol | Funciones principales |
|---|---|---|---|
| `admin` | `AdminLocal2026*` | `ADMIN` | Acceso completo, usuarios, reportes, anulaciones, SUNAT y DIGEMID. |
| `cajero` | `CajeroLocal2026*` | `CAJERO` | Punto de venta, apertura/cierre de caja, clientes, devoluciones y recetas. |
| `almacenero` | `AlmacenLocal2026*` | `ALMACENERO` | Compras, recepción, inventario, productos, proveedores y alertas. |

La semilla solo se ejecuta con `SPRING_PROFILES_ACTIVE=dev` y
`DEMO_DATA_ENABLED=true`. Es idempotente: crea únicamente los usuarios y registros
demo que falten; no reemplaza contraseñas de usuarios ya existentes.

## 4. Datos preparados para pruebas

La pestaña **Alertas de inventario** debe mostrar inicialmente seis situaciones:

- Ibuprofeno con stock crítico.
- Amoxicilina agotada.
- Vitamina C que vence en cinco días.
- Loratadina con un lote vencido y existencias.
- Omeprazol con stock bajo y lote que vence en veinte días.

También existen:

- Paracetamol con stock normal para realizar ventas.
- Clonazepam con receta retenida para probar la validación de recetas.
- Cliente público general.
- Un proveedor ficticio para registrar compras y recepciones.

## 5. Flujo manual completo y orden recomendado

Haz las pruebas en este orden porque cada operación alimenta a la siguiente. Si
quieres repetirlas desde cero, reinicia primero el volumen demo como se explica
en la última sección.

### Paso 1: acceso y permisos

1. Entra con cada usuario y confirma que el menú cambie según su rol.
2. `almacenero` no debe entrar a usuarios ni clientes.
3. `cajero` no debe entrar a lotes, compras ni usuarios.
4. `admin` debe tener acceso completo.

Un acceso prohibido debe responder 403 o redirigir a una pantalla permitida; no
debe mostrar el mensaje genérico de error del servidor.

### Paso 2: inventario inicial y alertas (`almacenero`)

1. Abre **Productos**: deben existir siete productos demo.
2. Abre **Kardex / Conteos**: la lista de lotes debe cargar sin error.
3. Abre **Alertas de inventario**: deben aparecer seis alertas iniciales.
4. Comprueba que se distingan stock bajo, agotado, próximo a vencer y vencido.

### Paso 3: compra, recepción y devolución a proveedor (`almacenero`)

1. Crea una compra al proveedor demo con documento `OC-PRUEBA-001`.
2. Agrega 20 unidades de **Ibuprofeno 400 mg Demo** con un costo válido.
3. Registra la recepción con lote `IBU-PRUEBA-001`, vencimiento superior a un
   año y documento `FAC-PRUEBA-001`.
4. Verifica en Productos, Lotes y Kardex que el stock aumentó y que la alerta de
   stock crítico desapareció al superar el mínimo.
5. Registra una devolución de 2 unidades de esa recepción y comprueba que el
   lote, el stock y el Kardex disminuyan exactamente en 2.

Usa códigos de documento y lote distintos (`...-002`, `...-003`) si repites la
prueba sin limpiar la base.

### Paso 4: conteo físico y conciliación (`almacenero`)

1. En **Kardex / Conteos**, selecciona el lote recién recibido.
2. Registra un conteo físico con una unidad menos que el saldo mostrado.
3. Finaliza el conteo y verifica un ajuste negativo de una unidad.
4. Confirma el mismo resultado en producto, lote, Kardex y conciliación. La suma
   de los lotes debe coincidir con el stock total del producto.

### Paso 5: apertura de caja y venta normal (`cajero`)

1. Abre caja con S/ 100.00.
2. En el POS vende una unidad de **Paracetamol 500 mg Demo**, emite boleta y
   paga S/ 8.50 en efectivo.
3. Comprueba que se genere el comprobante, la venta figure como `PAGADA`, el
   movimiento ingrese a caja y el stock baje de 60 a 59.
4. Haz doble clic una sola vez durante el cobro: no deben generarse dos ventas
   porque la clave de idempotencia protege la operación.

### Paso 6: receta y producto controlado (`cajero`)

1. Intenta vender **Clonazepam 0.5 mg Demo** sin receta: debe rechazarse.
2. Crea un cliente identificado y registra una receta válida para Clonazepam.
3. Vuelve al POS, selecciona esa receta y vende una unidad.
4. Comprueba que la receta retenida cambie al estado correspondiente, conserve
   la trazabilidad del paciente y no pueda reutilizarse por encima de su saldo.

### Paso 7: devolución y anulación

1. Como `cajero`, abre **Operaciones de venta**, elige la venta de Paracetamol y
   devuelve una unidad con un motivo claro.
2. Verifica el reembolso, el movimiento negativo de caja y la reposición exacta
   del stock en el lote original.
3. Realiza otra venta pequeña. Luego entra como `admin` y anúlala.
4. Comprueba que no pueda devolverse o anularse dos veces y que la auditoría
   conserve usuario, fecha, motivo y referencia.

### Paso 8: arqueo y cierre (`cajero` y `admin`)

1. Revisa el efectivo esperado: monto inicial más cobros, menos reembolsos y
   retiros, más ingresos manuales.
2. Cierra la caja registrando las denominaciones realmente contadas.
3. Primero prueba un cierre exacto. En otro ciclo provoca una diferencia pequeña
   y comprueba que requiera la aprobación de `admin`.

### Paso 9: control y cumplimiento (`admin`)

1. Revisa usuarios, ventas, compras, caja, stock, Kardex y conciliación.
2. Genera los reportes de ventas, inventario, caja y productos por vencer.
3. Comprueba el estado de documentos electrónicos SUNAT. En el entorno local se
   valida la generación y la cola; no se envía a producción.
4. Genera el reporte de precios DIGEMID y revisa sus totales y estado.
5. Confirma que **Alertas de inventario** refleje todas las operaciones previas.

## 6. Pruebas automáticas

Backend, desde la raíz del proyecto:

```powershell
cd BOTICA-BACK
.\mvnw.cmd test
cd ..
```

Frontend:

```powershell
cd botica-angular-front
npm run build
cd ..
```

La suite backend incluye pruebas unitarias de reglas de stock, lotes, pagos,
precios y SUNAT, además de integración HTTP para productos, lotes y el flujo
apertura de caja -> venta -> comprobante -> descuento de stock.

## 7. Diagnóstico

Ver todos los contenedores:

```powershell
docker compose -p botica-demo --env-file .env.local.example ps
```

Ver los últimos registros del backend:

```powershell
docker compose -p botica-demo --env-file .env.local.example logs backend-api --tail 150
```

Ver los registros en tiempo real:

```powershell
docker compose -p botica-demo --env-file .env.local.example logs -f
```

Comprobar la salud del backend:

```powershell
Invoke-RestMethod http://localhost:9091/actuator/health/readiness
```

Comprobar los usuarios directamente en la base demo:

```powershell
docker compose -p botica-demo --env-file .env.local.example exec postgres-db psql -U postgres -d botica_demo -c "select username, rol, estado from usuarios order by id_usuario;"
```

## 8. Detener, reconstruir o reiniciar los datos

Detener y conservar la base:

```powershell
docker compose -p botica-demo --env-file .env.local.example down
```

Reconstruir después de cambiar código:

```powershell
docker compose -p botica-demo --env-file .env.local.example up -d --build --wait --wait-timeout 240
```

Recrear solamente el backend:

```powershell
docker compose -p botica-demo --env-file .env.local.example up -d --build --force-recreate backend-api
```

Reiniciar completamente la base demo:

```powershell
docker compose -p botica-demo --env-file .env.local.example down --volumes
docker compose -p botica-demo --env-file .env.local.example up -d --build --wait --wait-timeout 240
```

`down --volumes` borra toda la información del proyecto `botica-demo`. El nombre
de proyecto fijo evita apuntar por accidente al volumen de otra instalación.
