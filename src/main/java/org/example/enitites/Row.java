package org.example.enitites;

import org.apache.commons.lang3.StringUtils;
import org.example.exeptions.BadLineException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public record Row(List<String> values) {

    public static Row fromString(String line) throws BadLineException {
        if (line == null || line.isBlank()) {
            throw new BadLineException();
        }

        List<String> values = Arrays.stream(line.split(";", -1))
                .map(x -> StringUtils.strip(x, "\""))
                .peek(x -> {
                    if (x.contains("\"")) {
                        throw new BadLineException();
                    }
                })
                .map(x -> x.isEmpty() ? null : x)
                .toList();
        return new Row(values);
    }


    public Row(List<String> values) {
        this.values = Collections.unmodifiableList(values);
    }

    @Override
    public String toString() {
        return values.stream()
                .map(x -> x != null ? x : "")
                .map(x -> "\"" + x + "\"")
                .collect(Collectors.joining(";"));
    }

}
