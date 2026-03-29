package dev.ghost.heightcontrol.manager;

import net.minecraft.core.BlockPos;
import java.util.*;

public class SelectionManager {

    private static final SelectionManager INSTANCE = new SelectionManager();
    public static SelectionManager get() { return INSTANCE; }

    public static class Selection {
        public BlockPos pos1, pos2;
        public String dimension;
        public boolean isComplete() { return pos1 != null && pos2 != null; }
    }

    private final Map<UUID, Selection> map = new HashMap<>();

    public void setPos1(UUID id, BlockPos pos, String dim) {
        Selection s = map.computeIfAbsent(id, k -> new Selection());
        s.pos1 = pos; s.dimension = dim;
    }

    public void setPos2(UUID id, BlockPos pos, String dim) {
        Selection s = map.computeIfAbsent(id, k -> new Selection());
        s.pos2 = pos; s.dimension = dim;
    }

    public Selection get(UUID id) { return map.get(id); }
    public void clear(UUID id) { map.remove(id); }
}
