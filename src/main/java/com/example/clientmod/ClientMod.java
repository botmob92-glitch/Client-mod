package com.example.clientmod;

import com.example.clientmod.gui.ClickGuiScreen;
import com.example.clientmod.module.Module;
import com.example.clientmod.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class ClientMod implements ClientModInitializer {
    public static final String NAME = "Customi Client";
    public static ModuleManager MODULES;
    private static KeyBinding openGui;

    @Override
    public void onInitializeClient() {
        MODULES = new ModuleManager();

        // 1.21.9+ keybinds need a Category object instead of a string.
        KeyBinding.Category category = KeyBinding.Category.create(Identifier.of("clientmod", "main"));
        openGui = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.clientmod.open_gui", GLFW.GLFW_KEY_RIGHT_SHIFT, category));

        // One unbound key per module. Set them in Options > Controls > Key Binds > Customi Client.
        for (Module m : MODULES.modules) {
            m.key = KeyBindingHelper.registerKeyBinding(
                    new KeyBinding("Toggle " + m.name, GLFW.GLFW_KEY_UNKNOWN, category));
        }

        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, Identifier.of("clientmod", "esp"),
                (ctx, tickCounter) -> MODULES.hud(ctx, MinecraftClient.getInstance()));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (openGui.wasPressed()) mc.setScreen(new ClickGuiScreen());
            for (Module m : MODULES.modules) {
                while (m.key != null && m.key.wasPressed()) m.toggle();
            }
            MODULES.tick(mc);
        });
    }
}
