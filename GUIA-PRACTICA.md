# Guía práctica — Microservicios con Eureka y API Gateway

> Sistema de hospital con Spring Boot 4 / Spring Cloud 2025.1. Esta guía sirve para
> mostrar el código funcionando a los alumnos y para que ellos lo reproduzcan paso a paso.

## Mapa del sistema

| Módulo | Puerto | Rol |
|---|---|---|
| `msvc-eureka` | 8761 | Servidor de descubrimiento (registro de servicios) |
| `msvc-gateway` | 8080 | API Gateway WebMVC: puerta de entrada única |
| `msvc-pacientes` | 8000 | Microservicio de pacientes |
| `msvc-medicos` | 8001 | Microservicio de médicos |
| `msvc-atenciones` | 8002 | Microservicio de atenciones (consulta a médicos y pacientes) |

URLs clave: panel Eureka `http://localhost:8761` · gateway `http://localhost:8080` ·
Swagger de cada servicio en `/docs/swagger-ui.html`.

## Índice

1. Qué construimos y por qué
2. Demo en vivo: paso a paso
3. Checklist rápido y guion de clase
4. Cómo armar una guía práctica (metodología)
5. Problemas comunes y soluciones (FAQ)
6. Apéndice: arranque rápido y cómo detener todo

---

## 1. Que construimos y por que

En esta guia vamos a levantar un pequeno sistema de hospital hecho con **microservicios**. En vez de una sola aplicacion gigante, tenemos varios servicios pequenos, cada uno con una sola responsabilidad:

- **msvc-pacientes** (puerto 8000): maneja pacientes.
- **msvc-medicos** (puerto 8001): maneja medicos.
- **msvc-atenciones** (puerto 8002): maneja atenciones (y consulta a medicos y pacientes).

Para que estos servicios trabajen juntos necesitamos dos piezas de apoyo: un **servicio de descubrimiento** y un **API Gateway**.

### El servicio de descubrimiento (Eureka)

Imagina que tienes tres servicios y cada uno corre en un puerto distinto. Si todos tuvieran que recordar la direccion exacta de los demas, cualquier cambio seria un dolor de cabeza.

**Eureka resuelve ese problema.** Es como una **agenda telefonica** de servicios.

- Cada microservicio, al arrancar, **se registra** en Eureka diciendo: "Hola, soy `MSVC-MEDICOS` y estoy en este puerto".
- Asi, cuando alguien necesita un servicio, no pregunta por una direccion fija (`localhost:8001`), sino por un **nombre** (`MSVC-MEDICOS`), y Eureka responde donde encontrarlo.

En este proyecto, Eureka corre en el **puerto 8761**. Tiene un panel web que puedes abrir en `http://localhost:8761` para ver, en vivo, que servicios estan registrados y arriba (UP).

### El API Gateway

Si tenemos tres microservicios en tres puertos distintos, el cliente (por ejemplo, el navegador o Postman) tendria que conocer los tres. Eso es incomodo e inseguro.

**El API Gateway resuelve eso siendo la puerta de entrada unica.** El cliente solo conoce **una direccion**: el gateway, en el **puerto 8080**.

- El cliente siempre llama al gateway (`http://localhost:8080/...`).
- El gateway mira la **ruta** de la peticion y decide a que microservicio enviarla.
- Por ejemplo, todo lo que empiece con `/api/v1/medicos` lo manda al servicio de medicos.

Asi el cliente tiene un solo punto de contacto y no necesita saber donde vive cada microservicio.

### Como se conectan las piezas

El flujo de una peticion es asi: el **cliente** llama al **gateway**, el gateway le pregunta a **Eureka** donde esta el microservicio, y luego reenvia la peticion al **microservicio** correcto.

```
                                  +------------------------+
                                  |   Eureka (8761)        |
                                  |   "agenda" de servicios|
                                  +-----------+------------+
                                        ^      |
                              2) ?donde |      | 3) esta en
                                 esta    |      |    tal IP/puerto
                              MSVC-MEDICOS?     v
  +----------+   1) GET           +-----------------+   4) reenvia    +------------------+
  | CLIENTE  | -----------------> |  GATEWAY (8080) | --------------> | MSVC-MEDICOS     |
  | (Postman)|  /api/v1/medicos   |  punto de       |   lb://msvc-    |  (8001)          |
  |          | <----------------- |  entrada unico  | <-------------- |                  |
  +----------+   5) respuesta     +-----------------+   respuesta     +------------------+
```

Los microservicios tambien se registran en Eureka al arrancar (flecha no dibujada, pero ocurre antes del paso 2).

