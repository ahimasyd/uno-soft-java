package org.example.utils;

import lombok.experimental.UtilityClass;
import org.example.enitites.Group;
import org.example.enitites.Index;
import org.example.enitites.Row;
import org.example.exeptions.BadLineException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@UtilityClass
public class Sorter {

    /**
     * Прочитать строки из файла, разбить на группы и вывести результат
     * @param filepath Путь файла для чтения
     */
    public static void sort(Path filepath) {
        if (!Files.exists(filepath)) {
            System.out.println("File doesn't exist: " + filepath);
            return;
        }

        // Результирующие группы
        Set<Group> groups = new HashSet<>();

        // Хэш-таблица для каждой колонки файла. Значение в колонке -> Группа
        List<Index> indexes = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(filepath)))) {
            while (reader.ready()) {
                // Строка файла -> Row
                String line = reader.readLine();
                Row row;
                try {
                    row = Row.fromString(line);
                } catch (BadLineException e) {
                    System.out.println("Bad line: " + line);
                    continue;
                }

                // Уже существующие группы, соответствующие новой строке
                List<Group> matchedGroups = List.copyOf(getMatchingGroups(indexes, row));

                // Если такие группы есть, то добавление туда
                if (!matchedGroups.isEmpty()) {
                    Group mainGroup = matchedGroups.getFirst();
                    mainGroup.add(row);

                    List<Row> rowsToUpdate = new ArrayList<>();
                    rowsToUpdate.add(row);

                    // Если Строка попадает сразу в несколько групп, сливание этих групп в одну (в mainGroup)
                    if (matchedGroups.size() > 1) {
                        for (int i = 1; i < matchedGroups.size(); i++) {
                            Group secondaryGroup = matchedGroups.get(i);
                            mainGroup.addAll(secondaryGroup);
                            rowsToUpdate.addAll(secondaryGroup.getRows());
                            groups.remove(secondaryGroup);
                        }
                    }

                    updateIndexes(indexes, mainGroup, rowsToUpdate);
                }
                // Иначе создание новой группы из одного элемента
                else {
                    Group newGroup = new Group(row);
                    groups.add(newGroup);
                    updateIndexes(indexes, newGroup, List.of(row));
                }
            }
        } catch (IOException e) {
            System.err.println("IO Exception during file reading");
            throw new RuntimeException(e);
        }

        Path resultPath = filepath.resolveSibling("sorter-result.txt");
        printResult(groups, resultPath);
    }


    /**
     * Получить множество групп, к которым можно добавить строка
     * @param indexes Список хэш-таблиц для колонок файла
     * @param row Строка
     * @return Множество подходящих групп
     */
    private static Set<Group> getMatchingGroups(List<Index> indexes, Row row) {
        ensureIndexesSize(indexes, row);

        Set<Group> matchedGroups = new HashSet<>();
        for (int i = 0; i < row.values().size(); i++) {
            String value = row.values().get(i);
            if (value == null) {
                continue;
            }
            Optional.ofNullable(indexes.get(i).get(value))
                    .ifPresent(matchedGroups::add);
        }
        return matchedGroups;
    }

    /**
     * Обновление хэш-таблиц
     * @param indexes Список хэш-таблиц для колонок файла
     * @param group Группа для указания нового значения в таблицах
     * @param rows Множество строк, значений которых будут использованы как ключи
     */
    private static void updateIndexes(List<Index> indexes, Group group, Collection<Row> rows) {
        rows.forEach(row -> {
            ensureIndexesSize(indexes, row);
            for (int i = 0; i < row.values().size(); i++) {
                String value = row.values().get(i);
                if (value == null) {
                    continue;
                }
                indexes.get(i).put(value, group);
            }
        });
    }

    /**
     * Убедиться, что количество индексов не меньше, чем количество колонок в строке. Добавляет новые при необходимости
     * @param indexes Список хэш-таблиц для колонок файла
     * @param row Строка
     */
    private static void ensureIndexesSize(List<Index> indexes, Row row) {
        while (indexes.size() < row.values().size()) {
            indexes.add(new Index());
        }
    }

    /**
     * Распечатать результат в консоль и файл
     * @param groups Результирующие группы
     * @param resultPath Путь файла для записи результата
     */
    private static void printResult(Set<Group> groups, Path resultPath) {
        long nonSingleElementGroupCount = groups.stream().filter(group -> group.getRows().size() > 1).count();
        List<Group> groupsSorted = groups.stream()
                .sorted(Comparator.<Group, Integer>comparing(x -> x.getRows().size()).reversed())
                .toList();

        try {
            Files.deleteIfExists(resultPath);
            Files.createFile(resultPath);
        } catch (IOException e) {
            System.err.println("Failed to create result file: " + resultPath);
            throw new RuntimeException(e);
        }
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(resultPath.toFile())))) {
            writer.write("Групп с более одним элементом: " + nonSingleElementGroupCount + "\n\n");
            for (int i = 0; i < groupsSorted.size(); i++) {
                writer.write("Группа " + (i + 1) + "\n");
                Group group = groupsSorted.get(i);
                for (Row row : group.getRows()) {
                    writer.write(row.toString() + "\n");
                }
                writer.write("\n");
            }
        } catch (IOException e) {
            System.err.println("Failed to write groups into file: " + resultPath);
            throw new RuntimeException(e);
        }

        System.out.println("Groups with more than one element: " + nonSingleElementGroupCount);
        System.out.println("Groups are described in file " + resultPath);
    }

}
