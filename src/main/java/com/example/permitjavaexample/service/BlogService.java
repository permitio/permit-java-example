package com.example.permitjavaexample.service;

import com.example.permitjavaexample.model.Blog;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class BlogService {
    private final List<Blog> blogs = new ArrayList<>();
    private final AtomicInteger blogIdCounter = new AtomicInteger();

    public BlogService(@Value("${permit.api-key}") String apiKey) {
        this.permit = new Permit(
                new PermitConfig.Builder(apiKey)
                        .withPdpAddress("https://cloudpdp.api.permit.io")
                        .build()
        );

        blogs.add(new Blog(blogIdCounter.incrementAndGet(), "user1", "First Blog Post"));
        blogs.add(new Blog(blogIdCounter.incrementAndGet(), "user2", "Second Blog Post"));
    }

    public List<Blog> getAllBlogs() {
        return new ArrayList<>(blogs);
    }

    public Blog getBlogById(int id) {
        return blogs.stream()
                .filter(blog -> blog.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public Blog addBlog(Blog blog) {
        blog.setId(blogIdCounter.incrementAndGet());
        blogs.add(blog);
        return blog;
    }

    public Blog updateBlog(int id, Blog updatedBlog) {
        Blog blog = getBlogById(id);
        if (blog != null) {
            blog.setContent(updatedBlog.getContent());
            return blog;
        }
        return null;
    }

    public boolean deleteBlog(int id) {
        return blogs.removeIf(blog -> blog.getId().equals(id));
    }
}
