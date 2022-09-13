package org.example;

import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Main {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException, URISyntaxException, ExecutionException, InterruptedException {
        var file = new File(Main.class.getResource("/urls.properties").toURI()).toPath();
        var images = Files.readAllLines(file).stream().filter(image -> !image.contains("civitatis")).toList();
        var executor = Executors.newFixedThreadPool(120);
        var tasks = new ArrayList<Future>();
        for (; ; ) {
            var now = new Date();
            for (var image : images) {
                tasks.add(executor.submit(() -> readImage(image)));
            }
            for (var task : tasks) {
                task.get();
            }
            LOGGER.warn("ITERATION In {} ms", new Date().getTime() - now.getTime());
        }
    }

    private static void readImage(String image) {
        Date start = new Date();
        try {
            InputStream in = new URL(image).openStream();

            try {
                in.readAllBytes();
            } finally {
                in.close();
            }
        } catch (IOException e) {
            LOGGER.debug("Error reading {} in {}ms:  {}", image, new Date().getTime() - start.getTime(), e.getMessage());
        }
    }
}
