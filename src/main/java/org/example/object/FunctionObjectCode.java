package org.example.object;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FunctionObjectCode implements Object{
    public int codeStart;
    public int parameterCount;

    @Override
    public ObjectType type() {
        return null;
    }

    @Override
    public String inspect() {
        return null;
    }
}
