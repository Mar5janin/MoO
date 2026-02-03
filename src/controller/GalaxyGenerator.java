package controller;

import model.*;
import java.util.*;

public class GalaxyGenerator {
    private static int galaxyWidth;
    private static int galaxyHeight;

    private static final GalaxyMask MASK = new GalaxyMask("/galaxy_mask.png");

    public static int getGalaxyWidth() {
        return galaxyWidth;
    }

    public static int getGalaxyHeight() {
        return galaxyHeight;
    }

    private static int minStarDistance() {
        int base = Math.min(galaxyWidth, galaxyHeight) / 18;
        return Math.max(80, Math.min(base, 180));
    }

    public static Galaxy generate(MapSize size) {
        Galaxy galaxy = new Galaxy();
        Random random = new Random();

        int targetStars = size.getStarCount();

        double densityMultiplier = switch (size) {
            case SMALL -> 0.9;
            case MEDIUM -> 1.2;
            case LARGE -> 1.6;
        };

        calculateGalaxySize(size);
        StarSystem sol = generateMaskedStar("Sol", galaxy, random);

        galaxy.getSystems().add(sol);
        galaxy.setHomeSystem(sol);

        generateNearbyStar("Alpha Centauri", sol, galaxy, random);
        generateNearbyStar("Sirius", sol, galaxy, random);

        List<String> names = new ArrayList<>(List.of(StarNames.NAMES));

        names.remove("Sol");
        names.remove("Alpha Centauri");
        names.remove("Sirius");

        int attempts = 0;

        while (galaxy.getSystems().size() < targetStars && attempts < 10_000) {
            attempts++;
            if (names.isEmpty()) break;

            double angle = random.nextDouble() * 2 * Math.PI;
            double radius = Math.sqrt(random.nextDouble());

            double maxRadiusX = galaxyWidth * 0.45;
            double maxRadiusY = galaxyHeight * 0.30;

            int centerX = galaxyWidth / 2;
            int centerY = galaxyHeight / 2;

            int x = (int) (centerX + Math.cos(angle) * radius * maxRadiusX);
            int y = (int) (centerY + Math.sin(angle) * radius * maxRadiusY);

            if (!MASK.isAllowedWeighted(x, y, galaxyWidth, galaxyHeight, densityMultiplier)) {
                continue;
            }

            if (!isFarEnough(x, y, galaxy)) continue;

            String name = names.remove(random.nextInt(names.size()));
            galaxy.getSystems().add(new StarSystem(name, x, y));
        }

        generateConnections(galaxy);
        ensureMinConnections(galaxy, 3);
        forceSolConnections(galaxy);
        ensureMinConnections(galaxy, 4);
        ensureConnectivity(galaxy);
        centerGalaxy(galaxy);

        for (StarSystem s : galaxy.getSystems()) {
            generateOrbits(s, random);
        }

        setupHomeSystem(galaxy);

        return galaxy;
    }


    private static void ensureMinConnections(Galaxy galaxy, int min) {
        List<StarSystem> systems = galaxy.getSystems();

        for (StarSystem system : systems) {
            if (system.getNeighbors().size() >= min) continue;

            List<StarSystem> sorted =
                    systems.stream().filter(s -> s != system)
                            .filter(s -> !system.getNeighbors().contains(s))
                            .sorted(Comparator.comparingDouble(system::distanceTo))
                            .toList();

            for (StarSystem other : sorted) {
                if (system.getNeighbors().size() >= min) break;

                if (system == other) continue;

                if (!canConnect(system, other, min + 1)) continue;

                system.addNeighbor(other);
                other.addNeighbor(system);
            }
        }
    }

    private static boolean isFarEnough(int x, int y, Galaxy galaxy) {
        for (StarSystem s : galaxy.getSystems()) {
            double dist = distance(x, y, s.getX(), s.getY());

            if (dist < minStarDistance())
                return false;
        }

        return true;
    }

    private static void generateConnections(Galaxy galaxy) {

        final int MIN_CONNECTIONS = 3;
        final int MAX_CONNECTIONS = 4;
        final double MAX_DISTANCE = Math.min(galaxyWidth, galaxyHeight) * 0.12;

        List<StarSystem> systems = galaxy.getSystems();

        for (StarSystem system : systems) {
            List<StarSystem> sorted = systems.stream()
                    .filter(s -> s != system)
                    .sorted(Comparator.comparingDouble(system::distanceTo))
                    .toList();

            int target = MIN_CONNECTIONS + (Math.random() < 0.35 ? 1 : 0) + (Math.random() < 0.15 ? 1 : 0);
            target = Math.min(target, MAX_CONNECTIONS);

            int added = 0;

            for (StarSystem other : sorted) {
                if (added >= target) break;

                if (system.distanceTo(other) > MAX_DISTANCE) continue;

                if (!system.getNeighbors().contains(other) &&
                        canConnect(system, other, MAX_CONNECTIONS)) {
                    system.addNeighbor(other);
                    other.addNeighbor(system);
                    added++;
                }
            }
        }
    }

