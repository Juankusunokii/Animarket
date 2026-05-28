package vista;

import controlador.AuthControlador;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * LoginUI
 * Pantalla de inicio de sesión. Bloquea la aplicación tras 3 intentos fallidos.
 */
public class LoginUI extends JFrame {

    // ─── Paleta de colores ────────────────────────────────────────────────────
    static final Color COLOR_VERDE     = new Color(44, 122, 75);
    static final Color COLOR_VERDE_OSC = new Color(30,  85, 52);
    static final Color COLOR_FONDO     = new Color(245, 250, 246);
    static final Color COLOR_TEXTO     = new Color(30,  30,  30);
    static final Color COLOR_ERROR     = new Color(180, 30,  30);
    static final Color COLOR_GRIS      = new Color(110, 110, 110);

    private final AuthControlador authControlador;

    private JTextField  campoUsuario;
    private JPasswordField campoPassword;
    private JLabel      lblError;
    private JButton     btnEntrar;

    public LoginUI(AuthControlador authControlador) {
        this.authControlador = authControlador;
        construirUI();
    }

    private void construirUI() {
        setTitle("Animarket – Inicio de sesión");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(420, 480);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(COLOR_FONDO);
        panel.setBorder(new EmptyBorder(50, 60, 50, 60));

        // Logo / título
        JLabel lblLogo = new JLabel("🐾 Animarket", SwingConstants.CENTER);
        lblLogo.setFont(new Font("SansSerif", Font.BOLD, 28));
        lblLogo.setForeground(COLOR_VERDE);
        lblLogo.setAlignmentX(CENTER_ALIGNMENT);

        JLabel lblSub = new JLabel("Sistema de gestión de citas", SwingConstants.CENTER);
        lblSub.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lblSub.setForeground(COLOR_GRIS);
        lblSub.setAlignmentX(CENTER_ALIGNMENT);

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(new Color(200, 220, 205));

        // Campos
        JLabel lblUsr = crearLabel("Usuario");
        campoUsuario  = crearCampoTexto();

        JLabel lblPwd = crearLabel("Contraseña");
        campoPassword = new JPasswordField();
        estilizarCampo(campoPassword);

        lblError = new JLabel(" ");
        lblError.setFont(new Font("SansSerif", Font.ITALIC, 12));
        lblError.setForeground(COLOR_ERROR);
        lblError.setAlignmentX(CENTER_ALIGNMENT);

        btnEntrar = new JButton("Ingresar");
        estilizarBoton(btnEntrar, COLOR_VERDE, Color.WHITE);

        // Acción: Enter en contraseña o click en botón
        ActionListener accionLogin = e -> intentarLogin();
        btnEntrar.addActionListener(accionLogin);
        campoPassword.addActionListener(accionLogin);
        campoUsuario.addActionListener(e -> campoPassword.requestFocus());

        // Ensamblar
        panel.add(lblLogo);
        panel.add(Box.createVerticalStrut(6));
        panel.add(lblSub);
        panel.add(Box.createVerticalStrut(20));
        panel.add(sep);
        panel.add(Box.createVerticalStrut(24));
        panel.add(lblUsr);
        panel.add(Box.createVerticalStrut(4));
        panel.add(campoUsuario);
        panel.add(Box.createVerticalStrut(14));
        panel.add(lblPwd);
        panel.add(Box.createVerticalStrut(4));
        panel.add(campoPassword);
        panel.add(Box.createVerticalStrut(10));
        panel.add(lblError);
        panel.add(Box.createVerticalStrut(20));
        panel.add(btnEntrar);

        setContentPane(panel);
    }

    // ─── Lógica de login ──────────────────────────────────────────────────────
    private void intentarLogin() {
        String usuario  = campoUsuario.getText().trim();
        String password = new String(campoPassword.getPassword());

        int resultado = authControlador.autenticar(usuario, password);

        switch (resultado) {
            case 0 -> {
                // Éxito: abrir ventana principal
                setVisible(false);
                new MainUI(authControlador).setVisible(true);
                dispose();
            }
            case 1 -> {
                lblError.setText("Credenciales incorrectas. Intentos restantes: "
                                 + authControlador.getIntentosRestantes());
                campoPassword.setText("");
                campoPassword.requestFocus();
            }
            case 2 -> {
                lblError.setText("Sistema bloqueado. Contacte al administrador.");
                btnEntrar.setEnabled(false);
                campoUsuario.setEnabled(false);
                campoPassword.setEnabled(false);
            }
        }
    }

    // ─── Helpers de estilo ────────────────────────────────────────────────────
    static JLabel crearLabel(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        lbl.setForeground(COLOR_TEXTO);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        return lbl;
    }

    static JTextField crearCampoTexto() {
        JTextField tf = new JTextField();
        estilizarCampo(tf);
        return tf;
    }

    static void estilizarCampo(JTextField tf) {
        tf.setFont(new Font("SansSerif", Font.PLAIN, 14));
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 210, 190), 1, true),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        tf.setBackground(Color.WHITE);
        tf.setAlignmentX(LEFT_ALIGNMENT);
    }

    static void estilizarBoton(JButton btn, Color fondo, Color texto) {
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setBackground(fondo);
        btn.setForeground(texto);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setAlignmentX(CENTER_ALIGNMENT);
        // Hover
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                btn.setBackground(fondo.darker());
            }
            @Override public void mouseExited(MouseEvent e) {
                btn.setBackground(fondo);
            }
        });
    }
}
