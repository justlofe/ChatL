package pr.lofe.mdr.xchat;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import pr.lofe.mdr.xchat.messaging.MultiServerService;
import pr.lofe.mdr.xchat.commands.IgnoreCommand;
import pr.lofe.mdr.xchat.commands.ReplyCommand;
import pr.lofe.mdr.xchat.commands.TellCommand;

import java.util.HashMap;


public final class xChat extends JavaPlugin {


    private static xChat instance;
    private final MiniMessage mm = MiniMessage.miniMessage();
    public HashMap<String, String> lastMessage = new HashMap<>();


    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        // Plugin startup logic

        getCommand("simplechatl").setExecutor((commandSender, command, s, strings) -> {
            if(!commandSender.isOp())
                return true;

            reloadConfig();
            commandSender.sendMessage(mm.deserialize(getConfig().getString("messages.reloaded", "<green>Config reloaded.")));

            return true;
        });
        Bukkit.getPluginManager().registerEvents(new xChatListener(), this);

        getCommand("msg").setExecutor(new TellCommand());
        getCommand("msg").setTabCompleter(new TellCommand());

        getCommand("reply").setExecutor(new ReplyCommand());
        getCommand("reply").setTabCompleter(new ReplyCommand());

        getCommand("ignore").setExecutor(new IgnoreCommand());
        getCommand("ignore").setTabCompleter(new IgnoreCommand());

        MultiServerService service = new MultiServerService();
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        Bukkit.getMessenger().registerIncomingPluginChannel(this, "BungeeCord", service);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        Bukkit.getMessenger().unregisterIncomingPluginChannel(this);
        Bukkit.getMessenger().unregisterOutgoingPluginChannel(this);
    }

    public static xChat i() { return instance; }
}
