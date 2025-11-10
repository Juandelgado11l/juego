import java.awt.Rectangle;

public class Ataque {
    
    private final Rectangle hitbox;

    public Ataque(int x, int y, int ancho, int alto) {
        this.hitbox = new Rectangle(x, y, ancho, alto); 
    }
    
    public Rectangle getHitbox() {
        return hitbox;
    }
}