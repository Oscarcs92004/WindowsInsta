package SimuladorWindow.servicios;

public class ValidadorPassword {
    public static boolean esSegura(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        boolean mayuscula = false;
        boolean minuscula = false;
        boolean simbolo = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                mayuscula = true;
            } else if (Character.isLowerCase(c)) {
                minuscula = true;
            } else if (!Character.isLetterOrDigit(c)) {
                simbolo = true;
            }
        }

        return mayuscula && minuscula && simbolo;
    }

    public static String mensajeError(String password) {
        if (password == null || password.length() < 8) {
            return "La contraseña debe tener mínimo 8 caracteres.";
        }

        boolean mayuscula = false;
        boolean minuscula = false;
        boolean simbolo = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                mayuscula = true;
            } else if (Character.isLowerCase(c)) {
                minuscula = true;
            } else if (!Character.isLetterOrDigit(c)) {
                simbolo = true;
            }
        }

        if (!mayuscula) {
            return "La contraseña debe tener al menos una mayúscula.";
        }

        if (!minuscula) {
            return "La contraseña debe tener al menos una minúscula.";
        }

        if (!simbolo) {
            return "La contraseña debe tener al menos un símbolo especial.";
        }

        return "";
    }
}
