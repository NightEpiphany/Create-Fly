package com.zurrtum.create.content.logistics.packagePort;

import com.zurrtum.create.infrastructure.packet.s2c.PackagePortPlacementRequestPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class PackagePortItem extends BlockItem {

    public PackagePortItem(Block pBlock, Properties pProperties) {
        super(pBlock, pProperties);
    }

    @Override
    public net.minecraft.world.InteractionResult useOn(net.minecraft.world.item.context.UseOnContext ctx) {
        BlockPos placePos = new net.minecraft.world.item.context.BlockPlaceContext(ctx).getClickedPos();
        net.minecraft.world.InteractionResult result = super.useOn(ctx);
        // 26.3: BlockItem.updateCustomBlockEntityTag is static and no longer a hook
        if (result.consumesAction() && !ctx.getLevel().isClientSide() && ctx.getPlayer() instanceof ServerPlayer sp) {
            sp.connection.send(new PackagePortPlacementRequestPacket(placePos));
        }
        return result;
    }

}
