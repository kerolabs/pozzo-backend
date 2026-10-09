# Pozzo Backend

API RESTful de **Pozzo**, una app para organizar panderos o juntas de ahorro: los miembros se unen a un grupo, reciben el pozo por turnos, registran sus aportes en cada periodo y construyen un historial de cumplimiento que pueden compartir.

## Stack

Java 21 · Spring Boot 4 · Spring Data JPA · Spring Security + JWT · PostgreSQL (Supabase) · springdoc OpenAPI

## Arquitectura

Domain-Driven Design con un paquete por bounded context, cada uno dividido en las capas `domain`, `application`, `infrastructure` e `interfaces` (servicios de comandos y consultas al estilo CQRS, eventos de dominio y facades ACL entre contextos). Cada contexto guarda sus tablas en su propio esquema de base de datos.

| Contexto | Responsabilidad |
|---|---|
| `iam` | Registro con celular y código SMS, sesiones JWT, perfil y recuperación de cuenta por correo |
| `savingsgroups` | Grupos, reglas, invitaciones, miembros y asignación de turnos (sorteo o acuerdo) |
| `contributions` | Ciclos, periodos, aportes (transferencia con comprobante, efectivo o cobertura), revisión y entrega del pozo |
| `compliancehistory` | Puntaje de cumplimiento por miembro y enlaces públicos para compartirlo |
| `notifications` | Dispositivos, notificaciones push (FCM) y planes de recordatorio |
| `shared` | Agregados base, manejo de errores, i18n (es/en) y configuración de OpenAPI y JPA |

## Ejecución local

```bash
cp .env.example .env   # completa como mínimo DATABASE_* y JWT_SECRET
./mvnw spring-boot:run
```

- API: `http://localhost:8080/api/v1`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- Health check: `http://localhost:8080/actuator/health`

Los proveedores de SMS, correo y push usan `log` por defecto y escriben en la consola, así que para arrancar solo se necesitan la base de datos y el secreto JWT. Revisa `.env.example` para la configuración opcional de Supabase Storage, SMS Gate, Brevo y Firebase.


## Pruebas y Calidad de Código

La suite tiene tres niveles: pruebas unitarias del dominio de cada bounded context, pruebas de integración del API REST y pruebas de aceptación escritas en Gherkin, un archivo `.feature` por Technical Story en `src/test/resources/features`. Las de integración y las de aceptación levantan PostgreSQL 16 en un contenedor con Testcontainers, así que necesitan Docker en ejecución; ninguna envía SMS, correos ni notificaciones reales.

```bash
# Todas las pruebas: unitarias, de integración y de aceptación
./mvnw test

# Solo las pruebas de aceptación
./mvnw test -Dtest=AcceptanceTestSuite
```

El reporte de los escenarios queda en `target/cucumber-report.html`. El workflow `Tests` corre la suite en cada pull request y en cada push a `develop` y `main`.

## Ejecución con Docker

El proyecto cuenta con un Dockerfile multi-stage optimizado para producción:

```bash
# Construir la imagen de contenedor
docker build -t kerolabs/pozzo-backend .

# Ejecutar el contenedor pasando el archivo de entorno
docker run -d --name pozzo-api --env-file .env -p 8080:8080 kerolabs/pozzo-backend
```

## Despliegue

Cada push a `main` ejecuta `.github/workflows/deploy.yml`: GitHub Actions compila el jar y lo envía por SSH a una instancia de Oracle Cloud que corre detrás de Caddy (`deploy/oracle-setup.sh` prepara el servidor). También se incluye un `Dockerfile` para plataformas de contenedores como Render.

## Contribuir

Los commits siguen Conventional Commits en inglés. Los valida `.githooks/commit-msg`, que se activa automáticamente al compilar con Maven, y también el workflow `commit-policy`.

---

Kerolabs · [Landing page](https://kerolabs.github.io/pozzo-landing-page/)
