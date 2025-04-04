package org.example.enitites;

import java.util.HashMap;
import java.util.Map;

public class Index {

    private final Map<String, Group> map = new HashMap<>();


    public void put(String key, Group group) {
        map.put(key, group);
    }

    public Group get(String key) {
        return map.get(key);
    }

}
