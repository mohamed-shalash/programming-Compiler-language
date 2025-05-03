package org.example.object;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class HashObjectCode implements Object{
    public final Map<Object, Object> pairs;

    public HashObjectCode() {
        this.pairs = new HashMap<>();
    }

    public void put(Object key, Object value) {
        pairs.put(key, value);
    }

    public Object get(Object key) {
        return pairs.getOrDefault(key, null);
    }

    @Override
    public String toString() {
        return pairs.toString();
    }

    @Override
    public ObjectType type() {
        return null;
    }

    @Override
    public String inspect() {
        return null;
    }
}
