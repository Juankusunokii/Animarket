package vista;

import controlador.AgendaControlador;
import controlador.CitaControlador;
import modelo.Cita;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * CitaUI
 * Formulario modal para registrar o modificar una cita.
 * Si cita != null, el formulario se pre-rellena para modificación.
 */
public class CitaUI extends JDialog {

    private static final DateTimeFormatter FMT_HORA = DateTimeFormatter.ofPattern("hh:mm a");

    private final MainUI           mainUI;
    private final CitaControlador   citaControlador;
    private final AgendaControlador agendaControlador;
    private final LocalDate        fecha;
    private final Cita             citaEditar;   // null = nueva cita

    // ── Campos del formulario ─────────────────────────────────────────────────
    private JTextField tfNombreMascota, tfRaza, tfPeso, tfNombreDueno, tfTelefono;
    private JTextArea  taObservaciones;
    private JComboBox<String> comboServicio;
    private JCheckBox  chkAntipulgas, chkDesparasitante, chkCorteUnas, chkCorteSanitario;
    private JComboBox<String> comboHora, comboMinuto, comboAMPM;
    private JLabel     lblDuracion, lblPrecio;

    public CitaUI(MainUI mainUI, CitaControlador citaControlador,
                  AgendaControlador agendaControlador, LocalDate fecha, Cita citaEditar) {
        super(mainUI, citaEditar == null ? "Nueva cita" : "Modificar cita #" + citaEditar.getId(), true);
        this.mainUI           = mainUI;
        this.citaControlador   = citaControlador;
        this.agendaControlador = agendaControlador;
        this.fecha            = fecha;
        this.citaEditar       = citaEditar;
        construirUI();
        if (citaEditar != null) preRellenar();
    }

    private void construirUI() {
        setSize(560, 680);
        setLocationRelativeTo(getOwner());
        setResizable(false);

        JPanel contenido = new JPanel();
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        contenido.setBackground(LoginUI.COLOR_FONDO);
        contenido.setBorder(new EmptyBorder(16, 20, 16, 20));

        contenido.add(seccionMascota());
        contenido.add(Box.createVerticalStrut(10));
        contenido.add(seccionDueno());
        contenido.add(Box.createVerticalStrut(10));
        contenido.add(seccionServicios());
        contenido.add(Box.createVerticalStrut(10));
        contenido.add(seccionHora());
        contenido.add(Box.createVerticalStrut(10));
        contenido.add(seccionInfo());
        contenido.add(Box.createVerticalStrut(16));
        contenido.add(seccionBotones());

        JScrollPane scroll = new JScrollPane(contenido);
        scroll.setBorder(null);
        setContentPane(scroll);
    }

    // ─── Secciones ────────────────────────────────────────────────────────────
    private JPanel seccionMascota() {
        JPanel p = seccion("Datos de la mascota");
        tfNombreMascota = fila(p, "Nombre *");
        tfRaza          = fila(p, "Raza *");
        tfPeso          = fila(p, "Peso (kg) *");
        taObservaciones = new JTextArea(3, 20);
        taObservaciones.setFont(new Font("SansSerif", Font.PLAIN, 13));
        taObservaciones.setLineWrap(true);
        taObservaciones.setBorder(BorderFactory.createLineBorder(new Color(180, 210, 190)));
        p.add(LoginUI.crearLabel("Observaciones previas"));
        p.add(Box.createVerticalStrut(3));
        p.add(new JScrollPane(taObservaciones));
        return p;
    }

    private JPanel seccionDueno() {
        JPanel p = seccion("Datos del dueño");
        tfNombreDueno = fila(p, "Nombre dueño *");
        tfTelefono    = fila(p, "Teléfono *");
        return p;
    }

    private JPanel seccionServicios() {
        JPanel p = seccion("Servicios");
        comboServicio = new JComboBox<>(new String[]{
                Cita.SERVICIO_BANIO,
                Cita.SERVICIO_BANIO_CORTE,
                Cita.SERVICIO_ESTETICA_COMPLETA});
        comboServicio.setFont(new Font("SansSerif", Font.PLAIN, 13));
        comboServicio.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        comboServicio.addActionListener(e -> calcularEstimados());

        p.add(LoginUI.crearLabel("Servicio base *"));
        p.add(Box.createVerticalStrut(4));
        p.add(comboServicio);
        p.add(Box.createVerticalStrut(8));
        p.add(LoginUI.crearLabel("Adicionales"));
        p.add(Box.createVerticalStrut(4));

        chkAntipulgas     = ck("Antipulgas (+$12.000, +15 min)");
        chkDesparasitante = ck("Desparasitante (+$11.000, +10 min)");
        chkCorteUnas      = ck("Corte de uñas (+$10.000, +10 min)");
        chkCorteSanitario = ck("Corte sanitario (+$15.000, +15 min)");

        for (JCheckBox cb : new JCheckBox[]{chkAntipulgas, chkDesparasitante, chkCorteUnas, chkCorteSanitario}) {
            cb.addActionListener(e -> calcularEstimados());
            p.add(cb);
        }
        return p;
    }

