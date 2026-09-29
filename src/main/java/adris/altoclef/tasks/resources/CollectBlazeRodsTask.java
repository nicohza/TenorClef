package adris.altoclef.tasks.resources;

import adris.altoclef.tasks.speedrun.testrun2.T2Log;
import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.multiversion.blockpos.BlockPosVer;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasks.construction.PutOutFireTask;
import adris.altoclef.tasks.entity.KillEntitiesTask;
import adris.altoclef.tasks.movement.DefaultGoToDimensionTask;
import adris.altoclef.tasks.movement.GetToBlockTask;
import adris.altoclef.tasks.movement.RunAwayFromHostilesTask;
import adris.altoclef.tasks.movement.SearchChunkForBlockTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.Dimension;
import adris.altoclef.util.helpers.WorldHelper;
import net.minecraft.block.Blocks;
import adris.altoclef.util.helpers.LookHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.BlazeEntity;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.Optional;
import java.util.function.Predicate;

public class CollectBlazeRodsTask extends ResourceTask {

    private static final double SPAWNER_BLAZE_RADIUS = 32;
    private static final double TOO_LITTLE_HEALTH_BLAZE = 10;
    private float _hpSample = 20;
    private long _hpSampleMs;
    private static final int TOO_MANY_BLAZES = 5;
    private final int _count;
    private final Task _searcher = new SearchChunkForBlockTask(Blocks.NETHER_BRICKS);

    // Why was this here???
    //private Entity _toKill;
    private BlockPos _foundBlazeSpawner = null;
    private boolean _retreating = false;
    // S286: s285t sat 5+ min at one block: every unexplored-chunk goal was unreachable
    // ("couldn't get more than 0.0 blocks" after 60k nodes) and wander failed too. Push out on a heading.
    private net.minecraft.util.math.Vec3d _searchAnchor;
    private long _searchAnchorMs;
    private int _pushDir;
    private Task _push;

    public CollectBlazeRodsTask(int count) {
        super(Items.BLAZE_ROD, count);
        _count = count;
    }

    private static boolean isHoveringAboveLavaOrTooHigh(AltoClef mod, Entity entity) {
        int MAX_HEIGHT = 11;
        for (BlockPos check = entity.getBlockPos(); entity.getBlockPos().getY() - check.getY() < MAX_HEIGHT; check = check.down()) {
            if (mod.getWorld().getBlockState(check).getBlock() == Blocks.LAVA) return true;
            if (WorldHelper.isSolidBlock(check)) return false;
        }
        return true;
    }

    @Override
    protected void onResourceStart(AltoClef mod) {

    }

