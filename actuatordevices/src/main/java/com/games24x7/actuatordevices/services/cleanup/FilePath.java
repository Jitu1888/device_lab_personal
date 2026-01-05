package com.games24x7.actuatordevices.services.cleanup;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.nio.file.Path;

@Getter
@Setter
@AllArgsConstructor
public class FilePath {

    private Path filePath;
    private long fileSize;
    private long timestamp;
}
