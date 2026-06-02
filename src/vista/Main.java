package vista;

import controlador.AuthControlador;
import persistencia.GestorArchivo;

import javax.swing.*;

/**
 * Punto de entrada de la aplicación Animarket.
 */
public class Main {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        GestorArchivo gestor = new GestorArchivo("citas.txt", "usuarios.txt");
        AuthControlador auth = new AuthControlador(gestor);

        SwingUtilities.invokeLater(() -> new LoginUI(auth).setVisible(true));
    }
}
