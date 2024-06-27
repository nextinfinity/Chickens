package net.creeperhost.chickens.neoforge;

import net.creeperhost.chickens.Chickens;
import net.creeperhost.chickens.client.RenderChickensChicken;
import net.creeperhost.chickens.init.ModEntities;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = Chickens.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class NeoForgeEvents
{
    @SubscribeEvent
    public static void event(EntityRenderersEvent.RegisterRenderers event)
    {
        ModEntities.CHICKENS.forEach((chickensRegistryItem, entityTypeSupplier) ->
        {
            Chickens.LOGGER.info("Registering render for " + entityTypeSupplier.get().getDescriptionId());
            event.registerEntityRenderer(entityTypeSupplier.get(), RenderChickensChicken::new);
        });
    }
}
