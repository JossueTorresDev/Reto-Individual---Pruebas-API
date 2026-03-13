package com.qa;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas automatizadas para validar el comportamiento de microservicios REST
 * utilizando la API pública: https://jsonplaceholder.typicode.com
 */
public class MicroservicioTest {

    private static final String BASE_URL = "https://jsonplaceholder.typicode.com";
    private final HttpClient client = HttpClient.newHttpClient();

    // -------------------------------------------------------------------------
    // PRUEBA 1: GET - Consulta de un usuario existente
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("GET /users/1 - Debe retornar datos válidos del usuario")
    void testConsultarUsuario() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/users/1"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Validar código de respuesta 200 OK
        assertEquals(200, response.statusCode(), "El código de respuesta debe ser 200");

        // Validar que el cuerpo no está vacío
        assertNotNull(response.body(), "El cuerpo de la respuesta no debe ser nulo");
        assertFalse(response.body().isEmpty(), "El cuerpo de la respuesta no debe estar vacío");

        // Validar campos del JSON
        JSONObject usuario = new JSONObject(response.body());
        assertEquals(1, usuario.getInt("id"), "El id del usuario debe ser 1");
        assertTrue(usuario.has("name"), "El JSON debe contener el campo 'name'");
        assertTrue(usuario.has("email"), "El JSON debe contener el campo 'email'");
        assertFalse(usuario.getString("name").isEmpty(), "El nombre del usuario no debe estar vacío");

        System.out.println("Usuario obtenido: " + usuario.getString("name"));
        System.out.println("Email: " + usuario.getString("email"));
    }

    // -------------------------------------------------------------------------
    // PRUEBA 2: POST - Creación de un nuevo post
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("POST /posts - Debe crear un recurso y retornar su ID")
    void testCrearPost() throws IOException, InterruptedException {
        // Cuerpo de la solicitud
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

        // Validar código de respuesta 201 Created
        assertEquals(201, response.statusCode(), "El código de respuesta debe ser 201 Created");

        // Validar que el recurso creado tiene un ID
        JSONObject postCreado = new JSONObject(response.body());
        assertTrue(postCreado.has("id"), "El recurso creado debe tener un campo 'id'");
        assertTrue(postCreado.getInt("id") > 0, "El ID del recurso debe ser mayor a 0");

        // Validar que los datos enviados se reflejan en la respuesta
        assertEquals("Prueba de microservicio", postCreado.getString("title"),
                "El título debe coincidir con el enviado");

        System.out.println("Post creado con ID: " + postCreado.getInt("id"));
    }

    // -------------------------------------------------------------------------
    // PRUEBA 3: GET - Manejo de error con recurso inexistente
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("GET /users/9999 - Debe retornar 404 para recurso inexistente")
    void testRecursoInexistente() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/users/9999"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Validar que el servidor responde con 404 Not Found
        assertEquals(404, response.statusCode(),
                "El código de respuesta debe ser 404 para un recurso inexistente");

        System.out.println("Código de error recibido: " + response.statusCode());
        System.out.println("Cuerpo de respuesta: " + response.body());
    }

    // -------------------------------------------------------------------------
    // PRUEBA EXTRA: GET - Consulta de lista de posts
    // -------------------------------------------------------------------------
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

        // Validar estructura del primer elemento
        JSONObject primerPost = posts.getJSONObject(0);
        assertTrue(primerPost.has("id"), "Cada post debe tener un campo 'id'");
        assertTrue(primerPost.has("title"), "Cada post debe tener un campo 'title'");
        assertTrue(primerPost.has("userId"), "Cada post debe tener un campo 'userId'");

        System.out.println("Total de posts obtenidos: " + posts.length());
    }
}