### Una distincion importante: registrarse vs comunicarse

Aqui hay dos cosas que parecen iguales pero NO lo son. Presta atencion:

1. **Registrarse en Eureka.** Los tres microservicios (y el gateway) se anuncian en Eureka al arrancar. Esto sirve, sobre todo, para que el **gateway** pueda encontrarlos por nombre. El gateway usa direcciones tipo `lb://msvc-medicos` (la `lb` significa *load balancer*): no escribe el puerto, sino el **nombre** del servicio, y Eureka traduce ese nombre a una direccion real.

2. **Comunicarse entre microservicios.** Cuando un microservicio llama a otro (por ejemplo, `msvc-atenciones` pide datos a `msvc-medicos` y `msvc-pacientes` usando **Feign**), en este proyecto **NO** usa el nombre de Eureka. Usa **URLs fijas** directas (`http://localhost:8001`, etc.).

En resumen:

| Quien | Como encuentra al otro |
|-------|------------------------|
| Gateway -> microservicio | Por **nombre** vía Eureka (`lb://msvc-medicos`) |
| Microservicio -> microservicio (Feign) | Por **URL fija** (`http://localhost:8001`) |

La idea clave: en este proyecto, **Eureka sirve para el registro y para que el gateway rutee por nombre**. La comunicacion interna por Feign va por URLs fijas. Son dos caminos distintos, y esta bien que asi sea para empezar a aprender.

## 2. Demo en vivo: paso a paso

Sigue estos pasos en orden. No te saltes el orden de arranque.

### Paso 1 — Compilar todo el monorepo

Abre una terminal en la raiz del repositorio (`msvcs-hospital`). Ejecuta:

```bash
mvn -DskipTests clean package
```

**Que debe verse:** la salida de Maven termina con `BUILD SUCCESS` y un resumen con los 5 modulos (`msvc-eureka`, `msvc-gateway`, `msvc-pacientes`, `msvc-medicos`, `msvc-atenciones`) en estado `SUCCESS`.

**Que explicar:** "Es un proyecto Maven multi-modulo. Con un solo comando compilamos y empaquetamos los 5 servicios. Saltamos los tests aqui para ir mas rapido en la demo."

### Paso 2 — Arrancar Eureka y mostrar el panel vacio

Deja una terminal dedicada solo a Eureka. Ejecuta:

```bash
mvn -pl msvc-eureka spring-boot:run
```

Espera a que arranque. Abre en el navegador:

```
http://localhost:8761
```

**Que debe verse:** el panel web de Eureka. En la seccion **Instances currently registered with Eureka** no hay nada (o aparece vacio).

**Que explicar:** "Eureka es el servidor de descubrimiento. Es la 'guia telefonica' del sistema. Corre en el puerto 8761. Ahora mismo nadie se ha registrado, por eso esta vacio. Eureka no se registra a si mismo: el ES el registro."

### Paso 3 — Arrancar un microservicio y verlo registrarse

En una terminal NUEVA (deja Eureka corriendo), arranca medicos:

```bash
mvn -pl msvc-medicos spring-boot:run
```

Espera ~30 segundos. Luego recarga en el navegador:

```
http://localhost:8761
```

**Que debe verse:** en **Instances currently registered with Eureka** aparece **MSVC-MEDICOS** con estado **UP** en el puerto **8001**.

**Que explicar:** "El microservicio de medicos arranco en el puerto 8001 y se anuncio a Eureka con su nombre `MSVC-MEDICOS`. Ahora Eureka sabe donde encontrarlo. Cada microservicio es cliente de Eureka."

(Opcional) Verifica el registro tambien por API:

```bash
curl -H "Accept: application/json" http://localhost:8761/eureka/apps
```

**Que debe verse:** un JSON que lista `MSVC-MEDICOS` con estado `UP` y puerto `8001`.

### Paso 4 — Mostrar el Swagger del microservicio (directo, sin gateway)

Abre en el navegador:

```
http://localhost:8001/docs/swagger-ui.html
```

**Que debe verse:** el Swagger UI de medicos, con los endpoints de la **API v1** (CRUD simple) y la **API v2** (HATEOAS).

**Que explicar:** "Cada microservicio documenta su propia API con Swagger. Aqui estamos hablando DIRECTO con el servicio en su puerto 8001. En el siguiente paso entraremos por la puerta unica."

### Paso 5 — Arrancar el gateway

En una terminal NUEVA (Eureka y medicos siguen corriendo), arranca el gateway:

