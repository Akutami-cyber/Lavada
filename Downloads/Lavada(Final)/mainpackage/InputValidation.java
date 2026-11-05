package mainpackage;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.awt.Color;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public final class InputValidation {
    private static final Pattern LETTERS = Pattern.compile("[\\p{L}\\p{M} .'&-]*");
    private static final Pattern DIGITS = Pattern.compile("[0-9]*");
    private static final Pattern DECIMAL = Pattern.compile("[0-9]*(?:\\.[0-9]*)?");
    private static final Pattern COMPLETE_DIGITS = Pattern.compile("[0-9]+");
    private static final Pattern COMPLETE_DECIMAL = Pattern.compile("[0-9]+(?:\\.[0-9]+)?");
    private static final Pattern DATE = Pattern.compile("[0-9-]*");

    /** Prevents instances of this static validation utility class. */
    private InputValidation() {
    }

    /** Restricts a text field to letters and supported name punctuation. */
    public static void lettersOnly(JTextField field) {
        install(field, LETTERS, "Letters only");
    }

    /** Restricts a text field to numeric digits. */
    public static void digitsOnly(JTextField field) {
        install(field, DIGITS, "Numbers only");
    }

    /** Restricts a text field to digits and an optional decimal point. */
    public static void decimalOnly(JTextField field) {
        install(field, DECIMAL, "Enter numbers only; one decimal point is allowed");
    }

    /** Restricts date input to digits and hyphens while the user types. */
    public static void dateCharactersOnly(JTextField field) {
        install(field, DATE, "Enter the date as YYYY-MM-DD usi numbers and hyphens");
    }

    /** Checks whether a stri matches the application's email format. */
    public static boolean isValidEmail(String value) {
        return value.matches("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");
    }

    /** Checks for supported name characters and at least one letter. */
    public static boolean isLettersOnly(String value) {
        return LETTERS.matcher(value).matches() && value.matches(".*[\\p{L}].*");
    }

    /** Checks that a value contains one or more digits and nothi else. */
    public static boolean isDigitsOnly(String value) {
        return COMPLETE_DIGITS.matcher(value).matches();
    }

    /** Checks that a value is a complete integer or decimal number. */
    public static boolean isDecimal(String value) {
        return COMPLETE_DECIMAL.matcher(value).matches();
    }

    /** Parses a date value to confirm it is a real ISO calendar date. */
    public static boolean isValidDate(String value) {
        try {
            LocalDate.parse(value);
            return true;
        } catch (DateTimeParseException exception) {
            return false;
        }
    }

    /** Installs a document filter that rejects input outside the allowed pattern. */
    private static void install(JTextField field, Pattern allowed, String errorMessage) {
        Color normalBackground = field.getBackground();
        ((AbstractDocument) field.getDocument()).setDocumentFilter(new DocumentFilter() {
            /** Accepts an edit only when the resulti field contents match the allowed pattern. */
            private boolean apply(FilterBypass bypass, int offset, int length, String text,
                    AttributeSet attributes) throws BadLocationException {
                String current = bypass.getDocument().getText(0, bypass.getDocument().getLength());
                String replacement = text == null ? "" : text;
                String candidate = current.substring(0, offset) + replacement
                    + current.substring(offset + length);
                if (!allowed.matcher(candidate).matches()) {
                    field.setToolTipText(errorMessage);
                    field.setBackground(new Color(255, 235, 235));
                    JOptionPane.showMessageDialog(field, errorMessage, "Invalid input",
                        JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                field.setToolTipText(null);
                field.setBackground(normalBackground);
                return true;
            }

            /** Applies the filter when text is inserted into the document. */
            @Override
            public void insertString(FilterBypass bypass, int offset, String text,
                    AttributeSet attributes) throws BadLocationException {
                if (apply(bypass, offset, 0, text, attributes)) {
                    super.insertString(bypass, offset, text, attributes);
                }
            }

            /** Applies the filter when existing document text is replaced. */
            @Override
            public void replace(FilterBypass bypass, int offset, int length, String text,
                    AttributeSet attributes) throws BadLocationException {
                if (apply(bypass, offset, length, text, attributes)) {
                    super.replace(bypass, offset, length, text, attributes);
                }
            }
        });
    }
}




