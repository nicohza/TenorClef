package adris.altoclef.tasks.pvp;

import adris.altoclef.AltoClef;
import adris.altoclef.control.InputControls;
import adris.altoclef.tasks.movement.GetToEntityTask;
import adris.altoclef.tasks.speedrun.testrun2.combat.BowLead;
import adris.altoclef.tasks.speedrun.testrun2.combat.WeaponPicker;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.LookHelper;
import baritone.api.utils.input.Input;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

/**
 * Melee/ranged PvP against players or mobs.
 *
 * Techniques: crit chaining (jump, drop sprint at the apex, hit on the way down), sprint hits
 * with W-tap, hit select (never swing into hurt immunity), jump resets on knockback, strafing,
 * axe vs a raised shield, shield vs drawn bows and arrows, bow at range, gapples, splash
 * healing and a totem in the offhand when low.
 */
public class PvpTask extends Task {

    private static final double REACH = 3.0;      // eye to hitbox
    private static final double MELEE_DRIVE = 7;  // closer than this we steer ourselves instead of pathing
    private static final double BOW_MIN = 10;
    private static final double CHASE = 48;

    private final Predicate<LivingEntity> filter;
    private final String label;
    private final Random rng = new Random(7);

    private LivingEntity target;
    private int strafeDir = 1, strafeLeft;
    private int wtap;          // ticks left with forward released after a sprint hit
    private int eatTicks, potCooldown, blockTicks;
    private boolean critArmed; // sprint dropped at the apex, hit on the next falling tick
    private float lastHealth = -1;

    // stats
    public int attacks, crits, sprintHits, blocks, gapples, pots, axeHits;
    public float damageTaken;

    public PvpTask(Predicate<LivingEntity> filter, String label) {
        this.filter = filter;
        this.label = label;
    }

    public static PvpTask player(String name) {
        return new PvpTask(e -> e instanceof PlayerEntity p && p.getName().getString().equalsIgnoreCase(name), name);
    }

    public static PvpTask nearestPlayer() {
        return new PvpTask(e -> e instanceof PlayerEntity, "players");
    }

    public static PvpTask hostiles() {
        return new PvpTask(e -> e instanceof net.minecraft.entity.mob.HostileEntity, "hostiles");
    }

    private int groundedJumps;
    private static volatile long lastTickMs;

    /** True while a PvpTask is ticking; MobDefense/Food chains stand down so it keeps control. */
    /** Marks PvP as active for this tick (the bench calls it between rounds too). */
    public static void touch() {
        lastTickMs = System.currentTimeMillis();
    }

    public static boolean anyActive() {
        return System.currentTimeMillis() - lastTickMs < 500;
    }

    @Override
    protected void onStart() {
        target = null;
        lastHealth = -1;
    }

