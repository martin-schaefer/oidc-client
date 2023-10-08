package ch.martin.oidcclient;

import lombok.Getter;

import java.util.Map;

public class KeyValue {

    @Getter
    private Object key;
    @Getter
    private Object value;
    public static KeyValue of(Map.Entry entry) {
        KeyValue keyValue = new KeyValue();
        keyValue.key = entry.getKey();
        keyValue.value = entry.getValue();
        return keyValue;
    }

}
