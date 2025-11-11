import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import javax.swing.*;

// 🌟 ADICIÓN: Implementa Runnable para el game loop
public class Tablero extends JPanel implements Runnable {

    private Image fondo;
    private Image suelo;
    private ArrayList<Rosa> rosas = new ArrayList<>();
    private ArrayList<Obstaculos> enemigos = new ArrayList<>();
    private ArrayList<Proyectil> proyectilesEnemigos = new ArrayList<>();
    private Personaje caballeroC;
    public static int contador = 0; // Máximo de 10 rosas recogidas

    // ✅ VARIABLE DE ESTADO DEL JEFE
    private boolean jefeActivo = false; 

    private int xFondo = 0;
    private int xSuelo = 0;
    private boolean cinematicaTerminada = false; // Asume 'false' al iniciar nueva partida
    private int velocidadY = 0;
    private final int gravedad = 2;
    private final int fuerzaSalto = -26;
    private boolean enAire = false;
    
    private int xLimiteIzquierdo = 0; 

    private Sonido sonido = new Sonido();
    private Controles controles;
    private Image gifUI;
    private Random random = new Random();
    
    private Vida vida;
    
    private static final int ALTO_POR_DEFECTO = 175; 
    
    private int ultimoXObstaculoMovil = 0;
    private int obstaculosMovilesConsecutivos = 0;
    private final int MAX_CONSECUTIVOS = 4;
    private final int ESPACIO_CONSECUTIVO = 150;
    
    private int ultimoXRosa = 0;
    private int ultimoXObstaculoFijo = 0;
    private int obstaculosFijosConsecutivos = 0;

    private final int DISTANCIA_MIN_OBSTACULO_MOVIL = 500; 
    private final int DISTANCIA_MIN_ROSA = 3000; // Separación para las rosas
    private final int DISTANCIA_MIN_OBSTACULO_FIJO = 2000; 
    
    // --- Constantes de Combate y Proyectiles ---
    private final int ATAQUE_ANCHO = 50;
    private final int ATAQUE_ALTO = 50;
    private final int RANGO_ACTIVACION_ENEMIGO = 50;
    private final int DAGA_ANCHO = 90;
    private final int DAGA_ALTO = 30;
    private final String DAGA_SPRITE = "/img/daga.jpg"; 
    private final int DAGA_VELOCIDAD = 9;
    private final int RANGO_LANZAMIENTO_ENREDADERA = 250;
    private final long INVULNERABILITY_DURATION = 1500;
    private long invulnerabilityEnd = 0;
    private long lastUpdateTime = 0;
    private boolean esInvulnerable = false;
    private boolean estaRalentizado = false;
    
    // ➡️ VELOCIDADES BASE (Ahora el personaje gestiona la velocidad base)
    private final int VELOCIDAD_RALENTIZADA = 1; // El mínimo de movimiento
    
    // ✅ Constantes de Jefes
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
    
    // ----------------------------------------------------------------------
    // 🌟 ADICIÓN: VARIABLES PARA CARGA DE PARTIDA
    // ----------------------------------------------------------------------
    private int idPartidaACargar = -1; // -1 significa iniciar nueva partida
    private PartidaDAO partidaDAO = new PartidaDAO(); // Instancia del DAO

    // ----------------------------------------------------------------------
    // 1. CONSTRUCTOR
    // ----------------------------------------------------------------------
    public Tablero(int idPartidaACargar) {
        setBackground(Color.BLACK);
        setFocusable(true);
        
        // Inicializa componentes
        vida = new Vida();
        sonido.loopSonido("juego");
        try {
            gifUI = new ImageIcon(getClass().getResource("/img/flor.gif")).getImage(); 
            fondo = new ImageIcon(getClass().getResource("/img/fondoPrincipal.png")).getImage();
            suelo = new ImageIcon(getClass().getResource("/img/suelo.png")).getImage();
        } catch (Exception e) {
            System.err.println("ERROR cargando imágenes: " + e.getMessage());
        }
        
        // Inicialización básica del Personaje.
        caballeroC = new Personaje(100, 100); 
        enAire = true;
        velocidadY = 1;

        controles = new Controles();
        addKeyListener(controles);

        this.idPartidaACargar = idPartidaACargar; // Guarda el ID en la variable de instancia
    }