    private static void forceSolConnections(Galaxy galaxy) {

        StarSystem sol = galaxy.getSystems().stream()
                .filter(s -> s.getName().equals("Sol"))
                .findFirst().orElse(null);
        if (sol == null) return;

        for (StarSystem system : galaxy.getSystems()) {

            if (system.getName().equals("Alpha Centauri") || system.getName().equals("Sirius")) {
                sol.addNeighbor(system);
                system.addNeighbor(sol);
            }
        }
    }

    private static double distance(int x1, int y1, int x2, int y2) {
        return Math.hypot(x1 - x2, y1 - y2);
    }

    private static void ensureConnectivity(Galaxy galaxy) {
        List<StarSystem> systems = galaxy.getSystems();
        Set<StarSystem> visited = new HashSet<>();

        dfs(systems.get(0), visited);

        while (visited.size() < systems.size()) {

            StarSystem a = null;
            StarSystem b = null;

            double best = Double.MAX_VALUE;

            for (StarSystem s1 : visited) {

                for (StarSystem s2 : systems) {

                    if (visited.contains(s2)) continue;

                    double d = s1.distanceTo(s2);

                    if (d < best) {
                        best = d;
                        a = s1;
                        b = s2;
                    }
                }
            }

            if (a != null && b != null) {
                a.addNeighbor(b);
                b.addNeighbor(a);
                dfs(b, visited);
            }
        }
    }

    private static void dfs(StarSystem system, Set<StarSystem> visited) {
        if (!visited.add(system)) return;

        for (StarSystem n : system.getNeighbors()) {
            dfs(n, visited);
        }
    }

    private static StarSystem generateMaskedStar(String name, Galaxy galaxy, Random random) {
        for (int i = 0; i < 5000; i++) {

            int x = random.nextInt(galaxyWidth);
            int y = random.nextInt(galaxyHeight);

            if (!MASK.isAllowedWeighted(x, y, galaxyWidth, galaxyHeight, 1.2)) continue;

            if (!isFarEnough(x, y, galaxy)) continue;

            return new StarSystem(name, x, y);
        }

        throw new RuntimeException("Nie udało się umieścić gwiazdy: " + name);
    }

    private static void generateNearbyStar(String name, StarSystem center, Galaxy galaxy, Random random) {
        for (int i = 0; i < 2000; i++) {

            double angle = random.nextDouble() * Math.PI * 2;
            double dist = 120 + random.nextDouble() * 120;

            int x = (int) (center.getX() + Math.cos(angle) * dist);
            int y = (int) (center.getY() + Math.sin(angle) * dist);

            if (x < 0 || y < 0 || x >= galaxyWidth || y >= galaxyHeight) continue;

            if (!MASK.isAllowedWeighted(x, y, galaxyWidth, galaxyHeight, 1.0)) continue;

            if (!isFarEnough(x, y, galaxy)) continue;

            galaxy.getSystems().add(new StarSystem(name, x, y));
            return;
        }

        System.err.println("Nie udało się umieścić: " + name);
    }

    private static void calculateGalaxySize(MapSize size) {
        int stars = size.getStarCount();

        double areaPerStar = switch (size) {
            case SMALL -> 45_000;
            case MEDIUM -> 38_000;
            case LARGE -> 30_000;
        };

        int side = (int) Math.sqrt(stars * areaPerStar);

        galaxyWidth = side;
        galaxyHeight = side;
    }

    private static boolean canConnect(StarSystem a, StarSystem b, int max) {
        return a.getNeighbors().size() < max && b.getNeighbors().size() < max;
    }

    private static void centerGalaxy(Galaxy galaxy) {

        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;

        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (StarSystem s : galaxy.getSystems()) {

            minX = Math.min(minX, s.getX());
            maxX = Math.max(maxX, s.getX());

            minY = Math.min(minY, s.getY());
            maxY = Math.max(maxY, s.getY());

        }
        int galaxyCenterX = galaxyWidth / 2;
        int galaxyCenterY = galaxyHeight / 2;

        int systemsCenterX = (minX + maxX) / 2;
        int systemsCenterY = (minY + maxY) / 2;

        int offsetX = galaxyCenterX - systemsCenterX;
        int offsetY = galaxyCenterY - systemsCenterY;

        for (StarSystem s : galaxy.getSystems()) {
            s.setX(s.getX() + offsetX);
            s.setY(s.getY() + offsetY);
        }
    }

    private static void generateOrbits(StarSystem system, Random random) {

        int orbitCount;

        double roll = random.nextDouble();
        if (roll < 0.02) orbitCount = 0;
        else if (roll < 0.10) orbitCount = 1;
        else if (roll < 0.25) orbitCount = 2;
        else if (roll < 0.55) orbitCount = 3;
        else orbitCount = 4;

        for (int i = 1; i <= orbitCount; i++) {

            double r = random.nextDouble();
            OrbitObject object;

            if (r < 0.60) {
                object = new Planet(randomPlanetType(random));
            } else if (r < 0.80) {
                object = new AsteroidField();
            } else {
                object = new GasGiant();
            }

            system.getOrbits().add(new OrbitSlot(i, object));
        }
    }

