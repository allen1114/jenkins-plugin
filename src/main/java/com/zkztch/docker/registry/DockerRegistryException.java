package com.zkztch.docker.registry;

public class DockerRegistryException extends Exception {

    public DockerRegistryException() {
    }

    public DockerRegistryException(String message) {
        super(message);
    }

    public DockerRegistryException(String message, Throwable cause) {
        super(message, cause);
    }

    public DockerRegistryException(Throwable cause) {
        super(cause);
    }

    public DockerRegistryException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
