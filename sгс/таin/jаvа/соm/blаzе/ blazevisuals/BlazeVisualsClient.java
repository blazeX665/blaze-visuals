package com.blaze.blazevisuals;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class BlazeVisualsClient implements ClientModInitializer {

    private static KeyBinding zoomKey;
    private static KeyBinding brightKey;
    private static boolean bright = false;
    private static boolean zooming = false;
    private static int savedFov = 70;

    @Override
    public void onInitializeClient() {
        zoomKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.blazevisuals.zoom", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_C, "category.blazevisuals"));
        brightKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.blazevisuals.bright", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G, "category.blazevisuals"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            if (zoomKey.isPressed() && !zooming) {
                zooming = true;
                savedFov = client.options.getFov().getValue();
                client.options.getFov().setValue(25);
            } else if (!zoomKey.isPressed() && zooming) {
                zooming = false;
                client.options.getFov().setValue(savedFov);
            }

            while (brightKey.wasPressed()) {
                bright = !bright;
                client.options.getGamma().setValue(bright ? 1.0 : 0.5);
                client.player.sendMessage(Text.literal("Bright: " + (bright ? "ON" : "OFF")), true);
            }
        });

        HudRenderCallback.EVENT.register((ctx, tick) -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null || mc.options.hudHidden) return;
            String coords = String.format("XYZ %.1f %.1f %.1f", mc.player.getX(), mc.player.getY(), mc.player.getZ());
            String fps = "FPS " + mc.getCurrentFps();
            String biome = mc.world.getBiome(mc.player.getBlockPos()).getKey()
                    .map(k -> k.getValue().getPath()).orElse("?");
            ctx.drawText(mc.textRenderer, coords, 5, 5, 0xFFFFFFFF, true);
            ctx.drawText(mc.textRenderer, fps, 5, 17, 0xFFFFFFFF, true);
            ctx.drawText(mc.textRenderer, "Biome: " + biome, 5, 29, 0xFFFFFFFF, true);
        });
    }
}
