package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.args.StringArg;
import adris.altoclef.commandsystem.exception.BadCommandSyntaxException;
import adris.altoclef.commandsystem.exception.CommandException;
import adris.altoclef.multiversion.entity.PlayerVer;
import baritone.api.Settings;
import baritone.api.pathing.goals.GoalBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a small course east of the player (needs cheats: uses /fill) and paths across it with the
 * Ostinato movement feature being shown off.
 */
public class ShowcaseCommand extends Command {

    public ShowcaseCommand() throws CommandException {
        super("show", "Showcase course: parkour | swim | dive | boat | kinematic | physics | off",
                new StringArg("demo"));
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        String demo = parser.get(String.class).toLowerCase();
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) {
            finish();
            return;
        }
        Settings s = mod.getClientBaritoneSettings();
        BlockPos p = mc.player.getBlockPos();
        int x = p.getX(), y = p.getY(), z = p.getZ();
        List<String> cmds = new ArrayList<>();
        BlockPos goal;
        s.kinematicTravel.value = false;
        //#if MC < 12111
        s.physicsTravel.value = false;
        //#endif
        switch (demo) {
            case "parkour" -> {
                // Gaps of 4, 3 and 2 blocks, the last onto a 1-wide pillar, over a pit.
                clear(cmds, x, y, z, 30, 3);
                cmds.add(fill(x + 3, y - 6, z - 3, x + 26, y - 1, z + 3, "air"));
                cmds.add(fill(x + 3, y - 7, z - 3, x + 26, y - 7, z + 3, "water"));
                cmds.add(fill(x - 1, y - 1, z - 1, x + 2, y - 1, z + 1, "gold_block"));
                cmds.add(fill(x + 7, y - 1, z - 1, x + 9, y - 1, z + 1, "gold_block"));   // after 4-gap
                cmds.add(fill(x + 13, y - 1, z - 1, x + 15, y - 1, z + 1, "gold_block")); // after 3-gap
                cmds.add(fill(x + 18, y - 1, z, x + 18, y - 1, z, "gold_block"));         // 2-gap, narrow
                cmds.add(fill(x + 22, y - 1, z - 1, x + 26, y - 1, z + 1, "emerald_block"));
                s.allowParkour.value = true;
                goal = new BlockPos(x + 25, y, z);
            }
            case "swim", "dive" -> {
                // Glass-walled pool; "dive" roofs the middle so the route goes under it via an air pocket.
                boolean roof = demo.equals("dive");
                // The pool spans the whole corridor and tall glass walls seal the sides, so there is no way round.
                clear(cmds, x, y, z, 32, 4);
                // Bedrock can't be broken and the barrier on top is too tall to pillar past cheaply.
                cmds.add(fill(x - 2, y - 8, z - 5, x + 32, y + 5, z - 5, "bedrock"));
                cmds.add(fill(x - 2, y - 8, z + 5, x + 32, y + 5, z + 5, "bedrock"));
                cmds.add(fill(x - 2, y - 8, z - 5, x - 2, y + 5, z + 5, "bedrock"));
                cmds.add(fill(x - 2, y + 6, z - 5, x + 32, y + 14, z - 5, "barrier"));
                cmds.add(fill(x - 2, y + 6, z + 5, x + 32, y + 14, z + 5, "barrier"));
                cmds.add(fill(x - 2, y + 6, z - 5, x - 2, y + 14, z + 5, "barrier"));
                cmds.add(fill(x - 1, y - 1, z - 4, x + 2, y - 1, z + 4, "gold_block"));
                cmds.add(fill(x + 3, y - 7, z - 4, x + 27, y - 7, z + 4, "glass"));
                cmds.add(fill(x + 3, y - 6, z - 4, x + 27, y - 1, z + 4, "water"));
                cmds.add(fill(x + 28, y - 7, z - 4, x + 28, y - 1, z + 4, "glass"));
                if (roof) {
                    cmds.add(fill(x + 8, y - 1, z - 4, x + 22, y + 5, z + 4, "bedrock"));
                    cmds.add(fill(x + 15, y - 1, z - 1, x + 15, y - 1, z + 1, "air")); // air pocket
                }
                cmds.add(fill(x + 29, y - 1, z - 4, x + 31, y - 1, z + 4, "emerald_block"));
                s.swimInWater.value = true;
                goal = new BlockPos(x + 30, y, z);
            }
            case "boat" -> {
                // Long canal; the BoatProcess places the boat, sails and picks it back up.
                clear(cmds, x, y, z, 100, 4);
                cmds.add(fill(x + 2, y - 3, z - 5, x + 94, y - 1, z + 5, "stone"));
                cmds.add(fill(x + 3, y - 2, z - 4, x + 93, y - 1, z + 4, "water"));
                cmds.add(fill(x + 95, y - 1, z - 2, x + 98, y - 1, z + 2, "emerald_block"));
                cmds.add("give @s oak_boat");
                s.allowBoats.value = true;
                goal = new BlockPos(x + 97, y, z);
            }
            case "kinematic", "physics" -> {
                // Runway with a slalom of pillars: the look-ahead controllers carve smooth lines through it.
                clear(cmds, x, y, z, 64, 4);
                cmds.add(fill(x - 1, y - 1, z - 5, x + 63, y - 1, z + 5, "smooth_stone"));
                for (int i = 0; i < 6; i++) {
                    int px = x + 8 + i * 9, pz = z + (i % 2 == 0 ? -1 : 1) * 2;
                    cmds.add(fill(px, y, pz - 2, px, y + 2, pz + 2, "red_concrete"));
                }
                cmds.add(fill(x + 60, y - 1, z - 1, x + 63, y - 1, z + 1, "emerald_block"));
                if (demo.equals("kinematic")) s.kinematicTravel.value = true;
                //#if MC >= 12111
                //$$ else { /* Ostinato's 1.21.11 build has no physicsTravel setting */ }
                //#else
                else s.physicsTravel.value = true;
                //#endif
                goal = new BlockPos(x + 62, y, z);
            }
            case "off" -> {
                mod.getClientBaritone().getPathingBehavior().cancelEverything();
                mod.log("Showcase stopped (kinematic/physics travel off).");
                finish();
                return;
            }
            default -> throw new BadCommandSyntaxException("Unknown demo '" + demo + "'. Try parkour, swim, dive, boat, kinematic, physics, off.");
        }
        for (String c : cmds) PlayerVer.sendChatCommand(mc.player, c);
        mod.log("Showcase '" + demo + "': course built, heading for the emerald pad.");
        final BlockPos g = goal;
        // Give the server a moment to apply the /fill commands before planning.
        Thread t = new Thread(() -> {
            try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
            mc.execute(() -> mod.getClientBaritone().getCustomGoalProcess().setGoalAndPath(new GoalBlock(g.getX(), g.getY(), g.getZ())));
            // -Dtenorclef.show.trace=true: log the player's water/pose state twice a second while the demo runs.
            for (int i = 0; Boolean.getBoolean("tenorclef.show.trace") && i < 80; i++) {
                try { Thread.sleep(500); } catch (InterruptedException ignored) {}
                mc.execute(() -> {
                    var pl = mc.player;
                    if (pl == null) return;
                    String mv = "-";
                    var ex = mod.getClientBaritone().getPathingBehavior().getCurrent();
                    if (ex != null && ex.getPosition() < ex.getPath().movements().size()) {
                        var m = ex.getPath().movements().get(ex.getPosition());
                        mv = m.getClass().getSimpleName() + " " + m.getSrc().toShortString() + "->" + m.getDest().toShortString();
                    }
                    System.out.println("SHOWTRACE mv=" + mv);
                    System.out.printf("SHOWTRACE x=%.2f y=%.2f water=%s under=%s swim=%s sprint=%s sneak=%s pose=%s pitch=%.0f pathing=%s%n",
                            pl.getX(), pl.getY(), pl.isTouchingWater(), pl.isSubmergedInWater(), pl.isSwimming(), pl.isSprinting(),
                            pl.isSneaking(), pl.getPose(), pl.getPitch(), mod.getClientBaritone().getPathingBehavior().isPathing());
                });
            }
        }, "showcase-start");
        t.setDaemon(true);
        t.start();
        finish();
    }

    private static void clear(List<String> cmds, int x, int y, int z, int len, int half) {
        cmds.add(fill(x - 1, y, z - half - 1, x + len, y + 6, z + half + 1, "air"));
    }

    private static String fill(int x1, int y1, int z1, int x2, int y2, int z2, String block) {
        return "fill " + x1 + " " + y1 + " " + z1 + " " + x2 + " " + y2 + " " + z2 + " " + block;
    }
}
