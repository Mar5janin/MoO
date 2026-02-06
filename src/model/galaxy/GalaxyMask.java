package model.galaxy;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class GalaxyMask {

    private BufferedImage image;

    public GalaxyMask(String path) {
        try {
            image = ImageIO.read(
                    GalaxyMask.class.getResource(path)
            );
        } catch (IOException | IllegalArgumentException e) {
            throw new RuntimeException("Nie można wczytać maski galaktyki", e);
        }
    }

//    Sprawdza czy dane miejsce może być użyte do umieszczenia gwiazdy na podstawie jasności pikselu
    public boolean isAllowed(
            int x, int y,
            int worldWidth, int worldHeight,
            double densityMultiplier
    ) {
        // Mapowanie współrzędnych świata na współrzędne obrazu maski
        int imgX = (int) ((double) x / worldWidth * image.getWidth());
        int imgY = (int) ((double) y / worldHeight * image.getHeight());

        if (imgX < 0 || imgY < 0 ||
                imgX >= image.getWidth() ||
                imgY >= image.getHeight()) {
            return false;
        }

        // Wydobycie rgb z piksela
        int rgb = image.getRGB(imgX, imgY);
        int r = (rgb >> 16) & 0xff;
        int g = (rgb >> 8) & 0xff;
        int b = rgb & 0xff;

        // Obliczenie jasności piksela (0-255)
        int brightness = (r + g + b) / 3;

        // Zbyt ciemne obszary = przestrzeń poza galaktyką
        if (brightness > 140) return false;

        // Zbyt jasne obszary = centrum galaktyki
        if (brightness < 40) return false;

        // Normalizacja jasności do zakresu 0.0-1.0 (zakres: 40-140)
        // Wyższe wartości = większa szansa na spawn
        double normalized = (brightness - 5) / 135.0;
        double chance = normalized * densityMultiplier;

        // Im wyższa szansa, tym częstszy spawn
        return Math.random() < chance;
    }

}