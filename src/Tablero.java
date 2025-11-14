import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import javax.swing.*;

public class Tablero extends JPanel implements Runnable {
    private Image fondo;
    private Image suelo;
    private ArrayList<Rosa> rosas = new ArrayList<>();
    private ArrayList<Obstaculos> enemigos = new ArrayList<>();
    private ArrayList<Proyectil> proyectilesEnemigos = new ArrayList<>();
    private Personaje caballeroC;
    public static int contador = 0;
    private volatile boolean cinematicaTerminada = false;
    private boolean jefeActivo = false; 
    private int xFondo = 0;
    private int xSuelo = 0;
    private int velocidadY = 0;
    private final int gravedad = 2;
    private final int fuerzaSalto = -37;
    private boolean enAire = false;
    private boolean partidaCargada = false;
    private int xLimiteIzquierdo = 0; 
    private Sonido sonido = new Sonido();
    private Controles controles;
    private Image gifUI;
    private Random random = new Random();
    private Vida vida;
    private int ultimoXObstaculoMovil = 0;
    private int obstaculosMovilesConsecutivos = 0;
    private final int MAX_CONSECUTIVOS = 4;
    private final int ESPACIO_CONSECUTIVO = 350;
    private int ultimoXRosa = 0;
    private int ultimoXObstaculoFijo = 0;
    private final int DISTANCIA_MIN_OBSTACULO_MOVIL = 500; 
    private final int DISTANCIA_MIN_OBSTACULO_FIJO = 1200; 
    private final int ATAQUE_ANCHO = 50;
    private final int ATAQUE_ALTO = 50;
    private final int RANGO_ACTIVACION_ENEMIGO = 50;
    private final int DAGA_ANCHO = 90;
    private final int DAGA_ALTO = 30;
    private final String DAGA_SPRITE = "/img/daga.jpg"; 
    private Image fondoControles;
    private final int DAGA_VELOCIDAD = 9;
    private final int RANGO_LANZAMIENTO_ENREDADERA = 250;
    private final long INVULNERABILITY_DURATION = 1500;
    private long invulnerabilityEnd = 0;
    private long lastUpdateTime = 0;
    private boolean esInvulnerable = false;
    private boolean estaRalentizado = false;
    private final int VELOCIDAD_RALENTIZADA = 1;
    private final int VIDA_JEFE_BASE = 80; 
    private final int VIDA_JEFE_FINAL = 200; 
    private final int ALTO_GARGOLA = 200; 
    private final int ALTO_NEBLINA = 150; 
    private final int ALTO_CABALLERO = 220; 
    private final int ALTO_VAMPIRO = 250; 
    private final String GARGOLA_SPRITE = "/img/gargola.gif"; 
    private final String NEBLINA_SPRITE = "/img/neblina.gif"; 
    private final String CABALLERO_SPRITE = "/img/caballero_oscuro.gif"; 
    private final String VAMPIRO_SPRITE = "/img/vampiro.gif"; 
    // En tu clase Tablero, junto a tus otras variables de estado
    private Image imagenCastillo; // La imagen del castillo que acabamos de generar
    private boolean mostrandoCastillo = false;
    private boolean castilloTocado = false; // Nueva bandera para saber si el jugador "entró"
    private long tiempoInicioCastillo = 0;
    private final int DURACION_IMAGEN_CASTILLO = 4000; // Duración de la imagen fija (3 segundos)
    private boolean jefe9Derrotado = false;
    private final int SCROLL_CASTILLO_TRIGGER = 7000;  
    private boolean juegoIniciado = false;
    private int idPartidaACargar = -1;
    private PartidaDAO partidaDAO = new PartidaDAO();
    int x = getWidth() / 5;
    private boolean posicionForzada = false;

    public Tablero(int idPartidaACargar) {
        setBackground(Color.BLACK);
        setFocusable(true);

        try {
            fondoControles = new ImageIcon(getClass().getResource("/img/fondoControles.png")).getImage();
        } catch (Exception e) {
            System.err.println("Error cargando imagen de controles: " + e.getMessage());
        }

        vida = new Vida();
        sonido.loopSonido("juego");
        try {
            gifUI = new ImageIcon(getClass().getResource("/img/flor.gif")).getImage(); 
            fondo = new ImageIcon(getClass().getResource("/img/fondoPrincipal.png")).getImage();
            suelo = new ImageIcon(getClass().getResource("/img/suelo.png")).getImage();
            imagenCastillo = new ImageIcon(getClass().getResource("/img/imagen_castillo.png")).getImage(); 
            // Nombre de archivo sugerido: imagen_castillo.png
        } catch (Exception e) {
            System.err.println("ERROR cargando imágenes: " + e.getMessage());
        }

        controles = new Controles();
        addKeyListener(controles);

        this.idPartidaACargar = idPartidaACargar;
        if (idPartidaACargar != -1) {
            boolean ok = cargarEstadoDelJuego(idPartidaACargar);
            if (ok) {
                this.cinematicaTerminada = true;
                System.out.println("DEBUG: Carga detectada → NO mostrar cinemática.");
            }
        }
    }

