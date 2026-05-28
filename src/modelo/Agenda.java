package modelo;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Agenda {

    private LocalDate   fecha;
    private List<Cita>  listaCitas;

    public Agenda(LocalDate fecha) {
        this.fecha     = fecha;
        this.listaCitas = new ArrayList<>();
    }

    // ─── Cargar citas externas ───────────────────────────────────────────────
    public void setListaCitas(List<Cita> citas) {
        this.listaCitas = new ArrayList<>(citas);
    }

    // ─── Disponibilidad horaria ──────────────────────────────────────────────
    /**
     * Verifica si el bloque (hora, duracion) se superpone con alguna cita
     * existente en la fecha actual. Ignora citas canceladas.
     *
     * @param hora     hora de inicio de la nueva cita
     * @param duracion duración estimada en minutos
     * @param idExcluir id de cita a excluir (útil al modificar una existente; -1 si es nueva)
     * @return true si el horario está libre
     */
    public boolean verificarDisponibilidad(LocalTime hora, int duracion, int idExcluir) {
        LocalTime finNueva = hora.plusMinutes(duracion);

        for (Cita c : listaCitas) {
            if (c.getId() == idExcluir) continue;
            if (!c.getFecha().equals(fecha)) continue;
            if (Cita.ESTADO_CANCELADA.equals(c.getEstado())) continue;

            LocalTime inicioExistente = c.getHora();
            LocalTime finExistente    = c.getHoraFin();

            // Dos intervalos [a,b) y [c,d) se superponen si a < d && c < b
            boolean solapa = hora.isBefore(finExistente) && inicioExistente.isBefore(finNueva);
            if (solapa) return false;
        }
        return true;
    }

    /**
     * Sugiere horarios alternativos disponibles (cada 30 min desde las 8:00
     * hasta las 18:00) para una duración dada.
     */
    public List<LocalTime> sugerirHorariosAlternativos(int duracion) {
        List<LocalTime> sugerencias = new ArrayList<>();
        LocalTime inicio = LocalTime.of(8, 0);
        LocalTime limite = LocalTime.of(18, 0);

        while (!inicio.plusMinutes(duracion).isAfter(limite)) {
            if (verificarDisponibilidad(inicio, duracion, -1)) {
                sugerencias.add(inicio);
                if (sugerencias.size() >= 5) break;   // máximo 5 sugerencias
            }
            inicio = inicio.plusMinutes(30);
        }
        return sugerencias;
    }

    // ─── CRUD sobre la lista en memoria ──────────────────────────────────────
    public void agregarCita(Cita cita) {
        listaCitas.add(cita);
    }

    public boolean eliminarCita(int id) {
        return listaCitas.removeIf(c -> c.getId() == id);
    }

    public Cita buscarPorId(int id) {
        return listaCitas.stream()
                .filter(c -> c.getId() == id)
                .findFirst()
                .orElse(null);
    }

    // ─── Filtros ─────────────────────────────────────────────────────────────
    public List<Cita> filtrarPorEstado(String estado) {
        return listaCitas.stream()
                .filter(c -> estado.equals(c.getEstado()))
                .sorted(Comparator.comparing(Cita::getHora))
                .collect(Collectors.toList());
    }

    public List<Cita> getCitasOrdenadas() {
        return listaCitas.stream()
                .sorted(Comparator.comparing(Cita::getHora))
                .collect(Collectors.toList());
    }

    // ─── Generación de ID único ───────────────────────────────────────────────
    public int generarNuevoId() {
        return listaCitas.stream()
                .mapToInt(Cita::getId)
                .max()
                .orElse(0) + 1;
    }

    // ─── Getters / Setters ───────────────────────────────────────────────────
    public LocalDate getFecha()            { return fecha; }
    public void setFecha(LocalDate fecha)  { this.fecha = fecha; }
    public List<Cita> getListaCitas()      { return listaCitas; }
}