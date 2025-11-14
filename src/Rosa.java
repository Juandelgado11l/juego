import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import javax.swing.ImageIcon;

public class Rosa {
    private int x, y;
    private Image imagen;
    
    // ⬅️ VARIABLE FALTANTE: Almacena el número de la Rosa (1 a 10)
    private  int numero; 
    
    // Puedes añadir una variable para la vida si la Rosa 10 actuará como un jefe
    // private int vida; 
    
    public static final int ANCHO = 74;
    public static final int ALTO = 74;

    // -------------------------------------------------------------
    // CONSTRUCTOR 1 (Completo): Usado para generar la Rosa Final (10) 
    // y para la generación automática si se incluye el contador.
    // -------------------------------------------------------------
    public Rosa(int x, int y, int contador) {
        this.x = x;
        this.y = y;
        this.numero = contador; // ⬅️ Inicializamos la variable 'numero'
        
        String rutaImagen;
        
        if (contador == 10) {
            // Lógica para la Rosa Final (Jefe)
            rutaImagen = "/img/rosa_final.png"; 
            // Si la Rosa 10 tiene vida de jefe: this.vida = VIDA_JEFE_FINAL;
        } else {
            // Lógica para Rosas de recompensa normales (1-9)
            rutaImagen = "/img/flor.gif"; 
        }
        
        try {
            this.imagen = new ImageIcon(getClass().getResource(rutaImagen)).getImage();
        } catch (Exception e) {
             System.err.println("Error al cargar imagen de Rosa (" + rutaImagen + "): " + e.getMessage());
             this.imagen = null;
        }
    }

    // -------------------------------------------------------------
    // CONSTRUCTOR 2 (Simple / Sobrecarga)
    // -------------------------------------------------------------
    public Rosa(int x, int y) {
        // Llama al constructor principal de 3 parámetros. Asumimos que es una rosa normal (contador 1).
        this(x, y, 1); 
    }
    
    // -------------------------------------------------------------
    // MÉTODOS DE DIBUJO Y LÓGICA
    // -------------------------------------------------------------

    public void dibujar(Graphics g) {
        if (imagen != null) {
            g.drawImage(imagen, x, y, ANCHO, ALTO, null);
        }
    }

    // Método de movimiento para que la rosa siga el scroll
    public void mover(int scrollSpeed) { 
        this.x += scrollSpeed; 
    }

    // -------------------------------------------------------------
    // SETTERS
    // -------------------------------------------------------------
    public void setX(int x) { 
        this.x = x; 
    }
    public void setY(int y) { 
        this.y = y; 
    }

    // -------------------------------------------------------------
    // GETTERS
    // -------------------------------------------------------------
    
    public Rectangle getRect() {
        return getHitbox(); 
    }
    
    public Rectangle getHitbox() {
        return new Rectangle(x, y, ANCHO, ALTO);
    }
    
    public Image getImagen() { return imagen; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getAncho() { return ANCHO; }
    public int getAlto() { return ALTO; }

    // ⬅️ MÉTODO FALTANTE: Resuelve el error de compilación en Tablero.java
    /**
     * Devuelve el número de identificación de la Rosa (1-9 para coleccionable, 10 para Jefe).
     * @return El número de la rosa.
     */
    public int getNumero() {
        return numero;
    }
    // dentro de public class Rosa { ... }
    public void setNumero(int numeroReal) {
    if (numeroReal > 0) {
        this.numero = numeroReal;
    }
}

    
}