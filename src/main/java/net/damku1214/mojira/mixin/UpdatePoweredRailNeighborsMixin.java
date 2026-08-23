package net.damku1214.mojira.mixin;

import net.damku1214.mojira.MojiraConfig;
import net.damku1214.mojira.mixin.accessor.BlockAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * <p>
 *     Fixes MC-957 (Powered rails do not update when additional power sources are added or removed).
 * </p>
 * <p>
 *     -- CAUSE -- <br>
 *     Powered rails only update themselves and adjacent ones when the existence of redstone input and current powered state are different. <br>
 *     Hence, if a redstone signal is given to an already powered rail, nothing will happen.
 * </p>
 * <p>
 *     -- SOLUTION -- <br>
 *     Create a new blockstate for powered rails; power. Instead of recursive searching for a redstone source, powered rails will only check rails
 *     adjacent and connected to them and update their power correspondingly. <br>
 *     Huge thanks to {@code ISRosillo14} for the solution code;
 *     their code can be found in the comments here: <a href="https://report.bugs.mojang.com/servicedesk/customer/portal/2/MC-957">LINK</a>
 * </p>
 */
@Mixin(PoweredRailBlock.class)
public abstract class UpdatePoweredRailNeighborsMixin {
    @Unique
    private static final IntegerProperty mojira$POWER = BlockStateProperties.POWER;

    @Shadow
    public abstract Property<RailShape> getShapeProperty();
    @Shadow
    protected abstract boolean findPoweredRailSignal(Level level, BlockPos pos, BlockState state, boolean forward, int searchDepth);

    @Inject(method = "registerDefaultState", at = @At("HEAD"), cancellable = true)
    private void mojira$registerDefaultState(CallbackInfo ci) {
        ((BlockAccessor) this).mojira$registerDefaultState(((BlockAccessor) this).mojira$getStateDefinition().any()
                .setValue(PoweredRailBlock.SHAPE, RailShape.NORTH_SOUTH)
                .setValue(PoweredRailBlock.POWERED, false)
                .setValue(mojira$POWER, 0)
                .setValue(PoweredRailBlock.WATERLOGGED, false));
        ci.cancel();
    }

    @Inject(method = "createBlockStateDefinition", at = @At("HEAD"), cancellable = true)
    private void mojira$createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder, CallbackInfo ci) {
        builder.add(getShapeProperty(), PoweredRailBlock.POWERED, mojira$POWER, PoweredRailBlock.WATERLOGGED);
        ci.cancel();
    }

    @Inject(method = "updateState", at = @At("HEAD"), cancellable = true)
    private void mojira$updateState(BlockState state, Level level, BlockPos pos, Block block, CallbackInfo ci) {PoweredRailBlock self = (PoweredRailBlock) (Object) this;
        int power = state.getValue(mojira$POWER);
        int checkPower = Math.max(level.hasNeighborSignal(pos) ? 9 : 0,
                Math.max(this.mojira$findPoweredRailSignal(level, pos, state, true),
                        this.mojira$findPoweredRailSignal(level, pos, state, false)) - 1);

        boolean isPowered = state.getValue(PoweredRailBlock.POWERED);
        boolean shouldPower = level.hasNeighborSignal(pos)
                || this.findPoweredRailSignal(level, pos, state, true, 0)
                || this.findPoweredRailSignal(level, pos, state, false, 0);

        if (MojiraConfig.CONFIG.MC_957.get() ? power != checkPower : isPowered != shouldPower) {
            level.setBlock(pos, state.setValue(mojira$POWER, checkPower).setValue(PoweredRailBlock.POWERED, MojiraConfig.CONFIG.MC_957.get() ? checkPower > 0 : shouldPower), Block.UPDATE_NEIGHBORS | Block.UPDATE_CLIENTS);
            level.updateNeighborsAt(pos.below(), self);
            if (((BaseRailBlock) state.getBlock()).getRailDirection(state, level, pos, null).isSlope()) {
                level.updateNeighborsAt(pos.above(), self);
            }
        }
        ci.cancel();
    }

    @Unique
    protected int mojira$findPoweredRailSignal(Level level, BlockPos pos, BlockState state, boolean forward) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        boolean checkBelow = true;
        RailShape shape = ((BaseRailBlock)state.getBlock()).getRailDirection(state, level, pos, null);
        switch (shape) {
            case NORTH_SOUTH:
                if (forward) {
                    z++;
                } else {
                    z--;
                }
                break;
            case EAST_WEST:
                if (forward) {
                    x--;
                } else {
                    x++;
                }
                break;
            case ASCENDING_EAST:
                if (forward) {
                    x--;
                } else {
                    x++;
                    y++;
                    checkBelow = false;
                }

                shape = RailShape.EAST_WEST;
                break;
            case ASCENDING_WEST:
                if (forward) {
                    x--;
                    y++;
                    checkBelow = false;
                } else {
                    x++;
                }

                shape = RailShape.EAST_WEST;
                break;
            case ASCENDING_NORTH:
                if (forward) {
                    z++;
                } else {
                    z--;
                    y++;
                    checkBelow = false;
                }

                shape = RailShape.NORTH_SOUTH;
                break;
            case ASCENDING_SOUTH:
                if (forward) {
                    z++;
                    y++;
                    checkBelow = false;
                } else {
                    z--;
                }

                shape = RailShape.NORTH_SOUTH;
        }
        int pow;
        if ((pow = this.mojira$getSameRailPower(level, new BlockPos(x, y, z), shape)) > 0) {
            return pow;
        } else {
            return checkBelow ? this.mojira$getSameRailPower(level, new BlockPos(x, y - 1, z), shape) : 0;
        }
    }

    @Unique
    protected int mojira$getSameRailPower(Level level, BlockPos pos, RailShape shape) {
        PoweredRailBlock self = (PoweredRailBlock) (Object) this;
        BlockState blockstate = level.getBlockState(pos);
        if (!blockstate.is(self)) {
            return 0;
        } else {
            RailShape railshape = blockstate.getValue(PoweredRailBlock.SHAPE);
            if (shape != RailShape.EAST_WEST || railshape != RailShape.NORTH_SOUTH && railshape != RailShape.ASCENDING_NORTH && railshape != RailShape.ASCENDING_SOUTH) {
                if (shape != RailShape.NORTH_SOUTH || railshape != RailShape.EAST_WEST && railshape != RailShape.ASCENDING_EAST && railshape != RailShape.ASCENDING_WEST) {
                    if (blockstate.getValue(mojira$POWER) > 0) {
                        return blockstate.getValue(mojira$POWER);
                    } else {
                        return 0;
                    }
                } else {
                    return 0;
                }
            } else {
                return 0;
            }
        }
    }
}
