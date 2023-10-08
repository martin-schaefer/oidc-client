package ch.martin.oidcclient;

import lombok.SneakyThrows;
import org.springframework.aot.hint.ExecutableMode;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

public class NativeRuntimeHints implements RuntimeHintsRegistrar {
    @SneakyThrows
    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        // Register method for reflection
        Method keyMethod = ReflectionUtils.findMethod(KeyValue.class, "getKey");
        hints.reflection().registerMethod(keyMethod, ExecutableMode.INVOKE);
        Method valueMethod = ReflectionUtils.findMethod(KeyValue.class, "getValue");
        hints.reflection().registerMethod(valueMethod, ExecutableMode.INVOKE);
    }
}
