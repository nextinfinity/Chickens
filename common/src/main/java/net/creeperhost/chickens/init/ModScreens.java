package net.creeperhost.chickens.init;

import net.creeperhost.chickens.client.gui.IncubatorGui;
import net.creeperhost.chickens.client.gui.BreederGui;
import net.creeperhost.chickens.client.gui.EggCrackerGui;
import net.creeperhost.chickens.client.gui.OvoscopeGui;
import net.minecraft.client.gui.screens.MenuScreens;

public class ModScreens
{
    public static void init()
    {
        MenuScreens.register(ModContainers.BREEDER_CONTAINER.get(), BreederGui.Screen::new);
        MenuScreens.register(ModContainers.INCUBATOR.get(), IncubatorGui.Screen::new);
        MenuScreens.register(ModContainers.EGG_CRACKER.get(), EggCrackerGui.Screen::new);
        MenuScreens.register(ModContainers.OVOSCOPE.get(), OvoscopeGui.Screen::new);
    }
}
