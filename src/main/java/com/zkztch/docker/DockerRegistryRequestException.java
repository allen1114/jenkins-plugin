package com.zkztch.docker;

import lombok.Getter;

@Getter
public class DockerRegistryRequestException extends Exception {

    private final int status;

    public DockerRegistryRequestException(int status) {
        this.status = status;
    }

    public DockerRegistryRequestException(String message, int status) {
        super(message);
        this.status = status;
    }

    public DockerRegistryRequestException(String message, Throwable cause, int status) {
        super(message, cause);
        this.status = status;
    }

    public DockerRegistryRequestException(Throwable cause, int status) {
        super(cause);
        this.status = status;
    }

    public DockerRegistryRequestException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace,
                                          int status) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.status = status;
    }
}
