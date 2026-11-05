package mainpackage;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

final class AdminAccounts {
    private static final int SALT_LENGTH = 16;
    private static final int MIN_SECRET_LENGTH = 8;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static Account account;
    private static int failedAttempts;
    private static boolean locked;
    private static int failedRecoveryAttempts;
    private static int failedAnswerAttempts;
    private static boolean supportRequired;

    static {
        Properties saved = AccountStorage.load();
        if (saved.getProperty("admin.username") == null) {
            byte[] salt = randomSalt();
            char[] password = "admin123".toCharArray();
            try {
                account = new Account("admin", salt,
                    CredentialHash.hash(password, salt), null, null);
                persistAccount(account);
            } finally {
                Arrays.fill(password, '\0');
            }
        } else {
            account = readAccount(saved);
        }
        failedAttempts = Integer.parseInt(saved.getProperty("admin.failedAttempts", "0"));
        locked = Boolean.parseBoolean(saved.getProperty("admin.locked", "false"));
        failedRecoveryAttempts = Integer.parseInt(saved.getProperty("admin.recoveryAttempts", "0"));
        failedAnswerAttempts = Integer.parseInt(saved.getProperty("admin.answerAttempts", "0"));
        supportRequired = Boolean.parseBoolean(saved.getProperty("admin.supportRequired", "false"));
    }

    private AdminAccounts() {
    }

    static synchronized boolean isUsername(String username) {
        return account.username.equalsIgnoreCase(username.trim());
    }

    static synchronized String getUsername() {
        return account.username;
    }

    static synchronized boolean authenticate(String username, char[] password) {
        if (!isUsername(username) || locked) {
            return false;
        }
        if (CredentialHash.matches(password, account.passwordSalt, account.passwordHash)) {
            failedAttempts = 0;
            persistState();
            return true;
        }
        failedAttempts++;
        if (failedAttempts >= 3) {
            locked = true;
        }
        persistState();
        return false;
    }

    static synchronized void recordFailedUsernameAttempt() {
        if (locked) {
            return;
        }
        failedAttempts++;
        if (failedAttempts >= 3) {
            locked = true;
        }
        persistState();
    }

    static synchronized boolean isLocked() {
        return locked;
    }

    static synchronized int getLoginAttemptsRemaining() {
        return Math.max(0, 3 - failedAttempts);
    }

    static synchronized boolean isRecoveryConfigured() {
        return account.recoverySalt != null && account.questions != null;
    }

    static synchronized String[] getQuestions() {
        return account.questions == null ? new String[] {"", "", ""} : account.questions.clone();
    }

    static synchronized boolean checkRecoveryCode(char[] code) {
        if (!locked || supportRequired || account.recoverySalt == null
                || account.recoveryHash == null || failedRecoveryAttempts >= 3) {
            return false;
        }
        if (CredentialHash.matches(code, account.recoverySalt, account.recoveryHash)) {
            resetRecoveryState();
            return true;
        }
        failedRecoveryAttempts++;
        persistState();
        return false;
    }

    static synchronized int getRecoveryAttemptsRemaining() {
        return Math.max(0, 3 - failedRecoveryAttempts);
    }

    static synchronized boolean isSupportRequired() {
        return supportRequired;
    }

    static synchronized boolean recoverWithAnswers(char[][] answers) {
        if (!locked || supportRequired || account.questions == null
                || account.answerSalts == null || account.answerHashes == null
                || answers == null || answers.length != 3 || failedAnswerAttempts >= 3) {
            return false;
        }
        boolean matches = true;
        for (int index = 0; index < 3; index++) {
            if (answers[index] == null || answers[index].length == 0
                    || !answerMatches(answers[index], account.answerSalts[index],
                        account.answerHashes[index])) {
                matches = false;
            }
        }
        if (matches) {
            resetRecoveryState();
            return true;
        }
        failedAnswerAttempts++;
        if (failedAnswerAttempts >= 3) {
            supportRequired = true;
        }
        persistState();
        return false;
    }

    static synchronized int getAnswerAttemptsRemaining() {
        return Math.max(0, 3 - failedAnswerAttempts);
    }

