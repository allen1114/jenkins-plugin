package com.zkztch.docker.registry.model;

import lombok.Data;

import java.util.List;

@Data
public class Catalog {
    private List<String> repositories;
}
