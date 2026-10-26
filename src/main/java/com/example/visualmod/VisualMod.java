package com.example.visualmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.Random;

public class VisualMod implements ClientModInitializer {
    private static KeyBinding openKey;
    private static final Random RNG = new Random();
    private int wait = 0;
    private float need = 0.95f;
    private boolean nvApplied = false;

    @Override
    public void onInitializeClient() {
        Config.load();
        openKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.visualmod.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_J,
                KeyBinding.Category.create(Identifier.of("visualmod", "main"))));
        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
    }

    private void tick(MinecraftClient mc) {
        while (openKey.wasPressed()) {
            if (mc.currentScreen == null) mc.setScreen(new MenuScreen());
        }
        ClientPlayerEntity p = mc.player;
        if (p == null || mc.world == null || mc.interactionManager == null) return;

        fullbright(p);
        if (mc.currentScreen != null) return;
        aimAssist(mc, p);
        triggerBot(mc, p);
    }

    private void fullbright(ClientPlayerEntity p) {
        if (Config.I.fullbright) {
            if (!p.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
                p.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 1_000_000, 0, false, false, false));
                nvApplied = true;
            }
        } else if (nvApplied) {
            p.removeStatusEffect(StatusEffects.NIGHT_VISION);
            nvApplied = false;
        }
    }

    private void aimAssist(MinecraftClient mc, ClientPlayerEntity p) {
        Config c = Config.I;
        if (!c.aimAssist) return;
        if (c.aimClickOnly && !mc.options.attackKey.isPressed()) return;

        PlayerEntity best = null;
        double bestAngle = Double.MAX_VALUE, bestYaw = 0, bestPitch = 0;
        for (PlayerEntity t : mc.world.getPlayers()) {
            if (t == p || !t.isAlive() || t.isSpectator()) continue;
            if (p.distanceTo(t) > c.aimRange) continue;
            Vec3d to = t.getPos().add(0, t.getHeight() * 0.6, 0).subtract(p.getEyePos());
            float yaw = (float) (MathHelper.atan2(to.z, to.x) * 180 / Math.PI) - 90f;
            float pitch = (float) -(MathHelper.atan2(to.y, Math.hypot(to.x, to.z)) * 180 / Math.PI);
            double dy = MathHelper.wrapDegrees(yaw - p.getYaw());
            double dp = pitch - p.getPitch();
            if (Math.abs(dy) > c.aimFov / 2) continue;
            double ang = Math.hypot(dy, dp);
            if (ang < bestAngle) { bestAngle = ang; best = t; bestYaw = dy; bestPitch = dp; }
        }
        if (best == null) return;
        float s = (float) c.aimSpeed;
        p.setYaw(p.getYaw() + MathHelper.clamp((float) bestYaw, -s, s));
        p.setPitch(MathHelper.clamp(p.getPitch() + MathHelper.clamp((float) bestPitch, -s, s), -90f, 90f));
    }

    private void triggerBot(MinecraftClient mc, ClientPlayerEntity p) {
        Config c = Config.I;
        if (!c.triggerBot) return;
        if (wait > 0) { wait--; return; }
        if (!(mc.crosshairTarget instanceof EntityHitResult hit)
                || !(hit.getEntity() instanceof LivingEntity t) || !t.isAlive()) return;
        if (p.getAttackCooldownProgress(0) < need) return;

        if (c.triggerCrit) {
            boolean canCrit = !p.isOnGround() && p.getVelocity().y < 0
                    && !p.isClimbing() && !p.isTouchingWater() && !p.hasVehicle()
                    && !p.hasStatusEffect(StatusEffects.BLINDNESS);
            if (!canCrit) return;
        }

        mc.interactionManager.attackEntity(p, t);
        p.swingHand(Hand.MAIN_HAND);

        int lo = (int) Math.min(c.triggerMinDelay, c.triggerMaxDelay);
        int hi = (int) Math.max(c.triggerMinDelay, c.triggerMaxDelay);
        wait = lo + RNG.nextInt(hi - lo + 1);
        need = 0.90f + RNG.nextFloat() * 0.10f;
    }
}
