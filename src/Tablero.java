import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import javax.swing.*;

public class Tablero extends JPanel {

    private Image fondo;
    private Image suelo;
    private ArrayList<Rosa> rosas = new ArrayList<>();
    private ArrayList<Obstaculos> enemigos = new ArrayList<>();
    private ArrayList<Proyectil> proyectilesEnemigos = new ArrayList<>(); 
    private Personaje caballeroC; 
    public static int contador = 0; 

    private int xFondo = 0;
    private int xSuelo = 0;
    
    private int velocidadY = 0;
    private final int gravedad = 2;
    private final int fuerzaSalto = -29;
    private boolean enAire = false; 

    private Sonido sonido = new Sonido();
    private Controles controles;
    private Image gifUI;
    private Random random = new Random();
    
    private Vida vida; 
    
    private static final int ALTO_POR_DEFECTO = 80; 
    
    private int ultimoXObstaculoMovil = 0;
    private int obstaculosMovilesConsecutivos = 0; 
    private final int MAX_CONSECUTIVOS = 4; 
    private final int ESPACIO_CONSECUTIVO = 110; 
    
    private int ultimoXRosa = 0;
    private int ultimoXObstaculoFijo = 0;
    private int obstaculosFijosConsecutivos = 0; 

    private final int DISTANCIA_MIN_OBSTACULO_MOVIL = 200; 
    private final int DISTANCIA_MIN_ROSA = 15000; 
    private final int DISTANCIA_MIN_OBSTACULO_FIJO = 400; 
    
    private final int ATAQUE_ANCHO = 50;
    private final int ATAQUE_ALTO = 50;
    private final int RANGO_ACTIVACION_ENEMIGO = 50; 
    
    private final int DAGA_ANCHO = 30;
    private final int DAGA_ALTO = 10;
    private final int DAGA_VELOCIDAD = 10; 
    private final String DAGA_SPRITE = "/img/daga.png"; 
    private final int RANGO_LANZAMIENTO_ENREDADERA = 250; 
    
    private final long INVULNERABILITY_DURATION = 1500; 
    private long invulnerabilityEnd = 0;
    
    private long lastUpdateTime = 0;
    private boolean esInvulnerable = false;
    
    private boolean estaRalentizado = false; 
    private final int VELOCIDAD_NORMAL = 4;
    private final int VELOCIDAD_RALENTIZADA = 1;


    public Tablero() {
        setBackground(Color.BLACK);
        setFocusable(true);
        
        vida = new Vida();

        try {
            gifUI = new ImageIcon(getClass().getResource("img/flor.gif")).getImage();
            fondo = new ImageIcon(getClass().getResource("img/fondoPrincipal.png")).getImage();
            suelo = new ImageIcon(getClass().getResource("img/suelo.png")).getImage();
        } catch (Exception e) {
            System.err.println("ERROR cargando imágenes en Tablero: " + e.getMessage());
        }

        sonido.loopSonido("juego");
        
        if (rosas.isEmpty()) {
            ultimoXRosa = 300; 
        }
        
        caballeroC = new Personaje(100, 100); 
        enAire = true;
        velocidadY = 1; 

        controles = new Controles();
        addKeyListener(controles);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

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

        for (Obstaculos o : enemigos) {
            o.dibujar(g);
            o.dibujarBarraVida(g); 
        }
        
        for (Proyectil p : proyectilesEnemigos) {
            p.dibujar(g);
        }
        
        if (caballeroC != null) {
            caballeroC.dibujar(g, this.esInvulnerable, this.lastUpdateTime, this);
        }
        
        for (Rosa r : rosas) r.dibujar(g);
    }
    
    public void actualizar() {
        if (caballeroC == null) return; 
        
        // --- LÓGICA DE FÍSICA ---
        
        if (enAire) {
            velocidadY += gravedad;
            caballeroC.setY(caballeroC.getY() + velocidadY);
        }

        int ySuelo = getHeight() - 20 - caballeroC.getAlto();
        if (caballeroC.getY() >= ySuelo) {
            caballeroC.setY(ySuelo);
            enAire = false;
            velocidadY = 0;
        }

        if (controles.isSaltando() && !enAire) {
            velocidadY = fuerzaSalto;
            enAire = true;
            sonido.reproducirSonido("salto"); 
            // 🐛 CORREGIDO: Llamada al método setSaltando que ahora existe en Controles
            controles.setSaltando(false); 
        }
        // ------------------------

        long currentTime = System.currentTimeMillis();
        this.lastUpdateTime = currentTime;
        this.esInvulnerable = currentTime < invulnerabilityEnd; 
        
        caballeroC.actualizarEstado(); 
        
        int velocidadBase = estaRalentizado ? VELOCIDAD_RALENTIZADA : VELOCIDAD_NORMAL;
        
        int desplazamiento = -velocidadBase;
        xFondo += desplazamiento / 2; 
        xSuelo += desplazamiento;
        
        moverElementosHorizontal(desplazamiento);
        
        moverObstaculosMoviles();
        
        for (Obstaculos o : enemigos) {
            o.actualizarEstado(); 
        }

        moverProyectiles();
        generarProyectilesEnemigos(); 
        
        if (caballeroC.estaAtacando()) {
             manejarAtaque();
        }

        if (xFondo <= -getWidth()) xFondo += getWidth();
        if (xFondo >= getWidth()) xFondo -= getWidth();
        if (xSuelo <= -getWidth()) xSuelo += getWidth();
        if (xSuelo >= getWidth()) xSuelo -= getWidth();

        generarElementos();
        limpiarElementos();
        proyectilesEnemigos.removeIf(p -> !p.isActivo()); 

        actualizarRosas();
        
        manejarColisionesDeProyectiles(); 
        manejarColisionesDePersonaje(); 

        repaint();
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
                 
                 if (hitboxPersonaje.intersects(rangoEfecto)) {
                     estaRalentizado = true; 
                 }
                
                 if (hitboxPersonaje.intersects(rectColision)) { 
                    
                     if (nombreSprite.contains("plantas_carnivoras") && obs.puedeAtacar()) {
                          if (!personajeEsInvulnerable) {
                             obs.iniciarAtaqueAnimacion(); 
                             vida.quitarVida(); 
                             sonido.reproducirSonido("golpe"); 
                             invulnerabilityEnd = currentTime + INVULNERABILITY_DURATION;
                             obs.iniciarCooldown();
                            
                             if (vida.estaMuerto()) {
                                 sonido.reproducirSonido("risa"); 
                                 JOptionPane.showMessageDialog(this, "💀 GAME OVER");
                                 System.exit(0);
                             }
                         }
                     }
                 }
            }
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
        