    // ----------------------------------------------------------------------
    // 2. MÉTODO INICIAR JUEGO (Lógica de Nueva Partida vs. Carga)
    // ----------------------------------------------------------------------
    public void iniciarJuego() {
    
        if (caballeroC == null) {
            caballeroC = new Personaje(100, 100);
        }
        
        boolean esPartidaNueva = (idPartidaACargar <= 0);
        
        if (!esPartidaNueva) {
            // Intenta cargar los datos de la base de datos
            if (cargarEstadoDelJuego(idPartidaACargar)) {
                System.out.println("Partida cargada con éxito.");
            } else {
                // La carga falló, iniciar nueva partida
                System.err.println("Carga de partida fallida. Reiniciando estados.");
                esPartidaNueva = true;
            }
        }
        
        if (esPartidaNueva) {
            // Reinicializa/Establece estados clave para una PARTIDA NUEVA
            Tablero.contador = 0;
            vida.setVidaActual(3); 
            caballeroC.setSaltosMaximos(1); 
            caballeroC.setVelocidadBase(4); 
            this.cinematicaTerminada = false;
            
            // Fija la posición inicial (X y Y) del personaje SOLO si es partida nueva
            caballeroC.setX(100);
            caballeroC.setY(100); 
        }
        
        // Aplicar la Posición Y (ajusta a suelo si es necesario, respeta Y cargada)
        setPersonajeY(caballeroC); 

        // Iniciar el game loop
        Thread gameThread = new Thread(this);
        gameThread.start();
    }
    
    // ----------------------------------------------------------------------
    // 3. LÓGICA DE CARGA Y GUARDADO (CORREGIDA)
    // ----------------------------------------------------------------------
    
