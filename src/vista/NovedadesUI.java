package vista;
import controlador.CitaControlador;
import modelo.Cita;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;

// ─────────────────────────────────────────────────────────────────────────────
// NovedadesUI: registro de observaciones post-servicio
// ─────────────────────────────────────────────────────────────────────────────
public class NovedadesUI extends JDialog {

    private final MainUI          mainUI;
    private final CitaControlador  citaControlador;
    private final Cita            cita;
    private final LocalDate       fecha;

    private JTextArea  taNovedades;
    private JCheckBox  chkConfirmado;

    public NovedadesUI(MainUI mainUI, CitaControlador citaControlador, Cita cita, LocalDate fecha) {
        super(mainUI, "Novedades post-servicio – " + cita.getNombreMascota(), true);
        this.mainUI         = mainUI;
        this.citaControlador = citaControlador;
        this.cita           = cita;
        this.fecha          = fecha;
        construirUI();
    }

    private void construirUI() {
        setSize(460, 380);
        setLocationRelativeTo(getOwner());
        setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(LoginUI.COLOR_FONDO);
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel titulo = new JLabel("Registro de novedades – cita #" + cita.getId());
        titulo.setFont(new Font("SansSerif", Font.BOLD, 14));
        titulo.setForeground(LoginUI.COLOR_VERDE_OSC);
        titulo.setAlignmentX(LEFT_ALIGNMENT);

        JLabel subInfo = new JLabel("Mascota: " + cita.getNombreMascota() + " · " + cita.getServicioSolicitado());
        subInfo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subInfo.setForeground(LoginUI.COLOR_GRIS);
        subInfo.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lblObs = LoginUI.crearLabel("Observaciones del peluquero (o 'Sin novedad'):");
        lblObs.setAlignmentX(LEFT_ALIGNMENT);

        taNovedades = new JTextArea(6, 30);
        taNovedades.setFont(new Font("SansSerif", Font.PLAIN, 13));
        taNovedades.setLineWrap(true);
        taNovedades.setWrapStyleWord(true);
        if (cita.getObservacionesPosteriores() != null)
            taNovedades.setText(cita.getObservacionesPosteriores());

        JScrollPane scroll = new JScrollPane(taNovedades);
        scroll.setAlignmentX(LEFT_ALIGNMENT);
        scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

        chkConfirmado = new JCheckBox("Confirmar que el servicio fue realizado");
        chkConfirmado.setFont(new Font("SansSerif", Font.BOLD, 13));
        chkConfirmado.setOpaque(false);
        chkConfirmado.setSelected(cita.isServicioConfirmado());
        chkConfirmado.setAlignmentX(LEFT_ALIGNMENT);

        // Botones
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.setOpaque(false);
        botones.setAlignmentX(LEFT_ALIGNMENT);
        JButton btnCancelar = new JButton("Cancelar");
        JButton btnGuardar  = new JButton("Guardar novedades");
        LoginUI.estilizarBoton(btnGuardar, LoginUI.COLOR_VERDE, Color.WHITE);
        btnCancelar.addActionListener(e -> dispose());
        btnGuardar.addActionListener(e -> guardar());
        botones.add(btnCancelar);
        botones.add(btnGuardar);

        panel.add(titulo);
        panel.add(Box.createVerticalStrut(4));
        panel.add(subInfo);
        panel.add(Box.createVerticalStrut(16));
        panel.add(lblObs);
        panel.add(Box.createVerticalStrut(6));
        panel.add(scroll);
        panel.add(Box.createVerticalStrut(12));
        panel.add(chkConfirmado);
        panel.add(Box.createVerticalStrut(20));
        panel.add(botones);

        setContentPane(panel);
    }

