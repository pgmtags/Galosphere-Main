package net.orcinus.galosphere.mixin;

import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ChorusFruitItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.orcinus.galosphere.blocks.WarpedAnchorBlock;
import net.orcinus.galosphere.init.GBlocks;
import net.orcinus.galosphere.init.GCriteriaTriggers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Comparator;
import java.util.List;

@Mixin(ChorusFruitItem.class)
public class ChorusFruitItemMixin {

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getX()D", ordinal = 0), method = "finishUsingItem", cancellable = true)
    private void G$finishUsingItem(ItemStack itemStack, Level world, LivingEntity livingEntity, CallbackInfoReturnable<ItemStack> cir) {
        List<BlockPos> poses = Lists.newArrayList();
        BlockPos pos = livingEntity.blockPosition();
        int radius = 64;
        if (livingEntity instanceof ServerPlayer player) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    for (int y = -radius; y <= radius; y++) {
                        BlockPos blockPos = BlockPos.containing(livingEntity.getX() + x, livingEntity.getY() + y, livingEntity.getZ() + z);
                        BlockState blockState = world.getBlockState(blockPos);
                        if (blockState.is(GBlocks.WARPED_ANCHOR) && blockState.getValue(WarpedAnchorBlock.WARPED_CHARGE) > 0 && blockPos.closerThan(pos, 16 * blockState.getValue(WarpedAnchorBlock.WARPED_CHARGE))) {
                            poses.add(blockPos);
                        }
                    }
                }
            }
            if (!poses.isEmpty()) {
                poses.sort(Comparator.comparingDouble(pos::distSqr));
                for (BlockPos blockPos : poses) {
                    cir.setReturnValue(itemStack);
                    player.getCooldowns().addCooldown((ChorusFruitItem)(Object)this, 20);
                    GCriteriaTriggers.WARPED_TELEPORT.trigger(player);
                    world.gameEvent(player, GameEvent.BLOCK_CHANGE, blockPos);
                    world.playSound(null, blockPos, SoundEvents.RESPAWN_ANCHOR_SET_SPAWN, SoundSource.BLOCKS, 1.0F, 1.0F);
                    player.teleportTo(blockPos.getX() + 0.5D, blockPos.getY() + 0.5D, blockPos.getZ() + 0.5D);
                    player.resetFallDistance();
                    world.setBlock(blockPos, world.getBlockState(blockPos).setValue(WarpedAnchorBlock.WARPED_CHARGE, world.getBlockState(blockPos).getValue(WarpedAnchorBlock.WARPED_CHARGE) - 1), 2);
                    break;
                }
            }
        }
    }

}
