package com.zkztch.docker;

import com.spotify.docker.client.exceptions.DockerException;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public interface DockerRegistryClient {

    TagsList tagsList(String image) throws DockerRegistryRequestException;

}
