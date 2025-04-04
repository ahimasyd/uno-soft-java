package org.example.enitites;

import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

@Getter
public class Group {

    private final Set<Row> rows = new HashSet<>();


    public Group(Row row) {
        rows.add(row);
    }

    public void add(Row row) {
        rows.add(row);
    }

    public void addAll(Group group) {
        rows.addAll(group.rows);
    }

}
