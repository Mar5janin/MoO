package controller;

import model.galaxy.StarSystem;
import java.util.*;

public class Pathfinder {

    public static List<StarSystem> findPath(StarSystem start, StarSystem destination) {
        if (start == null || destination == null) {
            return null;
        }

        if (start == destination) {
            return List.of(start);
        }

        Queue<StarSystem> queue = new LinkedList<>();
        Map<StarSystem, StarSystem> parentMap = new HashMap<>();
        Set<StarSystem> visited = new HashSet<>();

        queue.offer(start);
        visited.add(start);
        parentMap.put(start, null);

        while (!queue.isEmpty()) {
            StarSystem current = queue.poll();

            if (current == destination) {
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
}