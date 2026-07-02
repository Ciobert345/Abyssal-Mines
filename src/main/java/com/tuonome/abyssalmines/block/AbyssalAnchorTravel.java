package com.tuonome.abyssalmines.block;

import com.tuonome.abyssalmines.ModDimensions;
import com.tuonome.abyssalmines.world.MiningSpawnHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Salva e ripristina il punto di ritorno dell'ancora usata per entrare in mining_dim.
 */
public final class AbyssalAnchorTravel {

    private static final String RETURN_DIM = "abyssal_mines.return_dim";
    private static final String RETURN_X = "abyssal_mines.return_x";
    private static final String RETURN_Y = "abyssal_mines.return_y";
    private static final String RETURN_Z = "abyssal_mines.return_z";

    private AbyssalAnchorTravel() {}

    public static void saveReturnPoint(ServerPlayer player, ResourceKey<Level> dimension, BlockPos anchorPos) {
        CompoundTag data = player.getPersistentData();
        data.putString(RETURN_DIM, dimension.location().toString());
        data.putInt(RETURN_X, anchorPos.getX());
        data.putInt(RETURN_Y, anchorPos.getY());
        data.putInt(RETURN_Z, anchorPos.getZ());
    }

    @Nullable
    public static DimensionTransition createReturnTransition(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        if (!data.contains(RETURN_DIM)) {
            return null;
        }

        ResourceKey<Level> returnDim = ResourceKey.create(
                Registries.DIMENSION,
                ResourceLocation.parse(data.getString(RETURN_DIM))
        );
        ServerLevel returnLevel = player.server.getLevel(returnDim);
        if (returnLevel == null) {
            return null;
        }

        BlockPos anchorPos = new BlockPos(
                data.getInt(RETURN_X),
                data.getInt(RETURN_Y),
                data.getInt(RETURN_Z)
        );

        return new DimensionTransition(
                returnLevel,
                new Vec3(anchorPos.getX() + 0.5, anchorPos.getY() + 1.0, anchorPos.getZ() + 0.5),
                player.getDeltaMovement(),
                player.getYRot(),
                player.getXRot(),
                DimensionTransition.DO_NOTHING
        );
    }

    @Nullable
    public static DimensionTransition createEntryTransition(ServerPlayer player, ServerLevel targetLevel) {
        return new DimensionTransition(
                targetLevel,
                new Vec3(0.5, 80.0, 0.5),
                player.getDeltaMovement(),
                player.getYRot(),
                player.getXRot(),
                entity -> {
                    if (entity instanceof ServerPlayer serverPlayer
                            && serverPlayer.server.getLevel(ModDimensions.MINING_DIM_KEY) == targetLevel) {
                        BlockPos spawnPos = MiningSpawnHelper.ensureSpawnReady(targetLevel);
                        serverPlayer.teleportTo(
                                targetLevel,
                                spawnPos.getX() + 0.5,
                                spawnPos.getY(),
                                spawnPos.getZ() + 0.5,
                                serverPlayer.getYRot(),
                                serverPlayer.getXRot());
                    }
                }
        );
    }

    public static boolean isInMiningDim(Level level) {
        return level.dimension().equals(ModDimensions.MINING_DIM_KEY);
    }
}
