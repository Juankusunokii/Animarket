package controlador;

import modelo.Agenda;
import modelo.Cita;
import persistencia.GestorArchivo;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AgendaControlador
 * Carga la agenda de un día desde el archivo y expone filtros y consultas.
 * Mantiene la agenda del día actual en memoria para evitar lecturas repetidas.
 */
public class AgendaControlador {

    private final GestorArchivo gestorArchivo;
    private Agenda agendaActual;

    public AgendaControlador(GestorArchivo gestorArchivo) {
        this.gestorArchivo = gestorArchivo;
    }

    /**
     * Devuelve la agenda del día solicitado.
     * Si la fecha es la misma que la cargada previamente, reutiliza la instancia.
     * Si no, carga desde archivo filtrando por fecha.
     */
    public Agenda obtenerAgenda(LocalDate fecha) {
        if (agendaActual == null || !agendaActual.getFecha().equals(fecha)) {
            cargarAgenda(fecha);
        }
        return agendaActual;
    }

    /** Fuerza la recarga de la agenda desde el archivo (útil tras modificar). */
    public void recargar(LocalDate fecha) {
        cargarAgenda(fecha);
    }

    private void cargarAgenda(LocalDate fecha) {
        List<Cita> todasLasCitas = gestorArchivo.leerCitas();
        List<Cita> citasDelDia = todasLasCitas.stream()
                .filter(c -> c.getFecha().equals(fecha))
                .collect(Collectors.toList());

        agendaActual = new Agenda(fecha);
        agendaActual.setListaCitas(citasDelDia);
    }

    // ─── Consultas delegadas en Agenda ───────────────────────────────────────
    public List<Cita> getCitasDelDia(LocalDate fecha) {
        return obtenerAgenda(fecha).getCitasOrdenadas();
    }

    public List<Cita> getCitasPorEstado(LocalDate fecha, String estado) {
        return obtenerAgenda(fecha).filtrarPorEstado(estado);
    }

    public Cita buscarCita(LocalDate fecha, int id) {
        return obtenerAgenda(fecha).buscarPorId(id);
    }
}