```bash
mvn -pl msvc-gateway spring-boot:run
```

Espera ~30 segundos para que el gateway descargue el registro de Eureka. Recarga:

```
http://localhost:8761
```

**Que debe verse:** ahora el panel lista **MSVC-MEDICOS** (UP, 8001) **y** **MSVC-GATEWAY** (UP, 8080).

**Que explicar:** "El gateway es la puerta de entrada unica, en el puerto 8080. Tambien es cliente de Eureka: lo necesita para resolver las rutas `lb://`. El `lb` es 'load balancer': el gateway le pregunta a Eureka donde esta `msvc-medicos` y reenvia el trafico ahi. Por eso esperamos unos segundos: el gateway necesita bajar el registro antes de poder rutear."

### Paso 6 — Demostrar el ruteo: GET a traves del gateway (lista vacia)

Pide la lista de medicos POR EL PUERTO 8080:

```bash
curl http://localhost:8080/api/v1/medicos
```

Equivalente en navegador (pegar la URL):

```
http://localhost:8080/api/v1/medicos
```

**Que debe verse:** respuesta **200** con cuerpo `[]` (lista vacia).

**Que explicar:** "Fijense en el puerto: 8080, el del gateway, NO el 8001. La peticion entro por la puerta unica y el gateway la reenvio al servicio de medicos. La base de datos esta vacia, por eso devuelve `[]`."

### Paso 7 — POST a traves del gateway: crear un medico

```bash
curl -X POST http://localhost:8080/api/v1/medicos \
  -H "Content-Type: application/json" \
  -d '{"run":"11111111-1","nombreCompleto":"Dr. House","jefeTurno":true}'
```

Equivalente en **Postman**:
- Metodo: **POST**
- URL: `http://localhost:8080/api/v1/medicos`
- Header: `Content-Type: application/json`
- Body (raw → JSON):
```json
{"run":"11111111-1","nombreCompleto":"Dr. House","jefeTurno":true}
```

**Que debe verse:** respuesta **200** con el medico creado, incluyendo `medicoId: 1`.

**Que explicar:** "Creamos un medico, otra vez entrando por el puerto 8080. El gateway tambien rutea los POST. El servicio le asigno el `medicoId: 1`."

### Paso 8 — GET con HATEOAS (v2) a traves del gateway

```bash
curl http://localhost:8080/api/v2/medicos/1
```

Equivalente en navegador:

```
http://localhost:8080/api/v2/medicos/1
```

**Que debe verse:** respuesta **200** con el medico y un bloque `_links` (con `self` y `medicos`).

**Que explicar:** "La API v2 usa HATEOAS: la respuesta incluye enlaces `_links` que dicen al cliente que puede hacer a continuacion. Y todo paso por el gateway. Nota: los `_links` apuntan a la IP del microservicio (puerto 8001), no al gateway, porque el servicio se registra con su IP. Para clase es suficiente."

### Aviso importante para los alumnos

"Cada vez que reinician un microservicio, su base de datos H2 se recrea VACIA. Si paran y arrancan medicos de nuevo, el `Dr. House` desaparece. Es a proposito, para empezar siempre limpio en clase."

## 3. Checklist rapido y guion de clase

## Parte A — Checklist de verificacion

### Antes de la clase (preparacion)

- [ ] El proyecto compila y empaqueta sin errores: `mvn -DskipTests clean package` en la raiz del repo.
- [ ] Java 21 y Maven disponibles en la terminal (`java -version`, `mvn -version`).
- [ ] Puertos libres antes de arrancar: 8761 (Eureka), 8080 (Gateway), 8000 (pacientes), 8001 (medicos), 8002 (atenciones).
- [ ] Ningun proceso previo de la demo quedo corriendo (revisar y cerrar instancias anteriores que ocupen esos puertos).
- [ ] Tener listas las URLs en el navegador o en el cliente REST: panel de Eureka `http://localhost:8761` y Swagger UI `http://localhost:8080` o el de cada servicio en `/docs/swagger-ui.html`.

### Durante la demo (orden de arranque obligatorio)

