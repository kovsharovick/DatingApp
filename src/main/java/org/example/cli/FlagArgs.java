package org.example.cli;

import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FlagArgs {

    private final Map<String, List<String>> values = new HashMap<>();
    @Getter
    private String error;

    public static FlagArgs parse(String[] args, int startIndex) {
        FlagArgs parsed = new FlagArgs();
        for (int i = startIndex; i < args.length; i++) {
            String token = args[i];
            if (!token.startsWith("--")) {
                parsed.error = "Ожидался флаг, получено: " + token;
                return parsed;
            }
            if (i + 1 >= args.length || args[i + 1].startsWith("--")) {
                parsed.error = "Не указано значение параметра " + token;
                return parsed;
            }
            parsed.values.computeIfAbsent(token, k -> new ArrayList<>()).add(args[++i]);
        }
        return parsed;
    }

    public String require(String flag) {
        List<String> vals = values.get(flag);
        if (vals == null || vals.isEmpty()) {
            error = "Не указан обязательный параметр " + flag;
            return null;
        }
        return vals.get(vals.size() - 1);
    }

    public String optional(String flag) {
        List<String> vals = values.get(flag);
        if (vals == null || vals.isEmpty()) {
            return null;
        }
        return vals.get(vals.size() - 1);
    }

    public List<String> all(String flag) {
        return values.getOrDefault(flag, List.of());
    }
}

