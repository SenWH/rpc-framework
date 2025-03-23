package org.example.CustomUtil;

import org.example.CustomUtil.impl.JsonSerializer;
import org.example.CustomUtil.impl.KryoSerializer;

import java.io.IOException;

public interface CommonSerializer {

    byte[] serialize(Object obj);

    Object deserialize(byte[] bytes, Class<?> clazz) throws IOException;

    int getCode();

    static CommonSerializer getByCode(int code) {
        switch (code) {
            case 0:
                return new KryoSerializer();
            case 1:
                return new JsonSerializer();
            default:
                return null;
        }
    }
}
