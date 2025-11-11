import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MenuPrincipal extends JFrame {

    private PartidaDAO partidaDAO;
    private JComboBox<PartidaGuardada> comboPartidas;
    private JButton btnCargarPartida;

    private final Font FONT_BOTON = new Font("Arial", Font.BOLD, 24);

    public MenuPrincipal() {
        super("Menú Principal del Juego");
        partidaDAO = new PartidaDAO();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        JPanel panelPrincipal = new JPanel(new BorderLayout(20, 20));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
        panelPrincipal.setBackground(Color.DARK_GRAY);

        JLabel lblTitulo = new JLabel("CABALLERO DE ROSAS", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Serif", Font.BOLD, 48));
        lblTitulo.setForeground(Color.WHITE);
        panelPrincipal.add(lblTitulo, BorderLayout.NORTH);

        JPanel panelOpciones = new JPanel(new GridLayout(4, 1, 15, 15));
        panelOpciones.setOpaque(false);

        JButton btnNuevaPartida = new JButton("NUEVA PARTIDA");
        btnNuevaPartida.setFont(FONT_BOTON);
        btnNuevaPartida.addActionListener(e -> iniciarNuevaPartida());
        panelOpciones.add(btnNuevaPartida);

        JPanel panelCarga = new JPanel(new BorderLayout(10, 0));
        panelCarga.setOpaque(false);

        comboPartidas = new JComboBox<>();
        comboPartidas.setFont(FONT_BOTON.deriveFont(20f));

        btnCargarPartida = new JButton("CARGAR SELECCIÓN");
        btnCargarPartida.setFont(FONT_BOTON);
        btnCargarPartida.setEnabled(false);
        btnCargarPartida.addActionListener(e -> cargarPartidaSeleccionada());

        panelCarga.add(comboPartidas, BorderLayout.CENTER);
        panelCarga.add(btnCargarPartida, BorderLayout.EAST);

        panelOpciones.add(panelCarga);

        JButton btnSalir = new JButton("SALIR");
        btnSalir.setFont(FONT_BOTON);
        btnSalir.addActionListener(e -> System.exit(0));
        panelOpciones.add(btnSalir);

        panelPrincipal.add(panelOpciones, BorderLayout.CENTER);

        add(panelPrincipal);

        cargarListaPartidas();

        setVisible(true);
    }

    private void cargarListaPartidas() {
        List<PartidaGuardada> partidas = partidaDAO.listarPartidas();
        comboPartidas.removeAllItems();

        if (partidas.isEmpty()) {
            comboPartidas.addItem(new PartidaGuardada(-1, "No hay partidas guardadas", ""));
            btnCargarPartida.setEnabled(false);
        } else {
            for (PartidaGuardada p : partidas) {
                comboPartidas.addItem(p);
            }
            btnCargarPartida.setEnabled(true);
        }
    }

    private void iniciarNuevaPartida() {
        dispose();
        new Cinematica(-1).iniciar();;
    }

    private void cargarPartidaSeleccionada() {
        PartidaGuardada seleccion = (PartidaGuardada) comboPartidas.getSelectedItem();

        if (seleccion != null && seleccion.getId() != -1) {
            dispose();
            int idCarga = seleccion.getId();
            new Cinematica(idCarga).iniciar();;
        } else {
            JOptionPane.showMessageDialog(this, "Por favor, selecciona una partida válida.", "Error de Carga", JOptionPane.WARNING_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MenuPrincipal());
    }
}