    @Override
    protected Task onTick() {
        lastTickMs = System.currentTimeMillis();
        AltoClef mod = AltoClef.getInstance();
        PlayerEntity me = mod.getPlayer();
        if (me == null) return null;
        InputControls in = mod.getInputControls();

        float hp = me.getHealth() + me.getAbsorptionAmount();
        if (lastHealth >= 0 && hp < lastHealth) damageTaken += lastHealth - hp;
        lastHealth = hp;

        if (target == null || !target.isAlive() || target.isRemoved() || me.distanceTo(target) > CHASE) {
            target = pick(mod, me);
        }
        if (target == null) {
            releaseAll(in);
            setDebugState("No target");
            return null;
        }

        if (potCooldown > 0) potCooldown--;
        keepTotem(mod, me);

        // --- survival first ---
        if (me.getHealth() <= 7 && potCooldown == 0 && throwHealPot(mod, me, in)) return null;
        if (eatTicks > 0 || (me.getHealth() <= 11 && !me.hasStatusEffect(net.minecraft.entity.effect.StatusEffects.REGENERATION) && hasItem(mod, Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE))) {
            if (eat(mod, me, in)) return null;
        }

        double dist = eyeToBox(me, target);
        boolean los = me.canSee(target);

        // --- shield vs bows and incoming arrows ---
        if (shouldBlock(me, dist)) {
            if (me.getOffHandStack().getItem() != Items.SHIELD) mod.getSlotHandler().forceEquipItemToOffhand(Items.SHIELD);
            LookHelper.lookAt(mod, target.getEyePos());
            releaseMove(in);
            in.hold(Input.CLICK_RIGHT);
            if (blockTicks++ == 0) blocks++;
            setDebugState("Blocking");
            return null;
        }
        if (blockTicks > 0) {
            in.release(Input.CLICK_RIGHT);
            blockTicks = 0;
        }

        // --- far: bow, or walk in ---
        if (dist > MELEE_DRIVE || !los) {
            if (los && dist > BOW_MIN && hasItem(mod, Items.BOW) && hasItem(mod, Items.ARROW, Items.SPECTRAL_ARROW, Items.TIPPED_ARROW)) {
                return shootBow(mod, me, in);
            }
            if (in.isHeldDown(Input.CLICK_RIGHT)) in.release(Input.CLICK_RIGHT);
            releaseMove(in);
            setDebugState("Closing in on " + target.getName().getString());
            return new GetToEntityTask(target, 2.5);
        }
        if (me.isUsingItem() && isBow(me.getMainHandStack())) in.release(Input.CLICK_RIGHT);

        // --- melee ---
        boolean axeTime = target.isBlocking() && hasAxe(mod);
        equipWeapon(mod, axeTime);
        LookHelper.lookAt(mod, aimPoint(me, target));

        float cd = me.getAttackCooldownProgress(0.5f);
        boolean inReach = dist <= REACH;
        boolean immune = target.hurtTime > 1; // hit select: swinging now would be absorbed by hurt immunity
        boolean falling = !me.isOnGround() && me.getVelocity().y < -0.05;
        boolean canJump = me.isOnGround() && !me.isTouchingWater() && !me.isInLava() && !me.isClimbing();

        steer(me, in, dist);

        // jump reset: jump the tick we get hit to eat less knockback
        if (me.hurtTime == me.maxHurtTime - 1 && canJump) me.jump();

        if (axeTime && inReach && cd >= 0.9f) {
            hit(mod, me);
            axeHits++;
            setDebugState("Axe on shield");
            return null;
        }

        if (critArmed && falling && inReach && cd >= 0.9f && !immune) {
            hit(mod, me);
            crits++;
            critArmed = false;
            setDebugState("Crit");
            return null;
        }
        if (!me.isOnGround() && me.getVelocity().y < 0.08 && dist <= REACH + 0.6 && cd >= 0.75f) {
            // near the apex: drop sprint so the swing next tick counts as a crit
            me.setSprinting(false);
            in.release(Input.SPRINT);
            critArmed = true;
        }
        if (me.isOnGround()) critArmed = false;
        else groundedJumps = 0;

        if (canJump && dist <= REACH + 0.8 && cd >= 0.55f && !immune && groundedJumps < 4) {
            // wind up a crit: by the time we fall the cooldown is full. Direct jump: the keybind
            // gets overridden by Baritone's input handler while no path is running.
            me.jump();
            groundedJumps++;
            setDebugState("Crit jump");
            return null;
        }

        if (me.isOnGround() && inReach && cd >= 0.95f && !immune && (!canJump || groundedJumps >= 4)) {
            // can't jump (water, ladder, low ceiling): plain sprint hit + W-tap
            boolean sprint = me.isSprinting();
            hit(mod, me);
            if (sprint) {
                sprintHits++;
                wtap = 2;
            }
            groundedJumps = 0;
        }
        setDebugState(String.format("Melee d=%.1f cd=%.2f", dist, cd));
        return null;
    }

    private void steer(PlayerEntity me, InputControls in, double dist) {
        if (--strafeLeft <= 0) {
            strafeDir = rng.nextBoolean() ? 1 : -1;
            strafeLeft = 10 + rng.nextInt(20);
        }
        if (wtap > 0) {
            wtap--;
            in.release(Input.MOVE_FORWARD);
            in.release(Input.SPRINT);
        } else if (dist > 2.4) {
            in.hold(Input.MOVE_FORWARD);
            if (!critArmed) in.hold(Input.SPRINT);
            in.release(Input.MOVE_BACK);
        } else if (dist < 1.2) {
            in.release(Input.MOVE_FORWARD);
            in.hold(Input.MOVE_BACK);
        } else {
            in.release(Input.MOVE_FORWARD);
            in.release(Input.MOVE_BACK);
        }
        in.release(strafeDir > 0 ? Input.MOVE_LEFT : Input.MOVE_RIGHT);
        in.hold(strafeDir > 0 ? Input.MOVE_RIGHT : Input.MOVE_LEFT);
    }

