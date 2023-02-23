package net.creeperhost.chickens.item;

import net.creeperhost.chickens.client.ChickensCreativeTabs;
import net.creeperhost.chickens.handler.IColorSource;
import net.creeperhost.chickens.handler.LiquidEggFluidWrapper;
import net.creeperhost.chickens.init.ModItems;
import net.creeperhost.chickens.registry.LiquidEggRegistry;
import net.creeperhost.chickens.registry.LiquidEggRegistryItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemLiquidEgg extends Item implements IColorSource
{
    LiquidEggRegistryItem liquidEggRegistryItem;

    public ItemLiquidEgg(LiquidEggRegistryItem liquidEggRegistryItem)
    {
        super(new Item.Properties().tab(ChickensCreativeTabs.CHICKENS_BLOCKS));
        this.liquidEggRegistryItem = liquidEggRegistryItem;
    }

    @Override
    public int getColorFromItemStack(ItemStack stack, int renderPass)
    {
        return IClientFluidTypeExtensions.of(liquidEggRegistryItem.getFluid()).getTintColor();
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> components, TooltipFlag tooltipFlag)
    {
        super.appendHoverText(itemStack, level, components, tooltipFlag);
        components.add(Component.translatable("item.liquid_egg.tooltip"));
    }

    @Override
    public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> subItems)
    {
        if (this.allowedIn(tab))
        {
            for (LiquidEggRegistryItem liquid : LiquidEggRegistry.getAll())
            {
                subItems.add(of(liquid));
            }
        }
    }

    public static ItemStack of(LiquidEggRegistryItem liquidEggRegistryItem)
    {
        return new ItemStack(ModItems.FLUID_EGGS.get(liquidEggRegistryItem).get());
    }

    @Override
    public Component getName(ItemStack stack)
    {
        if (!stack.hasTag()) return super.getName(stack);

        String[] strings = stack.getTag().getString("id").split(":");
        return Component.translatable("item.liquid_egg." + strings[1] + ".name");
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext useOnContext)
    {
        BlockPos clickedPos = useOnContext.getClickedPos().relative(useOnContext.getClickedFace());
        ItemStack stack = useOnContext.getItemInHand();
        Level level = useOnContext.getLevel();
        Fluid fluid = getFluid(stack);
        if(level.getBlockState(clickedPos).isAir() || level.getBlockState(clickedPos).canBeReplaced(fluid))
        {
            level.setBlock(clickedPos, fluid.defaultFluidState().createLegacyBlock(), 3);
            useOnContext.getItemInHand().shrink(1);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    public Fluid getFluid(ItemStack stack)
    {
        String id = stack.getTag().getString("id");
        return Registry.FLUID.get(new ResourceLocation(id));
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt)
    {
        return new LiquidEggFluidWrapper(stack);
    }
}
