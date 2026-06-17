# MSVC Hospital — Sistema de microservicios

Sistema de gestión hospitalaria construido con **microservicios** sobre Spring Boot 4 y
Spring Cloud. Incluye descubrimiento de servicios (Eureka), un API Gateway como punto de
entrada único, documentación agregada con Swagger y seguridad con **JWT + control por roles**.

Este README explica **la implementación**. Para la demo paso a paso en clase ver
[GUIA-PRACTICA.md](GUIA-PRACTICA.md).

---

## 1. Arquitectura

```
                                  +------------------------+
                                  |   Eureka (8761)        |
                                  |  registro de servicios |
                                  +-----------+------------+
                                              ^  (todos se registran aqui)
                                              |
   Cliente            +---------------------------------------------+
   (Postman/      --> |              GATEWAY (8080)                 |
    navegador)        |  punto de entrada unico                     |
    + JWT             |  - valida el JWT (1a barrera)               |
                      |  - rutea por path usando lb://              |
                      |  - Swagger agregado de todos los servicios  |
                      +----+-------------+-------------+------------+
                           |             |             |        \
                    lb://  |       lb:// |       lb:// |    lb:// \
                           v             v             v          v
                   +-----------+  +-----------+  +-------------+  +-------------+
                   | medicos   |  | pacientes |  | atenciones  |  | usuarios    |
                   |  (8001)   |  |  (8000)   |  |   (8002)    |  |   (8003)    |
                   | valida JWT|  | valida JWT|  | valida JWT  |  | EMITE JWT   |
                   | + roles   |  | + roles   |  | + roles     |  | + roles     |
                   +-----------+  +-----------+  +------+------+  +-------------+
                                                        |  (Feign, URL fija)
                                                        v
                                              consulta a medicos y pacientes
```

| Modulo | Puerto | Rol |
|---|---|---|
| `msvc-eureka` | 8761 | Servidor de descubrimiento (registro de servicios) |
| `msvc-gateway` | 8080 | API Gateway WebMVC: entrada única, valida JWT, Swagger agregado |
| `msvc-usuarios` | 8003 | Usuarios/roles, login y **emisión** del JWT |
| `msvc-medicos` | 8001 | CRUD de médicos (v1 + v2 HATEOAS) |
| `msvc-pacientes` | 8000 | CRUD de pacientes |
| `msvc-atenciones` | 8002 | CRUD de atenciones (enriquece con médicos y pacientes vía Feign) |

**Tecnologías:** Java 21, Spring Boot 4.0.6, Spring Cloud 2025.1.1 (Eureka + Gateway WebMVC +
OpenFeign + LoadBalancer), Spring Security 7, springdoc-openapi (Swagger), H2, Maven multi-módulo.

---

## 2. Descubrimiento de servicios (Eureka)

- `msvc-eureka` activa el servidor con `@EnableEurekaServer` y no se registra a sí mismo
  (`register-with-eureka=false`, `fetch-registry=false`).
- Cada microservicio incluye `spring-cloud-starter-netflix-eureka-client` y se registra con su
  `spring.application.name`. Panel en `http://localhost:8761`.
- **Comunicación interna (Feign):** entre microservicios se usan **URLs fijas**
  (`localhost:800x`), no descubrimiento. Eureka se usa para el **registro** y para que el
  **gateway** rutee por nombre.

## 3. API Gateway (WebMVC)

- `spring-cloud-starter-gateway-server-webmvc` (modelo servlet, igual que el resto del stack).
- Rutas en [`msvc-gateway/.../application.yml`](msvc-gateway/src/main/resources/application.yml)
  bajo `spring.cloud.gateway.server.webmvc.routes`. Cada ruta enruta un `Path` a `lb://msvc-XXX`.
- `lb://` = *load balancer*: resuelve el nombre en Eureka y balancea entre instancias.
- El gateway reenvía el header `Authorization` a los servicios destino.

## 4. Documentación: Swagger agregado en el gateway

- El gateway expone **un solo** Swagger UI en `http://localhost:8080/docs/swagger-ui.html`
  con un **desplegable** para elegir el microservicio (usuarios / medicos / pacientes / atenciones).
- Rutas `/api-docs/<servicio>` reenvían (con `RewritePath`) al `/v3/api-docs` real de cada servicio.
- Cada microservicio también tiene su Swagger directo en `:<puerto>/docs/swagger-ui.html`.

