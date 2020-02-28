package top.mcland.sendtoLobby;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.plugin.java.JavaPlugin;
import fr.xephi.authme.api.v3.AuthMeApi;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import uk.org.whoami.authme.api.API;
import static java.lang.Integer.parseInt;


public class Main
        extends JavaPlugin
        implements Listener
{
    ConcurrentHashMap<String, BukkitTask> hash = new ConcurrentHashMap();

    public Main get()
    {
        return this;
    }

    @Override
    public void onEnable() {
        getConfig().addDefault("sendto", "Lobby");
        getConfig().addDefault("wait", "15");
        getConfig().options().copyDefaults(true);
        saveConfig();
        getServer().getPluginManager().registerEvents(this, this);
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        getLogger().info("sendtoLobby 让玩家登陆后指定时间内传送至指定服务器.");
        getLogger().info("玩家掉入虚空自动拉回出生点");
        getLogger().info("禁用聊天、雨天");
        getLogger().info("进服时补满饥饿值");
        getLogger().info("传送目标：" + getConfig().getString("sendto"));
        getLogger().info("传送等待时间：" + getConfig().getString("wait"));
        getLogger().info("作者：XiaoMingXD");
        getLogger().info("");
        getLogger().info("#++++++++++++++++++++++++++++++#");
        getLogger().info("#     优质服务商 就选大白云    #");
        getLogger().info("#        QQ群：564099403       #");
        getLogger().info("#++++++++++++++++++++++++++++++#");

        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        (new BukkitRunnable() {
            public void run() {
                Iterator var2 = Bukkit.getOnlinePlayers().iterator();

                while(var2.hasNext()) {
                    final Player p = (Player)var2.next();

                    try {
                        if (API.isAuthenticated(p)) {
                            continue;
                        }
                    } catch (Throwable var4) {
                        if (!AuthMeApi.getInstance().isAuthenticated(p)) {
                            continue;
                        }
                    }

                    if (!hash.containsKey(p.getName())) {
                        hash.put(p.getName(), (new BukkitRunnable() {
                            int count = parseInt(getConfig().getString("wait"));

                            public void run() {
                                if (this.count <= 0) {
                                    if (p != null && p.isOnline()) {
                                        send(p);
                                    }

                                    hash.remove(p.getName());
                                    cancel();
                                } else {
                                    if (p == null || !p.isOnline()) {
                                        hash.remove(p.getName());
                                        cancel();
                                    }

                                    sendActionBar(p, ChatColor.BOLD + "你会在 " + count + " 秒内传送至大厅");
                                    --count;
                                }
                            }
                        }).runTaskTimerAsynchronously(get(), 0L, 20L));
                    }
                }
            }
        }).runTaskTimer(this, 0L, 20L);
    }

    public void send(Player p)
    {
        if (AuthMeApi.getInstance().isAuthenticated(p))
        {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Connect");
            out.writeUTF(getConfig().getString("sendto"));
            p.sendPluginMessage(this, "BungeeCord", out.toByteArray());
        }
    }


    public void sendActionBar(Player p, String msg)
    {
        IChatBaseComponent cbc = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + msg + "\"}");
        PacketPlayOutChat ppoc = new PacketPlayOutChat(cbc, (byte)2);
        ((CraftPlayer)p).getHandle().playerConnection.sendPacket(ppoc);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e)
    {
        e.setCancelled(true);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent e)
    {
        if ((e.getEntity() instanceof Player))
        {
            if (e.getCause() == EntityDamageEvent.DamageCause.VOID)
            {
                ((Player)e.getEntity()).sendMessage("§d[§a麦块大陆§d]§b看来你掉下去了？ 看我把你拉上来！");
                e.getEntity().teleport(e.getEntity().getWorld().getSpawnLocation());
            }
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onRain(WeatherChangeEvent e)
    {
        e.setCancelled(true);
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onjoin(PlayerJoinEvent e)
    {
        e.getPlayer().setFoodLevel(25);
    }

    @Override
    public void onDisable() {
        getLogger().info("sendtoLobby已停用");
    }
}
