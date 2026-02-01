package model;

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

    public boolean isAllowedWeighted(
            int x, int y,
            int worldWidth, int worldHeight,
            double densityMultiplier
    ) {
        int imgX = (int) ((double) x / worldWidth * image.getWidth());
        int imgY = (int) ((double) y / worldHeight * image.getHeight());

        if (imgX < 0 || imgY < 0 ||
                imgX >= image.getWidth() ||
                imgY >= image.getHeight()) {
            return false;
        }

        int rgb = image.getRGB(imgX, imgY);
        int r = (rgb >> 16) & 0xff;
        int g = (rgb >> 8) & 0xff;
        int b = rgb & 0xff;

        int brightness = (r + g + b) / 3;

        if (brightness > 140) return false;

        if (brightness < 40) return false;

        double normalized = (brightness - 5) / 135.0;
        double chance = normalized * densityMultiplier;

        return Math.random() < chance;
    }

}
