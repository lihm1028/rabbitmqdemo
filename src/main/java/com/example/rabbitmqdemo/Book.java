package com.example.rabbitmqdemo;

import lombok.Data;

import java.io.Serializable;

@Data
public class Book implements Serializable {

    private Integer id;

    private String name;

    private String email;

    public Book() {
    }

    public Book(String name, String email) {
        this.name = name;
        this.email = email;
    }

    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
