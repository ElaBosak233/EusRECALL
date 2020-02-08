package cc.eumc.recall;

import com.sun.org.apache.regexp.internal.RE;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public final class RECALL extends JavaPlugin {

    private static final Logger log = Logger.getLogger("Minecraft");
    private static Economy econ = null;
    private static Permission perms = null;
    private static Chat chat = null;


    public Map<Player,Location> loc = new HashMap<Player, Location>();

    int done = 0;

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getConsoleSender().sendMessage(ChatColor.AQUA.UNDERLINE+"[[RECALL]] service has been started");
        saveDefaultConfig();

        if (!setupEconomy() ) {
            log.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        setupPermissions();
        setupChat();
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    private boolean setupChat() {
        RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
        if(rsp == null){
            return false;
        }
        chat = rsp.getProvider();
        return chat != null;
    }

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
    }



    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getServer().getConsoleSender().sendMessage(ChatColor.AQUA.UNDERLINE+"[[RECALL]] service has been closed!");
        getServer().getConsoleSender().sendMessage(ChatColor.GREEN.UNDERLINE+"Thank you to use Eus[[RECALL]]!");
    }


    //接下来是命令部分，控制/recall
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player p = (Player) sender;

        //配置命令/recall
        if (command.getName().equalsIgnoreCase("recall")) {
            if (sender instanceof Player) {
                //判断是不是玩家
                if (args.length > 0) {
                    //判断命令长短
                    sender.sendMessage("§9§l[[RECALL]]: Too many arguments!");
                    return false;
                } else {
                    if(loc.get(p) == null){
                        double money = econ.getBalance(p);
                        int cost = getConfig().getInt("RECALL.cost");
                        //判断金钱
                        if(money >= cost){
                            econ.withdrawPlayer(p,cost);
                            loc.put(p,p.getLocation()); //记录玩家名、坐标
                            sender.sendMessage("§e§l[[RECALL]]: You cost "+ChatColor.GREEN.UNDERLINE.BOLD+cost+" to start [[RECALL]]!");
                            Bukkit.broadcastMessage("§9§l[[RECALL]] Request:" + " "+ ChatColor.AQUA.UNDERLINE + p.getName() + "§a§l is starting the [[RECALL]]!"+"§c§l§n Use /rcaccept " +ChatColor.RED.UNDERLINE.BOLD +p.getName() + " " + "§9§lto accept!");
                            sender.sendMessage("§e§lYour [[RECALL]] location is "+ChatColor.RED.BOLD.UNDERLINE+p.getLocation().getWorld()+","+ChatColor.RED.BOLD.UNDERLINE+p.getLocation().getX()+","+ChatColor.RED.BOLD.UNDERLINE+p.getLocation().getY()+","+ChatColor.RED.BOLD.UNDERLINE+p.getLocation().getZ()+","+"§9§l Please wait for your friend~");
                            //
                            //异步线程开始计时
                            Bukkit.getScheduler().runTaskAsynchronously(this , new Runnable() {
                                @Override
                                public void run() {
                                    sender.sendMessage("§a§l[[RECALL]]: Starting the timer...");
                                    try {
                                        Thread.sleep(60000);
                                    } catch (InterruptedException e) {
                                        //空
                                    }
                                    if(!(command.getName().equalsIgnoreCase("rccancel"))){
                                        sender.sendMessage("§a§l[[RECALL]]: Timeout");
                                        loc.remove(p);
                                        sender.sendMessage("§a§l[[RECALL]]: Your [[RECALL]] has been closed by System!");
                                    }

                                }
                            });

                            return true;
                        }else{
                            sender.sendMessage("§9§l[[RECALL]]: You don't have enough money to use [[RECALL]]!");
                            return true;
                        }
                    }else{
                        sender.sendMessage("§9§l[[RECALL]]: You can't use it too quickly!");
                        return true;
                    }

                }
            }else {
                sender.sendMessage("§4§l[[RECALL]]: ONLY Player can use this command!");
                return true;
            }
        }

        //配置命令/rcaccept
        if(command.getName().equalsIgnoreCase("rcaccept")){
            if (sender instanceof Player) {
                if(args.length == 0){
                    sender.sendMessage("§e§l[[RECALL]]: Use /rcaccept [PlayerName] to accept!");
                }else{
                    if(args.length == 1){
                        //设置目标玩家target（args[1]即输入的玩家名）
                        Player target = Bukkit.getPlayer(args[1]);
                        if(target != null){
                            //玩家在线
                            if(loc.get(target) == null){
                                sender.sendMessage("§e§l[[RECALL]]: Player didn't use [[RECALL]]!");
                                return true;
                            }else{
                                Location back = p.getLocation();
                                p.sendMessage("§e§lTeleporting"+"§e§lTo "+ChatColor.AQUA.UNDERLINE + target.getName());
                                p.teleport(loc.get(target));
                                Location now = p.getLocation();
                                if(back != now){
                                    p.sendMessage("§e§lSucceed!"+"§e§lJoin your friend!");
                                    return true;
                                }else {
                                    p.sendMessage("§c§lFailed!"+"§c§lPlease try again later!");
                                    return true;
                                }
                            }
                        }else{
                            //玩家不在线
                            sender.sendMessage("§e§l[[RECALL]]: Player is not online!");
                            return true;
                        }
                    }else{
                        sender.sendMessage("§9§l[[RECALL]]: Too many arguments!");
                    }

                }
            }else{
                sender.sendMessage("§4§l[[RECALL]]: ONLY Player can use this command!");
                return true;
            }
        }

        //配置命令/rccancel
        if(command.getName().equalsIgnoreCase("rccancel")){
            if (sender instanceof Player) {
                if(loc.get(p) != null){
                    loc.remove(p);
                    Bukkit.broadcastMessage("§a§l[[RECALL]]: "+ ChatColor.BLUE.BOLD.UNDERLINE + p.getName() +"§a§l has closed the [[RECALL]]!");
                    p.sendMessage("§c§lClose it Successfully!"+"§c§l You can try it later!");
                    return true;
                }else{
                    sender.sendMessage("§a§l[[RECALL]]: Your [[RECALL]] isn't started!");
                    return true;
                }
            }else {
                sender.sendMessage("§4§l[[RECALL]]: ONLY Player can use this command!");
                return true;
            }
        }

        return false;
    }

}
