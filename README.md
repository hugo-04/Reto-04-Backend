# Customers Service

Microservicio REST para gestionar clientes y calcular indicadores de natalidad.

- Alta de clientes (nombre, apellido, email, DNI, fecha de nacimiento; la fecha de creación la asigna el servidor).
- Consulta de clientes por DNI, por email o sin filtros.
- Indicadores: nacidos por mes/año, mes/año con mayor y menor cantidad, y tasa de natalidad de cada mes.

## Tecnologías

| Componente | Versión |
|---|---|
| Java | 21 |
| Spring Boot | 3.5.6 (Web, Data JPA, Validation) |
| Base de datos | MySQL 8.4 (Docker) |
| Mapeo | MapStruct 1.6.3 |
| Utilidades | Lombok |
| Carga de `.env` | spring-dotenv 5.1.0 |
| Pruebas | JUnit 5 + Mockito + AssertJ |
| Build | Maven |

## Estructura

```
.
├── docker-compose.yml          # MySQL para desarrollo local
├── .env.example                # Plantilla de variables de entorno
├── postman/                    # Colección de Postman
└── backend/                    # Microservicio Spring Boot
    └── src/main/java/com/reto/customers
        ├── controller/         # Endpoints REST
        ├── service/            # Lógica de negocio
        ├── repository/         # Consultas JPQL
        ├── projection/         # Proyecciones de interfaz (lecturas mínimas)
        ├── mapper/             # Mappers MapStruct
        ├── dto/                # Request/Response (records)
        ├── entity/             # Entidad JPA
        ├── exception/          # Excepciones y manejador global
        └── config/             # CORS
```

## Requisitos previos

- JDK 21
- Maven 3.9+
- Docker Desktop (con el motor en ejecución)

## Instalación y ejecución

### 1. Configurar variables de entorno

Copiar la plantilla y, si se desea, ajustar los valores:

```bash
cp .env.example .env          # Windows PowerShell: Copy-Item .env.example .env
```

El mismo `.env` lo usan `docker-compose.yml` (para crear la base de datos) y el backend (para conectarse a ella).
Si no se crea el `.env`, el backend usa los valores por defecto, que coinciden con los de `.env.example`.

| Variable | Descripción | Valor por defecto |
|---|---|---|
| `DB_HOST` | Host de MySQL | `localhost` |
| `DB_PORT` | Puerto publicado en el equipo | `3307` |
| `DB_NAME` | Nombre de la base de datos | `customers_db` |
| `DB_USER` | Usuario de la aplicación | `customers_user` |
| `DB_PASSWORD` | Contraseña de la aplicación | `customers_pass` |
| `DB_ROOT_PASSWORD` | Contraseña root (solo contenedor) | `root_pass` |
| `SERVER_PORT` | Puerto del API | `8080` |
| `CORS_ALLOWED_ORIGINS` | Orígenes permitidos, separados por coma | `http://localhost:5173` |

### 2. Levantar la base de datos

Desde la raíz del repositorio:

```bash
docker compose up -d
```

Verificar que el contenedor esté saludable con `docker compose ps`. Los datos persisten en el volumen `mysql_data`.

### 3. Ejecutar el API

```bash
cd backend
mvn spring-boot:run
```

El API queda disponible en `http://localhost:8080`.

- La primera vez Hibernate crea la tabla `customers` automáticamente (`ddl-auto: update`).
  En los siguientes arranques no la vuelve a crear ni borra datos.
- `mvn spring-boot:run` se ejecuta desde la raíz del repositorio (configurado en el `pom.xml`) para que se lea el `.env`.
  Si se ejecuta desde un IDE, configurar el directorio de trabajo en la raíz del repositorio.

### 4. Ejecutar las pruebas unitarias

```bash
cd backend
mvn test
```

Son 3 pruebas con JUnit 5 y Mockito (no requieren base de datos):

1. Creación de cliente: guarda los datos normalizados (espacios y email en minúsculas).
2. Creación con DNI duplicado: lanza `DuplicateCustomerException` y no guarda.
3. Indicadores: calcula mayor, menor y tasa de natalidad.

