package net.damku1214.mojira.mixin.accessor;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Block.class)
public interface BlockAccessor {
    @Invoker("registerDefaultState")
    void mojira$registerDefaultState(BlockState state);

    @Accessor("stateDefinition")
    StateDefinition<Block, BlockState> mojira$getStateDefinition();
}
