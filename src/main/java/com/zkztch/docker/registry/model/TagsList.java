package com.zkztch.docker.registry.model;

import lombok.Data;

import java.util.List;

@Data
public class TagsList {
    private String name;
    private List<String> tags;
}