    private boolean shouldBlock(PlayerEntity me, double dist) {
        if (!hasShieldAnywhere(me)) return false;
        boolean drawing = target.isUsingItem() && (isBow(target.getActiveItem()) || target.getActiveItem().getItem() == Items.CROSSBOW);
        if (drawing && dist > 4) return true;
        if (target.getMainHandStack().getItem() == Items.CROSSBOW && BowLead.charged(target.getMainHandStack()) && dist > 4) return true;
        Box around = me.getBoundingBox().expand(6);
        for (PersistentProjectileEntity p : me.getWorld().getEntitiesByClass(PersistentProjectileEntity.class, around, a -> true)) {
            Vec3d v = p.getVelocity();
            if (v.lengthSquared() < 0.25) continue;
            Vec3d to = me.getPos().add(0, 1, 0).subtract(p.getPos());
            if (to.normalize().dotProduct(v.normalize()) > 0.9) return true;
        }
        return false;
    }

    private Task shootBow(AltoClef mod, PlayerEntity me, InputControls in) {
        releaseMove(in);
        mod.getSlotHandler().forceEquipItem(Items.BOW);
        if (!isBow(me.getMainHandStack())) return null;
        float charge = Math.min(1f, me.getItemUseTime() / 20f);
        LookHelper.lookAt(mod, BowLead.aimPoint(me, target, me.getMainHandStack(), me.isUsingItem() ? Math.max(charge, 0.9f) : 1f));
        if (me.isUsingItem() && me.getItemUseTime() >= 21) {
            in.release(Input.CLICK_RIGHT);
            attacks++;
        } else {
            in.hold(Input.CLICK_RIGHT);
        }
        setDebugState("Bow");
        return null;
    }

    private boolean eat(AltoClef mod, PlayerEntity me, InputControls in) {
        Item apple = hasItem(mod, Items.ENCHANTED_GOLDEN_APPLE) && me.getHealth() <= 6 ? Items.ENCHANTED_GOLDEN_APPLE : Items.GOLDEN_APPLE;
        if (!hasItem(mod, apple)) apple = Items.ENCHANTED_GOLDEN_APPLE;
        if (!hasItem(mod, apple)) {
            eatTicks = 0;
            return false;
        }
        mod.getSlotHandler().forceEquipItem(apple);
        if (me.getMainHandStack().getItem() != apple) return true;
        if (eatTicks == 0) gapples++;
        eatTicks++;
        // back off while chewing
        in.release(Input.MOVE_FORWARD);
        in.release(Input.SPRINT);
        in.hold(Input.MOVE_BACK);
        in.hold(Input.CLICK_RIGHT);
        if (target != null) LookHelper.lookAt(mod, target.getEyePos());
        if (eatTicks > 36) {
            in.release(Input.CLICK_RIGHT);
            eatTicks = 0;
        }
        setDebugState("Eating " + apple);
        return true;
    }

