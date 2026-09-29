package adris.altoclef.tasks.pvp;

import adris.altoclef.AltoClef;
import adris.altoclef.tasksystem.Task;
import baritone.process.PvpProcess;
import net.minecraft.entity.LivingEntity;

/**
 * Thin wrapper over Ostinato's {@code PvpProcess}, which owns the fighting movement (crits,
 * W-tap, strafe, jump reset, shield, axe, bow, gapples, totem). This task only starts it, keeps
 * the other chains out of the way and mirrors its stats.
 */
public class PvpTask extends Task {

    private enum Mode { PLAYER, PLAYERS, HOSTILES }

    private final Mode mode;
    private final String name;
    private static volatile long lastTickMs;

    public int attacks, crits, sprintHits, blocks, gapples, pots, axeHits;
    public float damageTaken;

    private PvpTask(Mode mode, String name) {
        this.mode = mode;
        this.name = name;
    }

    public static PvpTask player(String name) {
        return new PvpTask(Mode.PLAYER, name);
    }

    public static PvpTask nearestPlayer() {
        return new PvpTask(Mode.PLAYERS, "players");
    }

    public static PvpTask hostiles() {
        return new PvpTask(Mode.HOSTILES, "hostiles");
    }

    /** Marks PvP as active for this tick (the bench calls it between rounds too). */
    public static void touch() {
        lastTickMs = System.currentTimeMillis();
    }

    /** True while PvP is ticking; MobDefense/Food chains stand down so it keeps control. */
    public static boolean anyActive() {
        return System.currentTimeMillis() - lastTickMs < 500;
    }

    private static PvpProcess proc() {
        return AltoClef.getInstance().getClientBaritone().getPvpProcess();
    }

    @Override
    protected void onStart() {
        switch (mode) {
            case PLAYER -> proc().attackPlayer(name);
            case PLAYERS -> proc().attackPlayers();
            case HOSTILES -> proc().attackHostiles();
        }
    }

    @Override
    protected Task onTick() {
        touch();
        PvpProcess p = proc();
        if (!p.isActive()) onStart(); // cancelled from outside (e.g. #stop): pick it back up
        attacks = p.attacks;
        crits = p.crits;
        sprintHits = p.sprintHits;
        axeHits = p.axeHits;
        blocks = p.blocks;
        gapples = p.gapples;
        damageTaken = p.damageTaken;
        setDebugState(p.displayName0());
        return null;
    }

    public LivingEntity getTarget() {
        return (LivingEntity) (Object) proc().getTarget();
    }

    public String stats() {
        return proc().stats();
    }

    @Override
    protected void onStop(Task interruptTask) {
        proc().onLostControl();
    }

    @Override
    public boolean isFinished() {
        return false;
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof PvpTask p && p.mode == mode && p.name.equals(name);
    }

    @Override
    protected String toDebugString() {
        return "PvP " + name;
    }
}
