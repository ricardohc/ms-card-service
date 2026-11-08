# Skill universal — Migración tecnológica Reactor/WebFlux a RxJava 3

> Aplicable a cualquier microservicio Java/Spring Boot del repositorio abierto. No está asociado a Customer, Card ni a otro dominio concreto. Ejecución supervisada en IntelliJ/CodeGRP: solicitar aprobación antes de modificar y al terminar cada fase.

## Objetivo
Sustituir el uso de `Mono` y `Flux` en el código de aplicación por RxJava 3 (`Single`, `Maybe`, `Flowable`, `Observable` cuando proceda y `Completable`), preservando íntegramente el comportamiento existente. **Prohibido cambiar lógica de negocio, contratos, persistencia, eventos, seguridad, reglas, respuestas, validaciones o resultados.** La migración es tecnológica, no funcional.

## Alcance y límites
- Inspeccionar el proyecto real: `pom.xml`, código fuente, interfaces OpenAPI generadas, pruebas, configuraciones, clientes HTTP, repositorios, seguridad y Kafka si existen.
- No asumir clases, rutas, paquetes, endpoints, tópicos, eventos, esquemas o dependencias por el nombre del servicio.
- Mantener la versión de Java, Spring Boot y Spring Cloud salvo incompatibilidad demostrada y aprobada.
- Mantener `spring-boot-starter-webflux` cuando Spring lo necesite para HTTP, seguridad o integraciones. **RxJava 3 no reemplaza por sí mismo el runtime WebFlux/Reactor**. La meta es RxJava en la lógica de aplicación y adaptaciones mínimas en las fronteras de infraestructura.
- Preservar OpenAPI congelado, URL, método HTTP, cabeceras, códigos, DTO, JSON, manejo de errores y compatibilidad de consumidores.
- Conservar la base propia de cada servicio y sus repositorios, consultas, índices, transacciones, paginación y comportamiento ante datos inexistentes.
- Conservar Kafka únicamente donde ya exista: productores, consumidores, claves, eventos, tópicos, orden, reintentos e idempotencia. **No agregar Kafka a un CRUD por esta migración**.
- No introducir nuevas funcionalidades, patrones, refactorizaciones de negocio ni dependencias sin justificación.

## `pom.xml`: modelo de referencia, NO copiar ciegamente
Tomar como referencia el `pom.xml` proporcionado de `ms-card-service`, manteniendo la identidad, versiones y capacidades propias del microservicio abierto.

**Dependencias base cuando apliquen:**
- `io.reactivex.rxjava3:rxjava` para RxJava 3.
- `io.projectreactor.addons:reactor-adapter` solamente si hay conversiones necesarias entre Reactor y RxJava; comprobar versión compatible.
- `spring-boot-starter-webflux` para el runtime HTTP reactivo de Spring.
- `spring-boot-starter-data-mongodb-reactive` cuando el proyecto usa MongoDB reactivo.
- Mantener Lombok, validación, seguridad JWT, Spring Cloud Config, Eureka, Resilience4j, Actuator, Micrometer, Springdoc, OpenAPI Generator, Checkstyle y JaCoCo **si ya forman parte de los requisitos del proyecto**.
- Mantener `spring-kafka` y/o `reactor-kafka` solo cuando el código realmente los utilice; no eliminar una dependencia por su nombre sin comprobar usos.
- Mantener librerías de prueba apropiadas; `reactor-test` puede seguir siendo necesario para pruebas de infraestructura WebFlux.

**OpenAPI Generator:** inspeccionar la versión real y la salida generada. Opciones como `<async>true</async>` y `<rxjava3>true</rxjava3>` no garantizan que el generador `spring` produzca controladores con retornos RxJava. Si genera `CompletableFuture<ResponseEntity<T>>`, adaptar desde `Single` en el borde HTTP sin bloquear. Nunca editar código generado a mano. Mantener `inputSpec`, `apiPackage` y `modelPackage` del microservicio real.

**Validación Maven:** ejecutar `mvn dependency:tree`, `mvn -DskipTests compile` y, al final, `mvn clean verify`. No declarar compilación exitosa sin haberla ejecutado.

## Equivalencias semánticas
| Reactor existente | RxJava 3 propuesto | Condición |
|---|---|---|
| `Mono<T>` con exactamente un resultado | `Single<T>` | Nunca vacío |
| `Mono<T>` con cero o un resultado | `Maybe<T>` | Preservar ausencia y 404 |
| `Mono<Void>` | `Completable` | Sin valor |
| `Flux<T>` | `Flowable<T>` | Backpressure |
| `Flux<T>` sin necesidad de backpressure | `Observable<T>` | Solo si está justificado |
| `Mono.zip` | `Single.zip` u operador equivalente | Misma combinación y errores |
| `Flux.collectList()` | `Flowable.toList()` | Igual orden y elementos |
| `StepVerifier` | `TestObserver` / `TestSubscriber` | Misma cobertura de casos |

