package com.games24x7.actuatordevices.services.cleanup;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
public class SubDirectories {

    private static final String DATABASE_URL = "jdbc:sqlite:" + System.getenv("HOME")+"/.cache/appium-dashboard-plugin/database.sqlite"; // Replace with your database path

    public void cleanup(String directoryPath, int noOfDays, int maxStorageSize) {
        final long[] totalFileSize = {0};
        final long maxFileSizeCap = maxStorageSize * 1000000000L;
        try {
            String absDirectoryPath = System.getenv("HOME")+directoryPath;
            System.out.println("the directory path "+absDirectoryPath);
            Path directory = Paths.get( absDirectoryPath);
            if(!Files.exists(directory)){
                throw new RuntimeException("Input Path for cleanup --- "+ absDirectoryPath +" ---- doesn't exist ");
            }
            List<FilePath> files = new ArrayList<>();
            Files.walkFileTree(Paths.get(absDirectoryPath), new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        totalFileSize[0] += Files.size(file);
                        files.add(new FilePath(file, Files.size(file), Files.getLastModifiedTime(file).toMillis()));
                    return FileVisitResult.CONTINUE;
                }
            });
            log.info("File Size before deleting==== {}", totalFileSize[0]);

            log.info("Sorting files based on the age");
            files.sort((p1, p2) -> {
                try {
                    return Long.compare(Files.getLastModifiedTime(p1.getFilePath()).toMillis(),
                            Files.getLastModifiedTime(p2.getFilePath()).toMillis());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            LocalDate currentDate = LocalDate.now();
            LocalDate yesterdayDate = currentDate.minusDays(noOfDays);
            LocalDateTime startOfHistoricDate = yesterdayDate.atStartOfDay();
            log.info("Start of the day before {} days is :{}",noOfDays, startOfHistoricDate);

            log.info("Removing files which are older than {}",startOfHistoricDate);
            for (FilePath file : files) {
                if(file.getFilePath().toFile().exists()) {
                    LocalDateTime myFileDate = Instant.ofEpochMilli(file.getTimestamp())
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime();
                    if(myFileDate.isBefore(startOfHistoricDate) ) {
                        file.getFilePath().toFile().delete();
                        log.info("Deleted File by Age with name ==> {}, | FileSize ==> {} | timeStamp ==>", file.getFilePath(), file.getFileSize(), new Date(file.getTimestamp()));
                        totalFileSize[0] -= file.getFileSize();
                    }

                }
            }

//            Remove files which are still older and within yesterday
            if(totalFileSize[0] > maxFileSizeCap){
                log.info("Total file size is {} ,still more than max storage size {}, so cleaning up files by size and whichever is the oldest among them", totalFileSize[0], maxFileSizeCap);
            }
            for (FilePath file : files) {
                if(file.getFilePath().toFile().exists()) {
                    if(totalFileSize[0] > maxFileSizeCap) {
                        FilePath fileToBeDeleted = file;
                        long fileSizeToBeDeleted = fileToBeDeleted.getFileSize();
                        file.getFilePath().toFile().delete();
                        log.info("Deleted File by size with name ==> {}, | FileSize ==> {} | timeStamp ==>", file.getFilePath(), file.getFileSize(), new Date(file.getTimestamp()));
                        totalFileSize[0] -= fileSizeToBeDeleted;
                    }

                }
            }
            log.info("File Size after deleting==== {}", totalFileSize[0]);
        } catch (IOException e) {
            log.error("Error listing files: {}", e.getMessage());
        }
    }


    public void dbCleanup(List<String> tables) {

        String columnName = "updated_at";
        Statement statement = null;
        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {

            for (String tableName : tables) {

                statement = conn.createStatement();
                String deleteQuery = "DELETE FROM " + tableName + " WHERE " + columnName + " <= date('now', '-15 day')";
                log.info(deleteQuery);
                statement.execute("PRAGMA busy_timeout = 30000");
                int rowsDeleted = statement.executeUpdate(deleteQuery);
                log.info(rowsDeleted + " rows deleted from the database table " + tableName);

            }
            statement.execute("VACUUM");
        } catch (SQLException e) {
            log.error("Error cleaning table: " + e.getMessage());
        }
    }
}

