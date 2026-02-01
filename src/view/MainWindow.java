package view;

import model.Game;
import model.Planet;
import model.StarSystem;

import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame {

    private JPanel rootPanel;
    private JPanel galaxyPanelContainer;
    private JPanel sidePanel;
    private JPanel topPanel;
    private JLabel turnLabel;
    private JButton endTurnButton;
    private Game game;


    public MainWindow() {
        setTitle("Galactic Settlers");
        setContentPane(rootPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setUndecorated(true);

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screen);
        setLocation(0, 0);

        initPanels();
        setVisible(true);
    }

    private void initPanels() {
        galaxyPanelContainer.setLayout(new BorderLayout());

        GalaxyPanel galaxyPanel = new GalaxyPanel(this);
        galaxyPanelContainer.add(galaxyPanel, BorderLayout.CENTER);

        sidePanel.setPreferredSize(new Dimension(320, 0));
        sidePanel.setVisible(false);

        turnLabel = new JLabel("Tura: 1");
        endTurnButton = new JButton("Zakończ turę");

        endTurnButton.addActionListener(e -> {
            if (game == null) return;

            // zamknij panele
            sidePanel.removeAll();
            sidePanel.setVisible(false);

            // przelicz turę
            game.nextTurn();

            turnLabel.setText("Tura: " + game.getTurn());

            repaint();
        });
    }


    public void onSystemSelected(StarSystem system) {
        sidePanel.removeAll();

        if (system == null) {
            sidePanel.setVisible(false);
        } else {
            sidePanel.setVisible(true);
            sidePanel.setLayout(new BorderLayout());
            sidePanel.add(new SystemInfoPanel(system, this), BorderLayout.NORTH);
        }

        sidePanel.revalidate();
        sidePanel.repaint();
    }

    // ===== PLANETA =====
    public void showPlanet(Planet planet, StarSystem system) {
        sidePanel.removeAll();
        sidePanel.setVisible(true);
        sidePanel.setLayout(new BorderLayout());
        sidePanel.add(new PlanetInfoPanel(planet, system, this), BorderLayout.NORTH);
        sidePanel.revalidate();
        sidePanel.repaint();
    }

    public void setGame(Game game) {
        this.game = game;
    }

}
