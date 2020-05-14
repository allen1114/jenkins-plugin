package com.zkztch.docker.registry;

import java.util.List;

public interface DockerRegistryClient {

    List<String> listRepositories() throws DockerRegistryException;

    List<String> listTags(String repository) throws DockerRegistryException;

}
