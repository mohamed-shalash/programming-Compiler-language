package org.example.object;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IntegerObject implements Object, Hashable {
    private  long value;

    public IntegerObject(long value) {
        this.value = value;
    }

    @Override
    public ObjectType type() {
        return ObjectType.INTEGER;
    }

    @Override
    public String inspect() {
        return String.valueOf(value);
    }

    public long getValue() {
        return value;
    }

    @Override
    public HashKey hashKey() {
        return new HashKey(type(), value);
    }

    @Override
    public String toString() {
        return Long.toString(value);
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IntegerObject that = (IntegerObject) o;
        return value == that.value;
    }
    @Override
    public int hashCode() {
        return Integer.hashCode((int) value);
    }
}