---

## 5. Seguridad: Spring Security + JWT

### 5.1. Contrato del token

- Algoritmo **HS256** con **clave secreta compartida** (`jwt.secret`, igual en los 5 módulos).
- Claims: `sub` = username, `roles` = lista `["ROLE_ADMIN", ...]`, `iss` = `msvc-usuarios`,
  `iat` / `exp` (expira a los 60 min por defecto).
- **`msvc-usuarios` FIRMA** el token; **gateway + los 3 microservicios VALIDAN** (defensa en profundidad).

### 5.2. Modelo de datos (usuarios y roles)

- `Usuario` (id, username único, password BCrypt) y `Rol` (id, nombre `ROLE_X`).
- Relación **muchos-a-muchos** mediante la tabla intermedia `usuario_roles`.
- Roles iniciales: `ROLE_ADMIN`, `ROLE_MEDICO`, `ROLE_PACIENTE`.
- Un usuario puede representar a un médico o a un paciente según su rol.

### 5.3. Flujo de autenticación

```
1) POST /api/v1/auth/login {username, password}
       -> msvc-usuarios valida con BCrypt y devuelve un JWT firmado
2) El cliente envia el token en cada peticion:  Authorization: Bearer <token>
3) GATEWAY valida el token (rechaza 401 si falta/invalido) y rutea reenviando el header
4) El MICROSERVICIO destino vuelve a validar el token y aplica el rol (autorizacion)
```

### 5.4. Dónde se valida (defensa en profundidad)

- **Gateway** ([SecurityConfig](msvc-gateway/src/main/java/com/hospital/msvc_gateway/security/SecurityConfig.java)):
  `oauth2ResourceServer` valida el JWT. Público: `/api/v1/auth/**` y Swagger. Resto: autenticado.
  Hace **autenticación** (¿el token es válido?), no autorización fina.
- **Cada microservicio** (SecurityConfig en su paquete `config`): es *resource server*, valida el
  mismo JWT y aplica el **control por roles** (autorización). Así, aunque alguien llegue directo
  al puerto del servicio saltándose el gateway, sigue protegido.

### 5.5. Piezas clave del código

| Pieza | Archivo |
|---|---|
| Entidades usuario/rol | [`Usuario.java`](msvc-usuarios/src/main/java/com/hospital/msvc_usuarios/models/Usuario.java), [`Rol.java`](msvc-usuarios/src/main/java/com/hospital/msvc_usuarios/models/Rol.java) |
| Firma del JWT | [`JwtService.java`](msvc-usuarios/src/main/java/com/hospital/msvc_usuarios/security/JwtService.java) |
| Login / registro | [`AuthService.java`](msvc-usuarios/src/main/java/com/hospital/msvc_usuarios/services/AuthService.java), [`AuthController.java`](msvc-usuarios/src/main/java/com/hospital/msvc_usuarios/controllers/AuthController.java) |
| Seed de usuarios demo | [`DataLoader.java`](msvc-usuarios/src/main/java/com/hospital/msvc_usuarios/config/DataLoader.java) |
| Validación en el gateway | [`gateway/SecurityConfig.java`](msvc-gateway/src/main/java/com/hospital/msvc_gateway/security/SecurityConfig.java) |
| Validación + roles en servicios | `msvc-{medicos,pacientes,atenciones}/.../config/SecurityConfig.java` |

Detalles técnicos: el JWT se firma/valida con `NimbusJwtEncoder` / `NimbusJwtDecoder` usando una
`SecretKeySpec` HMAC-SHA256. Un `JwtAuthenticationConverter` mapea el claim `roles` a *authorities*
(sin prefijo, porque ya se guardan como `ROLE_X`).

### 5.6. Matriz de control de acceso

| Recurso | Leer (GET) | Escribir (POST/PUT/DELETE) |
|---|---|---|
| `/api/v1\|v2/medicos` | ADMIN, MEDICO, PACIENTE | ADMIN, MEDICO |
| `/api/v1/pacientes` | ADMIN, MEDICO, PACIENTE | ADMIN, MEDICO |
| `/api/v1/atenciones` | ADMIN, MEDICO, PACIENTE | ADMIN, MEDICO |
| `/api/v1/usuarios` | solo ADMIN | — |
| `/api/v1/auth/**` | público | público |

