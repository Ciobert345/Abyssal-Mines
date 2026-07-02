package com.tuonome.abyssalmines.world;

import com.tuonome.abyssalmines.AbyssalMines;
import com.tuonome.abyssalmines.AbyssalSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

/**
 * Genera il punto di spawn con spawn.nbt in una caverna naturale della mining_dim.
 */
public final class MiningSpawnHelper {

    public static final ResourceLocation SPAWN_STRUCTURE_ID =
            ResourceLocation.fromNamespaceAndPath(AbyssalMines.MODID, "spawn");

    private static final ResourceLocation SPAWN_RESOURCE =
            ResourceLocation.fromNamespaceAndPath(AbyssalMines.MODID, "structures/spawn.nbt");

    private static final int HUB_X = 0;
    private static final int HUB_Z = 0;

    /** Margine minimo tra struttura e roccia della caverna. */
    private static final int CAVE_MARGIN = 2;

    /** Percentuale minima di aria nel volume (struttura + margine). */
    private static final double MIN_AIR_RATIO = 0.72;

    /** Raggio di ricerca caverne in blocchi (da hub 0,0). */
    private static final int SEARCH_RADIUS = 96;

    private MiningSpawnHelper() {}

    public static AbyssalSavedData getSavedData(ServerLevel miningLevel) {
        return miningLevel.getDataStorage().computeIfAbsent(
                new net.minecraft.world.level.saveddata.SavedData.Factory<>(
                        AbyssalSavedData::new,
                        AbyssalSavedData::load,
                        net.minecraft.util.datafix.DataFixTypes.LEVEL
                ),
                "abyssal_mines_data"
        );
    }

    public static BlockPos ensureSpawnReady(ServerLevel miningLevel) {
        AbyssalSavedData savedData = getSavedData(miningLevel);
        if (savedData.spawnStructureGenerated) {
            return new BlockPos(savedData.spawnX, savedData.spawnY, savedData.spawnZ);
        }

        StructureTemplate template = loadSpawnTemplate(miningLevel);
        Vec3i size = template.getSize();

        if (isTemplateEmpty(template)) {
            BlockPos fallback = new BlockPos(HUB_X, 61, HUB_Z);
            saveSpawn(savedData, fallback.offset(size.getX() / 2, 1, size.getZ() / 2));
            AbyssalMines.LOGGER.error(
                    "AbyssalMines: impossibile caricare spawn.nbt ({}), nessuna struttura piazzata",
                    SPAWN_RESOURCE);
            return fallback;
        }

        BlockPos structureOrigin = findCaveOrigin(miningLevel, size)
                .orElseGet(() -> {
                    AbyssalMines.LOGGER.warn("AbyssalMines: nessuna caverna adatta entro {} blocchi, uso hub (0,0)", SEARCH_RADIUS);
                    return new BlockPos(HUB_X, 61, HUB_Z);
                }).below(1);

        loadChunksForArea(miningLevel, structureOrigin, size, CAVE_MARGIN);

        StructurePlaceSettings settings = new StructurePlaceSettings()
                .setRotation(Rotation.NONE)
                .setIgnoreEntities(true);
        template.placeInWorld(miningLevel, structureOrigin, structureOrigin, settings, miningLevel.getRandom(), 3);

        BlockPos playerSpawn = getPlayerSpawnPos(structureOrigin, size);
        freePlayerIfBlocked(miningLevel, playerSpawn);
        saveSpawn(savedData, playerSpawn);

        AbyssalMines.LOGGER.info(
                "AbyssalMines: spawn.nbt {} ({}x{}x{}) in caverna a {}, player spawn {}",
                SPAWN_STRUCTURE_ID, size.getX(), size.getY(), size.getZ(), structureOrigin, playerSpawn);

        return playerSpawn;
    }

    /**
     * Cerca una caverna naturale abbastanza grande per contenere la struttura.
     * Parte dall'hub (0,0) e si espande a spirale.
     */
    private static Optional<BlockPos> findCaveOrigin(ServerLevel level, Vec3i size) {
        BlockPos bestOrigin = null;
        double bestAirRatio = 0;

        for (int radius = 0; radius <= SEARCH_RADIUS; radius++) {
            for (BlockPos sample : spiralXZ(radius)) {
                int worldX = HUB_X + sample.getX();
                int worldZ = HUB_Z + sample.getZ();
                level.getChunk(new BlockPos(worldX, 60, worldZ));

                for (int y = 100; y >= level.getMinBuildHeight() + size.getY() + CAVE_MARGIN; y--) {
                    BlockPos origin = new BlockPos(worldX, y, worldZ);
                    double airRatio = measureAirRatio(level, origin, size, CAVE_MARGIN);
                    if (airRatio >= MIN_AIR_RATIO && hasStructureFloor(level, origin, size)) {
                        AbyssalMines.LOGGER.info(
                                "AbyssalMines: caverna trovata a {} (aria {:.0f}%, r={})",
                                origin, airRatio * 100, radius);
                        return Optional.of(origin);
                    }
                    if (airRatio > bestAirRatio && hasStructureFloor(level, origin, size)) {
                        bestAirRatio = airRatio;
                        bestOrigin = origin;
                    }
                }
            }
        }

        if (bestOrigin != null && bestAirRatio >= 0.55) {
            AbyssalMines.LOGGER.warn(
                    "AbyssalMines: caverna parziale a {} (aria {:.0f}%)",
                    bestOrigin, bestAirRatio * 100);
            return Optional.of(bestOrigin);
        }

        return Optional.empty();
    }

