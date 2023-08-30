package pb.lofe.chatl;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import pb.lofe.chatl.commands.TellCommand;


public final class ChatL extends JavaPlugin {


    private static ChatL instance;
    private final MiniMessage mm = MiniMessage.miniMessage();


    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        // Plugin startup logic

        getCommand("simplechatl").setExecutor(new CommandExecutor() {
            @Override
            public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
                if(!commandSender.isOp())
                    return true;
                if(!(commandSender instanceof Player)) {
                    return true;
                }

                reloadConfig();
                commandSender.sendMessage(mm.deserialize(getConfig().getString("messages.reloaded", "<green>Config reloaded.")));

                return true;
            }
        });
        Bukkit.getPluginManager().registerEvents(new ChatListener(), this);

        getCommand("msg").setExecutor(new TellCommand());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static ChatL i() { return instance; }
}