    public void iniciarJuego() {

        boolean esPartidaNueva = (idPartidaACargar <= 0);

        if (caballeroC == null) {
            caballeroC = new Personaje(100, 100);
        }

        if (!esPartidaNueva) {
            if (cargarEstadoDelJuego(idPartidaACargar)) {
                System.out.println("Partida cargada con éxito.");
                this.enAire = true;
                this.velocidadY = 1;
                repaint();
            } else {
                System.err.println("Error al cargar partida. Iniciando nueva.");
                esPartidaNueva = true; 
            }
        }

        if (esPartidaNueva) {
            Tablero.contador = 0;
            vida.setVidaActual(3);
            caballeroC.setSaltosMaximos(1);
            caballeroC.setVelocidadBase(4);
            this.cinematicaTerminada = false;
            caballeroC.setX(100);
            caballeroC.setY(100);
            this.enAire = true;
            this.velocidadY = 1;
        }

        setPersonajeY(caballeroC);

        if (this.cinematicaTerminada) {
            System.out.println("DEBUG: iniciando juego sin cinemática (carga).");
            iniciarBucleJuego();
        } else {
            System.out.println("DEBUG: iniciando cinemática (nueva partida).");
            iniciarCinematica();
        }
    }

    public boolean cargarEstadoDelJuego(int idPartida) {

        int[] datos = partidaDAO.cargarPartida(idPartida);
        if (datos == null) return false;
        if (datos.length != 7) return false;

        if (caballeroC == null) {
            caballeroC = new Personaje(100, 100);
        }

        Tablero.contador           = datos[0];
        vida.setVidaActual         (datos[1]);
        caballeroC.setSaltosMaximos(datos[2]);
        caballeroC.setVelocidadBase(datos[3]);
        caballeroC.setX            (datos[4]);
        caballeroC.setY            (datos[5]);
        this.cinematicaTerminada = (datos[6] == 1);
        this.partidaCargada = true;

        System.out.println("Partida cargada: cinematicaTerminada=" + this.cinematicaTerminada);

        return true;
    }

    public boolean guardarEstadoDelJuego(String nombreSlot) {

        int rosas = Tablero.contador; 
        int vidaActual = vida.getVidaActual(); 
        int saltosMaximos = caballeroC.getSaltosMaximos(); 
        int velocidadBase = caballeroC.getVelocidadBase(); 
        int posX = caballeroC.getX(); 
        int posY = caballeroC.getY(); 
        int cinematicaInt = this.cinematicaTerminada ? 1 : 0; 

        if (partidaDAO.existeNombre(nombreSlot)) {
            int opcion = javax.swing.JOptionPane.showConfirmDialog(null,
                "Ya hay una partida con ese nombre, ¿Deseas sobrescribir?",
                "Confirmar",
                javax.swing.JOptionPane.YES_NO_OPTION);

            if (opcion == javax.swing.JOptionPane.NO_OPTION) {
                return false;
            }
        }

        return partidaDAO.guardarPartida(
            nombreSlot, 
            rosas, 
            vidaActual, 
            saltosMaximos, 
            velocidadBase,
            posX,
            posY,
            cinematicaInt 
        );
    }

    @Override
    public void run() {
        long ultimaActualizacion = System.nanoTime();
        final double FPS = 60.0;
        final double tiempoPorFrame = 1000000000 / FPS;
        double delta = 0;
        
        while (true) {
            long ahora = System.nanoTime();
            delta += (ahora - ultimaActualizacion) / tiempoPorFrame;
            ultimaActualizacion = ahora;
            
            if (delta >= 1) {
                actualizar();
                repaint();
                delta--;
            }
            
            try {
                Thread.sleep(1); 
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (mostrandoCastillo) {
        dibujarCastillo(g);
        return;
    }

        if (!cinematicaTerminada) {
            dibujarCinematica(g);
            return;
        }

        int altoSuelo = 20;
        int ySuelo = getHeight() - altoSuelo;

        g.drawImage(fondo, xFondo, 0, getWidth(), getHeight(), this);
        g.drawImage(fondo, xFondo + getWidth(), 0, getWidth(), getHeight(), this);

        g.drawImage(suelo, xSuelo, ySuelo, getWidth(), altoSuelo, this);
        g.drawImage(suelo, xSuelo + getWidth(), ySuelo, getWidth(), altoSuelo, this);

        g.drawImage(gifUI, 10, 10, 100, 100, this);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 50));
        g.drawString(String.valueOf(contador), 115, 80);

        if (vida != null) {
            final int X_VIDA = getWidth() - 260;
            final int Y_VIDA = 20;
            vida.dibujar(g, X_VIDA, Y_VIDA);
        }

        synchronized (enemigos) {
            for (Obstaculos o : enemigos) {
                o.dibujar(g);
                o.dibujarBarraVida(g);
            }
        }

        synchronized (proyectilesEnemigos) {
            for (Proyectil p : proyectilesEnemigos) {
                p.dibujar(g);
            }
        }

        if (caballeroC != null) {
            caballeroC.dibujar(g, this.esInvulnerable, this.lastUpdateTime, this);
        }

        synchronized (rosas) {
            for (Rosa r : rosas) r.dibujar(g);
        }
    }

    private void dibujarCinematica(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 60));
        g.setColor(Color.WHITE);
        if (!cinematicaTerminada) {
            mostrarPantallaControles(g);
            return;
        }
    }
    // **********************************************
