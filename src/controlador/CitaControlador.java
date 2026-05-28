package controlador;

import modelo.Agenda;
import modelo.Cita;
import persistencia.GestorArchivo;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * CitaControlador
 * Orquesta la lógica de agendamiento: crea, modifica, cancela y consulta citas.
 * Delega la persistencia en GestorArchivo y la verificación de horarios en Agenda.
 */
public class CitaControlador {

    private final GestorArchivo gestorArchivo;
    private final AgendaControlador agendaControlador;

    public CitaControlador(GestorArchivo gestorArchivo, AgendaControlador agendaControlador) {
        this.gestorArchivo   = gestorArchivo;
        this.agendaControlador = agendaControlador;
    }

    // ─── Registrar nueva cita ─────────────────────────────────────────────────
    /**
     * Intenta registrar una nueva cita.
     * @return  null → éxito (cita persistida)
     *          String → mensaje de error o conflicto de horario
     */
    public String registrarCita(LocalDate fecha, LocalTime hora,
                                String nombreMascota, String raza, double peso,
                                String nombreDueno, String telefono,
                                String observacionesPrevias, String servicio,
                                List<String> adicionales) {

        // Validaciones mínimas
        if (nombreMascota == null || nombreMascota.isBlank()) return "El nombre de la mascota es obligatorio.";
        if (nombreDueno   == null || nombreDueno.isBlank())   return "El nombre del dueño es obligatorio.";
        if (telefono      == null || telefono.isBlank())       return "El teléfono es obligatorio.";
        if (raza          == null || raza.isBlank())           return "La raza es obligatoria.";
        if (peso <= 0)                                         return "El peso debe ser mayor a 0.";
        if (fecha == null || hora == null)                     return "Fecha y hora son obligatorias.";
        if (servicio == null || servicio.isBlank())            return "Debe seleccionar un servicio.";

        // Obtener agenda del día y calcular duración
        Agenda agenda = agendaControlador.obtenerAgenda(fecha);
        Cita temporal = new Cita(0, fecha, hora, nombreMascota, raza, peso,
                                 nombreDueno, telefono, observacionesPrevias,
                                 servicio, adicionales);
        int duracion = temporal.calcularDuracion();

        // Verificar disponibilidad
        if (!agenda.verificarDisponibilidad(hora, duracion, -1)) {
            List<LocalTime> alternativas = agenda.sugerirHorariosAlternativos(duracion);
            StringBuilder sb = new StringBuilder("Horario no disponible. Alternativas: ");
            if (alternativas.isEmpty()) {
                sb.append("sin horarios libres hoy.");
            } else {
                alternativas.forEach(t -> sb.append(t).append("  "));
            }
            return sb.toString();
        }

        // Crear cita con ID único y persistir
        int nuevoId = agenda.generarNuevoId();
        Cita cita = new Cita(nuevoId, fecha, hora, nombreMascota, raza, peso,
                             nombreDueno, telefono, observacionesPrevias,
                             servicio, adicionales);
        cita.calcularDuracion();
        agenda.agregarCita(cita);
        gestorArchivo.agregarCita(cita);
        return null;   // null = éxito
    }

    // ─── Modificar cita ───────────────────────────────────────────────────────
    public String modificarCita(int id, LocalDate fechaNueva, LocalTime horaNueva,
                                String servicio, List<String> adicionales,
                                String observacionesPrevias) {

        Agenda agenda = agendaControlador.obtenerAgenda(fechaNueva);
        Cita cita = agenda.buscarPorId(id);
        if (cita == null) return "No se encontró la cita con ID " + id;
        if (Cita.ESTADO_FINALIZADA.equals(cita.getEstado())) return "No se puede modificar una cita finalizada.";

        // Recalcular duración con nuevos datos
        cita.setServicioSolicitado(servicio);
        cita.setAdicionalesSolicitados(adicionales);
        cita.setObservacionesPrevias(observacionesPrevias);
        int nuevaDuracion = cita.calcularDuracion();

        if (!agenda.verificarDisponibilidad(horaNueva, nuevaDuracion, id)) {
            List<LocalTime> alternativas = agenda.sugerirHorariosAlternativos(nuevaDuracion);
            StringBuilder sb = new StringBuilder("Horario no disponible. Alternativas: ");
            alternativas.forEach(t -> sb.append(t).append("  "));
            return sb.toString();
        }

        cita.setFecha(fechaNueva);
        cita.setHora(horaNueva);
        persistirTodas(fechaNueva);
        return null;
    }