- [ ] 1) Arrancar primero `msvc-eureka`: `mvn -pl msvc-eureka spring-boot:run`.
- [ ] Confirmar que el panel `http://localhost:8761` carga antes de continuar.
- [ ] 2) Arrancar los microservicios: `msvc-pacientes` (8000), `msvc-medicos` (8001), `msvc-atenciones` (8002), con `mvn -pl msvc-medicos spring-boot:run` (idem cada uno).
- [ ] 3) Arrancar por ultimo `msvc-gateway`: `mvn -pl msvc-gateway spring-boot:run`.
- [ ] Esperar ~30s tras arrancar cada servicio para que el gateway baje el registro de Eureka antes de rutear por `lb://` (si no, puede aparecer un 503 transitorio).
- [ ] Verificar en `http://localhost:8761` que los servicios aparecen como UP (ej. MSVC-MEDICOS en 8001, MSVC-GATEWAY en 8080).
- [ ] Probar a traves del gateway (puerto 8080): `GET http://localhost:8080/api/v1/medicos` devuelve `200` con cuerpo `[]` (BD vacia al inicio).
- [ ] Crear un medico: `POST http://localhost:8080/api/v1/medicos` con `Content-Type: application/json` y body `{"run":"11111111-1","nombreCompleto":"Dr. House","jefeTurno":true}` devuelve `200` con `medicoId:1`.
- [ ] Mostrar HATEOAS via gateway: `GET http://localhost:8080/api/v2/medicos/1` devuelve `200` con `_links` (self y medicos).
- [ ] Avisar a los alumnos que `ddl-auto=create` recrea la BD H2 vacia en cada arranque: los datos se borran al reiniciar el servicio.

## Parte B — Guion de clase (talking points)

1. "Arrancamos primero Eureka en el puerto 8761: es el servidor de descubrimiento donde cada microservicio se registra. Este panel que ven en pantalla es el registro vivo de la arquitectura."

2. "Ahora levanto pacientes, medicos y atenciones en los puertos 8000, 8001 y 8002. Fijense como, en unos segundos, van apareciendo como UP en el panel de Eureka: ellos solos avisaron que existen."

3. "Por ultimo arranco el gateway en el 8080. Es la unica puerta de entrada: el cliente nunca llama a los puertos 8000/8001/8002 directamente, siempre pasa por aqui."

4. "Miren la ruta: pido `http://localhost:8080/api/v1/medicos` al gateway y el responde un arreglo vacio. El gateway no tiene la logica; uso `lb://msvc-medicos` para que, apoyandose en Eureka, encuentre el servicio real y le balancee la peticion."

5. "Hago un POST con un medico al mismo puerto 8080 y me devuelve el `medicoId:1`. Confirmamos que el gateway escribe en el microservicio correcto sin que yo sepa en que puerto vive."

6. "Ahora pido la version 2: `api/v2/medicos/1`. Vean los `_links` con `self` y `medicos` en la respuesta. Eso es HATEOAS: la propia API me dice a donde puedo navegar despues, y todo pasando por el gateway."

7. "Un detalle real: esos `_links` apuntan a la IP del microservicio y no al gateway, porque configuramos `prefer-ip-address=true` para el registro en Eureka. Es esperable; se puede ajustar con `forward-headers-strategy`."

8. "Ojo con los datos: usamos `ddl-auto=create`, asi que la base H2 se recrea vacia cada vez que reinicio un servicio. Si reinician, el `medicoId:1` que creamos desaparece."

## 4. Cómo armar una guía práctica (metodología para el profesor)

Una guía práctica de laboratorio no es un manual de referencia ni la documentación del sistema: es un **camino guiado** que lleva al alumno desde "no tengo nada" hasta "lo hice funcionar y entiendo por qué". Esta sección describe la metodología para diseñarla, usando como ejemplo el laboratorio de microservicios con Spring Cloud (Eureka + Gateway + tres microservicios).

### 4.1. Principio rector: diseñar desde el resultado de aprendizaje, no desde el contenido

Antes de escribir el primer paso, responda: **¿qué debe ser capaz de *hacer* el alumno al terminar?** No "qué le voy a contar", sino qué sabrá ejecutar y explicar. Todo lo demás (pasos, checkpoints, evaluación) se deriva de esa respuesta. Si un paso no contribuye a un objetivo de aprendizaje, sobra.

### 4.2. Definir objetivos de aprendizaje claros y observables

Redacte los objetivos con **verbos observables** (levantar, registrar, verificar, explicar, diagnosticar), no con verbos vagos ("conocer", "entender"). Un objetivo bien escrito ya sugiere cómo se evalúa.

Ejemplo aplicado a este laboratorio:

> Al finalizar la guía, el alumno será capaz de:
> 1. **Levantar** un ecosistema de microservicios en el orden correcto (Eureka → microservicios → Gateway).
> 2. **Verificar** en el panel de Eureka (`http://localhost:8761`) que los servicios quedaron registrados como `UP`.
> 3. **Enrutar** una petición REST a través del API Gateway (`http://localhost:8080`) hacia el microservicio correcto.
> 4. **Distinguir** una respuesta v1 (CRUD simple) de una v2 (con `_links` de HATEOAS).
> 5. **Diagnosticar** un error 503 transitorio y explicar por qué ocurre.