// PASO 3.2: NUEVO MÉTODO PARA DIBUJAR EL CASTILLO
// **********************************************
private void dibujarCastillo(Graphics g) {
    // Rellena el fondo de negro (pausa del juego)
    g.setColor(Color.BLACK);
    g.fillRect(0, 0, getWidth(), getHeight());

    // NOTA: Usamos fondoCastilloImagen, que es la variable donde se cargó la imagen.
    if (imagenCastillo != null) { 
        // Centrar la imagen del castillo
        // Usamos getWidth(this) y getHeight(this) si la variable es de tipo Image (java.awt)
        int x = (getWidth() - imagenCastillo.getWidth(this)) / 2;
        int y = (getHeight() - imagenCastillo.getHeight(this)) / 2;

        g.drawImage(imagenCastillo, x, y, this);
    }
    
    // Controlamos el tiempo de la pausa visual dentro del método, 
    // pero la lógica principal de la cinemática sigue en actualizar().
    long tiempoTranscurrido = System.currentTimeMillis() - tiempoInicioCastillo;
    
    // Dibujamos al personaje SOLO si ha pasado la pausa visual y no ha entrado aún.
    if (caballeroC != null && !castilloTocado && tiempoTranscurrido > DURACION_IMAGEN_CASTILLO) {
        
        // Dibuja al personaje
        caballeroC.dibujar(g, this.esInvulnerable, this.lastUpdateTime, this);
        
        // Dibuja un mensaje para el jugador
        g.setColor(Color.RED);
        g.setFont(new Font("Arial", Font.BOLD, 30));
        g.drawString("¡Entra al castillo!", getWidth() / 2 - 120, getHeight() - 50);
    }
}

    private long tiempoControles = 6000;
    private long inicioControles;

private void mostrarPantallaControles(Graphics g) {
    if (inicioControles == 0) {
        inicioControles = System.currentTimeMillis(); // Solo se inicializa una vez
    }

    if (fondoControles != null) {
        g.drawImage(fondoControles, 0, 0, getWidth(), getHeight(), this);
    }

    g.setColor(Color.WHITE);
    g.setFont(new Font("Arial", Font.BOLD, 40));
    g.drawString("Controles:", getWidth() / 8, getHeight() / 4 - 40);

    g.setFont(new Font("Arial", Font.PLAIN, 30));
    int x = getWidth() / 8;
    int y = getHeight() / 2 - 140;

    g.drawString("Arriba: W", x, y);
    g.drawString("Izquierda: A", x, y + 40);
    g.drawString("Derecha: D", x, y + 80);
    g.drawString("Pegar: P", x, y + 120);

    // Esperar el tiempo completo antes de iniciar el juego
    if (System.currentTimeMillis() - inicioControles >= tiempoControles) {
        cinematicaTerminada = true;

        if (caballeroC != null) {
            int altoSuelo = 20;
            int ySuelo = getHeight() - altoSuelo;
            int alturaPersonaje = caballeroC.getAlto();
            caballeroC.setY(ySuelo - alturaPersonaje);
            enAire = false;
            velocidadY = 0;
        }

        iniciarBucleJuego(); // Se llama solo una vez después de los 6s
    }
}

