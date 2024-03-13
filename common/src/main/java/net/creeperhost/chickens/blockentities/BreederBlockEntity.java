package net.creeperhost.chickens.blockentities;

import net.creeperhost.chickens.api.ChickenStats;
import net.creeperhost.chickens.api.ChickensRegistry;
import net.creeperhost.chickens.api.ChickensRegistryItem;
import net.creeperhost.chickens.block.BreederBlock;
import net.creeperhost.chickens.containers.BreederMenu;
import net.creeperhost.chickens.init.ModBlocks;
import net.creeperhost.chickens.init.ModItems;
import net.creeperhost.chickens.item.ItemChicken;
import net.creeperhost.chickens.item.ItemChickenEgg;
import net.creeperhost.chickens.polylib.CommonTags;
import net.creeperhost.polylib.blocks.PolyBlockEntity;
import net.creeperhost.polylib.data.serializable.IntData;
import net.creeperhost.polylib.helpers.ContainerUtil;
import net.creeperhost.polylib.inventory.item.ContainerAccessControl;
import net.creeperhost.polylib.inventory.item.ItemInventoryBlock;
import net.creeperhost.polylib.inventory.item.SerializableContainer;
import net.creeperhost.polylib.inventory.item.SimpleItemInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BreederBlockEntity extends PolyBlockEntity implements ItemInventoryBlock, MenuProvider {
    public static final int MAX_PROGRESS = 1000;

    public final SimpleItemInventory inventory = new SimpleItemInventory(this, 6)
            .setSlotValidator(0, e -> e.is(CommonTags.SEEDS))
            .setSlotValidator(1, e -> e.is(ModItems.CHICKEN_ITEM.get()))
            .setSlotValidator(2, e -> e.is(ModItems.CHICKEN_ITEM.get()));

    public final IntData progress = register("progress", new IntData(10), SAVE_BOTH);

    public BreederBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.BREEDER_TILE.get(), pos, state);
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level instanceof ServerLevel serverLevel)) return;

        ItemStack seeds = inventory.getItem(0);
        ItemStack chicken1 = inventory.getItem(1);
        ItemStack chicken2 = inventory.getItem(2);

        boolean canWork = chicken1.getItem() instanceof ItemChicken && chicken2.getItem() instanceof ItemChicken && seeds.is(CommonTags.SEEDS);
        setState(canWork);
        if (!canWork) {
            progress.set(0);
            return;
        }

        if (progress.get() < MAX_PROGRESS) {
            progress.add(getProgressIncrement());
            return;
        }

        ChickensRegistryItem chickensRegistryItem1 = ChickensRegistry.getByRegistryName(ItemChicken.getTypeFromStack(chicken1));
        ChickensRegistryItem chickensRegistryItem2 = ChickensRegistry.getByRegistryName(ItemChicken.getTypeFromStack(chicken2));
        ChickensRegistryItem baby = ChickensRegistry.getRandomChild(chickensRegistryItem1, chickensRegistryItem2);
        if (baby == null) {
            progress.set(0);
            return;
        }
        ItemStack chickenStack = ItemChickenEgg.of(baby);

        ChickenStats babyStats = increaseStats(chickenStack, chicken1, chicken2, level.random);
        babyStats.write(chickenStack);

        ChickenStats chickenStats = new ChickenStats(chicken1);
        int count = Math.max(1, ((1 + chickenStats.getGain()) / 3));

        chickenStack.setCount(count);

        if (ContainerUtil.insertStack(chickenStack, inventory, true) == 0) {
            ContainerUtil.insertStack(chickenStack, inventory);
            int random = level.getRandom().nextInt(1, 5);
            if (random >= 4) {
                damageChicken(1);
                damageChicken(2);
            }
            level.playSound(null, getBlockPos(), SoundEvents.CHICKEN_EGG, SoundSource.NEUTRAL, 0.5F, 0.8F);
            serverLevel.sendParticles(ParticleTypes.HEART, getBlockPos().getX() + 0.5, getBlockPos().getY() + 1, getBlockPos().getZ() + 0.5, 8, 0.45, 0.45, 0.45, 0.0125);
            seeds.shrink(1);
            progress.set(0);
        }
    }

    @Override
    public SerializableContainer getContainer(@Nullable Direction side) {
        if (side != Direction.DOWN) {
            //Allows extraction from any slot from sides or top
            return new ContainerAccessControl(inventory, 0, 6)
                    .slotInsertCheck(1, stack -> stack.getCount() == 1 && inventory.getItem(1).isEmpty()) //TODO This limiting slot to 1 item can be done better with a custom SidedInvWrapper, though not sure about fabric...
                    .slotInsertCheck(2, stack -> stack.getCount() == 1 && inventory.getItem(2).isEmpty()) //TODO This limiting slot to 1 item can be done better with a custom SidedInvWrapper, though not sure about fabric...
                    .containerInsertCheck((slot, stack) -> slot <= 2);
        }
        //Only allow extraction of outputs from bottom (basic hopper compatibility)
        return new ContainerAccessControl(inventory, 0, 6)
                .slotInsertCheck(1, stack -> stack.getCount() == 1 && inventory.getItem(1).isEmpty()) //TODO This limiting slot to 1 item can be done better with a custom SidedInvWrapper, though not sure about fabric...
                .slotInsertCheck(2, stack -> stack.getCount() == 1 && inventory.getItem(2).isEmpty()) //TODO This limiting slot to 1 item can be done better with a custom SidedInvWrapper, though not sure about fabric...
                .containerInsertCheck((slot, stack) -> slot <= 2)
                .containerRemoveCheck((slot, stack) -> slot > 2);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new BreederMenu(i, inventory, this);
    }

    @Override
    public void writeExtraData(CompoundTag nbt) {
        inventory.serialize(nbt);
    }

    @Override
    public void readExtraData(CompoundTag nbt) {
        inventory.deserialize(nbt);
    }

    public void setState(boolean canWork) {
        boolean hasSeeds = !inventory.getItem(0).isEmpty();
        level.setBlock(getBlockPos(), getBlockState().setValue(BreederBlock.HAS_SEEDS, hasSeeds).setValue(BreederBlock.IS_BREEDING, canWork), 3);
    }

    public int getProgressIncrement() {
        ChickenStats chickenStats1 = new ChickenStats(inventory.getItem(1));
        ChickenStats chickenStats2 = new ChickenStats(inventory.getItem(2));

        int progress = (chickenStats1.getGain() + chickenStats2.getGain());
        if (progress > 50) progress = 50;
        return progress;
    }

    public void damageChicken(int slot) {
        if (!inventory.getItem(slot).isEmpty() && inventory.getItem(slot).getItem() instanceof ItemChicken) {
            ItemStack copy = inventory.getItem(slot).copy();

            ChickenStats chickenStats = new ChickenStats(copy);
            int life = chickenStats.getLifespan() - 1;
            if (life > 0) {
                chickenStats.setLifespan(life);
                chickenStats.write(copy);
                inventory.setItem(slot, copy);
            } else {
                inventory.setItem(slot, ItemStack.EMPTY);
            }
        }
    }

    private ChickenStats increaseStats(ItemStack baby, ItemStack parent1, ItemStack parent2, RandomSource rand) {
        ChickenStats babyStats = new ChickenStats(baby);
        ChickenStats parent1Stats = new ChickenStats(parent1);
        ChickenStats parent2Stats = new ChickenStats(parent2);

        babyStats.setGrowth(calculateNewStat(parent1Stats.getStrength(), parent2Stats.getStrength(), parent1Stats.getGrowth(), parent2Stats.getGrowth(), rand));
        babyStats.setGain(calculateNewStat(parent1Stats.getStrength(), parent2Stats.getStrength(), parent1Stats.getGain(), parent2Stats.getGain(), rand));
        babyStats.setStrength(calculateNewStat(parent1Stats.getStrength(), parent2Stats.getStrength(), parent1Stats.getStrength(), parent2Stats.getStrength(), rand));

        return babyStats;
    }

    private int calculateNewStat(int thisStrength, int mateStrength, int stat1, int stat2, RandomSource rand) {
        int mutation = rand.nextInt(2) + 1;
        int newStatValue = (stat1 * thisStrength + stat2 * mateStrength) / (thisStrength + mateStrength) + mutation;
        if (newStatValue <= 1) return 1;
        return Math.min(newStatValue, 10);
    }
}