Note que cada objetivo es comprobable: puede mostrarse con una salida en pantalla. Eso es lo que después se convierte en checkpoint y en criterio de evaluación.

### 4.3. Establecer prerequisitos y setup verificable

El alumno no debe descubrir a mitad de camino que le falta el JDK. Liste los prerequisitos **al inicio** y, para cada uno, dé un comando de verificación con su salida esperada. Un prerequisito sin forma de comprobarlo no sirve.

| Requisito | Comando de verificación | Salida esperada |
|---|---|---|
| Java 21 | `java -version` | una línea con `version "21"` |
| Maven | `mvn -version` | `Apache Maven 3.9.x` o superior |
| Puertos libres | `lsof -i :8761 -i :8080 -i :8000 -i :8001 -i :8002` | sin resultados (puertos libres) |
| Proyecto compilable | `mvn -DskipTests clean package` (en la raíz) | `BUILD SUCCESS` |

Regla práctica: **el setup termina con un "compila todo" verde**. Si esa primera victoria no se da, el alumno no debe avanzar; es preferible resolver el entorno ahora que arrastrar el problema por toda la guía.

### 4.4. Dividir en pasos pequeños y verificables

Cada paso debe hacer **una sola cosa** y dejar un estado comprobable. Si un paso tiene tres acciones encadenadas y algo falla, el alumno no sabe cuál de las tres rompió. Heurística: un paso = una acción + una verificación.

Cómo decidir el tamaño del paso:
- Si para describir un paso necesita la palabra "y" más de una vez, probablemente son varios pasos.
- Cada paso debe poder "deshacerse" o reintentarse sin rehacer todo lo anterior.
- El orden importa cuando hay dependencias reales. En este laboratorio el **orden de arranque es obligatorio** (Eureka primero, Gateway al final), así que cada paso de arranque es su propio paso, no un bloque único.

Ejemplo de descomposición (arranque del ecosistema):

1. Arrancar Eureka — `mvn -pl msvc-eureka spring-boot:run`
2. Confirmar que Eureka responde — abrir `http://localhost:8761`
3. Arrancar `msvc-medicos` — `mvn -pl msvc-medicos spring-boot:run`
4. Esperar ~30 s y confirmar el registro en el panel de Eureka
5. Arrancar el Gateway — `mvn -pl msvc-gateway spring-boot:run`
6. Probar la primera petición a través del Gateway

### 4.5. Checkpoints: cómo el alumno confirma que va bien

Un **checkpoint** es una pregunta cerrada (sí/no) que el alumno puede contestar mirando algo concreto: una pantalla, un código de estado HTTP, una línea de log. Sin checkpoints, el alumno avanza a ciegas y solo descubre que algo falló mucho después, cuando ya es difícil rastrear la causa.

Formato recomendado para cada checkpoint:

> **✓ Checkpoint 4** — Antes de continuar, confirma:
> - Abriste `http://localhost:8761` y ves la sección *Instances currently registered with Eureka*.
> - Aparece `MSVC-MEDICOS` con estado **UP**.
> - **Si NO aparece:** espera 30 s más y recarga (el registro no es instantáneo). Ve a *Errores comunes → 503 / no aparece el registro*.

Buenas prácticas para los checkpoints:
- Colócalos **después de cada paso con riesgo de fallo**, no solo al final.
- Que la condición de éxito sea **objetiva** ("ves `UP`", "responde `200`"), nunca subjetiva ("debería funcionar").
- Cada checkpoint debe incluir una **ruta de escape**: qué hacer si el resultado no coincide.

### 4.6. Mostrar la salida esperada (capturas y respuestas)

El alumno necesita saber **contra qué comparar**. Junto a cada paso relevante, incluya la salida esperada: una captura de pantalla, un cuerpo de respuesta o un fragmento de log. La regla: *si el alumno no sabe cómo se ve el éxito, no puede reconocer el fracaso*.

Para acciones de UI (panel de Eureka, Swagger UI en `/docs/swagger-ui.html`), use **capturas anotadas** señalando dónde mirar. Para llamadas REST, muestre petición y respuesta literal:

```
POST http://localhost:8080/api/v1/medicos
Content-Type: application/json

{"run":"11111111-1","nombreCompleto":"Dr. House","jefeTurno":true}
```