    static synchronized void update(String username, char[] currentPassword, char[] newPassword,
            char[] recoveryCode, String[] questions, char[][] answers) {
        if (username == null || currentPassword == null || newPassword == null || recoveryCode == null
                || questions == null || questions.length != 3 || answers == null || answers.length != 3) {
            throw new IllegalArgumentException("Complete the admin account form.");
        }
        String normalizedUsername = username.trim();
        if (normalizedUsername.isEmpty()) {
            throw new IllegalArgumentException("Admin username is required.");
        }
        if (StaffAccounts.isUsernameInUse(normalizedUsername)) {
            throw new IllegalArgumentException("That username is already assigned to a staff account.");
        }
        if (!CredentialHash.matches(currentPassword, account.passwordSalt, account.passwordHash)) {
            throw new IllegalArgumentException("Current admin password is incorrect.");
        }
        if (newPassword.length > 0 && newPassword.length < MIN_SECRET_LENGTH) {
            throw new IllegalArgumentException("Password must be at least 8 characters.");
        }
        if (recoveryCode.length > 0 && recoveryCode.length < MIN_SECRET_LENGTH) {
            throw new IllegalArgumentException("Recovery code must be at least 8 characters.");
        }

        boolean updatingQuestions = false;
        boolean answersProvided = false;
        for (int index = 0; index < 3; index++) {
            String proposedQuestion = questions[index] == null ? "" : questions[index].trim();
            String existingQuestion = account.questions == null ? "" : account.questions[index];
            updatingQuestions |= !proposedQuestion.equals(existingQuestion);
            answersProvided |= answers[index] != null && answers[index].length > 0;
        }
        updatingQuestions |= answersProvided;
        if (recoveryCode.length == 0 && account.recoverySalt == null) {
            throw new IllegalArgumentException("Set a recovery code for the admin account.");
        }
        if (!isRecoveryConfigured() || updatingQuestions) {
            validateQuestions(questions, answers);
        }

        byte[] passwordSalt = account.passwordSalt;
        byte[] passwordHash = account.passwordHash;
        if (newPassword.length > 0) {
            passwordSalt = randomSalt();
            passwordHash = CredentialHash.hash(newPassword, passwordSalt);
        }
        byte[] recoverySalt = account.recoverySalt;
        byte[] recoveryHash = account.recoveryHash;
        if (recoveryCode.length > 0) {
            recoverySalt = randomSalt();
            recoveryHash = CredentialHash.hash(recoveryCode, recoverySalt);
        }

        String[] savedQuestions = account.questions;
        byte[][] answerSalts = account.answerSalts;
        byte[][] answerHashes = account.answerHashes;
        if (!isRecoveryConfigured() || updatingQuestions) {
            savedQuestions = new String[3];
            answerSalts = new byte[3][];
            answerHashes = new byte[3][];
            for (int index = 0; index < 3; index++) {
                savedQuestions[index] = questions[index].trim();
                answerSalts[index] = randomSalt();
                char[] normalizedAnswer = normalizeAnswer(answers[index]);
                try {
                    answerHashes[index] = CredentialHash.hash(normalizedAnswer, answerSalts[index]);
                } finally {
                    Arrays.fill(normalizedAnswer, '\0');
                }
            }
        }

        Account updated = new Account(normalizedUsername, passwordSalt, passwordHash,
            recoverySalt, recoveryHash, savedQuestions, answerSalts, answerHashes);
        persistAccount(updated);
        account = updated;
    }

    private static void validateQuestions(String[] questions, char[][] answers) {
        if (questions == null || questions.length != 3 || answers == null || answers.length != 3) {
            throw new IllegalArgumentException("Set all three recovery questions and answers.");
        }
        for (int index = 0; index < 3; index++) {
            if (questions[index] == null || questions[index].trim().isEmpty()
                    || answers[index] == null || answers[index].length == 0) {
                throw new IllegalArgumentException("Set all three recovery questions and answers.");
            }
            for (int earlier = 0; earlier < index; earlier++) {
                if (questions[index].trim().equalsIgnoreCase(questions[earlier].trim())) {
                    throw new IllegalArgumentException("Recovery questions must be different.");
                }
            }
        }
    }

    private static char[] normalizeAnswer(char[] answer) {
        int start = 0;
        int end = answer.length;
        while (start < end && Character.isWhitespace(answer[start])) {
            start++;
        }
        while (end > start && Character.isWhitespace(answer[end - 1])) {
            end--;
        }
        char[] normalized = Arrays.copyOfRange(answer, start, end);
        for (int index = 0; index < normalized.length; index++) {
            normalized[index] = Character.toLowerCase(normalized[index]);
        }
        return normalized;
    }