En los microservicios de dominio el control es por **método HTTP + ruta** (en `SecurityConfig`).
En `msvc-usuarios` se usa `@PreAuthorize("hasRole('ADMIN')")` como ejemplo de seguridad a nivel de método.

### 5.7. Usuarios de demostración (sembrados al arrancar)

| Usuario | Clave | Rol |
|---|---|---|
| `admin` | `admin123` | ROLE_ADMIN |
| `medico1` | `medico123` | ROLE_MEDICO |
| `paciente1` | `paciente123` | ROLE_PACIENTE |

---

## 6. Construir y ejecutar

```bash
# Compilar y empaquetar todo (en la raiz del repo)
mvn -DskipTests clean package

# Arrancar EN ESTE ORDEN (una terminal por servicio, o Run en IntelliJ):
mvn -pl msvc-eureka     spring-boot:run    # 1) descubrimiento (esperar a 8761)
mvn -pl msvc-usuarios   spring-boot:run    # 2) auth (emite tokens)
mvn -pl msvc-medicos    spring-boot:run    # 3) microservicios de dominio
mvn -pl msvc-pacientes  spring-boot:run
mvn -pl msvc-atenciones spring-boot:run
mvn -pl msvc-gateway    spring-boot:run    # 4) gateway al final
```

> Esperar ~30 s tras arrancar un servicio para que el gateway baje el registro de Eureka
> antes de rutear por `lb://` (si no, puede dar un `503` transitorio).

## 7. Uso (ejemplos verificados)

```bash
# 1) Login -> devuelve el token
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"medico1","password":"medico123"}'

# 2) Usar el token (entrando siempre por el gateway, 8080)
curl http://localhost:8080/api/v1/medicos -H "Authorization: Bearer <TOKEN>"

# Sin token -> 401 (rechazado en el gateway)
curl -i http://localhost:8080/api/v1/medicos

# PACIENTE intentando escribir -> 403 (el servicio niega por rol)
curl -i -X POST http://localhost:8080/api/v1/medicos \
  -H "Authorization: Bearer <TOKEN_PACIENTE>" \
  -H "Content-Type: application/json" \
  -d '{"run":"22222222-2","nombreCompleto":"Dra. Ana","jefeTurno":true}'
```

En Swagger del gateway (`http://localhost:8080/docs/swagger-ui.html`): botón **Authorize** y pegar el token.

## 8. Verificación realizada

Probado en runtime (no solo build):

```
login admin                 -> 200 + JWT con roles:["ROLE_ADMIN"]
/usuarios  sin token -> 401 | admin -> 200 | paciente -> 403
medicos    GET sin token -> 401 | GET paciente -> 200 | POST paciente -> 403 | POST medico -> 200
gateway    sin token -> 401 | login publico -> 200 | con token -> rutea
defensa en profundidad: gateway -> servicio seguro, POST paciente -> 403 (token reenviado, rol denegado)
```

Build: 7 módulos `BUILD SUCCESS`. Tests: 37 (servicios + contextLoads con seguridad) en verde.

## 9. Configuración relevante

- `jwt.secret`: clave HMAC compartida. **Debe ser idéntica** en `msvc-usuarios`, `msvc-gateway` y
  los 3 microservicios. En producción iría en una variable de entorno, no en el `application.properties`.
- `jwt.expiration-minutes`: vigencia del token (60 por defecto).
- Bases de datos H2 por servicio en `./data/`. Los servicios de dominio usan `ddl-auto=create`
  (se recrean vacías en cada arranque); `msvc-usuarios` usa `ddl-auto=update` para conservar usuarios.

## 10. Limitaciones y mejoras posibles

- El secreto JWT está en texto plano en los `.properties` (suficiente para clase, no para producción).
- HS256 con clave simétrica compartida; una alternativa más robusta es RSA (clave pública/privada).
- Los `_links` de HATEOAS (v2) apuntan a la IP del servicio, no al gateway
  (por `prefer-ip-address=true`). Mejora opcional: `server.forward-headers-strategy=framework`.
- La regla "el paciente solo ve sus propias atenciones" no está implementada a nivel de propietario;
  hoy `PACIENTE` puede leer atenciones. Se puede afinar con seguridad a nivel de método y el `sub` del token.

## 11. Documentos relacionados

- [GUIA-PRACTICA.md](GUIA-PRACTICA.md): guía paso a paso para demostrar el sistema a los alumnos
  (orden de arranque, qué mostrar, troubleshooting y metodología para armar una guía práctica).