Respuesta esperada (`200 OK`):

```json
{"medicoId":1,"run":"11111111-1","nombreCompleto":"Dr. House","jefeTurno":true}
```

Y para evidenciar HATEOAS, contraste v1 contra v2 (`GET http://localhost:8080/api/v2/medicos/1`), señalando el bloque `_links` que aparece solo en v2. Mostrar la diferencia lado a lado enseña más que un párrafo explicándola.

> **Nota honesta en la guía:** advierta de antemano dos comportamientos reales para que el alumno no los confunda con un error suyo:
> - Los `_links` de v2 apuntan a la IP/puerto del microservicio (ej. `http://10.x.x.x:8001/...`) y no al Gateway, por `prefer-ip-address=true`. Es esperado.
> - La base de datos H2 se **recrea vacía en cada arranque** (`ddl-auto=create`); los datos cargados se pierden al reiniciar el servicio.

### 4.7. Anticipar los errores comunes

El 80 % de los alumnos tropieza con los mismos cinco problemas. Anticípelos en una tabla de diagnóstico: **síntoma → causa → solución**. Esto convierte la frustración en aprendizaje y descarga al profesor de repetir lo mismo veinte veces.

| Síntoma | Causa probable | Solución |
|---|---|---|
| `503 Service Unavailable` al pasar por el Gateway | El Gateway aún no bajó el registro del servicio desde Eureka | Esperar ~30 s tras arrancar el microservicio y reintentar |
| El servicio no aparece en el panel de Eureka | Se arrancó el Gateway antes que los microservicios, o el orden fue incorrecto | Respetar el orden: Eureka → microservicios → Gateway |
| `Port 8761 already in use` | Quedó un proceso anterior ocupando el puerto | Cerrar el proceso (`lsof -i :8761`) o reiniciar el equipo |
| Los datos cargados desaparecieron | `ddl-auto=create` recrea la BD H2 vacía en cada arranque | Es esperado; volver a hacer el `POST` tras reiniciar |
| `Connection refused` desde Atenciones hacia Médicos/Pacientes | El microservicio destino no está arriba (Feign usa URL fija `localhost:800x`) | Verificar que `msvc-medicos` y `msvc-pacientes` estén corriendo |

Cómo se construye esta tabla: corra la guía usted mismo (o pida a un ayudante que la siga al pie de la letra) y **anote cada tropiezo real**. Los errores comunes no se adivinan desde el escritorio; se descubren observando a alguien hacer el laboratorio por primera vez.

### 4.8. Preguntas de comprensión al final

Terminar ejecutando los comandos no garantiza haber **entendido**. Cierre la guía con preguntas que obliguen a explicar el *porqué*, no a repetir el *cómo*. Apunte a los objetivos de aprendizaje del inicio: cada objetivo debería tener al menos una pregunta que lo verifique.

Ejemplos para este laboratorio:
1. ¿Por qué hay que arrancar Eureka **antes** que el resto? ¿Qué pasaría al revés?
2. ¿Por qué un `503` justo después de arrancar un servicio suele desaparecer solo en unos segundos?
3. El Gateway enruta con `lb://msvc-medicos`. ¿Qué significa el `lb://` y de dónde saca el Gateway la dirección real?
4. Los `@FeignClient` de Atenciones usan `localhost:8001` fijo en vez de `lb://`. ¿Qué ventaja y qué desventaja tiene esa decisión?
5. ¿Qué aporta la versión v2 (HATEOAS) que no tiene la v1? Da un caso donde los `_links` sean útiles para un cliente.

Mezcle tipos: alguna de respuesta cerrada (verificable de un vistazo) y alguna abierta (que revele el modelo mental del alumno).

### 4.9. Definir los criterios de evaluación

El alumno debe saber **cómo se le va a calificar antes de empezar**. Una rúbrica con criterios observables hace la corrección justa, rápida y reproducible entre distintos correctores. Derive cada criterio directamente de los objetivos de aprendizaje.

| Criterio | Evidencia esperada | Pts |
|---|---|---|
| Ecosistema levantado en el orden correcto | Captura del panel de Eureka con los servicios en `UP` | 25 |
| Petición exitosa a través del Gateway | Captura del `200` y el cuerpo del `POST`/`GET` por el puerto 8080 | 25 |
| Diferencia v1 vs v2 demostrada | Captura de la respuesta v2 con el bloque `_links` | 20 |
| Diagnóstico de un error | Explicación escrita de un `503` o de la BD vacía, con su causa | 15 |
| Preguntas de comprensión | Respuestas correctas y bien argumentadas | 15 |