public void actualizar() {
    
    // ------------------------------------------------------------------
    // LÓGICA DE ORDEN Y POSICIONAMIENTO INICIAL (EXISTENTE)
    // ------------------------------------------------------------------
    // NOTA: Este bloque se mueve aquí para asegurar que el caballero tenga posición antes de cualquier lógica.
    if (!partidaCargada && cinematicaTerminada && !posicionForzada) {
        if (caballeroC != null && getHeight() > 0) {
            final int altoSuelo = 20;
            int ySuelo = getHeight() - altoSuelo - caballeroC.getAlto();
            caballeroC.setY(ySuelo);
        }
        enAire = false;
        velocidadY = 0;
        posicionForzada = true;
    }

    if (caballeroC == null) return;
    
    // ------------------------------------------------------------------
    // PASO A: ACTIVACIÓN DE LA PANTALLA DEL CASTILLO (NUEVO)
    // Se activa cuando el jugador recoge la Rosa 9 (contador >= 9) y alcanza el límite de scroll.
    // ------------------------------------------------------------------
    if (contador >= 9 && !castilloTocado && !mostrandoCastillo) {
        
        final int LIMITE_SCROLL_CASTILLO = 6000; // Define el punto de scroll donde debe aparecer

        if (xLimiteIzquierdo >= LIMITE_SCROLL_CASTILLO) { 
            
            mostrandoCastillo = true;
            tiempoInicioCastillo = System.currentTimeMillis();
            posicionForzada = false; 
            
            try {
                // Cargar la imagen del CASTILLO (la pantalla estática que pausa el juego)
                imagenCastillo = new ImageIcon(getClass().getResource("/img/castillo.png")).getImage();
            } catch (Exception e) {
                System.err.println("ERROR cargando fondo del castillo: " + e.getMessage());
            }
            
            System.out.println("✅ PANTALLA DEL CASTILLO INICIADA.");
        }
    }

    // ------------------------------------------------------------------
    // PASO B: CONTROL DE LA PANTALLA DEL CASTILLO (NUEVO)
    // Este bloque maneja la pausa y la entrada al jefe final.
    // ------------------------------------------------------------------
    if (mostrandoCastillo) {
        final int alturaSuelo = 20; 
        long tiempoTranscurrido = System.currentTimeMillis() - tiempoInicioCastillo;
        
        // 1. Manejar el tiempo de la pausa inicial de la imagen
        if (tiempoTranscurrido <= DURACION_IMAGEN_CASTILLO) {
            return;
        }

        // 2. Forzar la posición del jugador al inicio del control de la pantalla
        if (!posicionForzada) {
            caballeroC.setX(getWidth() / 2 - 250); 
            caballeroC.setY(getHeight() - alturaSuelo - caballeroC.getAlto()); 
            
            caballeroC.setVelocidadX(0);
            caballeroC.setSaltando(false);
            
            xFondo = 0;
            xSuelo = 0;
            
            posicionForzada = true;
        }
        
        // 3. Lógica de movimiento, salto y gravedad básica (SIN SCROLL)
        
        // 3a. Actualizar estado y gravedad/salto
        caballeroC.actualizarEstado(controles); 

        if (enAire) {
            velocidadY += gravedad;
            caballeroC.setY(caballeroC.getY() + velocidadY);
        }

        int ySuelo = getHeight() - alturaSuelo - caballeroC.getAlto();
        if (caballeroC.getY() >= ySuelo) {
            caballeroC.setY(ySuelo);
            enAire = false;
            velocidadY = 0;
            caballeroC.resetearSaltos();
        }

        if (controles.isSaltando() && caballeroC.getSaltosDisponibles() > 0) {
            if (!enAire) enAire = true;
            velocidadY = fuerzaSalto;
            caballeroC.usarSalto();
            sonido.reproducirSonido("salto");
            controles.setSaltando(false);
        }

        // 3b. Movimiento horizontal (sin scroll)
        int velocidadBase = caballeroC.getVelocidadBase();
        int velocidadActual = estaRalentizado ? VELOCIDAD_RALENTIZADA : velocidadBase;

        int velocidadX = 0;
        if (controles.isMoviendoDerecha()) {
            velocidadX = velocidadActual;
        }
        if (controles.isMoviendoIzquierda()) {
            velocidadX = -velocidadActual;
        }

        caballeroC.setX(caballeroC.getX() + velocidadX);
        if (caballeroC.getX() < 0) caballeroC.setX(0);

        // 4. Detección de entrada al castillo (Colisión)
        int xPuerta = getWidth() / 2 - 100; 
        int anchoPuerta = 200; 

        Rectangle rectEntrada = new Rectangle(xPuerta, caballeroC.getY(), anchoPuerta, caballeroC.getAlto());
        
        if (caballeroC.getRect().intersects(rectEntrada)) {
            castilloTocado = true; 
            
            // Transición al ambiente final (¡Carga fondo5 y suelo5!)
            try {
                fondo = new ImageIcon(getClass().getResource("/img/fondo5.png")).getImage();
                suelo = new ImageIcon(getClass().getResource("/img/suelo5.png")).getImage();
                System.out.println("Transición al encuentro final: fondo5.png y suelo5.png");
            } catch (Exception e) {
                System.err.println("ERROR cargando imágenes de transición (5): " + e.getMessage());
            }

            generarJefe(VAMPIRO_SPRITE, ALTO_VAMPIRO, VIDA_JEFE_FINAL);
            // Generar la Rosa 10 (la rosa final)
            
            
            // Terminar la Pantalla del Castillo y activar el modo jefe
            mostrandoCastillo = false;
            jefeActivo = true; 
            posicionForzada = false; 
            
            // Posicionar al jugador en la nueva pantalla
            caballeroC.setX(50);
            caballeroC.setY(getHeight() - alturaSuelo - caballeroC.getAlto());
        }
        
        return; // Detiene el resto de la lógica del juego (NO-SCROLL)
    }
    // ------------------------------------------------------------------
    
    // ------------------------------------------------------------------
    // RESTO DE LA LÓGICA NORMAL DEL JUEGO (EXISTENTE)
    // ------------------------------------------------------------------

    if (enAire) {
        velocidadY += gravedad;
        caballeroC.setY(caballeroC.getY() + velocidadY);
    }

    int ySuelo = getHeight() - 20 - caballeroC.getAlto();
    if (caballeroC.getY() >= ySuelo) {
        caballeroC.setY(ySuelo);
        enAire = false;
        velocidadY = 0;
        caballeroC.resetearSaltos();
    }

    if (controles.isSaltando() && caballeroC.getSaltosDisponibles() > 0) {
        if (!enAire) enAire = true;
        velocidadY = fuerzaSalto;
        caballeroC.usarSalto();
        sonido.reproducirSonido("salto");
        controles.setSaltando(false);
    }

    long currentTime = System.currentTimeMillis();
    this.lastUpdateTime = currentTime;
    this.esInvulnerable = currentTime < invulnerabilityEnd;

    caballeroC.actualizarEstado(controles);

    int velocidadBase = caballeroC.getVelocidadBase();
    int velocidadActual = estaRalentizado ? VELOCIDAD_RALENTIZADA : velocidadBase;

    int velocidadX = 0;
    boolean moverFondo = false;
    int centro = getWidth() / 2;

    if (controles.isMoviendoDerecha()) {
        velocidadX = velocidadActual;
        moverFondo = true;
    }
    if (controles.isMoviendoIzquierda()) {
        velocidadX = -velocidadActual;
        moverFondo = true;
    }

    caballeroC.setX(caballeroC.getX() + velocidadX);
    if (caballeroC.getX() < 0) caballeroC.setX(0);

    if (moverFondo && velocidadX > 0 && caballeroC.getX() >= centro) {
        int scroll = velocidadX + 2;
        xFondo -= scroll;
        xSuelo -= scroll;

        for (Obstaculos o : enemigos) o.setX(o.getX() - scroll);
        for (Rosa r : rosas) r.setX(r.getX() - scroll);
        for (Proyectil p : proyectilesEnemigos) p.setX(p.getX() - scroll);

        caballeroC.setX(centro);
        ultimoXObstaculoMovil -= scroll;
        ultimoXObstaculoFijo -= scroll;
        ultimoXRosa -= scroll;
        xLimiteIzquierdo += scroll;
    }

    if (moverFondo && velocidadX < 0 && caballeroC.getX() <= centro) {
        if (xLimiteIzquierdo > 0) {
            int intentoScroll = velocidadActual + 2;
            int scroll = Math.min(xLimiteIzquierdo, intentoScroll);

            xFondo += scroll;
            xSuelo += scroll;

            for (Rosa r : rosas) r.setX(r.getX() + scroll);
            for (Proyectil p : proyectilesEnemigos) p.setX(p.getX() + scroll);

            caballeroC.setX(centro);

            ultimoXObstaculoMovil += scroll;
            ultimoXObstaculoFijo += scroll;
            ultimoXRosa += scroll;
            xLimiteIzquierdo -= scroll;

            if (xLimiteIzquierdo < 0) xLimiteIzquierdo = 0;
        }
    }

    if (xLimiteIzquierdo < 0) xLimiteIzquierdo = 0;

    moverObstaculosMoviles();

    for (Obstaculos o : enemigos) o.actualizarEstado();

    moverProyectiles();
    generarProyectilesEnemigos();

    if (controles.isAtacando()) {
        caballeroC.setAtacando(true);
        manejarAtaque();
    } else {
        caballeroC.setAtacando(false);
    }

    if (xFondo <= -getWidth()) xFondo += getWidth();
    if (xFondo >= getWidth()) xFondo -= getWidth();
    if (xSuelo <= -getWidth()) xSuelo += getWidth();
    if (xSuelo >= getWidth()) xSuelo -= getWidth();

    synchronized (enemigos) {
        generarElementos();
        enemigos.removeIf(o -> !o.isActivo());
    }
    synchronized (proyectilesEnemigos) {
        proyectilesEnemigos.removeIf(p -> !p.isActivo());
    }
    synchronized (rosas) {
        actualizarRosas();
    }
}

