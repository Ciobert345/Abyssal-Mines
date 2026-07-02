package com.tuonome.abyssalmines.block;

import com.tuonome.abyssalmines.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Ancora abissale: clic destro per entrare in mining_dim o tornare all'ancora d'origine.
 */
public class AbyssalAnchorBlock extends Block {

    private static final int TELEPORT_COOLDOWN = 80;

    public AbyssalAnchorBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hitResult) {
        return tryTeleport(player, level, pos);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        return toItemResult(tryTeleport(player, level, pos));
    }

    private static ItemInteractionResult toItemResult(InteractionResult result) {
        return switch (result) {
            case SUCCESS -> ItemInteractionResult.SUCCESS;
            case FAIL -> ItemInteractionResult.FAIL;
            case CONSUME -> ItemInteractionResult.CONSUME;
            case CONSUME_PARTIAL -> ItemInteractionResult.CONSUME_PARTIAL;
            default -> ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        };
    }

    private static InteractionResult tryTeleport(Player player, Level level, BlockPos anchorPos) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }
        if (serverPlayer.isOnPortalCooldown()) {
            if (level instanceof ServerLevel serverLevel) {
                spawnCooldownParticles(serverLevel, serverPlayer, anchorPos);
            }
            return InteractionResult.FAIL;
        }

        DimensionTransition transition;
        if (AbyssalAnchorTravel.isInMiningDim(level)) {
            transition = AbyssalAnchorTravel.createReturnTransition(serverPlayer);
        } else {
            ServerLevel targetLevel = serverPlayer.server.getLevel(ModDimensions.MINING_DIM_KEY);
            if (targetLevel == null) {
                return InteractionResult.FAIL;
            }
            AbyssalAnchorTravel.saveReturnPoint(serverPlayer, level.dimension(), anchorPos);
            transition = AbyssalAnchorTravel.createEntryTransition(serverPlayer, targetLevel);
        }

        if (transition == null) {
            return InteractionResult.FAIL;
        }

        serverPlayer.setPortalCooldown(TELEPORT_COOLDOWN);
        serverPlayer.changeDimension(transition);
        return InteractionResult.SUCCESS;
    }

    private static void spawnCooldownParticles(ServerLevel level, ServerPlayer player, BlockPos anchorPos) {
        double centerX = anchorPos.getX() + 0.5;
        double centerZ = anchorPos.getZ() + 0.5;
        double baseY = anchorPos.getY() + 1.0;

        double radius = 0.42;
        double columnHeight = 1.6;
        int rings = 7;
        int pointsPerRing = 14;

        for (int ring = 0; ring <= rings; ring++) {
            double y = baseY + (columnHeight * ring) / rings;
            for (int point = 0; point < pointsPerRing; point++) {
                double angle = (Math.PI * 2 * point) / pointsPerRing;
                double x = centerX + radius * Math.cos(angle);
                double z = centerZ + radius * Math.sin(angle);

                level.sendParticles(player, ParticleTypes.REVERSE_PORTAL, true, x, y, z, 1, 0.0, 0.0, 0.0, 0.0);
                if (ring % 2 == 0) {
                    level.sendParticles(player, ParticleTypes.WITCH, true, x, y, z, 1, 0.0, 0.02, 0.0, 0.0);
                }
            }
        }
    }
}