    /**
     * 💾 Carga los datos de la base de datos y restaura el estado del juego.
     * @param idPartida El ID de la fila a cargar.
     * @return true si la carga fue exitosa.
     */
    public boolean cargarEstadoDelJuego(int idPartida) {
        // CORRECCIÓN: PartidaDAO.cargarPartida devuelve 7 datos
        int[] datosCargados = partidaDAO.cargarPartida(idPartida);

        if (datosCargados != null && datosCargados.length == 7) {
            // Orden de los datos: [rosas, vida, saltos, velocidad, posX, posY, cinematica]
            
            Tablero.contador = datosCargados[0];        // 0. ROSA_CONTADOR
            vida.setVidaActual(datosCargados[1]);       // 1. VIDA_ACTUAL
            caballeroC.setSaltosMaximos(datosCargados[2]); // 2. SALTO_MAXIMO
            caballeroC.setVelocidadBase(datosCargados[3]); // 3. VELOCIDAD_BASE
            caballeroC.setX(datosCargados[4]);          // 4. POS_X
            caballeroC.setY(datosCargados[5]);          // 5. POS_Y
            
            int cinematicaInt = datosCargados[6];       // 6. CINEMATICA_TERMINADA
            this.cinematicaTerminada = (cinematicaInt == 1);
            
            return true;
        } else {
            System.err.println("Error al cargar datos para el ID: " + idPartida + ". Formato incorrecto.");
            return false;
        }
    }

    
    /**
     * 💾 Guarda el estado actual del juego.
     * @param nombreSlot El nombre para identificar la partida guardada.
     * @return true si el guardado fue exitoso.
     */
    public boolean guardarEstadoDelJuego(String nombreSlot) {
        
        // Obtener los 8 datos del estado actual
        int rosas = Tablero.contador; 
        int vidaActual = vida.getVidaActual(); 
        int saltosMaximos = caballeroC.getSaltosMaximos(); 
        int velocidadBase = caballeroC.getVelocidadBase(); 
        
        // CORRECCIÓN: Se añaden los 3 parámetros faltantes (POS_X, POS_Y, CINEMATICA)
        int posX = caballeroC.getX(); 
        int posY = caballeroC.getY(); 
        int cinematicaInt = this.cinematicaTerminada ? 1 : 0; 
        
        // Usar el DAO para insertar/actualizar (8 argumentos)
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

    // ----------------------------------------------------------------------
    // 4. IMPLEMENTACIÓN DEL BUCLE DE JUEGO (RUNNABLE)
    // ----------------------------------------------------------------------
    @Override
    public void run() {
        // Lógica de bucle de juego (Game Loop)
        long ultimaActualizacion = System.nanoTime();
        final double FPS = 60.0;
        final double tiempoPorFrame = 1000000000 / FPS;
        double delta = 0;
        
        while (true) {
            long ahora = System.nanoTime();
            delta += (ahora - ultimaActualizacion) / tiempoPorFrame;
            ultimaActualizacion = ahora;
            
            if (delta >= 1) {
                actualizar(); // Llama a tu método existente
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


    // ----------------------------------------------------------------------
    // 5. PAINT COMPONENT
    // ----------------------------------------------------------------------
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int altoSuelo = 20;
        int ySuelo = getHeight() - altoSuelo;

        // Dibujar Fondo (Paralaje)
        g.drawImage(fondo, xFondo, 0, getWidth(), getHeight(), this);
        g.drawImage(fondo, xFondo + getWidth(), 0, getWidth(), getHeight(), this);

        // Dibujar Suelo (Paralaje)
        g.drawImage(suelo, xSuelo, ySuelo, getWidth(), altoSuelo, this);
        g.drawImage(suelo, xSuelo + getWidth(), ySuelo, getWidth(), altoSuelo, this);

        // Dibujar HUD (Elemento fijo, no afectado por scroll)
        g.drawImage(gifUI, 10, 10, 100, 100, this);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 50));
        g.drawString(String.valueOf(contador), 115, 80);
        
        if (vida != null) {
            final int X_VIDA = getWidth() - 260;
            final int Y_VIDA = 20;
            vida.dibujar(g, X_VIDA, Y_VIDA);
        }

        // Dibuja enemigos
        synchronized (enemigos) {
            for (Obstaculos o : enemigos) {
                o.dibujar(g);
                o.dibujarBarraVida(g); 
            }
        }
        
        // Dibuja proyectiles
        synchronized (proyectilesEnemigos) {
            for (Proyectil p : proyectilesEnemigos) {
                p.dibujar(g);
            }
        }
        
        if (caballeroC != null) {
            caballeroC.dibujar(g, this.esInvulnerable, this.lastUpdateTime, this);
        }
        
        // Dibuja rosas
        synchronized (rosas) {
            for (Rosa r : rosas) r.dibujar(g);
        }
    }
    
    // -------------------------------------------------------------------------
    // 6. LÓGICA DE JUEGO PRINCIPAL (ACTUALIZAR)
    // -------------------------------------------------------------------------

    public void actualizar() {
        if (caballeroC == null) return;
        
        // --- LÓGICA DE FÍSICA Y SALTO ---
        if (enAire) {
            velocidadY += gravedad;
            caballeroC.setY(caballeroC.getY() + velocidadY);
        }

        int ySuelo = getHeight() - 20 - caballeroC.getAlto();
        if (caballeroC.getY() >= ySuelo) {
            caballeroC.setY(ySuelo);
            enAire = false;
            velocidadY = 0;
            caballeroC.resetearSaltos(); // 🌟 Reinicia saltos al tocar el suelo
        }

        // 👑 LÓGICA DEL DOBLE SALTO 👑
        if (controles.isSaltando() && caballeroC.getSaltosDisponibles() > 0) {
            
            if (!enAire) {
                enAire = true;
            } 
            
            velocidadY = fuerzaSalto;
            caballeroC.usarSalto(); // Consume un salto
            sonido.reproducirSonido("salto");
            controles.setSaltando(false);
        }
        // --------------------------------

        long currentTime = System.currentTimeMillis();
        this.lastUpdateTime = currentTime;
        this.esInvulnerable = currentTime < invulnerabilityEnd;
        
        caballeroC.actualizarEstado(controles);
        
        // 🌟 LÓGICA DE MOVIMIENTO HORIZONTAL Y SCROLL 
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
        
        // 1. Aplicar movimiento horizontal del personaje (antes del scroll)
        caballeroC.setX(caballeroC.getX() + velocidadX);
        
        // 2. No salir del borde
        if (caballeroC.getX() < 0) caballeroC.setX(0);

        // --- SCROLL DERECHA ---
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

        // --- SCROLL IZQUIERDA ---
        if (moverFondo && velocidadX < 0 && caballeroC.getX() <= centro) {
            
            if (xFondo < 0) { 
                int scroll = -(velocidadX - 2); 
                
                xFondo += scroll;
                xSuelo += scroll;
                
                for (Obstaculos o : enemigos) o.setX(o.getX() + scroll);
                for (Rosa r : rosas) r.setX(r.getX() + scroll);
                for (Proyectil p : proyectilesEnemigos) p.setX(p.getX() + scroll);

                caballeroC.setX(centro);
                
                ultimoXObstaculoMovil += scroll;
                ultimoXObstaculoFijo += scroll;
                ultimoXRosa += scroll;
                xLimiteIzquierdo -= scroll;
                
                if (xFondo > 0) xFondo = 0;
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
            limpiarElementos();
        }
        synchronized (proyectilesEnemigos) {
            proyectilesEnemigos.removeIf(p -> !p.isActivo());
        }
        synchronized (rosas) {
            actualizarRosas();
        }
        
        manejarColisionesDeProyectiles();
        manejarColisionesDePersonaje();

        repaint();
    }
    
    // -------------------------------------------------------------------------
    // 7. MÉTODOS AUXILIARES Y DE JUEGO (Colisiones, Generación, etc.)
    // -------------------------------------------------------------------------

    private void limpiarElementos() {
        enemigos.removeIf(o -> o.getX() + o.getAncho() < 0);
        rosas.removeIf(r -> r.getX() + 80 < 0); 
    }
    
    private void generarElementos() {
        generarObstaculosMoviles();
        generarObstaculosInmoviles();
        generarRosas(); 
    }

    private void actualizarRosas() {
        Iterator<Rosa> it = rosas.iterator();
        while (it.hasNext()) {
            Rosa r = it.next();
            if (caballeroC != null && caballeroC.getHitbox().intersects(r.getRect())) {
                it.remove();
                
                // ✅ Límite de recolección: Asegura que el contador NUNCA pase de 10
                if (contador < 10) { 
                    contador++;
                }
                
                // ➡️ Lógica de aparición de los Jefes: Se activa en 3, 6, 9 y 10.
                switch (contador) {
                    case 3:
                        generarJefe(GARGOLA_SPRITE, ALTO_GARGOLA, VIDA_JEFE_BASE);
                        break;
                    case 6:
                        generarJefe(NEBLINA_SPRITE, ALTO_NEBLINA, VIDA_JEFE_BASE);
                        break;
                    case 9:
                        generarJefe(CABALLERO_SPRITE, ALTO_CABALLERO, VIDA_JEFE_BASE);
                        break;
                    case 10:
                        generarJefe(VAMPIRO_SPRITE, ALTO_VAMPIRO, VIDA_JEFE_FINAL);
                        break;
                }
            }
        }
        
        if (rosas.isEmpty() && contador < 10 && ultimoXRosa < getWidth()) {
              ultimoXRosa = getWidth() + 10;
        }
    }

    /**
     * 👑 Genera un jefe dinámicamente, limpia obstáculos y activa el bloqueo de generación.
     */
    private void generarJefe(String sprite, int alto, int vida) {
        
        boolean jefeExiste = enemigos.stream()
                .anyMatch(o -> o.getNombreImagen().equals(sprite));

        if (jefeExiste) {
            return;
        }
        
        // ✅ PASO 1: Desaparecer los obstáculos que ya están en pantalla
        enemigos.clear(); 
        
        // ✅ PASO 2: Activar el estado de batalla
        jefeActivo = true;

        // 3. Calcular la posición Y y crear el Jefe
        final int ALTO_SUELO = 20;
        int yJefe = getHeight() - alto - ALTO_SUELO; 
        int xJefe = getWidth(); 

        enemigos.add(new Obstaculos(xJefe, yJefe, sprite, vida));
        
        System.out.println("¡JEFE GENERADO! " + sprite + " (Rosa #" + contador + ") Vida: " + vida);

        // 4. Deshabilitamos la generación normal para concentrarnos en el jefe
        ultimoXObstaculoFijo = Integer.MAX_VALUE;
        ultimoXObstaculoMovil = Integer.MAX_VALUE;
    }

    /**
     * ✅ Lógica de generación de Rosas: Separación de 3000px y límite de 3 en pantalla.
     */
    private void generarRosas() {
        
        // 🛑 BLOQUEO: Si el jefe está activo, NO generamos más rosas.
        if (jefeActivo) return; 

        // 1. LÍMITE DE RECOLECCIÓN
        if (contador >= 10) {
            return;
        }
        
        // 2. LÍMITE EN PANTALLA: Máximo de 3 rosas visibles.
        if (rosas.size() >= 3) {
            return;
        }

        // 3. LÍMITE DE DISTANCIA: Mínimo 3000px de separación.
        if (ultimoXRosa >= getWidth() + DISTANCIA_MIN_ROSA) {
            return;
        }
        
        if (ultimoXRosa < getWidth()) {
            ultimoXRosa = getWidth() + 10;
        }
        
        // 4. PROBABILIDAD: 1 en 200 de probabilidad.
        if (random.nextInt(200) < 1) {
            
            final int ALTO_SUELO = 20;
            
            // POSICIÓN Y CORREGIDA (usando Rosa.ALTO = 74)
            int yRosa = getHeight() - Rosa.ALTO - ALTO_SUELO; 
            
            rosas.add(new Rosa(getWidth(), yRosa));

            ultimoXRosa = getWidth();
        }
    }
    
    // -------------------------------------------------------------------------
    // --- LÓGICA DE ENEMIGOS Y OBSTÁCULOS (MODIFICADOS) ---
    // -------------------------------------------------------------------------

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
                    o.getX() - expansion,
                    o.getY(),
                    o.getAncho() + (2 * expansion),
                    o.getAlto()
                );
            }
            