    @Override
    protected Task onResourceTick(AltoClef mod) {
        // We must go to the nether.
        if (WorldHelper.getCurrentDimension() != Dimension.NETHER) {
            setDebugState("Going to nether");
            return new DefaultGoToDimensionTask(Dimension.NETHER);
        }

        Optional<Entity> toKill = Optional.empty();
        // If there is a blaze, kill it.
        if (mod.getEntityTracker().entityFound(BlazeEntity.class)) {
            toKill = mod.getEntityTracker().getClosestEntity(BlazeEntity.class);
            if (toKill.isPresent()) {
                // S253: s253t burned 14 -> 3.5hp vs two blazes and died; only 5+ blazes ever triggered a retreat.
                int blazes = mod.getEntityTracker().getTrackedEntities(BlazeEntity.class).size();
                // S278: s277t retreated at 5.6hp, re-engaged at 10.6 and was knocked back to 4 within
                // seconds, burning its food on regen until a fireball killed it. Once retreating,
                // stay away until hp is back to 16.
                float hp = mod.getPlayer().getHealth();
                if (hp >= 16) _retreating = false;
                // S299: s297u latched the retreat at hp 14 with hunger 16 and no food. Health cannot
                // regen below hunger 18, so it stood still for 40s waiting for hp 16. Only keep the
                // latch while regen is possible.
                if (_retreating && mod.getPlayer().getHungerManager().getFoodLevel() < 18 && !mod.getFoodChain().hasFood()) {
                    _retreating = false;
                }
                // S311: s307t went 20 -> 10.5 -> 6 -> dead in ~13s against 2+ blazes; hp<=10 was too
                // late. Retreat at 14 with multiple blazes, or on any fast hp drop.
                long nowMs = System.currentTimeMillis();
                if (nowMs - _hpSampleMs > 4000) { _hpSample = hp; _hpSampleMs = nowMs; }
                boolean fastDrop = _hpSample - hp >= 6 && hp <= 14;
                if (!_retreating && (fastDrop || hp <= 14 && blazes >= 2)) {
                    T2Log.force("S311", "blaze retreat hp=" + hp + " blazes=" + blazes + " fastDrop=" + fastDrop);
                    _retreating = true;
                }
                if (_retreating || hp <= TOO_LITTLE_HEALTH_BLAZE &&
                        (blazes >= TOO_MANY_BLAZES || blazes >= 2 || mod.getPlayer().isOnFire()
                                || hp <= 6)) {
                    _retreating = true;
                    setDebugState("Running away as there are too many blazes nearby.");
                    return new RunAwayFromHostilesTask(15 * 2, true);
                }
            }

            if (_foundBlazeSpawner != null && toKill.isPresent()) {
                Entity kill = toKill.get();
                Vec3d nearest = kill.getPos();

                double sqDistanceToPlayer = nearest.squaredDistanceTo(mod.getPlayer().getPos());//_foundBlazeSpawner.getX(), _foundBlazeSpawner.getY(), _foundBlazeSpawner.getZ());
                // Ignore if the blaze is too far away.
                if (sqDistanceToPlayer > SPAWNER_BLAZE_RADIUS * SPAWNER_BLAZE_RADIUS) {
                    // If the blaze can see us it needs to go lol
                    BlockHitResult hit = mod.getWorld().raycast(new RaycastContext(LookHelper.cameraPos(mod.getPlayer(), 1.0F), LookHelper.cameraPos(kill, 1.0F), RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, mod.getPlayer()));
                    if (hit != null && BlockPosVer.getSquaredDistance(hit.getBlockPos(),mod.getPlayer().getPos()) < sqDistanceToPlayer) {
                        toKill = Optional.empty();
                    }
                }
            }
        }
        if (toKill.isPresent() && toKill.get().isAlive() && !isHoveringAboveLavaOrTooHigh(mod, toKill.get())) {
            setDebugState("Killing blaze");
            Predicate<Entity> safeToPursue = entity -> !isHoveringAboveLavaOrTooHigh(mod, entity);
            return new KillEntitiesTask(safeToPursue, toKill.get().getClass());
        }


        // If the blaze spawner somehow isn't valid
        if (_foundBlazeSpawner != null && mod.getChunkTracker().isChunkLoaded(_foundBlazeSpawner) && !isValidBlazeSpawner(mod, _foundBlazeSpawner)) {
            Debug.logMessage("Blaze spawner at " + _foundBlazeSpawner + " too far away or invalid. Re-searching.");
            _foundBlazeSpawner = null;
        }

        // If we have a blaze spawner, go near it.
        if (_foundBlazeSpawner != null) {
            if (!_foundBlazeSpawner.isWithinDistance(mod.getPlayer().getPos(), 4)) {
                setDebugState("Going to blaze spawner");
                return new GetToBlockTask(_foundBlazeSpawner.up(), false);
            } else {

                // Put out fire that might mess with us.
                Optional<BlockPos> nearestFire = mod.getBlockScanner().getNearestWithinRange(_foundBlazeSpawner, 5, Blocks.FIRE);
                if (nearestFire.isPresent()) {
                    setDebugState("Clearing fire around spawner to prevent loss of blaze rods.");
                    return new PutOutFireTask(nearestFire.get());
                }

                setDebugState("Waiting near blaze spawner for blazes to spawn");
                return null;
            }
        } else {
            // Search for blaze
            Optional<BlockPos> pos = mod.getBlockScanner().getNearestBlock(blockPos->isValidBlazeSpawner(mod, blockPos),Blocks.SPAWNER);

            pos.ifPresent(blockPos -> _foundBlazeSpawner = blockPos);
        }

        // We need to find our fortress.
        setDebugState("Searching for fortress/Traveling around fortress");
        long now = System.currentTimeMillis();
        net.minecraft.util.math.Vec3d here = mod.getPlayer().getPos();
        if (_push != null && !_push.isFinished() && now - _searchAnchorMs < 45_000) {
            if (here.distanceTo(_searchAnchor) > 60) { _push = null; _searchAnchor = here; _searchAnchorMs = now; }
            else return _push;
        }
        if (_searchAnchor == null || here.distanceTo(_searchAnchor) > 8) {
            _searchAnchor = here;
            _searchAnchorMs = now;
            _push = null;
        } else if (now - _searchAnchorMs > 40_000) {
            int[][] dirs = {{1,0},{0,1},{-1,0},{0,-1}};
            int[] d = dirs[_pushDir++ % 4];
            adris.altoclef.tasks.speedrun.testrun2.T2Log.force("S286", "fortress search stuck 40s at " + mod.getPlayer().getBlockPos().toShortString() + " - pushing dir " + d[0] + "," + d[1]);
            _push = new adris.altoclef.tasks.movement.GoInDirectionXZTask(here, new net.minecraft.util.math.Vec3d(d[0], 0, d[1]), 10);
            _searchAnchorMs = now;
            return _push;
        }
        return _searcher;
    }

    private boolean isValidBlazeSpawner(AltoClef mod, BlockPos pos) {
        if (!mod.getChunkTracker().isChunkLoaded(pos)) {
            // If unloaded, go to it. Unless it's super far away.
            return false;
            //return pos.isWithinDistance(mod.getPlayer().getPos(),3000);
        }
        return WorldHelper.getSpawnerEntity(pos) instanceof BlazeEntity;
    }

    @Override
    protected void onResourceStop(AltoClef mod, Task interruptTask) {

    }

    @Override
    protected boolean isEqualResource(ResourceTask other) {
        return other instanceof CollectBlazeRodsTask;
    }

    @Override
    protected String toDebugStringName() {
        return "Collect blaze rods - "+ AltoClef.getInstance().getItemStorage().getItemCount(Items.BLAZE_ROD)+"/"+_count;
    }

    @Override
    protected boolean shouldAvoidPickingUp(AltoClef mod) {
        return false;
    }
}
