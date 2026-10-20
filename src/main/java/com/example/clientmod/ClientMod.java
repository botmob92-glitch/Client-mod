package com.example.clientmod;

import com.example.clientmod.gui.ClickGuiScreen;
import com.example.clientmod.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class ClientMod implements ClientModInitializer {
    public static ModuleManager MODULES;
    private static KeyBinding openGui;

    @Override
    public void onInitializeClient() {
        MODULES = new ModuleManager();

        // 1.21.9+ keybinds need a Category object instead of a string.
        KeyBinding.Category category = KeyBinding.Category.create(Identifier.of("clientmod", "main"));
        openGui = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.clientmod.open_gui", GLFW.GLFW_KEY_RIGHT_SHIFT, category));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (openGui.wasPressed()) mc.setScreen(new ClickGuiScreen());
            MODULES.tick(mc);
        });
    }
}
