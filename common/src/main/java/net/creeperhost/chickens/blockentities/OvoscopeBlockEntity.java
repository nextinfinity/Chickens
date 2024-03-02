package net.creeperhost.chickens.blockentities;

import net.creeperhost.chickens.containers.IncubatorMenu;
import net.creeperhost.chickens.containers.OvoscopeMenu;
import net.creeperhost.chickens.init.ModBlocks;
import net.creeperhost.polylib.blocks.PolyBlockEntity;
import net.creeperhost.polylib.inventory.item.ItemInventoryBlock;
import net.creeperhost.polylib.inventory.item.SerializableContainer;
import net.creeperhost.polylib.inventory.item.SimpleItemInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class OvoscopeBlockEntity extends PolyBlockEntity implements ItemInventoryBlock, MenuProvider {

    public final SimpleItemInventory inventory = new SimpleItemInventory(this, 3)
            .setMaxStackSize(1);

    public OvoscopeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.OVOSCOPE_TILE.get(), pos, state);
    }

    @Override
    public SerializableContainer getContainer(@Nullable Direction side) {
        return inventory;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new OvoscopeMenu(i, inventory, this);
    }

    //    public BlockEntityOvoscope(BlockPos blockPos, BlockState blockState)
//    {
//        super(ModBlocks.OVOSCOPE_TILE.get(), blockPos, blockState);
//        setInventory(new PolyItemInventory(3)
//        {
//            @Override
//            public int getMaxStackSize()
//            {
//                return 1;
//            }
//        });
//        getInventoryOptional().ifPresent(polyItemInventory ->
//        {
//            addSlot(new Slot(polyItemInventory,0,  89, 27));
//            addSlot(new Slot(polyItemInventory,1,  33, 61));
//            addSlot(new Slot(polyItemInventory,2,  141, 61));
//        });
//    }
//
//    public void tick()
//    {
//        if(level == null) return;
//        if(level.isClientSide) return;
//        if(!getItem(0).isEmpty() && getItem(0).getItem() instanceof ItemChickenEgg)
//        {
//            ItemStack stack = getItem(0);
//            if(stack.hasTag())
//            {
//                boolean viable = stack.getOrCreateTag().getBoolean("viable");
//                if(viable)
//                {
//                    if(getItem(1).isEmpty())
//                    {
//                        setItem(1, stack);
//                        setItem(0, ItemStack.EMPTY);
//                    }
//                }
//                else
//                {
//                    if(getItem(2).isEmpty())
//                    {
//                        setItem(2, stack);
//                        setItem(0, ItemStack.EMPTY);
//                    }
//                }
//            }
//        }
//    }
//
//    @Override
//    protected Component getDefaultName()
//    {
//        return Component.translatable("chickens.screen.ovoscope");
//    }
//
//    @Override
//    protected AbstractContainerMenu createMenu(int i, @NotNull Inventory inventory)
//    {
//
//    }
//
//    @Override
//    public boolean canPlaceItemThroughFace(int i, @NotNull ItemStack itemStack, @Nullable Direction direction)
//    {
//        return i == 0 && itemStack.getItem() instanceof ItemChickenEgg;
//    }
//
//    @Override
//    public boolean canTakeItemThroughFace(int i, @NotNull ItemStack itemStack, @NotNull Direction direction)
//    {
//        if(direction == Direction.WEST && i == 1) return true;
//        if(direction == Direction.EAST && i == 2) return true;
//        return false;
//    }
}