            // Solo se puede atacar si el obstáculo tiene vida
            if (ataqueHitbox.intersects(rectColisionAtaque)) {
                if (o.tieneVida()) {
                    o.recibirDano(1);
                    sonido.reproducirSonido("golpe");

                    if (o.estaDestruido()) {
                        
                        // 👑 LÓGICA DE RECOMPENSA AL DERROTAR JEFES 👑
                        if (nombreSprite.contains(GARGOLA_SPRITE)) {
                            caballeroC.desbloquearDobleSalto(); // Mejora al matar Gárgola
                        } else if (nombreSprite.contains(NEBLINA_SPRITE)) {
                            caballeroC.aumentarVelocidad(1); // Mejora al matar Neblina (Aumenta de 4 a 6)
                        }
                        // ---------------------------------------------
                        
                        it.remove();
                        
                        // ➡️ REANUDAR GENERACIÓN TRAS DERROTA DEL JEFE
                        if (jefeActivo && enemigos.isEmpty()) { 
                            jefeActivo = false;
                            
                            // Restablecer los límites
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
        // 🛑 BLOQUEO: Si el jefe está activo, los obstáculos móviles se detienen.
        if (jefeActivo) return; 
        
        for (Obstaculos o : enemigos) {
            o.mover();
        }
    }

    private void generarObstaculosMoviles() {
        
        // ✅ BLOQUEO: Si hay jefe activo, no generamos fantasmas.
        if (jefeActivo) return; 

        if (ultimoXObstaculoMovil >= getWidth() + DISTANCIA_MIN_OBSTACULO_MOVIL) {
            return;
        }
        
        if (ultimoXObstaculoMovil < getWidth()) {
            ultimoXObstaculoMovil = getWidth() + 10;
        }
        
        if (obstaculosMovilesConsecutivos >= MAX_CONSECUTIVOS) {
            ultimoXObstaculoMovil = ultimoXObstaculoMovil + DISTANCIA_MIN_OBSTACULO_MOVIL; 
            obstaculosMovilesConsecutivos = 0;
            return; 
        }
        
        boolean debeGenerar = obstaculosMovilesConsecutivos > 0 || random.nextInt(100) < 5;
        
        if (debeGenerar) {
            
            int yObs = getHeight() - 20 - ALTO_POR_DEFECTO;

            int xPosicion = ultimoXObstaculoMovil + ESPACIO_CONSECUTIVO; 
            
            enemigos.add(new Obstaculos(xPosicion, yObs, "/img/obstaculo.gif"));

            ultimoXObstaculoMovil = xPosicion; 
            obstaculosMovilesConsecutivos++;
        }
    }

    private void generarObstaculosInmoviles() {
        
        // ✅ BLOQUEO: Si hay jefe activo, no generamos obstáculos fijos.
        if (jefeActivo) return; 

        if (ultimoXObstaculoFijo >= getWidth() + DISTANCIA_MIN_OBSTACULO_FIJO) {
            return;
        }
        
        if (ultimoXObstaculoFijo < getWidth()) {
              ultimoXObstaculoFijo = getWidth() + 10;
        }
        
        if (obstaculosFijosConsecutivos >= 1) {
            ultimoXObstaculoFijo = ultimoXObstaculoFijo + DISTANCIA_MIN_OBSTACULO_FIJO; 
            obstaculosFijosConsecutivos = 0;
            return;
        }
        
        if (random.nextInt(100) < 3) {
            int yObs = getHeight() - 20 - ALTO_POR_DEFECTO;
            
            int xPosicion = getWidth() + 50; 
            
            String spriteElegido = elegirObstaculoFijo();
            
            if (spriteElegido.contains("plantas_carnivoras")) {
                enemigos.add(new Obstaculos(xPosicion, yObs, spriteElegido));
            } else {
                enemigos.add(new Obstaculos(xPosicion, yObs, spriteElegido, 50));
            }

            ultimoXObstaculoFijo = xPosicion + 175;
            obstaculosFijosConsecutivos++;
        }
    }
    
    // -------------------------------------------------------------------------
    // --- Resto de Métodos Auxiliares de Tablero (Colisiones, Movimiento, etc.) ---
    // -------------------------------------------------------------------------
    
    private String elegirObstaculoFijo() {
        int eleccion = random.nextInt(3); 
        
        switch (eleccion) {
            case 0:
                return "/img/plantas_carnivoras.gif";
            case 1:
                return "/img/enredaderas.gif";
            case 2:
                return "/img/arboles.gif"; 
            default:
                return "/img/plantas_carnivoras.gif"; 
        }
    }

    private void moverProyectiles() {
        final int anchoPanel = getWidth();
        for (Proyectil p : proyectilesEnemigos) {
            p.mover(anchoPanel);
        }
    }
    
    private void generarProyectilesEnemigos() {
        for (Obstaculos obs : enemigos) {
            String nombreSprite = obs.getNombreImagen();
            
            if (nombreSprite.contains("enredaderas")) {
                
                final int expansion = RANGO_LANZAMIENTO_ENREDADERA;
                Rectangle rangoActivacion = new Rectangle(
                    obs.getX() - expansion,
                    obs.getY() - 100,
                    obs.getAncho() + (2 * expansion),
                    obs.getAlto() + 100
                );
                
                if (caballeroC.getHitbox().intersects(rangoActivacion) && obs.puedeAtacar()) {
                    
                    obs.iniciarAtaqueAnimacion();
                    
                    int centroObsX = obs.getX() + obs.getAncho() / 2;
                    int dirX = (caballeroC.getX() < centroObsX) ? -DAGA_VELOCIDAD : DAGA_VELOCIDAD;
                    
                    int xInicial = centroObsX - DAGA_ANCHO / 2;
                    int yInicial = obs.getY() + obs.getAlto() / 2 - DAGA_ALTO / 2;
                    
                    Proyectil daga = new Proyectil(
                        xInicial, yInicial,
                        DAGA_SPRITE,
                        dirX,
                        DAGA_ANCHO, DAGA_ALTO
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

            if (nombreSprite.contains("enredaderas")) {
                continue;
            }
            
            if (nombreSprite.contains("arboles") || nombreSprite.contains("plantas_carnivoras")) {
                
                 final int expansion = RANGO_ACTIVACION_ENEMIGO;
                 Rectangle rangoEfecto = new Rectangle(
                    obs.getX() - expansion, obs.getY(),
                    obs.getAncho() + (2 * expansion), obs.getAlto()
                 );
                 
                 // Lógica de Ralentización
                 if (hitboxPersonaje.intersects(rangoEfecto)) {
                     estaRalentizado = true;
                 }
                 
                 if (hitboxPersonaje.intersects(rectColision)) {
                     
                     if (nombreSprite.contains("plantas_carnivoras") && obs.puedeAtacar()) {
                         // Planta Carnívora: Solo inicia la animación y cooldown. NO DAÑO.
                         obs.iniciarAtaqueAnimacion();
                         obs.iniciarCooldown();
                     }
                 }
            }
            // Colisión con Fantasmas (y cualquier otro no cubierto arriba)
            else {
                if (hitboxPersonaje.intersects(obs.getRect()) && !personajeEsInvulnerable) {
                    vida.quitarVida();
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
        
        // 🛑 Lógica para aplicar el estado de ralentización al personaje
        if (caballeroC != null) {
            caballeroC.setEstaRalentizado(estaRalentizado);
        }
    }

    
    public void setPersonajeY(Personaje c) {
        if (this.caballeroC == null) {
            this.caballeroC = c;
        }
        
        if (this.caballeroC != null) {
            // Corrige la Y si está muy por encima del suelo.
            final int sueloY = getHeight() - caballeroC.getAlto() - 20; 
            
            if (getHeight() > 0 && this.caballeroC.getY() < sueloY - 10) { 
                this.caballeroC.setY(sueloY);
            }
            
            this.enAire = false;
            this.velocidadY = 0;
        }
    }
    
    // -------------------------------------------------------------------------
    // --- MÉTODOS GETTERS/SETTERS PÚBLICOS ---
    // -------------------------------------------------------------------------

    /**
     * Establece el objeto Personaje principal.
     */
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
    
    /**
     * Obtiene la instancia del objeto Personaje principal (el jugador).
     * @return El objeto Personaje.
     */
    public Personaje getJugador() {
        return this.caballeroC;
    }

    /**
     * Obtiene la velocidad base del personaje (la que tiene almacenada).
     * @return Velocidad base.
     */
    public int getVelocidadBase() {
        if (caballeroC != null) {
            return caballeroC.getVelocidadBase();
        }
        return 4; // Valor por defecto en caso de que el personaje no exista aún
    }

    /**
     * Obtiene el objeto Vida para consultar o modificar la vida.
     * @return El objeto Vida.
     */
    public Vida getVidaObjeto() {
        return this.vida;
    }
    
    /**
     * Obtiene la cantidad de rosas recogidas.
     * @return Número de rosas (variable estática Tablero.contador).
     */
    public int getRosasRecogidas() {
        return Tablero.contador; 
    }

} // Fin de la clase Tablero