package dev.ghost.heightcontrol.manager;

import dev.ghost.heightcontrol.model.HeightZone;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class ZoneManager {

    private static final Logger LOGGER = LogManager.getLogger("HeightControl");
    private static final ZoneManager INSTANCE = new ZoneManager();
    public static ZoneManager get() { return INSTANCE; }

    private final Map<String, HeightZone> zones = new LinkedHashMap<>();
    private Path saveFile;

    private ZoneManager() {}

    public void init(Path configDir) {
        try { Files.createDirectories(configDir); } catch (IOException e) { LOGGER.error("Can't create config dir", e); }
        saveFile = configDir.resolve("zones.txt");
        load();
    }

    private void load() {
        zones.clear();
        if (!Files.exists(saveFile)) return;
        try (BufferedReader br = Files.newBufferedReader(saveFile)) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                HeightZone z = HeightZone.deserialize(line);
                if (z != null) zones.put(z.getName().toLowerCase(), z);
            }
        } catch (IOException e) { LOGGER.error("Failed to load zones.txt", e); }
        LOGGER.info("HeightControl: loaded {} zone(s).", zones.size());
    }

    public void save() {
        if (saveFile == null) return;
        try (BufferedWriter bw = Files.newBufferedWriter(saveFile)) {
            bw.write("# HeightControl zones\n");
            for (HeightZone z : zones.values()) { bw.write(z.serialize()); bw.newLine(); }
        } catch (IOException e) { LOGGER.error("Failed to save zones.txt", e); }
    }

    public void add(HeightZone zone) { zones.put(zone.getName().toLowerCase(), zone); save(); }

    public boolean delete(String name) {
        if (zones.remove(name.toLowerCase()) != null) { save(); return true; }
        return false;
    }

    /**
     * Update the height cap of an existing zone.
     * @return false if the zone doesn't exist.
     */
    public boolean setHeight(String name, int maxY) {
        HeightZone existing = zones.get(name.toLowerCase());
        if (existing == null) return false;
        zones.put(name.toLowerCase(), existing.withMaxY(maxY));
        save();
        return true;
    }

    public HeightZone get(String name) { return zones.get(name.toLowerCase()); }
    public boolean exists(String name) { return zones.containsKey(name.toLowerCase()); }
    public Collection<HeightZone> all() { return Collections.unmodifiableCollection(zones.values()); }

    /**
     * Returns the zone at (x, z, dim), or null if not inside any zone.
     * Use this instead of the old boolean isInZone so callers can read the zone's maxY.
     */
    public HeightZone getZoneAt(int x, int z, String dim) {
        for (HeightZone zone : zones.values()) {
            if (zone.containsXZ(x, z, dim)) return zone;
        }
        return null;
    }
}
