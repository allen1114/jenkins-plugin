package com.zkztch.docker;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TagsList {

    private String name = "";
    private List<String> tags = new ArrayList<>();

    public static final TagsList empty = new TagsList();
}