    private void guardar() {
        String obs = taNovedades.getText().trim();
        if (obs.isEmpty()) { mainUI.mostrarError("Ingrese las observaciones o escriba 'Sin novedad'."); return; }
        String error = citaControlador.registrarNovedades(cita.getId(), fecha, obs, chkConfirmado.isSelected());
        if (error != null) { mainUI.mostrarError(error); return; }
        mainUI.cargarAgenda();
        mainUI.mostrarExito("Novedades registradas. La cita quedó marcada como Finalizada.");
        dispose();
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// PagoUI: confirmación de pago y generación de comprobante
// ─────────────────────────────────────────────────────────────────────────────
class PagoUI extends JDialog {

    private final MainUI          mainUI;
    private final CitaControlador  citaControlador;
    private final Cita            cita;
    private final LocalDate       fecha;

    private JCheckBox     chkPagado;
    private JComboBox<String> comboMomento;
    private JTextField    tfNombreTransferencia;
    private JTextField    tfCodigo;
    private JPanel        panelTransferencia;

    public PagoUI(MainUI mainUI, CitaControlador citaControlador, Cita cita, LocalDate fecha) {
        super(mainUI, "Registro de pago – " + cita.getNombreMascota(), true);
        this.mainUI         = mainUI;
        this.citaControlador = citaControlador;
        this.cita           = cita;
        this.fecha          = fecha;
        construirUI();
    }

    private void construirUI() {
        setSize(480, 460);
        setLocationRelativeTo(getOwner());
        setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(LoginUI.COLOR_FONDO);
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Encabezado
        JLabel titulo = new JLabel("Registro de pago – cita #" + cita.getId());
        titulo.setFont(new Font("SansSerif", Font.BOLD, 14));
        titulo.setForeground(new Color(20, 130, 130));
        titulo.setAlignmentX(LEFT_ALIGNMENT);

        JLabel resumen = new JLabel("Total a cobrar: $" + String.format("%,d", cita.calcularPrecio()));
        resumen.setFont(new Font("SansSerif", Font.BOLD, 18));
        resumen.setForeground(LoginUI.COLOR_VERDE_OSC);
        resumen.setAlignmentX(LEFT_ALIGNMENT);

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        // Confirmación de pago
        chkPagado = new JCheckBox("Pago recibido");
        chkPagado.setFont(new Font("SansSerif", Font.BOLD, 14));
        chkPagado.setOpaque(false);
        chkPagado.setAlignmentX(LEFT_ALIGNMENT);
        chkPagado.setSelected(cita.isPagoRealizado());
        chkPagado.addActionListener(e -> panelTransferencia.setVisible(chkPagado.isSelected()));

        // Momento de pago
        JLabel lblMomento = LoginUI.crearLabel("¿Cuándo se realizó el pago?");
        lblMomento.setAlignmentX(LEFT_ALIGNMENT);
        comboMomento = new JComboBox<>(new String[]{"Después del servicio", "Antes del servicio"});
        comboMomento.setFont(new Font("SansSerif", Font.PLAIN, 13));
        comboMomento.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        comboMomento.setAlignmentX(LEFT_ALIGNMENT);
        if ("Antes".equals(cita.getMomentoPago())) comboMomento.setSelectedIndex(1);

        // Panel de transferencia
        panelTransferencia = new JPanel();
        panelTransferencia.setLayout(new BoxLayout(panelTransferencia, BoxLayout.Y_AXIS));
        panelTransferencia.setOpaque(false);
        panelTransferencia.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lblNombre = LoginUI.crearLabel("Nombre de quien transfiere:");
        tfNombreTransferencia = LoginUI.crearCampoTexto();
        if (cita.getNombreTransferencia() != null) tfNombreTransferencia.setText(cita.getNombreTransferencia());

        JLabel lblCod = LoginUI.crearLabel("Código del comprobante de transferencia:");
        tfCodigo = LoginUI.crearCampoTexto();
        if (cita.getCodigoComprobante() != null) tfCodigo.setText(cita.getCodigoComprobante());

        panelTransferencia.add(Box.createVerticalStrut(8));
        panelTransferencia.add(lblNombre);
        panelTransferencia.add(Box.createVerticalStrut(4));
        panelTransferencia.add(tfNombreTransferencia);
        panelTransferencia.add(Box.createVerticalStrut(8));
        panelTransferencia.add(lblCod);
        panelTransferencia.add(Box.createVerticalStrut(4));
        panelTransferencia.add(tfCodigo);
        panelTransferencia.setVisible(chkPagado.isSelected());

        // Botones
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.setOpaque(false);
        botones.setAlignmentX(LEFT_ALIGNMENT);
        JButton btnCancelar    = new JButton("Cancelar");
        JButton btnGuardar     = new JButton("Guardar pago");
        JButton btnComprobante = new JButton("Ver comprobante");

        LoginUI.estilizarBoton(btnGuardar, new Color(20, 130, 130), Color.WHITE);
        LoginUI.estilizarBoton(btnComprobante, LoginUI.COLOR_VERDE, Color.WHITE);

        btnCancelar.addActionListener(e -> dispose());
        btnGuardar.addActionListener(e -> guardar());
        btnComprobante.addActionListener(e -> verComprobante());

        botones.add(btnCancelar);
        botones.add(btnComprobante);
        botones.add(btnGuardar);

        panel.add(titulo);
        panel.add(Box.createVerticalStrut(6));
        panel.add(resumen);
        panel.add(Box.createVerticalStrut(12));
        panel.add(sep);
        panel.add(Box.createVerticalStrut(12));
        panel.add(chkPagado);
        panel.add(Box.createVerticalStrut(10));
        panel.add(lblMomento);
        panel.add(Box.createVerticalStrut(4));
        panel.add(comboMomento);
        panel.add(panelTransferencia);
        panel.add(Box.createVerticalStrut(20));
        panel.add(botones);

        setContentPane(panel);
    }

    private void guardar() {
        String momento = comboMomento.getSelectedIndex() == 0 ? "Después" : "Antes";
        String error = citaControlador.registrarPago(
                cita.getId(), fecha,
                chkPagado.isSelected(),
                momento,
                tfNombreTransferencia.getText().trim(),
                tfCodigo.getText().trim());
        if (error != null) { mainUI.mostrarError(error); return; }
        mainUI.cargarAgenda();
        mainUI.mostrarExito("Pago registrado correctamente.");
        dispose();
    }

    private void verComprobante() {
        // Aplicar los datos del formulario a la cita temporalmente para el comprobante
        cita.setPagoRealizado(chkPagado.isSelected());
        cita.setMomentoPago(comboMomento.getSelectedIndex() == 0 ? "Después" : "Antes");
        cita.setNombreTransferencia(tfNombreTransferencia.getText().trim());
        cita.setCodigoComprobante(tfCodigo.getText().trim());
        String html = citaControlador.generarComprobante(cita);
        JEditorPane pane = new JEditorPane("text/html", html);
        pane.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(pane),
                "Comprobante – " + cita.getNombreMascota(), JOptionPane.PLAIN_MESSAGE);
    }
}