No realizar sustituciones textuales automáticas: revisar cardinalidad, evaluación diferida, errores, cancelación, orden y concurrencia.

## Prohibiciones
- No usar `.block()`, `.blockingGet()`, `.blockingAwait()`, `Future.get()` o `.join()` en la ruta de petición.
- No ocultar errores con `.subscribe()` interno o fire-and-forget; preservar confirmación y tratamiento de fallos en Kafka y operaciones críticas.
- No cambiar comportamiento de `retry`, `timeout`, circuit breaker, transacciones, idempotencia, logging o trazabilidad.
- No reemplazar `@EnableWebFluxSecurity`, filtros o handlers de Spring sin comprobar compatibilidad.
- No tocar contratos OpenAPI, estructuras MongoDB ni eventos Kafka congelados.

## Ejecución supervisada
### Fase 0 — Diagnóstico (solo lectura)
Inventariar dependencias, todos los usos de Reactor, interfaces generadas, controladores, servicios, repositorios, Kafka, seguridad y tests. Producir matriz `archivo | método | tipo actual | tipo destino | adaptación | riesgo`. Ejecutar baseline si es posible. **Detenerse y pedir aprobación**.

### Fase 1 — POM y generación
Modificar exclusivamente dependencias y configuración de generación necesarias para RxJava 3, conservando el resto. Verificar compatibilidad y firmas realmente generadas; compilar. **Detenerse y pedir aprobación**.

### Fase 2 — Persistencia
Migrar repositorios a API RxJava 3 **solo si la versión de Spring Data soporta realmente la interfaz elegida**; en otro caso usar adaptación Reactor↔RxJava en la frontera del repositorio. Preservar métodos derivados, semántica de ausencia, paginación y errores. Compilar/probar. **Detenerse y pedir aprobación**.

### Fase 3 — Servicios
Cambiar interfaces, implementaciones y operadores a RxJava 3 sin modificar ninguna regla ni orden de operaciones. Conservar idempotencia y transacciones. Ejecutar pruebas. **Detenerse y pedir aprobación**.

### Fase 4 — Controladores y HTTP
Adaptar firmas a las interfaces OpenAPI generadas; si el borde Spring requiere `Mono`, `Flux` o `CompletableFuture`, usar adaptadores no bloqueantes. Mantener seguridad, manejo de excepciones, headers, status y respuestas idénticas. Ejecutar pruebas de contrato. **Detenerse y pedir aprobación**.

### Fase 5 — Integraciones existentes
Revisar clientes HTTP, Kafka, caché, resiliencia, trazas y métricas **solo si están presentes**. Migrar APIs de aplicación cuando proceda, preservando el protocolo y garantías actuales. No agregar integraciones nuevas. **Detenerse y pedir aprobación**.

### Fase 6 — Regresión y cierre
Ejecutar pruebas unitarias e integración, `mvn clean verify`, Checkstyle, JaCoCo y comparativa antes/después de respuestas, errores y efectos en Mongo/Kafka. Documentar usos residuales de Reactor y por qué son necesarios en el runtime o en fronteras. **No declarar migración 100 % RxJava si permanecen usos no justificados en lógica de aplicación.** Solicitar aprobación final.

## Entrega de cada fase
```text
MICROSERVICIO: [detectado del pom.xml]
FASE: [0-6]
ARCHIVOS ANALIZADOS:
ARCHIVOS MODIFICADOS:
CAMBIOS TECNOLÓGICOS:
CAMBIOS FUNCIONALES: NINGUNO (o BLOQUEAR Y EXPLICAR)
DEPENDENCIAS AÑADIDAS/RETIRADAS Y JUSTIFICACIÓN:
COMPILACIÓN / PRUEBAS EJECUTADAS Y RESULTADO:
CONTRATOS Y COMPORTAMIENTOS PRESERVADOS:
USOS DE REACTOR RESTANTES Y JUSTIFICACIÓN:
RIESGOS / BLOQUEOS:
¿AUTORIZAS LA SIGUIENTE FASE?:
```

## Instrucción para el agente de IntelliJ
```text
Lee @markdown_cambio_tec_webflux_to_rxjava_general.md. Aplica esta migración tecnológica al microservicio actualmente abierto, sin asumir su dominio. Cambia el código de aplicación de Reactor Mono/Flux a RxJava 3, usando las dependencias y adaptaciones necesarias. Mantén intactas todas las reglas de negocio, contratos OpenAPI, bases de datos, Kafka, seguridad, configuración, respuestas y pruebas funcionales. No copies nombres, rutas ni paquetes de otro microservicio. Empieza SOLO por la Fase 0, sin modificar archivos, y solicita mi aprobación expresa para cada fase.
```

## Criterio de aceptación
La aplicación compila, las pruebas relevantes pasan, el comportamiento externo y los efectos persistentes son equivalentes al estado anterior, el código de aplicación usa RxJava 3 y las dependencias/usos de Reactor restantes están justificados por Spring o integraciones existentes.