/*private void limpiarElementos() {
    enemigos.removeIf(o -> o.getX() + o.getAncho() < 0);
    rosas.removeIf(r -> r.getX() + 80 < 0);
} */

private void generarElementos() {
    generarObstaculosMoviles();
    generarObstaculosInmoviles();
    generarRosas();
}

   private void generarJefe(String sprite, int alto, int vida) {
    boolean jefeExiste = enemigos.stream().anyMatch(o -> o.getNombreImagen().equals(sprite));
    if (jefeExiste) return;

    enemigos.clear();
    jefeActivo = true;

    final int ALTO_SUELO = 20;
    int yJefe = getHeight() - alto - ALTO_SUELO;
    int xJefe = getWidth();

    enemigos.add(new Obstaculos(xJefe, yJefe, sprite, vida, false));
    System.out.println("¡JEFE GENERADO! " + sprite + " (Rosa #" + contador + ") Vida: " + vida);

    ultimoXObstaculoFijo = Integer.MAX_VALUE;
    ultimoXObstaculoMovil = Integer.MAX_VALUE;
}

private void generarRosas() {
    // 1. CONDICIONES DE SALIDA (igual que tenías)
    if (jefeActivo) return;
    if (contador >= 9) return; 
    if (rosas.size() >= 3) return;

    final int DISTANCIA_MIN_ROSA = 1000;

    // Evitar generar al inicio cuando el ancho aún no es estable
    if (getWidth() < 100) return;

    // Si la lista está vacía, espera a que el scroll avance un poco
    if (rosas.isEmpty() && xLimiteIzquierdo < 50) return;

    // Si ya hay rosas generadas, respeta la distancia mínima entre ellas (en absoluto)
    if (!rosas.isEmpty() &&
        ultimoXRosa < xLimiteIzquierdo + getWidth() + DISTANCIA_MIN_ROSA) {
        return;
    }

    // Probabilidad de generación (mantén tu probabilidad de pruebas)
    if (random.nextInt(20) < 1) {
        final int ALTO_SUELO = 20;
        int yRosa = getHeight() - Rosa.ALTO - ALTO_SUELO;
        int xRosa = getWidth();

        // NO incrementar contador aquí. La rosa nace SIN número asignado (-1).
        Rosa nueva = new Rosa(xRosa, yRosa, -1);
        rosas.add(nueva);

        // Guarda la X absoluta donde se generó (como tenías)
        ultimoXRosa = xLimiteIzquierdo + getWidth();

        System.out.println("🌹 Rosa generada (sin número) | X absoluto: " + ultimoXRosa +
                           " | rosas en pantalla: " + rosas.size());
    }
}

