package view;

import model.*;
import ui.Camera;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

public class GalaxyPanel extends JPanel {

    private static final int STAR_SIZE = 12;

    private final Galaxy galaxy;
    private StarSystem selectedSystem;
    private final MainWindow mainWindow;
    private final Camera camera;
    private final GalaxyBackground background;

    private Point lastMouse;

    private Rectangle endTurnButtonBounds;
    private Game game;

    public GalaxyPanel(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        setBackground(Color.BLACK);

        camera = new Camera(0, 0);

        camera.setBounds(
                GalaxyGenerator.getGalaxyWidth(),
                GalaxyGenerator.getGalaxyHeight(),
                getWidth(),
                getHeight()
        );

        galaxy = GalaxyGenerator.generate(MapSize.MEDIUM);
        this.game = new Game(galaxy);
        mainWindow.setGame(game);


        background = new GalaxyBackground();

        SwingUtilities.invokeLater(this::centerCameraOnSol);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastMouse = e.getPoint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                handleClick(e.getX(), e.getY());

                if (endTurnButtonBounds != null &&
                        endTurnButtonBounds.contains(e.getPoint())) {

                    game.nextTurn();
                    repaint();
                    return;
                }

            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int dx = e.getX() - lastMouse.x;
                int dy = e.getY() - lastMouse.y;

                camera.move(-dx, -dy);
                lastMouse = e.getPoint();
                repaint();
            }
        });

        addMouseWheelListener(e -> {
            double zoomAmount = -e.getPreciseWheelRotation() * 0.1;
            camera.zoomAt(zoomAmount, e.getX(), e.getY());
            repaint();
        });

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                camera.setBounds(
                        GalaxyGenerator.getGalaxyWidth(),
                        GalaxyGenerator.getGalaxyHeight(),
                        getWidth(),
                        getHeight()
                );
            }
        });

    }


    private void centerCameraOnSol() {
        StarSystem home = galaxy.getHomeSystem();
        if (home == null) return;

        double viewWidth = getWidth() / camera.getZoom();
        double viewHeight = getHeight() / camera.getZoom();

        camera.setPosition(
                home.getX() - viewWidth / 2,
                home.getY() - viewHeight / 2
        );

        repaint();
    }



    private void handleClick(int x, int y) {
        StarSystem clicked = null;

        for (StarSystem system : galaxy.getSystems()) {
            int size = (int) (STAR_SIZE * camera.getZoom());

            int sx = camera.worldToScreenX(system.getX());
            int sy = camera.worldToScreenY(system.getY());

            if (new Rectangle(sx, sy, size, size).contains(x, y)) {
                clicked = system;
                break;
            }
        }

        selectedSystem = clicked;
        mainWindow.onSystemSelected(selectedSystem);
        repaint();
    }

    private void drawTurnHud(Graphics2D g2) {

        int x = 20;
        int y = getHeight() - 70;
        int width = 160;
        int height = 36;

        endTurnButtonBounds = new Rectangle(x, y, width, height);

        g2.setColor(new Color(30, 30, 30, 220));
        g2.fillRoundRect(x, y, width, height, 12, 12);

        g2.setColor(Color.WHITE);
        g2.drawRoundRect(x, y, width, height, 12, 12);

        g2.drawString("Tura: " + game.getTurn(), x, y - 8);
        g2.drawString("Zakończ turę", x + 24, y + 22);
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        background.draw(
                g2,
                camera,
                GalaxyGenerator.getGalaxyWidth(),
                GalaxyGenerator.getGalaxyHeight()
        );

        Stroke oldStroke = g2.getStroke();

        g2.setStroke(new BasicStroke(
                2.5f,
                BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND
        ));

        Color mainLine = new Color(120, 170, 255, 140);   // główna linia
        Color glowLine = new Color(120, 170, 255, 60);    // delikatny glow

        for (StarSystem system : galaxy.getSystems()) {
            for (StarSystem neighbor : system.getNeighbors()) {

                int x1 = camera.worldToScreenX(system.getX() + STAR_SIZE / 2.0);
                int y1 = camera.worldToScreenY(system.getY() + STAR_SIZE / 2.0);

                int x2 = camera.worldToScreenX(neighbor.getX() + STAR_SIZE / 2.0);
                int y2 = camera.worldToScreenY(neighbor.getY() + STAR_SIZE / 2.0);

                g2.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.setColor(glowLine);
                g2.drawLine(x1, y1, x2, y2);

                g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.setColor(mainLine);
                g2.drawLine(x1, y1, x2, y2);
            }
        }

        g2.setStroke(oldStroke);

        for (StarSystem system : galaxy.getSystems()) {

            g2.setColor(system == selectedSystem ? Color.YELLOW : Color.WHITE);

            int size = (int) (STAR_SIZE * camera.getZoom());
            int x = camera.worldToScreenX(system.getX());
            int y = camera.worldToScreenY(system.getY());

            g2.fillOval(x, y, size, size);
            g2.drawString(system.getName(), x + size + 4, y + size);
        }

        drawTurnHud(g2);

    }
}
