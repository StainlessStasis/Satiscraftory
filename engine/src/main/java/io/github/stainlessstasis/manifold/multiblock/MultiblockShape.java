package io.github.stainlessstasis.manifold.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MultiblockShape {
    private final int width;  // x
    private final int depth;  // z
    private final int height; // y
    private final BlockPos controllerOffset;
    private final Set<BlockPos> unfilledOffsets; // canonical offsets (relative to controller) which are left without a filler block

    public MultiblockShape(int width, int depth, int height, BlockPos controllerOffset) {
        this(width, depth, height, controllerOffset, Set.of());
    }

    public MultiblockShape(int width, int depth, int height, BlockPos controllerOffset, Set<BlockPos> unfilledOffsets) {
        this.width = width;
        this.depth = depth;
        this.height = height;
        this.controllerOffset = controllerOffset;
        this.unfilledOffsets = unfilledOffsets;
    }

    public int width() { return width; }
    public int depth() { return depth; }
    public int height() { return height; }
    public BlockPos controllerOffset() { return controllerOffset; }

    /** Every cell in the box with canonical orientation, relative to the controller */
    public List<BlockPos> canonicalAllOffsets() {
        List<BlockPos> result = new ArrayList<>(width * depth * height);
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                for (int y = 0; y < height; y++) {
                    result.add(new BlockPos(x, y, z).subtract(controllerOffset));
                }
            }
        }
        return result;
    }

    /** Cells that should receive a filler block */
    public List<BlockPos> canonicalFillerOffsets() {
        List<BlockPos> result = new ArrayList<>();
        for (BlockPos offset : canonicalAllOffsets()) {
            if (offset.equals(BlockPos.ZERO)) continue;
            if (unfilledOffsets.contains(offset)) continue;
            result.add(offset);
        }
        return result;
    }

    /** Rotates a canonical offset to match the given horizontal facing */
    public static BlockPos rotate(BlockPos canonicalOffset, Direction facing) {
        int x = canonicalOffset.getX();
        int y = canonicalOffset.getY();
        int z = canonicalOffset.getZ();
        return switch (facing) {
            case NORTH -> new BlockPos(x, y, z);
            case SOUTH -> new BlockPos(-x, y, -z);
            case WEST  -> new BlockPos(z, y, -x);
            case EAST  -> new BlockPos(-z, y, x);
            default -> throw new IllegalArgumentException("Multiblocks only support horizontal facings, got " + facing);
        };
    }

    /** Absolute world positions of cells that should receive a filler block */
    public List<BlockPos> absoluteFillerPositions(BlockPos controllerPos, Direction facing) {
        List<BlockPos> result = new ArrayList<>();
        for (BlockPos canonical : canonicalFillerOffsets()) {
            result.add(controllerPos.offset(rotate(canonical, facing)));
        }
        return result;
    }

    /** Every cell's absolute world position, including the controller's */
    public List<BlockPos> absoluteAllPositions(BlockPos controllerPos, Direction facing) {
        List<BlockPos> result = new ArrayList<>();
        for (BlockPos canonical : canonicalAllOffsets()) {
            result.add(controllerPos.offset(rotate(canonical, facing)));
        }
        return result;
    }
}