private void actualizarRosas() {
    Iterator<Rosa> it = rosas.iterator();
    while (it.hasNext()) {
        Rosa r = it.next();

        // Restringir chequeo de colisión a rosas cercanas o dentro de pantalla
        final int MARGEN_COLISION_DERECHA = 200;

        if (r.getX() < getWidth() + MARGEN_COLISION_DERECHA) {

            // Colisión con el caballero
            if (caballeroC != null && caballeroC.getHitbox().intersects(r.getRect())) {

                // Si la rosa NO tiene número asignado, ahora sí se asigna al recoger
                int nro = r.getNumero();
                if (nro <= 0) {                // -1 o 0 = sin asignar
                    contador++;
                    r.setNumero(contador);
                    nro = r.getNumero();
                    System.out.println("🌹 Rosa recogida y numerada como #" + nro);
                } else {
                    System.out.println("🌹 Rosa numerada encontrada: #" + nro);
                }

                // Si es una rosa coleccionable normal (1..9)
                if (nro > 0 && nro < 10) {
                    // Lógica de recolección que ya tenías
                    caballeroC.recogerRosa();

                    // Mantengo la activación de jefes según el número real
                    switch (nro) {
                        case 3:
                            generarJefe(GARGOLA_SPRITE, ALTO_GARGOLA, VIDA_JEFE_BASE);
                            break;
                        case 6:
                            generarJefe(NEBLINA_SPRITE, ALTO_NEBLINA, VIDA_JEFE_BASE);
                            break;
                        case 9:
                            generarJefe(CABALLERO_SPRITE, ALTO_CABALLERO, VIDA_JEFE_BASE);
                            break;
                    }

                    // Se elimina la rosa porque fue recogida
                    it.remove();
                    continue;
                }

                // Si es la rosa jefe (10) -> comportamiento original: no eliminar por colisión
                if (nro == 10) {
                    System.out.println("⚠️ Rosa jefe (10) tocada — manejo de daño en otra parte.");
                    // No la removemos aquí (como en tu diseño original).
                    // Salimos del método para conservar el comportamiento previo
                    return;
                }
            }
        } // fin del chequeo de proximidad

        // Eliminación por scroll si sale completamente de pantalla (solo coleccionables)
        if (r.getNumero() < 10 && r.getX() + r.getAncho() < 0) {
            System.out.println("🗑 Rosa fuera de pantalla eliminada (num=" + r.getNumero() + ")");
            it.remove();
        }
    } // fin while
    // Nota: NO reseteamos ultimoXRosa aquí: lo manejas en generarRosas()
}

private void manejarAtaque() {
    int ataqueX = caballeroC.getX() + (caballeroC.isMirandoDerecha() ? caballeroC.getAncho() - 10 : -ATAQUE_ANCHO + 10);
    int ataqueY = caballeroC.getY() + (caballeroC.getAlto() / 2) - (ATAQUE_ALTO / 2);

    Rectangle ataqueHitbox = new Rectangle(ataqueX, ataqueY, ATAQUE_ANCHO, ATAQUE_ALTO);

    Iterator<Obstaculos> it = enemigos.iterator();
    while (it.hasNext()) {
        Obstaculos o = it.next();
        String nombreSprite = o.getNombreImagen();

        Rectangle rectColisionAtaque = o.getRect();
        boolean esEnredaderaOPlanta = nombreSprite.contains("enredaderas") || nombreSprite.contains("plantas_carnivoras");

        if (esEnredaderaOPlanta) {
            final int expansion = RANGO_ACTIVACION_ENEMIGO;
            rectColisionAtaque = new Rectangle(
                o.getX() - expansion, o.getY(),
                o.getAncho() + (2 * expansion), o.getAlto()
            );
        }

        if (ataqueHitbox.intersects(rectColisionAtaque)) {
            if (o.tieneVida()) {
                o.recibirDano(1);
                sonido.reproducirSonido("golpe");

                if (o.estaDestruido()) {
                    // --- LÓGICA DE JEFES Y RECOMPENSAS ---
                    
                    // LÓGICA ESPECIAL PARA EL JEFE DEL CONTADOR 9 (Caballero Oscuro)
                    if (nombreSprite.contains(CABALLERO_SPRITE) && contador == 9) {
                        
                        it.remove(); // Se remueve el Caballero Oscuro inmediatamente
                        jefeActivo = true; // Mantiene la generación de obstáculos y rosas desactivada
                        
                        // **********************************************
                        // PASO 2: ACTIVACIÓN DE LA IMAGEN DEL CASTILLO
                        // **********************************************
                        mostrandoCastillo = true; 
                        tiempoInicioCastillo = System.currentTimeMillis(); 
                        posicionForzada = false; // Permite forzar la posición del jugador delante del castillo
                        
                        System.out.println("Jefe 9 Derrotado. Mostrando imagen del Castillo...");
                        return; // Detiene la iteración y el método para evitar errores de concurrencia y reanudación prematura
                    } 
                    
                    // Lógica para Jefes 3 y 6
                    if (nombreSprite.contains(GARGOLA_SPRITE)) {
                        caballeroC.desbloquearDobleSalto(); 
                        if (contador == 3) { 
                            try {
                                fondo = new ImageIcon(getClass().getResource("/img/fondo2.png")).getImage();
                                suelo = new ImageIcon(getClass().getResource("/img/suelo2.png")).getImage(); 
                                System.out.println("FONDO Y SUELO CAMBIADOS: fondo2.png y suelo2.png");
                            } catch (Exception e) {
                                System.err.println("ERROR cargando imágenes de transición (2): " + e.getMessage());
                            }
                        }
                    } else if (nombreSprite.contains(NEBLINA_SPRITE)) {
                        caballeroC.aumentarVelocidad(1); 
                        if (contador == 6) { 
                            try {
                                fondo = new ImageIcon(getClass().getResource("/img/fondo3.png")).getImage();
                                suelo = new ImageIcon(getClass().getResource("/img/suelo3.png")).getImage(); 
                                System.out.println("FONDO Y SUELO CAMBIADOS: fondo3.png y suelo3.png");
                            } catch (Exception e) {
                                System.err.println("ERROR cargando imágenes de transición (3): " + e.getMessage());
                            }
                        }
                    }
                    
                    // El cambio de ambiente a fondo4/suelo4 se ejecutará si el jefe 9 (Caballero Oscuro) no tiene sprite y se basa solo en el contador. 
                    // Ya que arriba pusimos una lógica específica para CABALLERO_SPRITE, esta lógica de fondo4/suelo4 podría moverse o depender solo del contador, según tu diseño:
                     if (contador == 9 && !nombreSprite.contains(GARGOLA_SPRITE) && !nombreSprite.contains(NEBLINA_SPRITE)) {
                         try {
                             fondo = new ImageIcon(getClass().getResource("/img/fondo4.png")).getImage();
                             suelo = new ImageIcon(getClass().getResource("/img/suelo4.png")).getImage(); 
                             System.out.println("FONDO Y SUELO CAMBIADOS: fondo4.png y suelo4.png");
                         } catch (Exception e) {
                             System.err.println("ERROR cargando imágenes de transición (4): " + e.getMessage());
                         }
                     }
                    
                    // --- FIN LÓGICA DE JEFES Y RECOMPENSAS ---

                    it.remove(); // Remueve el obstáculo/jefe

                    if (jefeActivo && enemigos.isEmpty()) {
                        jefeActivo = false;
                        ultimoXObstaculoFijo = getWidth() + 10;
                        ultimoXObstaculoMovil = getWidth() + 10;
                        System.out.println("JEFE DERROTADO. Generación de obstáculos y rosas reanudada.");
                    }
                }
            }
        }
    }
}

