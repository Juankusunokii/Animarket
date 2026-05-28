package persistencia;

import modelo.Cita;
import modelo.Usuario;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * GestorArchivo
 * Responsable de toda la lectura y escritura sobre citas.txt y usuarios.txt.
 *
 * Formato de una línea en citas.txt (campos separados por "|"):
 *   id | fecha | hora | duracion | estado | nombreMascota | raza | peso |
 *   nombreDueno | telefono | observacionesPrevias | servicioSolicitado |
 *   adicionales(,) | observacionesPosteriores | servicioConfirmado |
 *   pagoRealizado | momentoPago | nombreTransferencia | codigoComprobante
 *
 * Formato de una línea en usuarios.txt:
 *   nombreUsuario | contrasena | rol
 */
public class GestorArchivo {

    private static final String SEP       = "|";
    private static final String SEP_REGEX = "\\|";
    private static final String SEP_ADIC  = ",";
    private static final String NULO      = "NULL";

    private final String rutaCitas;
    private final String rutaUsuarios;

    public GestorArchivo(String rutaCitas, String rutaUsuarios) {
        this.rutaCitas    = rutaCitas;
        this.rutaUsuarios = rutaUsuarios;
        inicializarArchivos();
    }

    // ─── Inicialización ───────────────────────────────────────────────────────
    private void inicializarArchivos() {
        crearSiNoExiste(rutaCitas);
        crearSiNoExiste(rutaUsuarios);
        // Si usuarios.txt está vacío, crear usuario por defecto
        try {
            if (Files.size(Paths.get(rutaUsuarios)) == 0) {
                List<String> lineas = new ArrayList<>();
                lineas.add("admin" + SEP + "1234" + SEP + "Recepcionista");
                Files.write(Paths.get(rutaUsuarios), lineas, StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            System.err.println("Error inicializando usuarios.txt: " + e.getMessage());
        }
    }

    private void crearSiNoExiste(String ruta) {
        try {
            Path p = Paths.get(ruta);
            if (!Files.exists(p)) Files.createFile(p);
        } catch (IOException e) {
            System.err.println("No se pudo crear el archivo " + ruta + ": " + e.getMessage());
        }
    }

    // ─── Backup automático ────────────────────────────────────────────────────
    public void realizarBackup() {
        try {
            Path origen  = Paths.get(rutaCitas);
            Path destino = Paths.get(rutaCitas + ".bak");
            Files.copy(origen, destino, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.err.println("Error en backup: " + e.getMessage());
        }
    }

    // ─── LECTURA: citas ───────────────────────────────────────────────────────
    public List<Cita> leerCitas() {
        List<Cita> lista = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(rutaCitas), StandardCharsets.UTF_8))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty()) continue;
                Cita c = parsearLinea(linea);
                if (c != null) lista.add(c);
            }
        } catch (IOException e) {
            System.err.println("Error leyendo citas.txt: " + e.getMessage());
        }
        return lista;
    }

    private Cita parsearLinea(String linea) {
        String[] partes = linea.split(SEP_REGEX, -1);
        if (partes.length < 19) return null;
        try {
            Cita c = new Cita();
            c.setId(Integer.parseInt(partes[0].trim()));
            c.setFecha(LocalDate.parse(partes[1].trim()));
            c.setHora(LocalTime.parse(partes[2].trim()));
            c.setDuracion(Integer.parseInt(partes[3].trim()));
            c.setEstado(partes[4].trim());
            c.setNombreMascota(partes[5].trim());
            c.setRaza(partes[6].trim());
            c.setPeso(Double.parseDouble(partes[7].trim()));
            c.setNombreDueno(partes[8].trim());
            c.setTelefono(partes[9].trim());
            c.setObservacionesPrevias(nulable(partes[10].trim()));
            c.setServicioSolicitado(partes[11].trim());

            // adicionales separados por coma
            String adicStr = partes[12].trim();
            List<String> adics = new ArrayList<>();
            if (!adicStr.isEmpty() && !NULO.equals(adicStr)) {
                adics.addAll(Arrays.asList(adicStr.split(SEP_ADIC)));
            }
            c.setAdicionalesSolicitados(adics);

            c.setObservacionesPosteriores(nulable(partes[13].trim()));
            c.setServicioConfirmado(Boolean.parseBoolean(partes[14].trim()));
            c.setPagoRealizado(Boolean.parseBoolean(partes[15].trim()));
            c.setMomentoPago(nulable(partes[16].trim()));
            c.setNombreTransferencia(nulable(partes[17].trim()));
            c.setCodigoComprobante(nulable(partes[18].trim()));
            return c;
        } catch (Exception e) {
            System.err.println("Error parseando línea: " + linea + " → " + e.getMessage());
            return null;
        }
    }

    // ─── ESCRITURA: citas ─────────────────────────────────────────────────────
    public void escribirTodasLasCitas(List<Cita> citas) {
        try (PrintWriter pw = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(rutaCitas, false), StandardCharsets.UTF_8))) {
            for (Cita c : citas) {
                pw.println(citaALinea(c));
            }
        } catch (IOException e) {
            System.err.println("Error escribiendo citas.txt: " + e.getMessage());
        }
    }

    /** Agrega una sola cita al final del archivo (no reescribe todo). */
    public void agregarCita(Cita cita) {
        try (PrintWriter pw = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(rutaCitas, true), StandardCharsets.UTF_8))) {
            pw.println(citaALinea(cita));
        } catch (IOException e) {
            System.err.println("Error agregando cita: " + e.getMessage());
        }
    }

    private String citaALinea(Cita c) {
        String adics = (c.getAdicionalesSolicitados() == null || c.getAdicionalesSolicitados().isEmpty())
                       ? NULO
                       : String.join(SEP_ADIC, c.getAdicionalesSolicitados());
        return String.join(SEP,
                String.valueOf(c.getId()),
                c.getFecha().toString(),
                c.getHora().toString(),
                String.valueOf(c.getDuracion()),
                c.getEstado(),
                c.getNombreMascota(),
                c.getRaza(),
                String.valueOf(c.getPeso()),
                c.getNombreDueno(),
                c.getTelefono(),
                noNulo(c.getObservacionesPrevias()),
                c.getServicioSolicitado(),
                adics,
                noNulo(c.getObservacionesPosteriores()),
                String.valueOf(c.isServicioConfirmado()),
                String.valueOf(c.isPagoRealizado()),
                noNulo(c.getMomentoPago()),
                noNulo(c.getNombreTransferencia()),
                noNulo(c.getCodigoComprobante())
        );
    }

    // ─── LECTURA: usuarios ────────────────────────────────────────────────────
    public List<Usuario> leerUsuarios() {
        List<Usuario> lista = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(rutaUsuarios), StandardCharsets.UTF_8))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty()) continue;
                String[] p = linea.split(SEP_REGEX, -1);
                if (p.length >= 3) {
                    lista.add(new Usuario(p[0].trim(), p[1].trim(), p[2].trim()));
                }
            }
        } catch (IOException e) {
            System.err.println("Error leyendo usuarios.txt: " + e.getMessage());
        }
        return lista;
    }

    // ─── Utilidades ───────────────────────────────────────────────────────────
    private String noNulo(String s)  { return (s == null || s.isEmpty()) ? NULO : s; }
    private String nulable(String s) { return NULO.equals(s) ? null : s; }
}
