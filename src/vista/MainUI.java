package vista;

import controlador.AgendaControlador;
import controlador.AuthControlador;
import controlador.CitaControlador;
import modelo.Cita;
import persistencia.GestorArchivo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * MainUI
 * Ventana principal del sistema. Muestra la agenda del día con opciones
 * de filtro por estado y acceso a todas las acciones disponibles.
 */
public class MainUI extends JFrame {

    private static final DateTimeFormatter FMT_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final AuthControlador    authControlador;
    private final GestorArchivo     gestorArchivo;
    private final AgendaControlador  agendaControlador;
    private final CitaControlador    citaControlador;

    private LocalDate fechaActual = LocalDate.now();

    // Componentes UI
    private JLabel      lblFecha;
    private JTable      tablaAgenda;
    private DefaultTableModel modeloTabla;
    private JComboBox<String> comboFiltro;

    public MainUI(AuthControlador authControlador) {
        this.authControlador   = authControlador;
        this.gestorArchivo    = new GestorArchivo("citas.txt", "usuarios.txt");
        this.agendaControlador = new AgendaControlador(gestorArchivo);
        this.citaControlador   = new CitaControlador(gestorArchivo, agendaControlador);
        construirUI();
        cargarAgenda();
    }

    // ─── Construcción de UI ───────────────────────────────────────────────────
    private void construirUI() {
        setTitle("Animarket – Panel Principal");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(960, 620);
        setLocationRelativeTo(null);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) {
                gestorArchivo.realizarBackup();
                authControlador.cerrarSesion();
                dispose();
                System.exit(0);
            }
        });

        setLayout(new BorderLayout());
        add(construirHeader(), BorderLayout.NORTH);
        add(construirPanel(),  BorderLayout.CENTER);
        add(construirBarra(),  BorderLayout.SOUTH);
    }

    // ─── Header ───────────────────────────────────────────────────────────────
    private JPanel construirHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(LoginUI.COLOR_VERDE);
        header.setBorder(new EmptyBorder(12, 20, 12, 20));

        JLabel titulo = new JLabel("🐾 Animarket – Gestión de citas");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 20));
        titulo.setForeground(Color.WHITE);

        JLabel usuario = new JLabel("Usuario: " + authControlador.getUsuarioActivo().getNombreUsuario());
        usuario.setFont(new Font("SansSerif", Font.PLAIN, 13));
        usuario.setForeground(new Color(200, 240, 210));

        header.add(titulo,  BorderLayout.WEST);
        header.add(usuario, BorderLayout.EAST);
        return header;
    }

    // ─── Panel central ────────────────────────────────────────────────────────
    private JPanel construirPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(LoginUI.COLOR_FONDO);
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        // Controles de fecha y filtro
        JPanel controles = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        controles.setOpaque(false);

        JButton btnAnterior = new JButton("◀");
        JButton btnSiguiente = new JButton("▶");
        lblFecha = new JLabel(fechaActual.format(FMT_FECHA));
        lblFecha.setFont(new Font("SansSerif", Font.BOLD, 14));

        btnAnterior.addActionListener(e -> { fechaActual = fechaActual.minusDays(1); actualizarFecha(); });
        btnSiguiente.addActionListener(e -> { fechaActual = fechaActual.plusDays(1); actualizarFecha(); });

        estilizarBtnNavegacion(btnAnterior);
        estilizarBtnNavegacion(btnSiguiente);

        comboFiltro = new JComboBox<>(new String[]{"Todas", Cita.ESTADO_PENDIENTE,
                                                   Cita.ESTADO_ACTIVA, Cita.ESTADO_FINALIZADA,
                                                   Cita.ESTADO_CANCELADA});
        comboFiltro.addActionListener(e -> cargarAgenda());
        comboFiltro.setFont(new Font("SansSerif", Font.PLAIN, 13));

        controles.add(btnAnterior);
        controles.add(lblFecha);
        controles.add(btnSiguiente);
        controles.add(Box.createHorizontalStrut(20));
        controles.add(new JLabel("Filtrar:"));
        controles.add(comboFiltro);

        // Tabla
        String[] columnas = {"ID", "Hora", "Mascota", "Raza", "Dueño", "Servicio", "Duración", "Estado"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaAgenda = new JTable(modeloTabla);
        configurarTabla();

        JScrollPane scroll = new JScrollPane(tablaAgenda);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(200, 220, 200)));

        // Botones de acción
        JPanel acciones = construirPanelAcciones();

        panel.add(controles, BorderLayout.NORTH);
        panel.add(scroll,    BorderLayout.CENTER);
        panel.add(acciones,  BorderLayout.SOUTH);
        return panel;
    }

    private JPanel construirPanelAcciones() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        p.setOpaque(false);

        JButton btnNueva     = crearBotonAccion("+ Nueva cita",     LoginUI.COLOR_VERDE);
        JButton btnVer       = crearBotonAccion("Ver detalle",       new Color(60, 100, 170));
        JButton btnModificar = crearBotonAccion("Modificar",         new Color(160, 100, 20));
        JButton btnCancelar  = crearBotonAccion("Cancelar cita",     new Color(160, 40,  40));
        JButton btnNovedades = crearBotonAccion("Novedades",         new Color(80, 60, 150));
        JButton btnPago      = crearBotonAccion("Registrar pago",    new Color(20, 130, 130));
        JButton btnRefresh   = crearBotonAccion("↻ Actualizar",      LoginUI.COLOR_GRIS);

        btnNueva.addActionListener(e -> abrirFormularioCita(null));
        btnVer.addActionListener(e -> verDetalleCita());
        btnModificar.addActionListener(e -> modificarCita());
        btnCancelar.addActionListener(e -> cancelarCita());
        btnNovedades.addActionListener(e -> registrarNovedades());
        btnPago.addActionListener(e -> registrarPago());
        btnRefresh.addActionListener(e -> { agendaControlador.recargar(fechaActual); cargarAgenda(); });

        p.add(btnNueva); p.add(btnVer); p.add(btnModificar); p.add(btnCancelar);
        p.add(btnNovedades); p.add(btnPago); p.add(btnRefresh);
        return p;
    }

    // ─── Barra de estado ──────────────────────────────────────────────────────
    private JPanel construirBarra() {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(new Color(230, 242, 234));
        barra.setBorder(new EmptyBorder(4, 16, 4, 16));

        JLabel info = new JLabel("Sistema de gestión de citas · Animarket · Ciudad Salitre, Bogotá");
        info.setFont(new Font("SansSerif", Font.PLAIN, 11));
        info.setForeground(LoginUI.COLOR_GRIS);

        JButton btnCerrar = new JButton("Cerrar sesión");
        btnCerrar.setFont(new Font("SansSerif", Font.PLAIN, 11));
        btnCerrar.setBorderPainted(false);
        btnCerrar.setFocusPainted(false);
        btnCerrar.setBackground(new Color(230, 242, 234));
        btnCerrar.setForeground(LoginUI.COLOR_VERDE_OSC);
        btnCerrar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCerrar.addActionListener(e -> cerrarSesion());

        barra.add(info,      BorderLayout.WEST);
        barra.add(btnCerrar, BorderLayout.EAST);
        return barra;
    }

    // ─── Carga de datos en la tabla ───────────────────────────────────────────
    public void cargarAgenda() {
        modeloTabla.setRowCount(0);
        String filtro = (String) comboFiltro.getSelectedItem();
        List<Cita> citas;

        if ("Todas".equals(filtro)) {
            citas = agendaControlador.getCitasDelDia(fechaActual);
        } else {
            citas = agendaControlador.getCitasPorEstado(fechaActual, filtro);
        }

        for (Cita c : citas) {
            modeloTabla.addRow(new Object[]{
                c.getId(),
                c.getHora(),
                c.getNombreMascota(),
                c.getRaza(),
                c.getNombreDueno(),
                c.getServicioSolicitado(),
                c.getDuracion() + " min",
                c.getEstado()
            });
        }
    }

    private void actualizarFecha() {
        lblFecha.setText(fechaActual.format(FMT_FECHA));
        agendaControlador.recargar(fechaActual);
        cargarAgenda();
    }

    // ─── Acciones ─────────────────────────────────────────────────────────────
    private void abrirFormularioCita(Cita cita) {
        new CitaUI(this, citaControlador, agendaControlador, fechaActual, cita).setVisible(true);
    }

    private void verDetalleCita() {
        Cita c = getCitaSeleccionada();
        if (c == null) return;
        String html = citaControlador.generarComprobante(c);
        JEditorPane pane = new JEditorPane("text/html", html);
        pane.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(pane),
                "Detalle de cita #" + c.getId(), JOptionPane.PLAIN_MESSAGE);
    }

    private void modificarCita() {
        Cita c = getCitaSeleccionada();
        if (c == null) return;
        if (Cita.ESTADO_FINALIZADA.equals(c.getEstado()) || Cita.ESTADO_CANCELADA.equals(c.getEstado())) {
            mostrarError("No se puede modificar una cita " + c.getEstado().toLowerCase() + ".");
            return;
        }
        abrirFormularioCita(c);
    }

    private void cancelarCita() {
        Cita c = getCitaSeleccionada();
        if (c == null) return;
        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Confirma cancelar la cita de " + c.getNombreMascota() + "?",
                "Cancelar cita", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        String error = citaControlador.cancelarCita(c.getId(), fechaActual);
        if (error != null) mostrarError(error);
        else { agendaControlador.recargar(fechaActual); cargarAgenda(); }
    }

    private void registrarNovedades() {
        Cita c = getCitaSeleccionada();
        if (c == null) return;
        new NovedadesUI(this, citaControlador, c, fechaActual).setVisible(true);
    }

    private void registrarPago() {
        Cita c = getCitaSeleccionada();
        if (c == null) return;
        new PagoUI(this, citaControlador, c, fechaActual).setVisible(true);
    }

    private void cerrarSesion() {
        gestorArchivo.realizarBackup();
        authControlador.cerrarSesion();
        dispose();
        new LoginUI(authControlador).setVisible(true);
    }

    // ─── Utilidades UI ────────────────────────────────────────────────────────
    private Cita getCitaSeleccionada() {
        int fila = tablaAgenda.getSelectedRow();
        if (fila < 0) { mostrarError("Seleccione una cita de la tabla."); return null; }
        int id = (int) modeloTabla.getValueAt(fila, 0);
        Cita c = agendaControlador.buscarCita(fechaActual, id);
        if (c == null) mostrarError("No se pudo obtener la cita seleccionada.");
        return c;
    }

    public void mostrarError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void mostrarExito(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }

    private void configurarTabla() {
        tablaAgenda.setRowHeight(28);
        tablaAgenda.setFont(new Font("SansSerif", Font.PLAIN, 13));
        tablaAgenda.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        tablaAgenda.getTableHeader().setBackground(LoginUI.COLOR_VERDE);
        tablaAgenda.getTableHeader().setForeground(Color.WHITE);
        tablaAgenda.setSelectionBackground(new Color(200, 230, 210));
        tablaAgenda.setGridColor(new Color(220, 235, 222));
        tablaAgenda.setShowHorizontalLines(true);
        tablaAgenda.setShowVerticalLines(false);
        tablaAgenda.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Colorear filas según estado
        tablaAgenda.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    String estado = (String) modeloTabla.getValueAt(row, 7);
                    c.setBackground(switch (estado) {
                        case Cita.ESTADO_FINALIZADA -> new Color(230, 245, 230);
                        case Cita.ESTADO_CANCELADA  -> new Color(245, 225, 225);
                        case Cita.ESTADO_ACTIVA     -> new Color(255, 250, 220);
                        default                     -> Color.WHITE;
                    });
                }
                return c;
            }
        });

        // Ancho de columnas
        int[] anchos = {40, 60, 130, 100, 130, 130, 80, 90};
        for (int i = 0; i < anchos.length; i++)
            tablaAgenda.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
    }

    private JButton crearBotonAccion(String texto, Color color) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(6, 12, 6, 12));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(color.darker()); }
            @Override public void mouseExited(java.awt.event.MouseEvent e)  { btn.setBackground(color); }
        });
        return btn;
    }

    private void estilizarBtnNavegacion(JButton btn) {
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBackground(LoginUI.COLOR_FONDO);
        btn.setBorder(BorderFactory.createLineBorder(new Color(180, 210, 190)));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
}
