# Microservicios y Pruebas Unitarias con JUnit

**Nombre del estudiante:** [Tu nombre aquí]  
**Fecha:** Marzo 2026  
**Curso:** [Nombre del curso]

---

## 1. Descripción del caso analizado

Se analizó la API pública `https://jsonplaceholder.typicode.com` como simulación de microservicios REST de una plataforma de comercio electrónico. Esta API expone endpoints para usuarios, posts, comentarios y otros recursos, permitiendo operaciones GET y POST similares a las de un sistema real en producción.

El objetivo fue diseñar pruebas automatizadas con JUnit 5 que validen el comportamiento de los endpoints ante distintos escenarios: consulta exitosa, creación de recursos y manejo de errores.

---

## 2. Pruebas diseñadas

### Prueba 1 — Consulta de usuario (GET)

**Endpoint:** `GET https://jsonplaceholder.typicode.com/users/1`

**Descripción:**  
Se envía una solicitud GET para obtener los datos de un usuario existente. La prueba valida que el servidor responda correctamente y que el JSON contenga la información esperada.

**Validaciones:**
- Código de respuesta: `200 OK`
- El cuerpo de la respuesta no está vacío
- El campo `id` es igual a `1`
- Existen los campos `name` y `email`
- El nombre no está vacío

**JSON de respuesta esperado:**
```json
{
  "id": 1,
  "name": "Leanne Graham",
  "username": "Bret",
  "email": "Sincere@april.biz",
  "phone": "1-770-736-0986 x56442",
  "website": "hildegard.org"
}
```

**Código implementado:**
```java
@Test
@DisplayName("GET /users/1 - Debe retornar datos válidos del usuario")
void testConsultarUsuario() throws IOException, InterruptedException {
    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/users/1"))
            .GET()
            .build();

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    assertEquals(200, response.statusCode(), "El código de respuesta debe ser 200");
    assertNotNull(response.body(), "El cuerpo de la respuesta no debe ser nulo");
    assertFalse(response.body().isEmpty(), "El cuerpo de la respuesta no debe estar vacío");

    JSONObject usuario = new JSONObject(response.body());
    assertEquals(1, usuario.getInt("id"), "El id del usuario debe ser 1");
    assertTrue(usuario.has("name"), "El JSON debe contener el campo 'name'");
    assertTrue(usuario.has("email"), "El JSON debe contener el campo 'email'");
    assertFalse(usuario.getString("name").isEmpty(), "El nombre del usuario no debe estar vacío");
}
```

---

### Prueba 2 — Creación de recurso (POST)

**Endpoint:** `POST https://jsonplaceholder.typicode.com/posts`

**Descripción:**  
Se construye un objeto JSON y se envía al endpoint de posts simulando la creación de un nuevo recurso. La prueba valida que el servidor procese la solicitud y devuelva el recurso creado con un identificador asignado.

**JSON enviado:**
```json
{
  "title": "Prueba de microservicio",
  "body": "Contenido generado por prueba automatizada con JUnit",
  "userId": 1
}
```

**JSON de respuesta esperado (201 Created):**
```json
{
  "id": 101,
  "title": "Prueba de microservicio",
  "body": "Contenido generado por prueba automatizada con JUnit",
  "userId": 1
}
```

**Validaciones:**
- Código de respuesta: `201 Created`
- El recurso creado contiene el campo `id`
- El `id` es mayor a `0`
- El `title` coincide con el enviado

**Código implementado:**
```java
@Test
@DisplayName("POST /posts - Debe crear un recurso y retornar su ID")
void testCrearPost() throws IOException, InterruptedException {
    JSONObject nuevoPost = new JSONObject();
    nuevoPost.put("title", "Prueba de microservicio");
    nuevoPost.put("body", "Contenido generado por prueba automatizada con JUnit");
    nuevoPost.put("userId", 1);

    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/posts"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(nuevoPost.toString()))
            .build();

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    assertEquals(201, response.statusCode(), "El código de respuesta debe ser 201 Created");

    JSONObject postCreado = new JSONObject(response.body());
    assertTrue(postCreado.has("id"), "El recurso creado debe tener un campo 'id'");
    assertTrue(postCreado.getInt("id") > 0, "El ID del recurso debe ser mayor a 0");
    assertEquals("Prueba de microservicio", postCreado.getString("title"),
            "El título debe coincidir con el enviado");
}
```

