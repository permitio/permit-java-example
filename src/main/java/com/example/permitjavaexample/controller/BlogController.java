package com.example.permitjavaexample.controller;

import com.example.permitjavaexample.model.Blog;
import com.example.permitjavaexample.service.BlogService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import io.permit.sdk.enforcement.User;

import java.util.List;

@RestController
@RequestMapping("/api/blogs")
public class BlogController {
    private final BlogService blogService;

    @Autowired
    public BlogController(BlogService blogService) {
        this.blogService = blogService;
    }

    @GetMapping
    public List<Blog> getAllBlogs(HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("user");
        return blogService.getAllBlogs(currentUser);
    }

    @GetMapping("/{id}")
    public Blog getBlogById(HttpServletRequest request, @PathVariable("id") int id) {
        User currentUser = (User) request.getAttribute("user");
        return blogService.getBlog(currentUser, id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Blog addBlog(HttpServletRequest request, @RequestBody String content) {
        User currentUser = (User) request.getAttribute("user");
        return blogService.addBlog(currentUser, content);
    }

    @PutMapping("/{id}")
    public Blog updateBlog(HttpServletRequest request, @PathVariable("id") int id, @RequestBody String content) {
        User currentUser = (User) request.getAttribute("user");
        return blogService.updateBlog(currentUser, id, content);
    }

    @DeleteMapping("/{id}")
    public String deleteBlog(HttpServletRequest request, @PathVariable("id") int id) {
        User currentUser = (User) request.getAttribute("user");
        blogService.deleteBlog(currentUser, id);
        return "Deleted blog with id " + id;
    }
}
