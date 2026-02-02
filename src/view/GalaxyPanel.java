package view;

import controller.FogOfWar;
import controller.GalaxyGenerator;
import controller.Game;
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

        if (selectedSystem != null && game.getFogOfWar().isSystemVisible(selectedSystem)) {
            mainWindow.onSystemSelected(selectedSystem);
        } else if (selectedSystem != null) {
            mainWindow.onSystemSelected(null);
            JOptionPane.showMessageDialog(
                    this,
                    "System " + selectedSystem.getName() + " jest poza zasięgiem twoich czujników.\nWyślij zwiadowcę aby go rozpoznać.",
                    "Brak informacji",
                    JOptionPane.INFORMATION_MESSAGE
            );
        } else {
            mainWindow.onSystemSelected(null);
        }

        repaint();
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

        Color mainLine = new Color(120, 170, 255, 140);
        Color glowLine = new Color(120, 170, 255, 60);

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

        FogOfWar fog = game.getFogOfWar();

        for (StarSystem system : galaxy.getSystems()) {
            boolean isVisible = fog.isSystemVisible(system);
            boolean isSelected = (system == selectedSystem);

            int size = (int) (STAR_SIZE * camera.getZoom());
            int x = camera.worldToScreenX(system.getX());
            int y = camera.worldToScreenY(system.getY());

            if (isVisible) {
                g2.setColor(isSelected ? Color.YELLOW : Color.WHITE);
                g2.fillOval(x, y, size, size);

                g2.setColor(isSelected ? Color.YELLOW : Color.WHITE);
                g2.drawString(system.getName(), x + size + 4, y + size);

                boolean hasColony = false;
                for (OrbitSlot orbit : system.getOrbits()) {
                    if (orbit.getObject() instanceof Planet planet && planet.isColonized()) {
                        hasColony = true;
                        break;
                    }
                }

                if (hasColony) {
                    g2.setColor(new Color(100, 255, 100, 200));
                    g2.drawOval(x - 3, y - 3, size + 6, size + 6);
                    g2.drawOval(x - 4, y - 4, size + 8, size + 8);
                }

                if (!system.getFleets().isEmpty()) {
                    g2.setColor(new Color(255, 200, 100));
                    int triangleSize = (int) (6 * camera.getZoom());
                    int[] xPoints = {x + size/2, x + size/2 - triangleSize/2, x + size/2 + triangleSize/2};
                    int[] yPoints = {y - 8, y - 8 - triangleSize, y - 8 - triangleSize};
                    g2.fillPolygon(xPoints, yPoints, 3);
                }

            } else {
                g2.setColor(new Color(100, 100, 100, 150));
                g2.fillOval(x, y, size, size);

                g2.setColor(new Color(150, 150, 150, 100));
                g2.drawString(system.getName(), x + size + 4, y + size);
            }
        }
    }
}