private void moverObstaculosMoviles() {
    // Solo se mueven los que son móviles
    for (Obstaculos o : enemigos) {
        if (o.esMovil()) {
            o.mover(); // este método desplaza al obstáculo automáticamente
        }
    }
}

private void generarObstaculosMoviles() {
    if (jefeActivo) return;
    if (ultimoXObstaculoMovil >= getWidth() + DISTANCIA_MIN_OBSTACULO_MOVIL) return;
    if (ultimoXObstaculoMovil < getWidth()) ultimoXObstaculoMovil = getWidth() + 10;

    if (obstaculosMovilesConsecutivos >= MAX_CONSECUTIVOS) {
        ultimoXObstaculoMovil += DISTANCIA_MIN_OBSTACULO_MOVIL;
        obstaculosMovilesConsecutivos = 0;
        return;
    }

    boolean debeGenerar = obstaculosMovilesConsecutivos > 0 || random.nextInt(100) < 80; // 80% de probabilidad

    if (debeGenerar) {
        int xPosicion = ultimoXObstaculoMovil + ESPACIO_CONSECUTIVO;

        // Crear obstáculo móvil
        Obstaculos obst = new Obstaculos(xPosicion, 0, "/img/obstaculo.gif", true);

        // Colocar a nivel del suelo según su alto
        obst.setY(getHeight() - 20 - obst.getAlto());

        enemigos.add(obst);
        ultimoXObstaculoMovil = xPosicion;
        obstaculosMovilesConsecutivos++;
    }
}

private void generarObstaculosInmoviles() {
    if (jefeActivo) return;

    // Solo generar si el último obstáculo está suficientemente lejos
    if (getWidth() - ultimoXObstaculoFijo < DISTANCIA_MIN_OBSTACULO_FIJO) return;

    // Probabilidad de generar un obstáculo fijo
    if (random.nextInt(100) < 3) { // 3% por actualización
        int xPosicion = getWidth() + 50; // Posición inicial fuera de pantalla
        String spriteElegido = elegirObstaculoFijo();

        Obstaculos obst;
        if (spriteElegido.contains("plantas_carnivoras")) {
            obst = new Obstaculos(xPosicion, 0, spriteElegido, false); // Obstáculo fijo sin vida
        } else {
            obst = new Obstaculos(xPosicion, 0, spriteElegido, 50, false); // Obstáculo fijo destructible
        }

        // Colocar a nivel del suelo
        obst.setY(getHeight() - 20 - obst.getAlto());

        enemigos.add(obst);

        // Actualizar la posición del último obstáculo generado
        ultimoXObstaculoFijo = xPosicion + obst.getAncho() + 50; // Aumenta espacio extra entre obstáculos
    }
}



private String elegirObstaculoFijo() {
    int eleccion = random.nextInt(3);
    switch (eleccion) {
        case 0: return "/img/plantas_carnivoras.gif";
        case 1: return "/img/enredaderas.gif";
        case 2: return "/img/arboles.gif";
        default: return "/img/plantas_carnivoras.gif";
    }
}

private void moverProyectiles() {
    final int anchoPanel = getWidth();
    final int altoPanel = getHeight();
    for (Proyectil p : proyectilesEnemigos) {
        p.mover(anchoPanel, altoPanel);
    }
}

private void generarProyectilesEnemigos() {
    for (Obstaculos obs : enemigos) {
        String nombreSprite = obs.getNombreImagen();
        if (nombreSprite.contains("enredaderas")) {
            final int expansion = RANGO_LANZAMIENTO_ENREDADERA;
            Rectangle rangoActivacion = new Rectangle(
                obs.getX() - expansion, obs.getY() - 100,
                obs.getAncho() + (2 * expansion), obs.getAlto() + 100
            );

            if (caballeroC.getHitbox().intersects(rangoActivacion) && obs.puedeAtacar()) {
                obs.iniciarAtaqueAnimacion();
                int centroObsX = obs.getX() + obs.getAncho() / 2;
                int dirX = (caballeroC.getX() < centroObsX) ? -DAGA_VELOCIDAD : DAGA_VELOCIDAD;

                int xInicial = centroObsX - DAGA_ANCHO / 2;
                int yInicial = obs.getY() + obs.getAlto() / 2 - DAGA_ALTO / 2;

                Proyectil daga = new Proyectil(
                    xInicial, yInicial, DAGA_SPRITE,
                    dirX, 0, DAGA_ANCHO, DAGA_ALTO
                );

                proyectilesEnemigos.add(daga);
                obs.iniciarCooldown();
            }
        }
    }
}