    private JPanel seccionHora() {
        JPanel p = seccion("Horario");
        
        // Crear arrays para horas (09-18), minutos y AM/PM
        String[] horas = new String[10];
        for (int i = 0; i < 10; i++) horas[i] = String.format("%02d", 9 + i);
        String[] minutos = {"00", "15", "30", "45"};
        String[] ampm = {"AM", "PM"};
        
        comboHora = new JComboBox<>(horas);
        comboMinuto = new JComboBox<>(minutos);
        comboAMPM = new JComboBox<>(ampm);
        
        comboHora.setFont(new Font("SansSerif", Font.PLAIN, 13));
        comboMinuto.setFont(new Font("SansSerif", Font.PLAIN, 13));
        comboAMPM.setFont(new Font("SansSerif", Font.PLAIN, 13));
        
        comboHora.setMaximumSize(new Dimension(60, 32));
        comboMinuto.setMaximumSize(new Dimension(60, 32));
        comboAMPM.setMaximumSize(new Dimension(60, 32));
        
        comboHora.addActionListener(e -> calcularEstimados());
        comboMinuto.addActionListener(e -> calcularEstimados());
        comboAMPM.addActionListener(e -> calcularEstimados());
        
        JPanel pHora = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        pHora.setOpaque(false);
        pHora.add(comboHora);
        pHora.add(new JLabel(":"));
        pHora.add(comboMinuto);
        pHora.add(comboAMPM);
        
        p.add(LoginUI.crearLabel("Hora de inicio *"));
        p.add(Box.createVerticalStrut(4));
        p.add(pHora);
        
        tfPeso.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { calcularEstimados(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { calcularEstimados(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { calcularEstimados(); }
        });
        return p;
    }

    private JPanel seccionInfo() {
        JPanel p = new JPanel(new GridLayout(2, 1, 4, 4));
        p.setOpaque(false);
        lblDuracion = new JLabel("Duración estimada: —");
        lblPrecio   = new JLabel("Precio estimado: —");
        lblDuracion.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblPrecio.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblDuracion.setForeground(LoginUI.COLOR_VERDE_OSC);
        lblPrecio.setForeground(LoginUI.COLOR_VERDE_OSC);
        p.add(lblDuracion);
        p.add(lblPrecio);
        return p;
    }

    private JPanel seccionBotones() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        p.setOpaque(false);

        JButton btnCancelar = new JButton("Cancelar");
        JButton btnGuardar  = new JButton(citaEditar == null ? "Registrar cita" : "Guardar cambios");
        LoginUI.estilizarBoton(btnGuardar, LoginUI.COLOR_VERDE, Color.WHITE);
        btnCancelar.addActionListener(e -> dispose());
        btnGuardar.addActionListener(e -> guardar());

        p.add(btnCancelar);
        p.add(btnGuardar);
        return p;
    }

    // ─── Lógica ───────────────────────────────────────────────────────────────
    private void calcularEstimados() {
        try {
            double peso = Double.parseDouble(tfPeso.getText().trim().replace(",", "."));
            Cita temp = new Cita();
            temp.setPeso(peso);
            temp.setServicioSolicitado((String) comboServicio.getSelectedItem());
            temp.setAdicionalesSolicitados(getAdicionalesSeleccionados());
            int dur = temp.calcularDuracion();
            long precio = temp.calcularPrecio();
            lblDuracion.setText("Duración estimada: " + dur + " minutos");
            lblPrecio.setText("Precio estimado: $" + String.format("%,d", precio));
        } catch (NumberFormatException ex) {
            lblDuracion.setText("Duración estimada: —");
            lblPrecio.setText("Precio estimado: —");
        }
    }

    private void guardar() {
        // Validar y parsear hora desde comboboxes
        LocalTime hora;
        try {
            int h = Integer.parseInt((String) comboHora.getSelectedItem());
            int m = Integer.parseInt((String) comboMinuto.getSelectedItem());
            String ampm = (String) comboAMPM.getSelectedItem();
            
            // Convertir a formato 24 horas
            if ("PM".equals(ampm) && h < 12) h += 12;
            if ("AM".equals(ampm) && h == 12) h = 0;
            
            hora = LocalTime.of(h, m);
        } catch (Exception ex) {
            mainUI.mostrarError("Error al parsear la hora. Intente de nuevo.");
            return;
        }

        List<String> adics = getAdicionalesSeleccionados();
        String error;

        if (citaEditar == null) {
            // Nueva cita
            error = citaControlador.registrarCita(
                    fecha, hora,
                    tfNombreMascota.getText().trim(),
                    tfRaza.getText().trim(),
                    parsePeso(),
                    tfNombreDueno.getText().trim(),
                    tfTelefono.getText().trim(),
                    taObservaciones.getText().trim(),
                    (String) comboServicio.getSelectedItem(),
                    adics);
        } else {
            // Modificar
            error = citaControlador.modificarCita(
                    citaEditar.getId(), fecha, hora,
                    (String) comboServicio.getSelectedItem(),
                    adics,
                    taObservaciones.getText().trim());
        }

        if (error != null) {
            mainUI.mostrarError(error);
        } else {
            agendaControlador.recargar(fecha);
            mainUI.cargarAgenda();
            mainUI.mostrarExito(citaEditar == null ? "Cita registrada correctamente." : "Cita modificada correctamente.");
            dispose();
        }
    }

    private double parsePeso() {
        try { return Double.parseDouble(tfPeso.getText().trim().replace(",", ".")); }
        catch (NumberFormatException e) { return 0; }
    }

    private List<String> getAdicionalesSeleccionados() {
        List<String> adics = new ArrayList<>();
        if (chkAntipulgas.isSelected())     adics.add(Cita.ADICIONAL_ANTIPULGAS);
        if (chkDesparasitante.isSelected())  adics.add(Cita.ADICIONAL_DESPARASITANTE);
        if (chkCorteUnas.isSelected())       adics.add(Cita.ADICIONAL_CORTE_UNAS);
        if (chkCorteSanitario.isSelected())  adics.add(Cita.ADICIONAL_CORTE_SANITARIO);
        return adics;
    }

    private LocalTime parseHora(String texto) {
        if (texto == null || texto.isBlank()) throw new DateTimeParseException("Hora vacía", texto, 0);
        String normalized = texto.trim().toUpperCase().replaceAll("\\s+", " ");
        // Aceptar entradas sin espacio antes de AM/PM
        normalized = normalized.replace("AM", " AM").replace("PM", " PM").replaceAll("\\s+", " ").trim();

        try {
            return LocalTime.parse(normalized);
        } catch (DateTimeParseException ex) {
            return LocalTime.parse(normalized, FMT_HORA);
        }
    }

    private void preRellenar() {
        tfNombreMascota.setText(citaEditar.getNombreMascota());
        tfRaza.setText(citaEditar.getRaza());
        tfPeso.setText(String.valueOf(citaEditar.getPeso()));
        tfNombreDueno.setText(citaEditar.getNombreDueno());
        tfTelefono.setText(citaEditar.getTelefono());
        taObservaciones.setText(citaEditar.getObservacionesPrevias() != null ? citaEditar.getObservacionesPrevias() : "");
        comboServicio.setSelectedItem(citaEditar.getServicioSolicitado());
        
        // Cargar hora en formato AM/PM
        LocalTime hora = citaEditar.getHora();
        int h = hora.getHour();
        String ampm = h >= 12 ? "PM" : "AM";
        if (h > 12) h -= 12;
        if (h == 0) { h = 12; ampm = "AM"; }
        
        comboHora.setSelectedItem(String.format("%02d", h));
        comboMinuto.setSelectedItem(String.format("%02d", hora.getMinute()));
        comboAMPM.setSelectedItem(ampm);
        
        List<String> adics = citaEditar.getAdicionalesSolicitados();
        chkAntipulgas.setSelected(adics.contains(Cita.ADICIONAL_ANTIPULGAS));
        chkDesparasitante.setSelected(adics.contains(Cita.ADICIONAL_DESPARASITANTE));
        chkCorteUnas.setSelected(adics.contains(Cita.ADICIONAL_CORTE_UNAS));
        chkCorteSanitario.setSelected(adics.contains(Cita.ADICIONAL_CORTE_SANITARIO));
        calcularEstimados();
    }

    // ─── Helpers de layout ────────────────────────────────────────────────────
    private JPanel seccion(String titulo) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(180, 215, 190)),
                titulo, TitledBorder.LEFT, TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 12), LoginUI.COLOR_VERDE_OSC));
        p.setAlignmentX(LEFT_ALIGNMENT);
        return p;
    }

    private JTextField fila(JPanel p, String label) {
        p.add(LoginUI.crearLabel(label));
        p.add(Box.createVerticalStrut(3));
        JTextField tf = LoginUI.crearCampoTexto();
        p.add(tf);
        p.add(Box.createVerticalStrut(8));
        return tf;
    }

    private JCheckBox ck(String texto) {
        JCheckBox cb = new JCheckBox(texto);
        cb.setFont(new Font("SansSerif", Font.PLAIN, 13));
        cb.setOpaque(false);
        return cb;
    }
}
