package org.example;

import org.example.utils.Sorter;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;

public class Main {

    public static void main(String[] args) {
        Instant start = Instant.now();

        if (args.length < 1) {
            System.out.println("Specify text file path as 1st argument");
            return;
        }

        Path filepath;
        try {
            filepath = Path.of(args[0]);
        } catch (InvalidPathException e) {
            System.out.println("Invalid path: " + args[0]);
            return;
        }

        Sorter.sort(filepath);

        Duration duration = Duration.between(start, Instant.now());
        System.out.println("Time taken: " + duration.toSeconds() + "s " + duration.toMillisPart() + "ms");
    }

}