private void manejarColisionesDeProyectiles() {
    long currentTime = System.currentTimeMillis();
    boolean personajeEsInvulnerable = currentTime < invulnerabilityEnd;

    Iterator<Proyectil> it = proyectilesEnemigos.iterator();
    while (it.hasNext()) {
        Proyectil p = it.next();
        if (p.isActivo() && p.getHitbox().intersects(caballeroC.getHitbox()) && !personajeEsInvulnerable) {
            vida.quitarVida();
            sonido.reproducirSonido("golpe");
            invulnerabilityEnd = currentTime + INVULNERABILITY_DURATION;
            p.setActivo(false);

            if (vida.estaMuerto()) {
                sonido.reproducirSonido("risa");
                JOptionPane.showMessageDialog(this, "💀 GAME OVER");
                System.exit(0);
            }
        }
    }
}

private void manejarColisionesDePersonaje() {
    long currentTime = System.currentTimeMillis();
    boolean personajeEsInvulnerable = currentTime < invulnerabilityEnd;
    estaRalentizado = false;

    Iterator<Obstaculos> it = enemigos.iterator();
    while (it.hasNext()) {
        Obstaculos obs = it.next();
        String nombreSprite = obs.getNombreImagen();
        Rectangle hitboxPersonaje = caballeroC.getHitbox();
        Rectangle rectColision = obs.getRect();

        if (obs.estaDestruido()) continue;
        //if (nombreSprite.contains("enredaderas")) continue;

        if (nombreSprite.contains("arboles") || nombreSprite.contains("plantas_carnivoras")) {
            final int expansion = RANGO_ACTIVACION_ENEMIGO;
            Rectangle rangoEfecto = new Rectangle(
                //obs.getX() - expansion, obs.getY(),
               // obs.getAncho() + (2 * expansion), obs.getAlto()
            );

            if (hitboxPersonaje.intersects(rangoEfecto)) {
                estaRalentizado = true;
            }

            if (hitboxPersonaje.intersects(rectColision)) {
                if (nombreSprite.contains("plantas_carnivoras") && obs.puedeAtacar()) {
                    obs.iniciarAtaqueAnimacion();
                    obs.iniciarCooldown();
                }
            }
        } else {
            if (hitboxPersonaje.intersects(obs.getRect()) && !personajeEsInvulnerable) {
               // vida.quitarVida();
                sonido.reproducirSonido("golpe");
                invulnerabilityEnd = currentTime + INVULNERABILITY_DURATION;

                if (vida.estaMuerto()) {
                    sonido.reproducirSonido("risa");
                    JOptionPane.showMessageDialog(this, "💀 GAME OVER");
                    System.exit(0);
                }
            }
        }
    }

    if (caballeroC != null) caballeroC.setEstaRalentizado(estaRalentizado);
}

public void setPersonajeY(Personaje c) {
    if (this.caballeroC == null) this.caballeroC = c;

    if (this.caballeroC != null) {
        final int sueloY = getHeight() - caballeroC.getAlto() - 20;
        if (getHeight() > 0 && this.caballeroC.getY() < sueloY - 10)
            this.caballeroC.setY(sueloY);

        this.enAire = false;
        this.velocidadY = 0;
    }
}

public void setPersonaje(Personaje c) {
    this.caballeroC = c;
    if (this.caballeroC != null) {
        if (getHeight() > 0) {
            final int sueloY = getHeight() - caballeroC.getAlto() - 20;
            this.caballeroC.setY(sueloY);
        }
        this.enAire = false;
        this.velocidadY = 0;
    }
}

public Personaje getJugador() {
    return this.caballeroC;
}

private void iniciarBucleJuego() {
    if (this.juegoIniciado) {
        System.out.println("ADVERTENCIA: El bucle de juego ya está activo. Evitando doble inicio.");
        return;
    }

    this.juegoIniciado = true;
    Thread gameThread = new Thread(this, "GameLoopThread");
    try {
        gameThread.start();
        System.out.println("DEBUG: Hilo principal del juego iniciado correctamente.");
    } catch (Exception e) {
        System.err.println("ERROR al iniciar el hilo del juego:");
        e.printStackTrace();
        this.juegoIniciado = false;
    }
}

private void iniciarCinematica() {
    if (this.partidaCargada) {
        System.out.println("Partida cargada -> se omite cinemática.");
        this.cinematicaTerminada = true;
        if (!this.juegoIniciado) iniciarBucleJuego();
        return;
    }

    System.out.println("Iniciando cinemática de nueva partida...");
    Timer t = new Timer(3000, e -> {
        System.out.println("Cinemática finalizada -> iniciando juego");
        this.cinematicaTerminada = true;
        if (!this.juegoIniciado) iniciarBucleJuego();
    });
    t.setRepeats(false);
    t.start();
}

public int getVelocidadBase() {
    if (caballeroC != null) return caballeroC.getVelocidadBase();
    return 4;
}

public Vida getVidaObjeto() {
    return this.vida;
}

public int getRosasRecogidas() {
    return Tablero.contador;
    }
}