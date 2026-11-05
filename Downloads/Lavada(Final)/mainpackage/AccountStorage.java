package mainpackage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;

final class AccountStorage {
    private static final Path LEGACY_DATA_FILE = Paths.get(System.getProperty("user.home"),
        ".lavada", "accounts.properties");
    private static final Properties DATA = new Properties();

    static {
        try {
            Files.deleteIfExists(LEGACY_DATA_FILE);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not remove saved account data.", exception);
        }
    }

    private AccountStorage() {
    }

    static synchronized Properties load() {
        Properties properties = new Properties();
        properties.putAll(DATA);
        return properties;
    }

    static synchronized void update(Map<String, String> updates) {
        for (Map.Entry<String, String> entry : updates.entrySet()) {
            DATA.setProperty(entry.getKey(), entry.getValue());
        }
    }
}
