package com.zurrtum.create.content.logistics.depot;

import com.zurrtum.create.infrastructure.packet.s2c.EjectorPlacementRequestPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class EjectorItem extends BlockItem {

    public EjectorItem(Block p_i48527_1_, Properties p_i48527_2_) {
        super(p_i48527_1_, p_i48527_2_);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Player player = ctx.getPlayer();
        if (player != null && player.isShiftKeyDown()) {
            return InteractionResult.SUCCESS;
        }
        BlockPos placePos = new net.minecraft.world.item.context.BlockPlaceContext(ctx).getClickedPos();
        InteractionResult result = super.useOn(ctx);
        // 26.3: BlockItem.updateCustomBlockEntityTag is static and no longer a hook
        if (result.consumesAction() && !ctx.getLevel().isClientSide() && ctx.getPlayer() instanceof ServerPlayer sp) {
            sp.connection.send(new EjectorPlacementRequestPacket(placePos));
        }
        return result;
    }

    @Override
    public boolean canDestroyBlock(
        ItemStack stack,
        BlockState state,
        Level world,
        BlockPos pos,
        LivingEntity p_195938_4_
    ) {
        return !p_195938_4_.isShiftKeyDown();
    }

}