Principios de una buena rúbrica:
- Califique **evidencias observables** (una captura, una respuesta), no impresiones.
- Reparta el puntaje según la **importancia pedagógica**, no según el esfuerzo mecánico.
- Hágala visible en la guía desde el principio: la rúbrica también enseña qué es lo importante.

### 4.10. Plantilla reutilizable de una guía práctica

Use esta estructura como esqueleto para cualquier guía de laboratorio; basta rellenar cada sección:

```markdown
# [Título de la práctica]

## 0. Ficha
- Duración estimada: [p. ej. 90 min]
- Modalidad: [individual / en parejas]
- Entregable: [capturas + respuestas / repositorio / informe]

## 1. Objetivos de aprendizaje
Al finalizar, el alumno será capaz de:
- [verbo observable] …
- [verbo observable] …

## 2. Prerequisitos y setup
| Requisito | Comando de verificación | Salida esperada |
|---|---|---|
| … | … | … |
> Termina cuando: [primera victoria verificable, p. ej. "BUILD SUCCESS"]

## 3. Desarrollo paso a paso
### Paso 1 — [una sola acción]
- Qué hacer: …
- Comando / acción: `…`
- Salida esperada: [captura o bloque de salida]
> ✓ Checkpoint 1 — Confirma que: …
>   Si no coincide → ver Errores comunes / [enlace al ítem]

### Paso 2 — …
( repetir el patrón: acción → salida esperada → checkpoint → ruta de escape )

## 4. Errores comunes (diagnóstico)
| Síntoma | Causa probable | Solución |
|---|---|---|
| … | … | … |

## 5. Preguntas de comprensión
1. ¿Por qué …?
2. ¿Qué pasaría si …?

## 6. Criterios de evaluación (rúbrica)
| Criterio | Evidencia esperada | Pts |
|---|---|---|
| … | … | … |

## 7. (Opcional) Para profundizar
- Mejora sugerida / lectura / reto adicional
```

### 4.11. Lista de verificación para el profesor antes de publicar la guía

Antes de entregar la guía a los alumnos, valídela contra esta checklist:

- [ ] **La hice yo mismo de principio a fin** siguiendo solo lo escrito (sin atajos mentales).
- [ ] Cada paso tiene **una sola acción** y un checkpoint con condición objetiva.
- [ ] Cada checkpoint tiene una **ruta de escape** hacia los errores comunes.
- [ ] Toda salida esperada que muestro **coincide** con lo que realmente produce el sistema.
- [ ] Los **errores comunes** salieron de una corrida real, no de mi imaginación.
- [ ] Cada **objetivo de aprendizaje** tiene su pregunta de comprensión y su criterio en la rúbrica.
- [ ] Advertí los comportamientos "raros pero esperados" (BD que se vacía, `_links` con IP) para que no se confundan con errores.
- [ ] Un compañero que **no conoce el proyecto** pudo completarla sin preguntarme nada.

## 5. Problemas comunes y soluciones (FAQ)

A continuación se listan los problemas más frecuentes al levantar el ecosistema (Eureka, gateway y los 3 microservicios), junto con su causa y solución concreta.

