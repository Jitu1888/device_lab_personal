package com.games24x7.actuatordevices.services;

import com.games24x7.actuatordevices.services.cleanup.SubDirectories;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.List;

@Component
public class AppiumCleanupComponent {

    @Value(value = "${folder.to.cleanup}")
    private String cleanupDirs;

    @Value(value = "${last.n.days.to.keep.data}")
    private int lastNDaysToCleanup;

    @Value(value = "${max.storage.in.GB}")
    private int maxStoragSizeInGB;

    @Value(value = "${tables.to.cleanup}")
    private String tablesToClean;


    /**
     * Making the script to run hourly,
     * feel free to update the frequency as per the need
     *
     * Usage:
     * ------
     * To run this cleanup in multiple folders, please update the application.properties
     * in folder.to.cleanup
     * eg) folder.to.cleanup=/Users/myuser/Downloads,/Users/myuser/Downloads
     */
    @Scheduled(cron = "0 */1 * * * ?")
    public void videoCleanup(){
        System.out.println("Running cleanup .... ");
        SubDirectories subDir = new SubDirectories();
        String[] directories = cleanupDirs.split(",");
        System.out.println("mydirectories " + Arrays.toString(directories));
        Arrays.stream(directories).forEach(directory -> {
            subDir.cleanup(directory, lastNDaysToCleanup, maxStoragSizeInGB);
        });
    }

    // Scheduled job to run every day at midnight
    @Scheduled(cron = "0 0 0 * * *")
    public void sqliteDBCleanup(){
        System.out.println("Running DB cleanup .... ");
        SubDirectories subDir = new SubDirectories();
        List<String> tables = Arrays.asList(tablesToClean.split(","));
        System.out.println("Cleaning tables " + tables);
        if (!tables.isEmpty())
            subDir.dbCleanup(tables);
        else
            throw new RuntimeException("Provide table names");



    }
}
