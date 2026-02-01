package model;

import java.util.*;

public class Pathfinder {

    /**
     * Znajduje najkrótszą ścieżkę między dwoma systemami używając algorytmu BFS
     *
     * @param start System startowy
     * @param destination System docelowy
     * @return Lista systemów tworzących ścieżkę (włącznie ze startem i celem), lub null jeśli nie ma połączenia
     */
    public static List<StarSystem> findPath(StarSystem start, StarSystem destination) {
        if (start == null || destination == null) {
            return null;
        }

        if (start == destination) {
            return List.of(start);
        }

        // BFS do znajdowania najkrótszej ścieżki
        Queue<StarSystem> queue = new LinkedList<>();
        Map<StarSystem, StarSystem> parentMap = new HashMap<>();
        Set<StarSystem> visited = new HashSet<>();

        queue.offer(start);
        visited.add(start);
        parentMap.put(start, null);

        while (!queue.isEmpty()) {
            StarSystem current = queue.poll();

            if (current == destination) {
                // Znaleziono - odtwórz ścieżkę
                return reconstructPath(parentMap, destination);
            }

            for (StarSystem neighbor : current.getNeighbors()) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    parentMap.put(neighbor, current);
                    queue.offer(neighbor);
                }
            }
        }

        // Nie znaleziono ścieżki
        return null;
    }

    private static List<StarSystem> reconstructPath(Map<StarSystem, StarSystem> parentMap, StarSystem destination) {
        List<StarSystem> path = new ArrayList<>();
        StarSystem current = destination;

        while (current != null) {
            path.add(current);
            current = parentMap.get(current);
        }

        Collections.reverse(path);
        return path;
    }

    /**
     * Oblicza całkowitą odległość ścieżki
     */
    public static int calculatePathLength(List<StarSystem> path) {
        if (path == null || path.size() <= 1) {
            return 0;
        }
        return path.size() - 1; // Liczba przeskoków
    }
}