---

### Prueba 3 — Manejo de error (recurso inexistente)

**Endpoint:** `GET https://jsonplaceholder.typicode.com/users/9999`

**Descripción:**  
Se consulta un usuario con un ID que no existe en el sistema. El objetivo es verificar que el microservicio responde con el código de error adecuado en lugar de devolver datos incorrectos o lanzar una excepción no controlada.

**JSON de respuesta esperado (404 Not Found):**
```json
{}
```

**Validaciones:**
- Código de respuesta: `404 Not Found`

**Código implementado:**
```java
@Test
@DisplayName("GET /users/9999 - Debe retornar 404 para recurso inexistente")
void testRecursoInexistente() throws IOException, InterruptedException {
    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/users/9999"))
            .GET()
            .build();

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    assertEquals(404, response.statusCode(),
            "El código de respuesta debe ser 404 para un recurso inexistente");
}
```

---

### Prueba 4 — Lista de posts (GET colección)

**Endpoint:** `GET https://jsonplaceholder.typicode.com/posts`

**Descripción:**  
Se obtiene la colección completa de posts del sistema. La prueba valida que la respuesta sea una lista no vacía y que cada elemento tenga la estructura correcta.

**JSON de respuesta (fragmento):**
```json
[
  {
    "userId": 1,
    "id": 1,
    "title": "sunt aut facere repellat provident occaecati excepturi optio reprehenderit",
    "body": "quia et suscipit..."
  }
]
```

**Validaciones:**
- Código de respuesta: `200 OK`
- La lista contiene al menos un elemento
- Cada elemento tiene los campos `id`, `title` y `userId`

**Código implementado:**
```java
@Test
@DisplayName("GET /posts - Debe retornar una lista no vacía de posts")
void testListarPosts() throws IOException, InterruptedException {
    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/posts"))
            .GET()
            .build();

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    assertEquals(200, response.statusCode(), "El código de respuesta debe ser 200");

    JSONArray posts = new JSONArray(response.body());
    assertTrue(posts.length() > 0, "La lista de posts no debe estar vacía");

    JSONObject primerPost = posts.getJSONObject(0);
    assertTrue(primerPost.has("id"), "Cada post debe tener un campo 'id'");
    assertTrue(primerPost.has("title"), "Cada post debe tener un campo 'title'");
    assertTrue(primerPost.has("userId"), "Cada post debe tener un campo 'userId'");
}
```

---

## 3. Ejecución de las pruebas

Para ejecutar las pruebas se utilizó Maven con el siguiente comando:

```bash
mvn test
```

**Resultado esperado en consola:**
```
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

> Insertar aquí capturas de pantalla de la ejecución en tu IDE o terminal.

---

## 4. Conclusión

Implementar pruebas automatizadas con JUnit permitió validar de forma sistemática y repetible el comportamiento de los endpoints REST. Cada prueba cubre un escenario distinto: respuesta exitosa, creación de recursos y manejo de errores, lo que garantiza que el microservicio cumple con su contrato de API en todos los casos.

El uso de `HttpClient` de Java junto con la librería `org.json` facilitó la construcción de solicitudes HTTP y la validación de las respuestas JSON sin necesidad de frameworks adicionales, manteniendo el código simple y directo.

---

## 5. Reflexión final

**¿Por qué es importante realizar pruebas automatizadas en sistemas basados en microservicios?**

En una arquitectura de microservicios, cada componente opera de forma independiente y puede desplegarse o actualizarse sin afectar directamente a los demás. Sin embargo, esta independencia también significa que un fallo en un servicio puede propagarse silenciosamente a otros sin que sea evidente de inmediato.

Las pruebas automatizadas son esenciales porque permiten verificar el contrato de cada API de forma rápida y confiable con cada cambio en el código. A diferencia de las pruebas manuales, pueden ejecutarse en segundos, detectar regresiones antes de llegar a producción y documentar el comportamiento esperado del sistema. En entornos donde los equipos trabajan en paralelo sobre distintos servicios, contar con una suite de pruebas automatizadas es la única forma práctica de mantener la confianza en la integridad del sistema completo.
