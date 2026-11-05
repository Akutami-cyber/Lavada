package mainpackage;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

final class StaffAccounts {
    private static final int SALT_LENGTH = 16;
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Map<String, Account> ACCOUNTS = new LinkedHashMap<String, Account>();

    static {
        Properties saved = AccountStorage.load();
        for (String key : saved.stringPropertyNames()) {
            if (!key.startsWith("staff.") || !key.endsWith(".username")) {
                continue;
            }
            String employeeId = key.substring("staff.".length(),
                key.length() - ".username".length());
            ACCOUNTS.put(employeeId, readAccount(saved, employeeId));
        }
        if (!ACCOUNTS.containsKey("EMP-001")) {
            byte[] salt = new byte[SALT_LENGTH];
            RANDOM.nextBytes(salt);
            char[] password = "staff123".toCharArray();
            try {
                Account account = new Account("staff", salt,
                    CredentialHash.hash(password, salt), true, 0, false);
                ACCOUNTS.put("EMP-001", account);
                persist("EMP-001", account);
            } finally {
                Arrays.fill(password, '\0');
            }
        }
    }

    private StaffAccounts() {
    }

    static synchronized String getUsername(String employeeId) {
        Account account = ACCOUNTS.get(employeeId);
        return account == null ? " - " : account.username;
    }

    static synchronized String getStatus(String employeeId) {
        Account account = ACCOUNTS.get(employeeId);
        if (account == null) {
            return "Not Set";
        }
        if (account.locked) {
            return "Locked";
        }
        return account.active ? "Active" : "Inactive";
    }

    static synchronized void save(String employeeId, String username, char[] password, boolean active) {
        String normalizedUsername = username.trim();
        if (normalizedUsername.isEmpty()) {
            throw new IllegalArgumentException("Username is required.");
        }
        if ("admin".equalsIgnoreCase(normalizedUsername)
                || AdminAccounts.isUsername(normalizedUsername)) {
            throw new IllegalArgumentException("That username is reserved for the administrator.");
        }
        for (Map.Entry<String, Account> entry : ACCOUNTS.entrySet()) {
            if (!entry.getKey().equals(employeeId)
                    && entry.getValue().username.equalsIgnoreCase(normalizedUsername)) {
                throw new IllegalArgumentException("That username is already assigned to another employee.");
            }
        }

        Account existing = ACCOUNTS.get(employeeId);
        if (password.length == 0 && existing == null) {
            throw new IllegalArgumentException("Set a password for this employee account.");
        }
        if (password.length > 0 && password.length < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("Password must be at least 8 characters.");
        }

        byte[] salt;
        byte[] passwordHash;
        if (password.length == 0) {
            salt = existing.salt;
            passwordHash = existing.passwordHash;
        } else {
            salt = new byte[SALT_LENGTH];
            RANDOM.nextBytes(salt);
            passwordHash = CredentialHash.hash(password, salt);
        }
        Account updated = new Account(normalizedUsername, salt, passwordHash, active,
            active ? 0 : (existing == null ? 0 : existing.failedAttempts),
            active ? false : (existing != null && existing.locked));
        persist(employeeId, updated);
        ACCOUNTS.put(employeeId, updated);
        if (existing != null && password.length > 0) {
            Arrays.fill(existing.salt, (byte) 0);
            Arrays.fill(existing.passwordHash, (byte) 0);
        }
    }

    static synchronized String authenticate(String username, char[] password) {
        Account account = findByUsername(username);
        if (account == null || !account.active || account.locked) {
            return null;
        }
        if (CredentialHash.matches(password, account.salt, account.passwordHash)) {
            account.failedAttempts = 0;
            persist(findEmployeeId(account), account);
            return findEmployeeId(account);
        }
        account.failedAttempts++;
        if (account.failedAttempts >= 3) {
            account.locked = true;
        }
        persist(findEmployeeId(account), account);
        return null;
    }

    static synchronized boolean isInactive(String username) {
        Account account = findByUsername(username);
        return account != null && !account.active && !account.locked;
    }

    static synchronized boolean isLocked(String username) {
        Account account = findByUsername(username);
        return account != null && account.locked;
    }

    static synchronized int getLoginAttemptsRemaining(String username) {
        Account account = findByUsername(username);
        return account == null ? 0 : Math.max(0, 3 - account.failedAttempts);
    }

    static synchronized boolean isUsernameInUse(String username) {
        return findByUsername(username) != null;
    }

    private static Account findByUsername(String username) {
        String normalized = username.trim();
        for (Account account : ACCOUNTS.values()) {
            if (account.username.equalsIgnoreCase(normalized)) {
                return account;
            }
        }
        return null;
    }

    private static String findEmployeeId(Account account) {
        for (Map.Entry<String, Account> entry : ACCOUNTS.entrySet()) {
            if (entry.getValue() == account) {
                return entry.getKey();
            }
        }
        throw new IllegalStateException("Staff account is not registered.");
    }

    private static Account readAccount(Properties saved, String employeeId) {
        String prefix = "staff." + employeeId + ".";
        return new Account(saved.getProperty(prefix + "username"),
            decode(saved.getProperty(prefix + "salt")),
            decode(saved.getProperty(prefix + "passwordHash")),
            Boolean.parseBoolean(saved.getProperty(prefix + "active", "false")),
            Integer.parseInt(saved.getProperty(prefix + "failedAttempts", "0")),
            Boolean.parseBoolean(saved.getProperty(prefix + "locked", "false")));
    }

    private static void persist(String employeeId, Account account) {
        String prefix = "staff." + employeeId + ".";
        Map<String, String> updates = new LinkedHashMap<String, String>();
        updates.put(prefix + "username", account.username);
        updates.put(prefix + "salt", encode(account.salt));
        updates.put(prefix + "passwordHash", encode(account.passwordHash));
        updates.put(prefix + "active", Boolean.toString(account.active));
        updates.put(prefix + "failedAttempts", Integer.toString(account.failedAttempts));
        updates.put(prefix + "locked", Boolean.toString(account.locked));
        AccountStorage.update(updates);
    }

    private static byte[] decode(String value) {
        return Base64.getDecoder().decode(value);
    }

    private static String encode(byte[] value) {
        return Base64.getEncoder().encodeToString(value);
    }

    private static final class Account {
        private final String username;
        private final byte[] salt;
        private final byte[] passwordHash;
        private final boolean active;
        private int failedAttempts;
        private boolean locked;

        private Account(String username, byte[] salt, byte[] passwordHash, boolean active,
                int failedAttempts, boolean locked) {
            this.username = username;
            this.salt = salt;
            this.passwordHash = passwordHash;
            this.active = active;
            this.failedAttempts = failedAttempts;
            this.locked = locked;
        }
    }
}
