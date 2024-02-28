package com.example.permitjavaexample.model;

public class Blog {
    private Integer id;
    private String author;
    private String content;

    public Blog() {
    }

    public Blog(Integer id, String author, String content) {
        this.id = id;
        this.author = author;
        this.content = content;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
