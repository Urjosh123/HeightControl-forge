package dev.ghost.heightcontrol.model;

public class HeightZone {

    private final String name;
    private final String dimension;
    private final int minX, minZ, maxX, maxZ;

    // The normal cap applied OUTSIDE all zones
    public static final int NORMAL_CAP = 320;

    public HeightZone(String name, String dimension,
                      int minX, int minZ, int maxX, int maxZ) {
        this.name      = name;
        this.dimension = dimension;
        this.minX      = Math.min(minX, maxX);
        this.minZ      = Math.min(minZ, maxZ);
        this.maxX      = Math.max(minX, maxX);
        this.maxZ      = Math.max(minZ, maxZ);
    }

    public String getName()      { return name; }
    public String getDimension() { return dimension; }
    public int getMinX()         { return minX; }
    public int getMinZ()         { return minZ; }
    public int getMaxX()         { return maxX; }
    public int getMaxZ()         { return maxZ; }
    public int getSizeX()        { return maxX - minX + 1; }
    public int getSizeZ()        { return maxZ - minZ + 1; }

    public boolean containsXZ(int x, int z, String dim) {
        return this.dimension.equals(dim)
            && x >= minX && x <= maxX
            && z >= minZ && z <= maxZ;
    }

    /** name,dimension,minX,minZ,maxX,maxZ */
    public String serialize() {
        return name + "," + dimension + "," + minX + "," + minZ + "," + maxX + "," + maxZ;
    }

    public static HeightZone deserialize(String line) {
        String[] p = line.split(",", 6);
        if (p.length < 6) return null;
        try {
            return new HeightZone(p[0], p[1],
                Integer.parseInt(p[2]), Integer.parseInt(p[3]),
                Integer.parseInt(p[4]), Integer.parseInt(p[5]));
        } catch (NumberFormatException e) { return null; }
    }
}