    private static boolean answerMatches(char[] answer, byte[] salt, byte[] expectedHash) {
        char[] normalized = normalizeAnswer(answer);
        try {
            return normalized.length > 0 && CredentialHash.matches(normalized, salt, expectedHash);
        } finally {
            Arrays.fill(normalized, '\0');
        }
    }

    private static void resetRecoveryState() {
        locked = false;
        failedAttempts = 0;
        failedRecoveryAttempts = 0;
        failedAnswerAttempts = 0;
        supportRequired = false;
        persistState();
    }

    private static byte[] randomSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        RANDOM.nextBytes(salt);
        return salt;
    }

    private static Account readAccount(Properties saved) {
        String[] questions = null;
        byte[][] answerSalts = null;
        byte[][] answerHashes = null;
        if (saved.getProperty("admin.question.0") != null) {
            questions = new String[3];
            answerSalts = new byte[3][];
            answerHashes = new byte[3][];
            for (int index = 0; index < 3; index++) {
                questions[index] = saved.getProperty("admin.question." + index);
                answerSalts[index] = decode(saved.getProperty("admin.answerSalt." + index));
                answerHashes[index] = decode(saved.getProperty("admin.answerHash." + index));
            }
        }
        return new Account(saved.getProperty("admin.username"),
            decode(saved.getProperty("admin.passwordSalt")),
            decode(saved.getProperty("admin.passwordHash")),
            decodeOptional(saved.getProperty("admin.recoverySalt")),
            decodeOptional(saved.getProperty("admin.recoveryHash")),
            questions, answerSalts, answerHashes);
    }

    private static void persistAccount(Account value) {
        Map<String, String> updates = new LinkedHashMap<String, String>();
        updates.put("admin.username", value.username);
        updates.put("admin.passwordSalt", encode(value.passwordSalt));
        updates.put("admin.passwordHash", encode(value.passwordHash));
        if (value.recoverySalt != null) {
            updates.put("admin.recoverySalt", encode(value.recoverySalt));
            updates.put("admin.recoveryHash", encode(value.recoveryHash));
        }
        if (value.questions != null) {
            for (int index = 0; index < 3; index++) {
                updates.put("admin.question." + index, value.questions[index]);
                updates.put("admin.answerSalt." + index, encode(value.answerSalts[index]));
                updates.put("admin.answerHash." + index, encode(value.answerHashes[index]));
            }
        }
        AccountStorage.update(updates);
        persistState();
    }

    private static void persistState() {
        Map<String, String> updates = new LinkedHashMap<String, String>();
        updates.put("admin.failedAttempts", Integer.toString(failedAttempts));
        updates.put("admin.locked", Boolean.toString(locked));
        updates.put("admin.recoveryAttempts", Integer.toString(failedRecoveryAttempts));
        updates.put("admin.answerAttempts", Integer.toString(failedAnswerAttempts));
        updates.put("admin.supportRequired", Boolean.toString(supportRequired));
        AccountStorage.update(updates);
    }

    private static byte[] decode(String value) {
        return Base64.getDecoder().decode(value);
    }

    private static byte[] decodeOptional(String value) {
        return value == null ? null : decode(value);
    }

    private static String encode(byte[] value) {
        return Base64.getEncoder().encodeToString(value);
    }

    private static final class Account {
        private final String username;
        private final byte[] passwordSalt;
        private final byte[] passwordHash;
        private final byte[] recoverySalt;
        private final byte[] recoveryHash;
        private final String[] questions;
        private final byte[][] answerSalts;
        private final byte[][] answerHashes;

        private Account(String username, byte[] passwordSalt, byte[] passwordHash,
                byte[] recoverySalt, byte[] recoveryHash) {
            this(username, passwordSalt, passwordHash, recoverySalt, recoveryHash,
                null, null, null);
        }

        private Account(String username, byte[] passwordSalt, byte[] passwordHash,
                byte[] recoverySalt, byte[] recoveryHash, String[] questions,
                byte[][] answerSalts, byte[][] answerHashes) {
            this.username = username;
            this.passwordSalt = passwordSalt;
            this.passwordHash = passwordHash;
            this.recoverySalt = recoverySalt;
            this.recoveryHash = recoveryHash;
            this.questions = questions;
            this.answerSalts = answerSalts;
            this.answerHashes = answerHashes;
        }
    }
}