| Problema | Causa | Solución |
|----------|-------|----------|
| **`Address already in use` / `Web server failed to start. Port 8761 (u 8080, 8000, 8001, 8002) was already in use`** | El puerto ya está ocupado por otra instancia del mismo servicio que quedó corriendo, o por otro programa. | Identifica y mata el proceso que ocupa el puerto. En macOS/Linux: `lsof -i :8761` y luego `kill -9 <PID>` (cambia el puerto según el servicio: 8080 gateway, 8000 pacientes, 8001 medicos, 8002 atenciones). En Windows: `netstat -ano \| findstr :8761` y `taskkill /PID <PID> /F`. Revisa que no tengas el mismo módulo arrancado dos veces (IntelliJ + terminal). |
| **Un microservicio no aparece en el panel de Eureka** (`http://localhost:8761`) | El servicio aún no termina de registrarse, no se levantó Eureka primero, o falta/está mal la URL `eureka.client.service-url.defaultZone`. | 1) Verifica que `msvc-eureka` esté arriba ANTES de arrancar el microservicio. 2) Espera ~30s y refresca el panel (el registro no es instantáneo). 3) Confirma en el `application.properties` del microservicio que exista `eureka.client.service-url.defaultZone=http://localhost:8761/eureka/` y que `spring.application.name` esté definido. 4) Consulta el estado por API: `curl -H "Accept: application/json" http://localhost:8761/eureka/apps`. |
| **El gateway responde `503 Service Unavailable` justo al iniciar** | El gateway rutea con `lb://msvc-XXX`, pero el registro del microservicio en Eureka aún no se ha propagado al gateway, por lo que el balanceador no tiene instancias disponibles. | Es un 503 **transitorio**. Espera ~30s después de arrancar el microservicio para que el gateway baje el registro desde Eureka, y reintenta la petición. Verifica también que el microservicio destino aparezca como `UP` en `http://localhost:8761`. |
| **Orden de arranque incorrecto** (gateway o microservicios antes que Eureka) | Si Eureka no está arriba, los demás no pueden registrarse; si el gateway arranca antes que los microservicios, no encuentra instancias para rutear. | Respeta el orden obligatorio: **1) `msvc-eureka` → 2) microservicios (`msvc-pacientes`, `msvc-medicos`, `msvc-atenciones`) → 3) `msvc-gateway`**. Para la demo en clase: `mvn -pl msvc-eureka spring-boot:run`, luego `mvn -pl msvc-medicos spring-boot:run` (etc.), y por último `mvn -pl msvc-gateway spring-boot:run`. Espera ~30s entre cada paso. |
| **Los `_links` de HATEOAS (API v2) apuntan a la IP del servicio y no al gateway** (ej. `http://10.x.x.x:8001/...` en vez de `http://localhost:8080/...`) | Los microservicios tienen `eureka.instance.prefer-ip-address=true`, por lo que el host que se publica en los enlaces es la IP de la instancia y no la del gateway. | Para clase es **aceptable** (no rompe la demo). Mejora opcional: agregar `server.forward-headers-strategy=framework` en el `application.properties` de cada microservicio, para que respeten las cabeceras de host reenviadas por el gateway y los `_links` apunten al host correcto. |
| **Los datos desaparecen al reiniciar el servicio** (la BD vuelve a quedar vacía) | Cada microservicio usa `spring.jpa.hibernate.ddl-auto=create`, lo que **recrea** la base de datos H2 (archivo `./data/XXX`) vacía en cada arranque. | Comportamiento esperado en este proyecto: avisa que los datos se borran al reiniciar. Si necesitas conservar los datos entre arranques, cambia a `spring.jpa.hibernate.ddl-auto=update` en el `application.properties` del microservicio (no es necesario para la demo). |
| **Feign falla al llamar a otro servicio** (ej. `msvc-atenciones` no puede enriquecer su DTO con datos de medicos o pacientes) | Los `@FeignClient` usan **URLs fijas** (`localhost:8000/8001/8002`). Si el servicio destino está apagado, la llamada falla con error de conexión (connection refused / timeout). | Asegúrate de que el servicio destino esté **arriba** antes de invocar el endpoint que lo consume. Para `msvc-atenciones`, levanta primero `msvc-pacientes` (8000) y `msvc-medicos` (8001). Verifica que respondan directamente, p. ej. `curl http://localhost:8001/api/v1/medicos`, antes de probar atenciones a través del gateway. |

---

## 6. Apéndice: arranque rápido y cómo detener todo

### Arranque rápido (resumen)

```bash
# En la raiz del repo. Una terminal por servicio, EN ESTE ORDEN:
mvn -DskipTests clean package          # compilar todo una vez

mvn -pl msvc-eureka     spring-boot:run   # 1) Eureka  (esperar a que cargue 8761)
mvn -pl msvc-pacientes  spring-boot:run   # 2) microservicios
mvn -pl msvc-medicos    spring-boot:run   #    (esperar ~30s entre cada uno)
mvn -pl msvc-atenciones spring-boot:run
mvn -pl msvc-gateway    spring-boot:run   # 3) Gateway al final
```

Alternativa para clase: ejecutar la clase `main` de cada módulo desde IntelliJ (botón Run),
respetando el mismo orden.

### Cómo detener todo

- Si cada servicio corre en su terminal: `Ctrl + C` en cada una (o cerrar la pestaña Run en IntelliJ).
- Si quedó algún proceso ocupando un puerto:

```bash
# Ver quién ocupa un puerto (cambiar el número segun el servicio)
lsof -i :8761
# Matar ese proceso
kill -9 <PID>
```

### Verificación rápida de que todo está arriba

```bash
curl -H "Accept: application/json" http://localhost:8761/eureka/apps   # servicios registrados
curl http://localhost:8080/api/v1/medicos                              # ruteo por el gateway -> []
```