    private static PlanetType randomPlanetType(Random random) {

        PlanetType[] values = PlanetType.values();

        if (random.nextDouble() < 0.45) {
            PlanetType[] bad = {
                    PlanetType.BARREN,
                    PlanetType.TOXIC,
                    PlanetType.RADIATED,
                    PlanetType.VOLCANIC,
                    PlanetType.ICE
            };
            return bad[random.nextInt(bad.length)];
        } else {
            PlanetType[] good = {
                    PlanetType.TERRAN,
                    PlanetType.OCEAN,
                    PlanetType.DESERT,
                    PlanetType.TUNDRA
            };
            return good[random.nextInt(good.length)];
        }
    }

    private static void setupHomeSystem(Galaxy galaxy) {
        StarSystem home = galaxy.getHomeSystem();
        if (home == null) return;

        Planet startPlanet = new Planet(PlanetType.TERRAN);
        startPlanet.colonizeHomePlanet();
        startPlanet.setMoon(startPlanet);

        startPlanet.getBuildings().add(new Building(BuildingType.OSADA_GORNICZA));
        startPlanet.getBuildings().add(new Building(BuildingType.CENTRUM_ADMINISTRACYJNE));
        startPlanet.getBuildings().add(new Building(BuildingType.WIEZA_KOMUNIKACYJNA));
        startPlanet.getBuildings().add(new Building(BuildingType.TARG_KOLONIALNY));

        if (home.getOrbits().isEmpty()) {
            home.addOrbit(new OrbitSlot(1, startPlanet));
        } else {
            home.getOrbits().set(0, new OrbitSlot(1, startPlanet));
        }

        Fleet startingFleet = new Fleet(home);
        startingFleet.addShip(new Ship(ShipType.SCOUT));
        startingFleet.addShip(new Ship(ShipType.SCOUT));
        home.addFleet(startingFleet);

        Enemy enemy = setupAIPlayer(galaxy, home);
        galaxy.setEnemy(enemy);
    }

    private static Enemy setupAIPlayer(Galaxy galaxy, StarSystem playerHome) {
        Enemy ai = new Enemy("Imperium Galaktyczne", java.awt.Color.RED);

        List<StarSystem> candidateSystems = findDistantSystems(galaxy, playerHome);

        if (candidateSystems.isEmpty()) return ai;

        StarSystem aiHome = candidateSystems.get(new java.util.Random().nextInt(candidateSystems.size()));
        ai.setHomeSystem(aiHome);

        Planet aiStartPlanet = new Planet(PlanetType.TERRAN);
        aiStartPlanet.colonizeHomePlanetForAI(ai);
        aiStartPlanet.setMoon(aiStartPlanet);

        aiStartPlanet.getBuildings().add(new Building(BuildingType.OSADA_GORNICZA));
        aiStartPlanet.getBuildings().add(new Building(BuildingType.CENTRUM_ADMINISTRACYJNE));
        aiStartPlanet.getBuildings().add(new Building(BuildingType.WIEZA_KOMUNIKACYJNA));
        aiStartPlanet.getBuildings().add(new Building(BuildingType.TARG_KOLONIALNY));

        if (aiHome.getOrbits().isEmpty()) {
            aiHome.addOrbit(new OrbitSlot(1, aiStartPlanet));
        } else {
            aiHome.getOrbits().set(0, new OrbitSlot(1, aiStartPlanet));
        }

        Fleet aiFleet = new Fleet(aiHome, ai);
        aiFleet.addShip(new Ship(ShipType.SCOUT, ai));
        aiFleet.addShip(new Ship(ShipType.SCOUT, ai));
        aiHome.addFleet(aiFleet);

        return ai;
    }

    private static List<StarSystem> findDistantSystems(Galaxy galaxy, StarSystem playerHome) {
        List<StarSystem> allSystems = new ArrayList<>(galaxy.getSystems());

        Map<StarSystem, Integer> distances = new HashMap<>();

        for (StarSystem system : allSystems) {
            if (system == playerHome) continue;

            List<StarSystem> path = Pathfinder.findPath(playerHome, system);
            if (path != null) {
                int distance = path.size() - 1;
                distances.put(system, distance);
            }
        }

        if (distances.isEmpty()) return List.of();

        int maxDistance = distances.values().stream().max(Integer::compareTo).orElse(0);

        if (maxDistance < 3) return List.of();

        int minAcceptableDistance = Math.max(maxDistance - 1, 4);

        return distances.entrySet().stream()
                .filter(e -> e.getValue() >= minAcceptableDistance)
                .map(Map.Entry::getKey)
                .filter(s -> s.getOrbits().stream()
                        .anyMatch(o -> o.getObject() instanceof Planet p && p.isHabitable()))
                .collect(java.util.stream.Collectors.toList());
    }
}