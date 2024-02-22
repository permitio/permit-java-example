package com.example.permitjavaexample;

import com.example.permitjavaexample.service.BlogService;
import io.permit.sdk.enforcement.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BlogControllerIntegrationTests {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private BlogService blogService;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
    }

    ResponseEntity<String> sendRequestWithToken(String url, HttpMethod method, String token, Object body) {
        if (token == null)
            return restTemplate.exchange(baseUrl + url, method, null, String.class);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<Object> entity = new HttpEntity<>(body, headers);

        return restTemplate.exchange(baseUrl + url, method, entity, String.class);
    }

    ResponseEntity<String> sendRequestWithToken(String url, HttpMethod method, String token) {
        return sendRequestWithToken(url, method, token, null);
    }

    @Test
    void listBlogsUnauthenticated() {
        var response = sendRequestWithToken("/api/blogs", HttpMethod.GET, null);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @ParameterizedTest
    @ValueSource(strings = {"viewer", "editor", "admin"})
    void listBlogs(String user) {
        var response = sendRequestWithToken("/api/blogs", HttpMethod.GET, user);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void listBlogsUnknown() {
        var response = sendRequestWithToken("/api/blogs", HttpMethod.GET, "someone");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @ParameterizedTest
    @ValueSource(strings = {"viewer", "editor", "admin"})
    void getBlogById(String user) {
        var blog = blogService.addBlog(new User.Builder("editor").build(), "Test Content");
        var response = sendRequestWithToken("/api/blogs/" + blog.getId(), HttpMethod.GET, user);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getBlogByIdNotFound() {
        var response = sendRequestWithToken("/api/blogs/999", HttpMethod.GET, "viewer");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @ParameterizedTest
    @ValueSource(strings = {"editor", "admin"})
    void createBlog(String user) {
        var response = sendRequestWithToken("/api/blogs", HttpMethod.POST, user, "Test Content");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void createBlogForbidden() {
        var response = sendRequestWithToken("/api/blogs", HttpMethod.POST, "viewer", "Test Content");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @ParameterizedTest
    @ValueSource(strings = {"editor", "admin"})
    void updateOwnBlog(String user) {
        var blog = blogService.addBlog(new User.Builder("editor").build(), "Test Content");
        var response = sendRequestWithToken("/api/blogs/" + blog.getId(), HttpMethod.PUT, user, "Test Content");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void updateOthersBlogAsAdmin() {
        var blog = blogService.addBlog(new User.Builder("editor").build(), "Test Content");
        var response = sendRequestWithToken("/api/blogs/" + blog.getId(), HttpMethod.PUT, "admin", "Test Content");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @ParameterizedTest
    @ValueSource(strings = {"viewer", "editor"})
    void updateOthersBlogForbidden(String user) {
        var blog = blogService.addBlog(new User.Builder("admin").build(), "Test Content");
        var response = sendRequestWithToken("/api/blogs/" + blog.getId(), HttpMethod.PUT, user, "Test Content");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void updateBlogNotFound() {
        var response = sendRequestWithToken("/api/blogs/999", HttpMethod.PUT, "editor", "Test Content");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @ParameterizedTest
    @ValueSource(strings = {"editor", "admin"})
    void deleteOwnBlog(String user) {
        var blog = blogService.addBlog(new User.Builder("editor").build(), "Test Content");
        var response = sendRequestWithToken("/api/blogs/" + blog.getId(), HttpMethod.DELETE, user);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void deleteOthersBlogAsAdmin() {
        var blog = blogService.addBlog(new User.Builder("editor").build(), "Test Content");
        var response = sendRequestWithToken("/api/blogs/" + blog.getId(), HttpMethod.DELETE, "admin");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @ParameterizedTest
    @ValueSource(strings = {"viewer", "editor"})
    void deleteOthersBlogForbidden(String user) {
        var blog = blogService.addBlog(new User.Builder("admin").build(), "Test Content");
        var response = sendRequestWithToken("/api/blogs/" + blog.getId(), HttpMethod.DELETE, user);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void deleteBlogNotFound() {
        var response = sendRequestWithToken("/api/blogs/999", HttpMethod.DELETE, "editor");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
