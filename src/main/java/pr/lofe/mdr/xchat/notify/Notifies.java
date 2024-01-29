package pr.lofe.mdr.xchat.notify;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pr.lofe.mdr.engine.util.ObjectUtils;
import pr.lofe.mdr.expansion.MainExpansion;
import pr.lofe.mdr.xchat.xChat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

public class Notifies {

    private static final double FRAMES = 3D;
    private static final int FRAME_DELAY = 1;

    private static final HashSet<Player> notifyTimeout = new HashSet<>();
    private static final HashMap<Player, Integer> dots = new HashMap<>();

    public static void trySend(@Nullable Player player, @Nullable String string) {
        if(notifyTimeout.contains(player)) {
        }
        else {
            send(player, string);
        }
    }

    public static boolean send(@Nullable Player player, @Nullable String string) {
        if (player == null || string == null) return false;
        else {
            notifyTimeout.add(player);
            Bukkit.getScheduler().runTaskLater(xChat.i(), () -> notifyTimeout.remove(player), 20L);

            if(dots.get(player) == null) dots.put(player, 0);
            else dots.put(player, dots.get(player) + 1);

            if(dots.get(player) >= 3) dots.put(player, 0);

            player.sendActionBar(MiniMessage.miniMessage().deserialize(PlaceholderAPI.setPlaceholders(player, string.replaceAll("#DOTS", xChat.i().getConfig().getStringList("dots").get(dots.get(player))))));
            return true;
        }
    }

    public static void sendWithAnimation(@NotNull Player player, @NotNull String[] strings) {
        Stack<String> args = new Stack<>();
        for(int i = strings.length - 1; i >= 0; i--)
            args.push(strings[i]);
        int frameSize = round((double)(strings.length) / FRAMES);
        String[] rawFrames = new String[3];
        int remainder = strings.length;
        int currFrame = 0, frameParts = 0;
        while (remainder > frameSize) {
            if(frameParts >= frameSize) {
                frameParts = 0;
                currFrame++;
            }
            else {
                rawFrames[currFrame] = ObjectUtils.notNull(rawFrames[currFrame], "") + args.pop() + " ";
                remainder--;
                frameParts++;
            }
        }
        if(remainder > 0) {
            for(int i = 0; i < remainder; i++)
                rawFrames[2] = ObjectUtils.notNull(rawFrames[2], "") + args.pop() + " ";
        }

        String[] frames = {
                rawFrames[0],
                rawFrames[0] + rawFrames[1],
                rawFrames[0] + rawFrames[1] + rawFrames[2]
        };


        int delay = FRAME_DELAY;
        for(String string : frames) {
            Bukkit.getScheduler().runTaskLater(xChat.i(), () -> {
                MainExpansion.textStack.put(player, string);
                send(player, "%nameplates_background_mention%");
                if(string.equals(frames[(int) FRAMES - 1])) {
                    player.playSound(player.getLocation(), "custom.sfx.notify", SoundCategory.MASTER, 100, 1);
                }   
            }, delay);
            delay += FRAME_DELAY;
        }
    }

    private static int round(double expression) {
        double frac = expression % 1;
        if(frac == expression) return (int) expression;
        else if(frac >= 0.5) return (int) (expression - frac) + 1;
        else return (int) (expression - frac);
    }

}
