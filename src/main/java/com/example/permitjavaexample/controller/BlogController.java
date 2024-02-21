package com.example.permitjavaexample.controller;

import com.example.permitjavaexample.model.Blog;
import com.example.permitjavaexample.service.BlogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    public List<Blog> getAllBlogs() {
        return blogService.getAllBlogs();
    }

    @GetMapping("/{id}")
    public Blog getBlogById(@PathVariable("id") int id) {
        Blog blog = blogService.getBlogById(id);
        if (blog == null) {
            throw new RuntimeException("Blog with id " + id + " not found");
        }
        return blog;
    }

    @PostMapping
    public Blog addBlog(@RequestBody Blog blog) {
        return blogService.addBlog(blog);
    }

    @PutMapping("/{id}")
    public Blog updateBlog(@PathVariable("id") int id, @RequestBody Blog blog) {
        Blog updatedBlog = blogService.updateBlog(id, blog);
        if (updatedBlog == null) {
            throw new RuntimeException("Blog with id " + id + " not found");
        }
        return updatedBlog;
    }

    @DeleteMapping("/{id}")
    public String deleteBlog(@PathVariable("id") int id) {
        boolean isDeleted = blogService.deleteBlog(id);
        if (!isDeleted) {
            throw new RuntimeException("Blog with id " + id + " not found");
        }
        return "Deleted blog with id " + id;
    }
}
