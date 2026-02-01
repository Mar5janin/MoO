package view;

import ui.Camera;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class GalaxyBackground {

    private BufferedImage image;

    public GalaxyBackground() {
        try {
            image = ImageIO.read(
                    getClass().getResource("/galaxy_mask.png")
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void draw(Graphics2D g, Camera camera,
                     int worldWidth, int worldHeight) {

        int x = camera.worldToScreenX(0);
        int y = camera.worldToScreenY(0);

        int w = (int) (worldWidth * camera.getZoom());
        int h = (int) (worldHeight * camera.getZoom());

        g.drawImage(image, x, y, w, h, null);
    }
}
