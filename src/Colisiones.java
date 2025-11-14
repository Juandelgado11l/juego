import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.Graphics;

public class Colisiones {

    /**
     * Convierte una imagen (Image) a un tipo BufferedImage compatible.
     * Esto es necesario para acceder a los píxeles.
     */
    private static BufferedImage toBufferedImage(Image img) {
        if (img == null) return null;
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }

        // Crea una imagen Buffered con transparencia
        BufferedImage bimage = new BufferedImage(
            img.getWidth(null), 
            img.getHeight(null), 
            BufferedImage.TYPE_INT_ARGB
        );

        // Dibuja la imagen original en el BufferedImage
        Graphics g = bimage.getGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();

        return bimage;
    }

    /**
     * ✅ MÉTODO Detecta la colisión a nivel de píxel entre dos objetos.
     * Resuelve la advertencia "is never used locally" al estar listo para ser llamado.
     *
     * @param img1 La imagen (sprite) del primer objeto.
     * @param img2 La imagen (sprite) del segundo objeto.
     * @param rect1 El hitbox (Rectangle) del primer objeto.
     * @param rect2 El hitbox (Rectangle) del segundo objeto.
     * @return true si hay colisión de píxeles opacos, false en caso contrario.
     */
    public static boolean pixelPerfectCollision(Image img1, Image img2, Rectangle rect1, Rectangle rect2) {
        if (img1 == null || img2 == null) {
            // No podemos hacer colisión pixelada si falta la imagen.
            return rect1.intersects(rect2); 
        }

        // 1. Chequeo de colisión de Hitbox (Pre-test)
        if (!rect1.intersects(rect2)) {
            return false;
        }

        // 2. Convertir imágenes para acceso a píxeles
        BufferedImage bimg1 = toBufferedImage(img1);
        BufferedImage bimg2 = toBufferedImage(img2);

        if (bimg1 == null || bimg2 == null) {
             // Si la conversión falla, volvemos a la colisión simple de Hitbox
             return rect1.intersects(rect2);
        }

        // 3. Calcular la región de solapamiento
        Rectangle intersection = rect1.intersection(rect2);

        // 4. Recorrer la región de solapamiento
        for (int i = intersection.x; i < intersection.x + intersection.width; i++) {
            for (int j = intersection.y; j < intersection.y + intersection.height; j++) {
                
                // Calcular las coordenadas relativas al sprite 1
                int localX1 = i - rect1.x;
                int localY1 = j - rect1.y;

                // Calcular las coordenadas relativas al sprite 2
                int localX2 = i - rect2.x;
                int localY2 = j - rect2.y;

                // Chequear el componente Alpha (transparencia) del píxel
                // Si ambos píxeles en la intersección son opacos (alpha > 0), hay colisión.
                try {
                    boolean isOpaque1 = (bimg1.getRGB(localX1, localY1) & 0xFF000000) != 0;
                    boolean isOpaque2 = (bimg2.getRGB(localX2, localY2) & 0xFF000000) != 0;

                    if (isOpaque1 && isOpaque2) {
                        return true; // ¡Colisión de píxeles encontrada!
                    }
                } catch (Exception e) {
                    // Ignorar errores de acceso fuera de límites (puede ocurrir en los bordes de la imagen)
                }
            }
        }

        return false; // No hay colisión de píxeles opacos
    }
}