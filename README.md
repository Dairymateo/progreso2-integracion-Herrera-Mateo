# Progreso 2 - Integración de Sistema de Citas Médicas

**1. Nombre del estudiante:** Mateo Herrera

---

## 2. Descripción breve de la solución
Este proyecto implementa una capa de integración mediante **Apache Camel** y **Spring Boot** para gestionar el registro de citas médicas. Expone un endpoint REST para recibir solicitudes de citas, valida los datos y distribuye la información hacia diferentes sistemas de destino usando patrones de integración empresarial (EIPs) apoyados por el broker de mensajería **RabbitMQ** y manipulación de archivos locales.

---

## 3. Tecnologías utilizadas
- **Java 17 / 24** (Compatible)
- **Spring Boot 3.2.5** (Web, RabbitMQ, Validation)
- **Apache Camel 4.6.0** (Core, Spring Boot, RabbitMQ, File)
- **RabbitMQ** (Docker)
- **Swagger / OpenAPI** (springdoc-openapi)
- **Maven**

---

## 4. Instrucciones para levantar RabbitMQ
El proyecto incluye un archivo `docker-compose.yml` preconfigurado. Para levantar la instancia local de RabbitMQ con su interfaz de administración, ejecuta en la raíz del proyecto:
```bash
docker-compose up -d
```
El panel de administración estará disponible en `http://localhost:15672` con credenciales `guest` / `guest`.

---

## 5. Instrucciones para ejecutar la aplicación
Una vez RabbitMQ esté corriendo, puedes iniciar la aplicación usando Maven:
```bash
mvn clean spring-boot:run
```
La aplicación arrancará en el puerto **8080**.

---

## 6. Endpoint disponible
La API expone un endpoint POST documentado mediante Swagger (disponible visualmente en `http://localhost:8080/swagger-ui.html`):

**`POST /api/citas`**
- **Descripción:** Recibe el payload de una cita médica, lo valida y lo procesa mediante las rutas de Camel.
- **Content-Type:** `application/json`

También incluye un endpoint de salud:
**`GET /api/citas/status`**

---

## 7. Ejemplo de request válido
Cita correcta con todos los campos y valor positivo:

```json
{
  "idCita": "C-001",
  "paciente": "Juan Pérez",
  "correo": "juan@mail.com",
  "especialidad": "Cardiología",
  "fechaCita": "2026-07-15",
  "sede": "Norte",
  "valor": 150.00
}
```

---

## 8. Ejemplo de request inválido
Cita incorrecta por falta de nombre y valor nulo/cero (provocará rechazo y error 400):

```json
{
  "idCita": "ERR-999",
  "paciente": "",
  "correo": "falla@mail.com",
  "especialidad": "Medicina General",
  "fechaCita": "2026-07-20",
  "sede": "Oeste",
  "valor": 0
}
```

---

## 9. Explicación breve de Patrones EIP y Errores

* **Dónde se aplica Point-to-Point (Punto a Punto):** 
  Se aplica en el envío de mensajes hacia el sistema de facturación. La ruta transforma el mensaje (agregando `tipoMensaje`: `COMANDO_FACTURAR_CITA` y filtrando atributos) y lo envía a la cola `billing.queue` (vía `billing.exchange`). Un mensaje es consumido por un único consumidor de facturación.
  
* **Dónde se aplica Publish/Subscribe (Publicador/Suscriptor):**
  Se aplica en la distribución de eventos generales de creación de citas. El mensaje se inyecta en un `FanoutExchange` de RabbitMQ (`appointments.events`). Este exchange multiplica (clona) el mensaje y lo distribuye simultáneamente a todas las colas suscritas (`notifications.queue` y `analytics.queue`).

* **Dónde se aplica transferencia de archivos:**
  Se usa para integración con un sistema legado. Las citas procesadas se transforman en formato CSV y se escriben (anexan) en el directorio local `data/outbox/auditoria-citas.csv`.

* **Cómo se manejan errores:**
  Se utiliza el mecanismo global `onException` de Apache Camel. Toda excepción derivada de la validación (`CitaValidationException`) es atrapada por Camel. El bloque de error intercepta el mensaje original, le agrega el motivo del error y un Timestamp, y lo registra como texto plano en el archivo de logs de auditoría `data/errors/citas-rechazadas.log`. El error viaja de vuelta al controlador REST devolviendo un estado HTTP `400 Bad Request`.

---

## 10. Evidencia esperada para verificar el funcionamiento
Para comprobar que el sistema funciona correctamente, revisa los siguientes puntos tras enviar una solicitud válida:

1. **API y Swagger:** Entrar a `http://localhost:8080/swagger-ui.html` confirmando que levanta bien.
2. **RabbitMQ - Point-to-Point:** Verificar la llegada de un mensaje con `COMANDO_FACTURAR_CITA` en la cola `billing.queue` (`http://localhost:15672`).
3. **RabbitMQ - Pub/Sub:** Verificar la llegada de mensajes idénticos (con evento `CITA_CONFIRMADA`) en los contadores de `notifications.queue` y `analytics.queue`.
4. **Archivos - Auditoría (Caso Éxito):** Abrir `data/outbox/auditoria-citas.csv` y verificar la existencia de una nueva línea en formato CSV con los datos de la cita exitosa.
5. **Manejo de Errores:** Enviar un request inválido, obtener el `400 Bad Request` en Postman/Swagger, y comprobar la inserción de una nueva línea descriptiva en `data/errors/citas-rechazadas.log`.
