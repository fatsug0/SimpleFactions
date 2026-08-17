package com.gus.simpleFactions.FactionHandlers.FactionObjectServices;

import com.flowpowered.math.vector.Vector2d;
import com.flowpowered.math.vector.Vector3d;
import com.gus.simpleFactions.FactionHandlers.Objects.FactionObject;
import com.gus.simpleFactions.SimpleFactions;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.markers.ExtrudeMarker;
import de.bluecolored.bluemap.api.math.Color;
import de.bluecolored.bluemap.api.math.Shape;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FactionMapRenderService {

    private final SimpleFactions plugin;
    public FactionMapRenderService(SimpleFactions plugin) {
        this.plugin = plugin;
        USE_BLUEMAP_ADDON = plugin.getConfig().getBoolean("enable-bluemap-addon");
    }

    private final boolean USE_BLUEMAP_ADDON;
    public boolean getUSE_BLUEMAP_ADDON() {
        return USE_BLUEMAP_ADDON;
    }

    /**
     * Rebuilds and redraws every BlueMap marker for this faction from its current claimed
     * chunks - call this after any claim/unclaim. Adjacent claimed chunks of the same type are
     * merged into a single marker per contiguous region (rather than one marker per chunk), so
     * the extrude marker "continues" seamlessly instead of showing a seam at every chunk border.
     */
    public void RedrawClaims(FactionObject faction) {
        faction.getFactionMarkerSet().getMarkers().clear();

        addClaimMarkers(faction, faction.getHardClaimedChunks(), "FULL");
        addClaimMarkers(faction, faction.getWeakClaimedChunks(), "WEAK");

        pushToMap(faction);
    }

    /** Removes this faction entirely from the map - call this when a faction is disbanded. */
    public void RemoveFactionFromMap(FactionObject faction) {
        faction.getFactionMarkerSet().getMarkers().clear();
        BlueMapAPI.onEnable(api -> api.getWorld(Bukkit.getWorld("world")).ifPresent(world -> {
            for (BlueMapMap map : world.getMaps()) {
                map.getMarkerSets().remove("FactionMarkerSet" + faction.getFactionName() + " claims");
            }
        }));
    }

    private void addClaimMarkers(FactionObject faction, ArrayList<Chunk> claimedChunks, String claimType) {
        if (claimedChunks.isEmpty()) return;

        World world = claimedChunks.get(0).getWorld();
        float minY = world.getMinHeight();
        float maxY = world.getMaxHeight();

        int regionIndex = 0;
        for (List<Chunk> region : connectedComponents(claimedChunks)) {
            List<List<Vector2d>> loops = traceBoundaryLoops(region);
            if (loops.isEmpty()) continue;

            // Drop redundant points along straight runs (one point per chunk-corner otherwise),
            // keeping only actual corners - BlueMap's polygon triangulation falls apart on long
            // runs of collinear points, rendering as a mess of thin vertical slivers instead of
            // one solid shape.
            loops.replaceAll(this::simplifyCollinear);
            loops.removeIf(loop -> loop.size() < 3);
            if (loops.isEmpty()) continue;

            // The loop with the largest bounding box is the outer boundary of the region;
            // any other loops are unclaimed pockets fully enclosed inside it (holes).
            loops.sort((a, b) -> Double.compare(boundingArea(b), boundingArea(a)));
            Shape outer = new Shape(loops.get(0));
            Shape[] holes = loops.subList(1, loops.size()).stream().map(Shape::new).toArray(Shape[]::new);

            Chunk anchor = region.get(0);

            // Extrude the whole merged region through the full height of the world, so it
            // renders as one continuous, seamless column instead of one box per chunk.
            ExtrudeMarker extrudeMarker = ExtrudeMarker.builder()
                    .shape(outer, minY, maxY)
                    .holes(holes)
                    .depthTestEnabled(false)
                    .lineWidth(2)
                    .lineColor(factionColor(faction, 0.8f))
                    .fillColor(factionColor(faction, 0.3f))
                    .detail(faction.getFactionName() + " claimed " + claimType + " land")
                    .minDistance(10)
                    .maxDistance(10000)
                    .label(faction.getFactionName() + " claimed " + claimType + " land")
                    .position(new Vector3d(anchor.getX() * 16, 64, anchor.getZ() * 16))
                    .build();

            faction.getFactionMarkerSet().getMarkers().put("claimedLand_" + claimType + "_" + regionIndex, extrudeMarker);

            regionIndex++;
        }
    }

    /** Removes points that don't represent an actual change in direction (i.e. lie exactly on
     *  the straight line between their neighbors), so long straight runs collapse to their two
     *  true endpoints instead of keeping one point per chunk-corner along the way. */
    private List<Vector2d> simplifyCollinear(List<Vector2d> loop) {
        if (loop.size() < 3) return loop;

        List<Vector2d> simplified = new ArrayList<>();
        int n = loop.size();
        for (int i = 0; i < n; i++) {
            Vector2d prev = loop.get((i - 1 + n) % n);
            Vector2d current = loop.get(i);
            Vector2d next = loop.get((i + 1) % n);

            double cross = (current.getX() - prev.getX()) * (next.getY() - current.getY())
                    - (current.getY() - prev.getY()) * (next.getX() - current.getX());
            if (Math.abs(cross) > 1e-6) {
                simplified.add(current);
            }
        }
        return simplified.size() >= 3 ? simplified : loop;
    }

    /** Groups a faction's claimed chunks into sets that are connected via shared (N/S/E/W) edges. */
    private List<List<Chunk>> connectedComponents(List<Chunk> chunks) {
        List<List<Chunk>> components = new ArrayList<>();
        Set<Chunk> remaining = new HashSet<>(chunks);

        while (!remaining.isEmpty()) {
            Chunk seed = remaining.iterator().next();
            remaining.remove(seed);

            List<Chunk> component = new ArrayList<>();
            Deque<Chunk> queue = new ArrayDeque<>();
            queue.add(seed);

            while (!queue.isEmpty()) {
                Chunk current = queue.poll();
                component.add(current);
                for (int[] dir : new int[][]{{0, -1}, {0, 1}, {-1, 0}, {1, 0}}) {
                    Chunk n = neighbor(current, dir[0], dir[1]);
                    if (remaining.remove(n)) {
                        queue.add(n);
                    }
                }
            }
            components.add(component);
        }
        return components;
    }

    /**
     * Traces the boundary of a connected group of chunks into one or more closed point loops
     * (one outer loop, plus one loop for each fully-enclosed unclaimed pocket, if any). Standard
     * grid boundary tracing: each chunk contributes an edge along every side that faces a chunk
     * NOT in the region, oriented so consecutive edges chain head-to-tail around the loop.
     */
    private List<List<Vector2d>> traceBoundaryLoops(List<Chunk> region) {
        Set<Chunk> set = new HashSet<>(region);
        Map<Vector2d, Deque<Vector2d>> edges = new java.util.HashMap<>();

        for (Chunk chunk : region) {
            int x0 = chunk.getX() * 16, z0 = chunk.getZ() * 16;
            int x1 = x0 + 16, z1 = z0 + 16;

            if (!set.contains(neighbor(chunk, 0, -1))) addEdge(edges, new Vector2d(x1, z0), new Vector2d(x0, z0)); // north
            if (!set.contains(neighbor(chunk, 0, 1)))  addEdge(edges, new Vector2d(x0, z1), new Vector2d(x1, z1)); // south
            if (!set.contains(neighbor(chunk, -1, 0))) addEdge(edges, new Vector2d(x0, z0), new Vector2d(x0, z1)); // west
            if (!set.contains(neighbor(chunk, 1, 0)))  addEdge(edges, new Vector2d(x1, z1), new Vector2d(x1, z0)); // east
        }

        List<List<Vector2d>> loops = new ArrayList<>();
        int safetyLimit = region.size() * 4 + 8; // a chunk contributes at most 4 boundary edges

        while (!edges.isEmpty()) {
            Vector2d start = edges.keySet().iterator().next();
            List<Vector2d> loop = new ArrayList<>();
            Vector2d current = start;

            do {
                loop.add(current);
                Deque<Vector2d> candidates = edges.get(current);
                if (candidates == null || candidates.isEmpty()) break; // malformed - bail out safely
                Vector2d next = candidates.poll();
                if (candidates.isEmpty()) edges.remove(current);
                current = next;
            } while (!current.equals(start) && loop.size() <= safetyLimit);

            if (loop.size() >= 3) loops.add(loop);
        }
        return loops;
    }

    private void addEdge(Map<Vector2d, Deque<Vector2d>> edges, Vector2d from, Vector2d to) {
        edges.computeIfAbsent(from, k -> new ArrayDeque<>()).add(to);
    }

    private double boundingArea(List<Vector2d> loop) {
        double minX = Double.MAX_VALUE, maxX = -Double.MAX_VALUE, minZ = Double.MAX_VALUE, maxZ = -Double.MAX_VALUE;
        for (Vector2d p : loop) {
            minX = Math.min(minX, p.getX());
            maxX = Math.max(maxX, p.getX());
            minZ = Math.min(minZ, p.getY());
            maxZ = Math.max(maxZ, p.getY());
        }
        return (maxX - minX) * (maxZ - minZ);
    }

    private Chunk neighbor(Chunk chunk, int dx, int dz) {
        return chunk.getWorld().getChunkAt(chunk.getX() + dx, chunk.getZ() + dz);
    }

    private void pushToMap(FactionObject faction) {
        BlueMapAPI.onEnable(api -> api.getWorld(Bukkit.getWorld("world")).ifPresent(world -> {
            for (BlueMapMap map : world.getMaps()) {
                map.getMarkerSets().put("FactionMarkerSet" + faction.getFactionName() + " claims", faction.getFactionMarkerSet());
            }
        }));
    }

    /**
     * The faction's own color, as currently shown in its tab-list prefix (see
     * FactionFormatterService#setTeamPrefix, which keeps FactionObject#getFactionColors in sync).
     * Falls back to a neutral gray if the faction has no color yet, or its color isn't a plain
     * hex value (e.g. a named MiniMessage color like "red" used in the prefix instead of a hex
     * code) - that can't be reliably converted to an RGB value here.
     */
    private Color factionColor(FactionObject faction, float alpha) {
        ArrayList<String> colors = faction.getFactionColors();
        if (colors.isEmpty()) {
            return new Color(127, 127, 127, alpha);
        }

        String hex = colors.get(0).trim();
        try {
            int rgb = Integer.decode(hex.startsWith("#") ? hex : "#" + hex);
            return new Color(rgb, alpha);
        } catch (NumberFormatException e) {
            return new Color(127, 127, 127, alpha);
        }
    }
}
