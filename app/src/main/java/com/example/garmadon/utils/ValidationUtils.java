package com.example.garmadon.utils;

import android.util.Patterns;
import java.util.regex.Pattern;

public class ValidationUtils {

    // Patrón para contraseña segura: mínimo 8 caracteres, al menos una mayúscula, una minúscula y un número
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$");

    // Patrón para nombre (solo letras y espacios)
    private static final Pattern NAME_PATTERN =
            Pattern.compile("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]{2,50}$");

    // Patrón para teléfono (8-15 dígitos)
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[0-9]{8,15}$");

    /**
     * Valida el formato del email
     */
    public static boolean isValidEmail(String email) {
        return email != null && !email.trim().isEmpty() &&
                Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Valida la fortaleza de la contraseña
     * @return true si la contraseña es segura
     */
    public static boolean isValidPassword(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * Obtiene el mensaje de error de contraseña
     */
    public static String getPasswordErrorMessage(String password) {
        if (password == null || password.isEmpty()) {
            return "La contraseña es obligatoria";
        }
        if (password.length() < 8) {
            return "Mínimo 8 caracteres";
        }
        if (!password.matches(".*[A-Z].*")) {
            return "Debe contener al menos una mayúscula";
        }
        if (!password.matches(".*[a-z].*")) {
            return "Debe contener al menos una minúscula";
        }
        if (!password.matches(".*\\d.*")) {
            return "Debe contener al menos un número";
        }
        return null;
    }

    /**
     * Valida el nombre completo
     */
    public static boolean isValidName(String name) {
        return name != null && NAME_PATTERN.matcher(name.trim()).matches();
    }

    /**
     * Valida el número de teléfono
     */
    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    /**
     * Valida el precio (debe ser positivo y con máximo 2 decimales)
     */
    public static boolean isValidPrice(String priceStr) {
        try {
            double price = Double.parseDouble(priceStr.replace(",", "."));
            return price > 0 && price <= 999999999.99;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Valida la longitud de un texto
     */
    public static boolean isValidLength(String text, int minLength, int maxLength) {
        if (text == null) return false;
        int length = text.trim().length();
        return length >= minLength && length <= maxLength;
    }

    /**
     * Sanitiza el input del usuario para evitar inyecciones
     */
    public static String sanitizeInput(String input) {
        if (input == null) return "";
        return input.trim()
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;")
                .replaceAll("\"", "&quot;")
                .replaceAll("'", "&#x27;")
                .replaceAll("/", "&#x2F;");
    }
}