    // ─── Cancelar cita ────────────────────────────────────────────────────────
    public String cancelarCita(int id, LocalDate fecha) {
        Agenda agenda = agendaControlador.obtenerAgenda(fecha);
        Cita cita = agenda.buscarPorId(id);
        if (cita == null) return "No se encontró la cita.";
        if (Cita.ESTADO_FINALIZADA.equals(cita.getEstado())) return "No se puede cancelar una cita finalizada.";
        cita.setEstado(Cita.ESTADO_CANCELADA);
        persistirTodas(fecha);
        return null;
    }

    // ─── Registrar novedades post-servicio ────────────────────────────────────
    public String registrarNovedades(int id, LocalDate fecha,
                                     String observaciones, boolean confirmado) {
        Agenda agenda = agendaControlador.obtenerAgenda(fecha);
        Cita cita = agenda.buscarPorId(id);
        if (cita == null) return "No se encontró la cita.";
        cita.setObservacionesPosteriores(observaciones);
        cita.setServicioConfirmado(confirmado);
        cita.setEstado(Cita.ESTADO_FINALIZADA);
        persistirTodas(fecha);
        return null;
    }

    // ─── Registrar pago ───────────────────────────────────────────────────────
    public String registrarPago(int id, LocalDate fecha, boolean pagado,
                                String momento, String nombreTransferencia,
                                String codigo) {
        Agenda agenda = agendaControlador.obtenerAgenda(fecha);
        Cita cita = agenda.buscarPorId(id);
        if (cita == null) return "No se encontró la cita.";
        cita.setPagoRealizado(pagado);
        cita.setMomentoPago(momento);
        cita.setNombreTransferencia(nombreTransferencia);
        cita.setCodigoComprobante(codigo);
        persistirTodas(fecha);
        return null;
    }

    // ─── Generar comprobante HTML ────────────────────────────────────────────
    public String generarComprobante(Cita cita) {
        String adics = cita.getAdicionalesSolicitados().isEmpty()
                ? "Ninguno"
                : String.join(", ", cita.getAdicionalesSolicitados());

        return "<html><body style='font-family:Arial,sans-serif;padding:10px'>"
             + "<h2 style='color:#2c7a4b'>🐾 Animarket – Comprobante</h2>"
             + "<table border='0' cellpadding='4'>"
             + fila("Mascota",     cita.getNombreMascota())
             + fila("Raza",        cita.getRaza())
             + fila("Peso",        cita.getPeso() + " kg")
             + fila("Dueño",       cita.getNombreDueno())
             + fila("Teléfono",    cita.getTelefono())
             + fila("Fecha",       cita.getFecha().toString())
             + fila("Hora",        cita.getHora().toString())
             + fila("Duración",    cita.getDuracion() + " min")
             + fila("Servicio",    cita.getServicioSolicitado())
             + fila("Adicionales", adics)
             + fila("Total",       "$" + String.format("%,d", cita.calcularPrecio()))
             + fila("Pago",        cita.isPagoRealizado() ? "Realizado" : "Pendiente")
             + (cita.getMomentoPago() != null ? fila("Momento pago", cita.getMomentoPago()) : "")
             + (cita.getCodigoComprobante() != null && !cita.getCodigoComprobante().isEmpty()
                ? fila("Código transferencia", cita.getCodigoComprobante()) : "")
             + (cita.getObservacionesPosteriores() != null
                ? fila("Observaciones", cita.getObservacionesPosteriores()) : "")
             + "</table></body></html>";
    }

    private String fila(String k, String v) {
        return "<tr><td><b>" + k + ":</b></td><td>" + v + "</td></tr>";
    }

    // ─── Persistencia ─────────────────────────────────────────────────────────
    private void persistirTodas(LocalDate fecha) {
        gestorArchivo.escribirTodasLasCitas(
            agendaControlador.obtenerAgenda(fecha).getListaCitas()
        );
    }
}