    private boolean throwHealPot(AltoClef mod, PlayerEntity me, InputControls in) {
        int slot = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack s = me.getInventory().getStack(i);
            if (s.getItem() == Items.SPLASH_POTION && s.getName().getString().toLowerCase().contains("heal")) {
                slot = i;
                break;
            }
        }
        if (slot < 0) return false;
        //#if MC >= 12105
        //$$ me.getInventory().setSelectedSlot(slot);
        //#else
        me.getInventory().selectedSlot = slot;
        //#endif
        in.forceLook(me.getYaw(), 90);
        in.tryPress(Input.CLICK_RIGHT);
        pots++;
        potCooldown = 10;
        setDebugState("Splash heal");
        return true;
    }

    private void keepTotem(AltoClef mod, PlayerEntity me) {
        if (me.getHealth() > 8 || me.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING) return;
        if (hasItem(mod, Items.TOTEM_OF_UNDYING)) mod.getSlotHandler().forceEquipItemToOffhand(Items.TOTEM_OF_UNDYING);
    }

    private void equipWeapon(AltoClef mod, boolean axe) {
        Item best = null;
        double bestScore = -1;
        for (ItemStack s : mod.getItemStorage().getItemStacksPlayerInventory(true)) {
            if (s == null || s.isEmpty()) continue;
            Item it = s.getItem();
            if (!WeaponPicker.isKnownWeapon(it)) continue;
            if (axe && !WeaponPicker.isAxe(it)) continue;
            // crit play wants damage per swing more than dps
            double score = WeaponPicker.stats(it).damage() + WeaponPicker.dps(it);
            if (score > bestScore) {
                bestScore = score;
                best = it;
            }
        }
        if (best != null) mod.getSlotHandler().forceEquipItem(best);
    }

    private void hit(AltoClef mod, PlayerEntity me) {
        MinecraftClient mc = MinecraftClient.getInstance();
        mc.interactionManager.attackEntity(me, target);
        me.swingHand(Hand.MAIN_HAND);
        attacks++;
    }

    private LivingEntity pick(AltoClef mod, PlayerEntity me) {
        List<LivingEntity> list = me.getWorld().getEntitiesByClass(LivingEntity.class, me.getBoundingBox().expand(CHASE),
                e -> e != me && e.isAlive() && !e.isRemoved() && filter.test(e));
        return list.stream()
                .filter(e -> me.distanceTo(e) <= CHASE)
                .min(Comparator.comparingDouble(me::squaredDistanceTo))
                .orElse(null);
    }

    private static Vec3d aimPoint(PlayerEntity me, LivingEntity t) {
        Vec3d eye = me.getEyePos();
        Box b = t.getBoundingBox().contract(0.05);
        return new Vec3d(MathHelper.clamp(eye.x, b.minX, b.maxX),
                MathHelper.clamp(eye.y, b.minY + 0.2, b.maxY - 0.1),
                MathHelper.clamp(eye.z, b.minZ, b.maxZ));
    }

    private static double eyeToBox(PlayerEntity me, LivingEntity t) {
        return me.getEyePos().distanceTo(aimPoint(me, t));
    }

    private static boolean isBow(ItemStack s) {
        return s.getItem() == Items.BOW;
    }

    private static boolean hasShieldAnywhere(PlayerEntity me) {
        if (me.getOffHandStack().getItem() == Items.SHIELD) return true;
        for (int i = 0; i < me.getInventory().size(); i++)
            if (me.getInventory().getStack(i).getItem() == Items.SHIELD) return true;
        return false;
    }

    private static boolean hasAxe(AltoClef mod) {
        for (ItemStack s : mod.getItemStorage().getItemStacksPlayerInventory(true))
            if (s != null && WeaponPicker.isAxe(s.getItem())) return true;
        return false;
    }

    private static boolean hasItem(AltoClef mod, Item... items) {
        return mod.getItemStorage().hasItem(items);
    }

    private static void releaseMove(InputControls in) {
        in.release(Input.MOVE_FORWARD);
        in.release(Input.MOVE_BACK);
        in.release(Input.MOVE_LEFT);
        in.release(Input.MOVE_RIGHT);
        in.release(Input.SPRINT);
    }

    private static void releaseAll(InputControls in) {
        releaseMove(in);
        in.release(Input.JUMP);
        in.release(Input.CLICK_RIGHT);
    }

    public LivingEntity getTarget() {
        return target;
    }

    public String stats() {
        return String.format("attacks=%d crits=%d sprintHits=%d axeHits=%d blocks=%d gapples=%d pots=%d dmgTaken=%.1f",
                attacks, crits, sprintHits, axeHits, blocks, gapples, pots, damageTaken);
    }

    @Override
    protected void onStop(Task interruptTask) {
        releaseAll(AltoClef.getInstance().getInputControls());
    }

    @Override
    public boolean isFinished() {
        return false;
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof PvpTask p && p.label.equals(label);
    }

    @Override
    protected String toDebugString() {
        return "PvP " + label + (target == null ? "" : " -> " + target.getName().getString());
    }
}
