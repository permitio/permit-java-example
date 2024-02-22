package com.example.permitjavaexample.service;

import com.example.permitjavaexample.exception.ResourceNotFoundException;
import com.example.permitjavaexample.model.Blog;
import io.permit.sdk.enforcement.Resource;
import io.permit.sdk.enforcement.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class BlogService {
    private final List<Blog> blogs = new ArrayList<>();
    private final AtomicInteger blogIdCounter = new AtomicInteger();

    private final Resource.Builder resourceBuilder = new Resource.Builder("blog");
    private final UserService userService;


    public BlogService(UserService userService) {
        this.userService = userService;
        blogs.add(new Blog(blogIdCounter.incrementAndGet(), "user1", "First Blog Post"));
        blogs.add(new Blog(blogIdCounter.incrementAndGet(), "user2", "Second Blog Post"));
    }

    private void authorize(User user, String action) {
        userService.authorize(user, action, resourceBuilder.build());
    }

    private void authorize(User user, String action, Blog blog) {
        var attributes = new HashMap<String, Object>();
        attributes.put("author", blog.getAuthor());
        userService.authorize(user, action, resourceBuilder.withKey(blog.getId().toString()).withAttributes(attributes).build());
    }


    private Blog getBlogById(int id) {
        return blogs.stream()
                .filter(blog -> blog.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Blog with id " + id + " not found"));
    }

    public List<Blog> getAllBlogs(User user) {
        authorize(user, "read");
        return new ArrayList<>(blogs);
    }

    public Blog getBlog(User user, int id) {
        authorize(user, "read");
        return getBlogById(id);
    }

    public Blog addBlog(User user, String content) {
        authorize(user, "create");
        Blog blog = new Blog(blogIdCounter.incrementAndGet(), user.getKey(), content);
        blogs.add(blog);
        return blog;
    }

    public Blog updateBlog(User user, int id, String content) {
        Blog blog = getBlogById(id);
        authorize(user, "update", blog);
        blog.setContent(content);
        return blog;
    }

    public void deleteBlog(User user, int id) {
        boolean isDeleted = blogs.removeIf(blog -> {
            if (blog.getId().equals(id)) {
                authorize(user, "delete", blog);
                return true;
            } else {
                return false;
            }
        });
        if (!isDeleted) {
            throw new ResourceNotFoundException("Blog with id " + id + " not found");
        }
    }
}