## Endpoints

Base: `http://localhost:8080/api/customers`

### Crear cliente

`POST /api/customers`

```json
{
  "firstName": "Ana",
  "lastName": "Pérez",
  "email": "ana.perez@mail.com",
  "dni": "12345678",
  "birthDate": "1990-05-20"
}
```

Respuesta `201 Created` (con cabecera `Location`):

```json
{
  "id": 1,
  "firstName": "Ana",
  "lastName": "Pérez",
  "email": "ana.perez@mail.com",
  "dni": "12345678",
  "birthDate": "1990-05-20",
  "createdAt": "2026-10-16T17:30:12.345"
}
```

Validaciones: todos los campos son obligatorios; el email debe tener formato válido; el DNI debe tener exactamente 8 dígitos;
la fecha de nacimiento (`yyyy-MM-dd`) debe ser anterior a hoy. DNI y email son únicos.

### Consultar clientes

`GET /api/customers` — devuelve todos, del más reciente al más antiguo.

`GET /api/customers?dni=12345678` — filtra por DNI.

`GET /api/customers?email=ana.perez@mail.com` — filtra por email (no distingue mayúsculas).

Los filtros son opcionales y, si se envían ambos, se combinan con AND. El filtrado se hace en la base de datos.

### Indicadores de natalidad

`GET /api/customers/indicators`

```json
{
  "totalCustomers": 5,
  "birthsByMonth": [
    { "year": 1985, "month": 3, "count": 1, "birthRate": 20.00 },
    { "year": 1990, "month": 5, "count": 3, "birthRate": 60.00 },
    { "year": 1992, "month": 7, "count": 1, "birthRate": 20.00 }
  ],
  "highest": { "year": 1990, "month": 5, "count": 3, "birthRate": 60.00 },
  "lowest":  { "year": 1985, "month": 3, "count": 1, "birthRate": 20.00 }
}
```

- `birthsByMonth`: cantidad de clientes nacidos por mes/año.
- `highest` / `lowest`: mes/año con mayor y menor cantidad. Solo se consideran los meses con al menos un cliente;
  en caso de empate se devuelve el más antiguo. Son `null` si no hay clientes.
- `birthRate` (tasa de natalidad): porcentaje de clientes nacidos en ese mes/año sobre el total de clientes
  (`count / totalCustomers * 100`, con 2 decimales).

### Errores

Todas las respuestas de error tienen el mismo formato:

```json
{
  "timestamp": "2026-10-16T17:30:12.345",
  "status": 400,
  "error": "Bad Request",
  "message": "Datos de entrada inválidos",
  "details": ["dni: El DNI debe tener exactamente 8 dígitos"]
}
```

| Código | Cuándo ocurre |
|---|---|
| 400 | Datos inválidos o JSON/fecha mal formados |
| 409 | DNI o email ya registrados |
| 500 | Error interno o de acceso a datos |

## Postman

Importar [postman/Customers.postman_collection.json](postman/Customers.postman_collection.json).
La variable `baseUrl` apunta a `http://localhost:8080`. Incluye: crear cliente, listar, buscar por DNI,
buscar por email e indicadores.

## Decisiones de diseño

- **Arquitectura en capas** (controller → service → repository) con inyección por constructor.
- **DTOs como `record`** y entidad JPA sin exponer; **MapStruct** genera el mapeo y normaliza los datos de entrada.
- **JPQL explícito** con **proyecciones de interfaz**: las lecturas traen solo las columnas necesarias.
- **Filtros en la base de datos**: una sola consulta con parámetros opcionales, apoyada en los índices únicos de DNI y email.
  La agregación de indicadores también se hace en MySQL (`group by` año/mes) y no en memoria.
- **Transacciones solo en escritura**: `create` es transaccional; las consultas GET no.
- **Manejo de errores centralizado** con `@RestControllerAdvice`; el respaldo ante concurrencia (UNIQUE de la BD)
  se captura con `try/catch` al guardar.
- **Configuración externa** con `application.yml` y variables de entorno (`.env`).

