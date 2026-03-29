package dev.ghost.heightcontrol.model;

public class HeightZone {

    private final String name;
    private final String dimension;
    private final int minX, minZ, maxX, maxZ;

    /** The Y cap enforced OUTSIDE all zones. */
    public static final int NORMAL_CAP = 320;

    /** Sentinel: no cap inside this zone (full world height allowed). */
    public static final int UNLIMITED = Integer.MAX_VALUE;

    /**
     * Per-zone Y cap. Building above this Y is denied even inside this zone.
     * UNLIMITED means no extra restriction — players can reach the world ceiling.
     */
    private int maxY;

    public HeightZone(String name, String dimension,
                      int minX, int minZ, int maxX, int maxZ) {
        this(name, dimension, minX, minZ, maxX, maxZ, UNLIMITED);
    }

    public HeightZone(String name, String dimension,
                      int minX, int minZ, int maxX, int maxZ, int maxY) {
        this.name      = name;
        this.dimension = dimension;
        this.minX      = Math.min(minX, maxX);
        this.minZ      = Math.min(minZ, maxZ);
        this.maxX      = Math.max(minX, maxX);
        this.maxZ      = Math.max(minZ, maxZ);
        this.maxY      = maxY;
    }

    public String getName()      { return name; }
    public String getDimension() { return dimension; }
    public int getMinX()         { return minX; }
    public int getMinZ()         { return minZ; }
    public int getMaxX()         { return maxX; }
    public int getMaxZ()         { return maxZ; }
    public int getSizeX()        { return maxX - minX + 1; }
    public int getSizeZ()        { return maxZ - minZ + 1; }
    public int getMaxY()         { return maxY; }
    public boolean hasHeightLimit() { return maxY != UNLIMITED; }

    /** Returns a copy of this zone with a new maxY. */
    public HeightZone withMaxY(int newMaxY) {
        return new HeightZone(name, dimension, minX, minZ, maxX, maxZ, newMaxY);
    }

    public boolean containsXZ(int x, int z, String dim) {
        return this.dimension.equals(dim)
            && x >= minX && x <= maxX
            && z >= minZ && z <= maxZ;
    }

    /**
     * Serialized format (backwards-compatible):
     *   name,dimension,minX,minZ,maxX,maxZ[,maxY]
     * Old lines with 6 fields load fine (maxY defaults to UNLIMITED).
     */
    public String serialize() {
        String base = name + "," + dimension + "," + minX + "," + minZ + "," + maxX + "," + maxZ;
        return hasHeightLimit() ? base + "," + maxY : base;
    }

    public static HeightZone deserialize(String line) {
        String[] p = line.split(",", 7);
        if (p.length < 6) return null;
        try {
            int maxY = (p.length >= 7) ? Integer.parseInt(p[6]) : UNLIMITED;
            return new HeightZone(p[0], p[1],
                Integer.parseInt(p[2]), Integer.parseInt(p[3]),
                Integer.parseInt(p[4]), Integer.parseInt(p[5]),
                maxY);
        } catch (NumberFormatException e) { return null; }
    }
}