        if (caballeroC != null) {
            caballeroC.setEstaRalentizado(estaRalentizado);
        }
    }
    
    private void manejarAtaque() {
        int ataqueX = caballeroC.getX() + (caballeroC.isMirandoDerecha() ? caballeroC.getAncho() - 10 : -ATAQUE_ANCHO + 10);
        int ataqueY = caballeroC.getY() + (caballeroC.getAlto() / 2) - (ATAQUE_ALTO / 2);

        Ataque ataque = new Ataque(ataqueX, ataqueY, ATAQUE_ANCHO, ATAQUE_ALTO);
        
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

            if (ataque.getHitbox().intersects(rectColisionAtaque)) { 
                if (o.tieneVida()) { 
                    o.recibirDano(1); 
                    sonido.reproducirSonido("golpe"); 

                    if (o.estaDestruido()) {
                        it.remove(); 
                    }
                }
            }
        }
    }
    
    private void moverElementosHorizontal(int desplazamiento) {
        for (Obstaculos o : enemigos) {
            o.setX(o.getX() + desplazamiento);
        }
        for (Rosa r : rosas) r.setX(r.getX() + desplazamiento);
        
        for (Proyectil p : proyectilesEnemigos) {
            p.setX(p.getX() + desplazamiento);
        }
        
        ultimoXObstaculoMovil += desplazamiento;
        ultimoXObstaculoFijo += desplazamiento;
        ultimoXRosa += desplazamiento;
    }
    
    private void moverObstaculosMoviles() {
        for (Obstaculos o : enemigos) {
            o.mover(); 
        }
    }
    
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
                
                if (contador < 10) { 
                    contador++;
                }
            }
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

    private void generarObstaculosMoviles() {
        if (ultimoXObstaculoMovil >= getWidth() + DISTANCIA_MIN_OBSTACULO_MOVIL) {
            return;
        }
        
        if (ultimoXObstaculoMovil < getWidth()) {
            ultimoXObstaculoMovil = getWidth() + 10;
        }
        
        if (obstaculosMovilesConsecutivos >= MAX_CONSECUTIVOS) {
            obstaculosMovilesConsecutivos = 0; 
            return;
        }
        
        if (random.nextInt(100) < 5) { 
            
            int yObs = getHeight() - 20 - ALTO_POR_DEFECTO; 

            int xPosicion = ultimoXObstaculoMovil + ESPACIO_CONSECUTIVO;
            
            enemigos.add(new Obstaculos(xPosicion, yObs, "/img/fantasma.gif"));

            ultimoXObstaculoMovil = xPosicion;
            obstaculosMovilesConsecutivos++; 
        }
    }

    private void generarObstaculosInmoviles() {
        if (ultimoXObstaculoFijo >= getWidth() + DISTANCIA_MIN_OBSTACULO_FIJO) {
            return;
        }
        
        if (ultimoXObstaculoFijo < getWidth()) {
             ultimoXObstaculoFijo = getWidth() + 10;
        }
        
        if (obstaculosFijosConsecutivos >= 1) { 
             obstaculosFijosConsecutivos = 0;
             return;
        }
        
        if (random.nextInt(100) < 10) {
            int yObs = getHeight() - 20 - ALTO_POR_DEFECTO; 
            
            if (random.nextBoolean()) {
                enemigos.add(new Obstaculos(getWidth(), yObs, "/img/plantas_carnivoras.gif"));
            } else {
                 enemigos.add(new Obstaculos(getWidth(), yObs, "/img/enredaderas.gif"));
            }

            ultimoXObstaculoFijo = getWidth();
            obstaculosFijosConsecutivos++; 
        }
    }

    private void generarRosas() {
        if (ultimoXRosa >= getWidth() + DISTANCIA_MIN_ROSA) {
            return;
        }
        
        if (ultimoXRosa < getWidth()) {
            ultimoXRosa = getWidth() + 10;
        }
        
        if (random.nextInt(500) < 1) { 
            int yRosa = getHeight() - 20 - 40; 
            
            rosas.add(new Rosa(getWidth(), yRosa));

            ultimoXRosa = getWidth();
        }
    }
}