    private static Iterable<BlockPos> spiralXZ(int radius) {
        if (radius == 0) {
            return () -> java.util.List.of(BlockPos.ZERO).iterator();
        }

        java.util.List<BlockPos> points = new java.util.ArrayList<>();
        for (int x = -radius; x <= radius; x++) {
            points.add(new BlockPos(x, 0, -radius));
            points.add(new BlockPos(x, 0, radius));
        }
        for (int z = -radius + 1; z <= radius - 1; z++) {
            points.add(new BlockPos(-radius, 0, z));
            points.add(new BlockPos(radius, 0, z));
        }
        return points;
    }

    private static double measureAirRatio(ServerLevel level, BlockPos origin, Vec3i size, int margin) {
        int air = 0;
        int total = 0;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int x = -margin; x < size.getX() + margin; x++) {
            for (int y = -margin; y < size.getY() + margin; y++) {
                for (int z = -margin; z < size.getZ() + margin; z++) {
                    pos.set(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
                    total++;
                    if (isCaveSpace(level.getBlockState(pos))) {
                        air++;
                    }
                }
            }
        }

        return total == 0 ? 0 : (double) air / total;
    }

    private static boolean hasStructureFloor(ServerLevel level, BlockPos origin, Vec3i size) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int[] xs = {0, size.getX() / 2, size.getX() - 1};
        int[] zs = {0, size.getZ() / 2, size.getZ() - 1};

        for (int x : xs) {
            for (int z : zs) {
                pos.set(origin.getX() + x, origin.getY() - 1, origin.getZ() + z);
                if (!level.getBlockState(pos).blocksMotion()) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean isCaveSpace(BlockState state) {
        return state.isAir();
    }

    private static BlockPos getPlayerSpawnPos(BlockPos origin, Vec3i size) {
        return origin.offset(size.getX() / 2, 1, size.getZ() / 2);
    }

    /** Rimuove al massimo i blocchi che coprirebbero testa/piedi del player. */
    private static void freePlayerIfBlocked(ServerLevel level, BlockPos spawn) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dy = 0; dy <= 1; dy++) {
            pos.set(spawn.getX(), spawn.getY() + dy, spawn.getZ());
            BlockState state = level.getBlockState(pos);
            if (!state.isAir() && state.blocksMotion()) {
                level.setBlock(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
            }
        }
    }

    private static void loadChunksForArea(ServerLevel level, BlockPos origin, Vec3i size, int margin) {
        int minChunkX = (origin.getX() - margin) >> 4;
        int maxChunkX = (origin.getX() + size.getX() + margin) >> 4;
        int minChunkZ = (origin.getZ() - margin) >> 4;
        int maxChunkZ = (origin.getZ() + size.getZ() + margin) >> 4;
        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                level.getChunk(cx, cz);
            }
        }
    }

    private static StructureTemplate loadSpawnTemplate(ServerLevel level) {
        StructureTemplateManager manager = level.getStructureManager();

        StructureTemplate fromManager = manager.getOrCreate(SPAWN_STRUCTURE_ID);
        if (!isTemplateEmpty(fromManager)) {
            return fromManager;
        }

        ResourceManager resourceManager = level.getServer().getResourceManager();
        var resource = resourceManager.getResource(SPAWN_RESOURCE);
        if (resource.isEmpty()) {
            AbyssalMines.LOGGER.error("AbyssalMines: risorsa {} assente nel ResourceManager", SPAWN_RESOURCE);
            logAvailableStructureResources(resourceManager);
            return fromManager;
        }

        try (InputStream stream = resource.get().open()) {
            byte[] bytes = stream.readAllBytes();
            CompoundTag tag = readStructureTag(bytes);
            StructureTemplate loaded = manager.readStructure(tag);
            if (!isTemplateEmpty(loaded)) {
                AbyssalMines.LOGGER.info("AbyssalMines: spawn.nbt caricato da {}", SPAWN_RESOURCE);
                return loaded;
            }
            AbyssalMines.LOGGER.error("AbyssalMines: spawn.nbt letto ma template risulta vuoto");
        } catch (Exception e) {
            AbyssalMines.LOGGER.error("AbyssalMines: errore lettura {}", SPAWN_RESOURCE, e);
        }

        return fromManager;
    }

    private static CompoundTag readStructureTag(byte[] bytes) throws java.io.IOException {
        if (bytes.length >= 2 && bytes[0] == 0x1f && bytes[1] == (byte) 0x8b) {
            return NbtIo.readCompressed(new ByteArrayInputStream(bytes), NbtAccounter.unlimitedHeap());
        }
        return NbtIo.read(new DataInputStream(new ByteArrayInputStream(bytes)));
    }

    private static void logAvailableStructureResources(ResourceManager resourceManager) {
        Map<ResourceLocation, Resource> resources = resourceManager.listResources(
                "structures", location -> AbyssalMines.MODID.equals(location.getNamespace()));
        if (resources.isEmpty()) {
            AbyssalMines.LOGGER.error("AbyssalMines: nessuna risorsa in data/{}/structures/", AbyssalMines.MODID);
            return;
        }
        resources.keySet().forEach(id -> AbyssalMines.LOGGER.info("AbyssalMines: risorsa struttura trovata: {}", id));
    }

    private static boolean isTemplateEmpty(StructureTemplate template) {
        Vec3i size = template.getSize();
        return size.getX() == 0 && size.getY() == 0 && size.getZ() == 0;
    }

    private static void saveSpawn(AbyssalSavedData savedData, BlockPos playerSpawn) {
        savedData.spawnStructureGenerated = true;
        savedData.spawnX = playerSpawn.getX();
        savedData.spawnY = playerSpawn.getY();
        savedData.spawnZ = playerSpawn.getZ();
        savedData.setDirty();
    }
}
