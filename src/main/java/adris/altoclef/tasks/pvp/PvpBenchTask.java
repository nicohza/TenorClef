package adris.altoclef.tasks.pvp;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.tasksystem.Task;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Singleplayer PvP bench: gears the bot, summons armed mobs in front of it and fights them with
 * {@link PvpTask}. One CSV row per round in run/pvpbench. Needs the integrated server (commands
 * run as the server). Summon NBT is 1.21.4 (HandItems/ArmorItems).
 */
public class PvpBenchTask extends Task {

    private static final String ARMOR = "ArmorItems:[{id:\"iron_boots\",count:1},{id:\"iron_leggings\",count:1},{id:\"iron_chestplate\",count:1},{id:\"iron_helmet\",count:1}]";
    // name, summon commands (relative to the arena origin, bot at 0 0)
    private static final String[][] SCENARIOS = {
            {"husk_iron", "summon husk 8 ~ 0 {HandItems:[{id:\"iron_sword\",count:1},{}]," + ARMOR + ",PersistenceRequired:1b}"},
            {"vindicator", "summon vindicator 8 ~ 0 {PersistenceRequired:1b}"},
            {"skeleton", "summon skeleton 14 ~ 0 {HandItems:[{id:\"bow\",count:1},{}],ArmorItems:[{},{},{},{id:\"iron_helmet\",count:1}],PersistenceRequired:1b}"},
            {"husk_trio", "summon husk 8 ~ 2 {HandItems:[{id:\"iron_sword\",count:1},{}]," + ARMOR + ",PersistenceRequired:1b}",
                    "summon husk 8 ~ -2 {HandItems:[{id:\"iron_sword\",count:1},{}]," + ARMOR + ",PersistenceRequired:1b}",
                    "summon husk 10 ~ 0 {HandItems:[{id:\"iron_sword\",count:1},{}]," + ARMOR + ",PersistenceRequired:1b}"},
    };
    private static final int ROUND_TICKS = 20 * 60;

    private final int rounds;
    private int round = -1, ticks, wait;
    private PvpTask fight;
    private final List<String> rows = new ArrayList<>();
    private int wins;
    private boolean done;

    public PvpBenchTask(int rounds) {
        this.rounds = rounds;
    }

    @Override
    protected void onStart() {
        run("gamerule doMobSpawning false", "gamerule doImmediateRespawn true", "gamerule keepInventory true",
                "gamerule doDaylightCycle false", "time set day", "difficulty hard", "gamemode survival @a");
        nextRound();
    }

    private void nextRound() {
        round++;
        if (round >= rounds) {
            finish();
            return;
        }
        String[] sc = SCENARIOS[round % SCENARIOS.length];
        run("kill @e[type=!player]", "clear @a", "effect clear @a",
                "tp @a 0 ~ 0 -90 0",
                "item replace entity @a armor.head with iron_helmet",
                "item replace entity @a armor.chest with iron_chestplate",
                "item replace entity @a armor.legs with iron_leggings",
                "item replace entity @a armor.feet with iron_boots",
                "item replace entity @a weapon.offhand with shield",
                "give @a diamond_sword", "give @a diamond_axe", "give @a bow", "give @a golden_apple 3",
                "give @a splash_potion[potion_contents={potion:\"minecraft:healing\"}] 2",
                "give @a arrow 32", "give @a cooked_beef 16",
                "effect give @a instant_health 1 10", "effect give @a saturation 1 10");
        String[] summons = new String[sc.length - 1];
        System.arraycopy(sc, 1, summons, 0, summons.length);
        run(summons);
        fight = PvpTask.hostiles();
        ticks = 0;
        wait = 10;
    }

    @Override
    protected Task onTick() {
        if (done) return null;
        PlayerEntity me = AltoClef.getInstance().getPlayer();
        if (me == null) return null;
        if (wait > 0) {
            wait--;
            return null;
        }
        ticks++;
        boolean dead = me.isDead() || me.getHealth() <= 0;
        boolean cleared = ticks > 20 && fight.getTarget() == null && noHostiles(me);
        if (dead || cleared || ticks >= ROUND_TICKS) {
            String result = dead ? "death" : cleared ? "win" : "timeout";
            if (cleared) wins++;
            String row = String.format("%d,%s,%s,%d,%.1f,%d,%d,%d,%d,%d,%d,%d", round, SCENARIOS[round % SCENARIOS.length][0], result, ticks,
                    fight.damageTaken, fight.attacks, fight.crits, fight.sprintHits, fight.axeHits, fight.blocks, fight.gapples, fight.pots);
            rows.add(row);
            Debug.logMessage("PVPBENCH " + row);
            nextRound();
            return null;
        }
        return fight;
    }

    private boolean noHostiles(PlayerEntity me) {
        return me.getWorld().getEntitiesByClass(net.minecraft.entity.mob.HostileEntity.class,
                me.getBoundingBox().expand(64), e -> e.isAlive()).isEmpty();
    }

    private void finish() {
        done = true;
        try {
            Path dir = Paths.get("pvpbench");
            Files.createDirectories(dir);
            Path f = dir.resolve("pvpbench_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv");
            List<String> out = new ArrayList<>();
            out.add("round,opponent,result,ticks,dmgTaken,attacks,crits,sprintHits,axeHits,blocks,gapples,pots");
            out.addAll(rows);
            Files.write(f, out);
        } catch (IOException e) {
            Debug.logWarning("pvpbench csv: " + e);
        }
        Debug.logMessage("PVPBENCH SUMMARY wins=" + wins + "/" + rows.size());
        if (Boolean.getBoolean("tenorclef.pathbench.exit")) {
            Debug.logMessage("PVPBENCH exit requested");
            MinecraftClient.getInstance().scheduleStop();
        }
    }

    private static void run(String... cmds) {
        MinecraftServer server = MinecraftClient.getInstance().getServer();
        if (server == null) {
            Debug.logWarning("pvpbench needs singleplayer");
            return;
        }
        server.submit(() -> {
            var src = server.getCommandSource().withLevel(4);
            PlayerEntity p = server.getPlayerManager().getPlayerList().isEmpty() ? null : server.getPlayerManager().getPlayerList().get(0);
            if (p != null) src = src.withPosition(new net.minecraft.util.math.Vec3d(0, p.getY(), 0)).withWorld((net.minecraft.server.world.ServerWorld) p.getWorld());
            for (String c : cmds) server.getCommandManager().executeWithPrefix(src, c);
        }).join();
    }

    @Override
    public boolean isFinished() {
        return done;
    }

    @Override
    protected void onStop(Task interruptTask) {
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof PvpBenchTask;
    }

    @Override
    protected String toDebugString() {
        return "PvP bench round " + round;
    }
}
