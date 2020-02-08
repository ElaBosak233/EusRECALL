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
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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

    int done = 0;



    public Map<Player,Location> loc = new HashMap<Player, Location>();
    SimpleDateFormat df = new SimpleDateFormat("mm");



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


    //接下来是命令部分，控制RECALL
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {


        //配置命令/rc
        if (command.getName().equalsIgnoreCase("rc")) {
            if (sender instanceof Player) {
                //判断是不是玩家
                Player p = (Player) sender;
                if (args.length > 0) {
                    //判断命令长短
                    sender.sendMessage("§9§l[[RECALL]]: Too many arguments!");
                    return false;
                } else {
                    if(loc.get(p) == null){
                        int now = Calendar.MINUTE;
                        //RECALL.cd负责重新开启的时间
//                        if(Math.abs(now - done) <= getConfig().getInt("RECALL.cd")){
//                            sender.sendMessage("§9§l[[RECALL]]: You can't use it too quickly!");
//                            return false;
//                        }else{
                            double money = econ.getBalance(p);
                            int cost = getConfig().getInt("RECALL.cost");
                            //判断金钱
                            if(money >= cost){
                                econ.withdrawPlayer(p,cost);
                                loc.put(p,p.getLocation()); //记录玩家名、坐标
                                sender.sendMessage("§e§l[[RECALL]]: You cost "+ChatColor.GREEN.UNDERLINE.BOLD+cost+" to start [[RECALL]]!");
                                Bukkit.broadcastMessage("§b§l[[RECALL]] Request:" + " "+ ChatColor.AQUA.UNDERLINE + p.getName() + "§a§l is starting the [[RECALL]]!"+"§c§l§n Use /rcaccept " +ChatColor.RED.UNDERLINE.BOLD +p.getName() + " " + "§b§lto accept!");
                                sender.sendMessage("§e§lYour [[RECALL]] location is "+ChatColor.RED.BOLD.UNDERLINE+Math.rint(p.getLocation().getX())+" , "+ChatColor.RED.BOLD.UNDERLINE+Math.rint(p.getLocation().getY())+" , "+ChatColor.RED.BOLD.UNDERLINE+Math.rint(p.getLocation().getZ())+" , "+"§a§l Please wait for your friend~");

                                //异步线程开始计时
                                Bukkit.getScheduler().runTaskAsynchronously(this , new Runnable() {
                                    @Override
                                    public void run() {
                                        sender.sendMessage("§d§l[[RECALL]]: Starting the timer...");
                                        try {
                                            Thread.sleep(getConfig().getInt("RECALL.duration"));
                                        } catch (InterruptedException e) {
                                            //空
                                        }
                                        if(loc.get(p) !=null ){
                                            sender.sendMessage("§2§l[[RECALL]]: Timeout");
                                            loc.remove(p);
                                            Bukkit.broadcastMessage("§a§l[[RECALL]]: [[RECALL]] of "+ChatColor.RED.BOLD.UNDERLINE+p.getName()+"§2§l has been closed by System!");

                                        }

                                    }
                                });

                                return true;
                            }else{
                                sender.sendMessage("§9§l[[RECALL]]: You don't have enough money to use [[RECALL]]!");
                                return false;
                        }

//                        }
                    }else{
                        sender.sendMessage("§9§l[[RECALL]]: You can't use it too quickly!");
                        return false;
                    }

                }
            }else {
                sender.sendMessage("§4§l[[RECALL]]: ONLY Player can use this command!");
                return false;
            }
        }

        //配置命令/rcaccept
        if(command.getName().equalsIgnoreCase("rcaccept")){
            if (sender instanceof Player) {
                Player p = (Player) sender;
                if(args.length == 0){
                    sender.sendMessage("§e§l[[RECALL]]: Use /rcaccept [PlayerName] to accept!");
                }else{
                    if(args.length == 1){
                        //设置目标玩家target（args[1]即输入的玩家名）
                        Player target = Bukkit.getPlayer(args[0]);
                        if(target != null){
                            //玩家在线
                            if(loc.get(target) == null){
                                sender.sendMessage("§e§l[[RECALL]]: Player didn't use [[RECALL]]!");
                                return true;
                            }else{
                                Location back = p.getLocation();
                                p.sendMessage("§e§lTeleporting "+"§e§lTo "+ChatColor.AQUA.UNDERLINE + target.getName());
                                //建议添加传送计时
                                Bukkit.getScheduler().runTaskAsynchronously(this , new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            Thread.sleep(getConfig().getInt("ACCEPT.tptime"));
                                        } catch (InterruptedException e) {
                                            //空
                                        }
                                        p.teleport(loc.get(target));
                                    }
                                });
                                Location now = p.getLocation();
                                if(back != now){
                                    p.sendMessage("§e§lSucceed! "+"§e§lJoin your friend!");
                                    return true;
                                }else {
                                    p.sendMessage("§c§lFailed! "+"§c§lPlease try again later!");
                                    return false;
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
                return false;
            }
        }

        //配置命令/rccancel
        if(command.getName().equalsIgnoreCase("rccancel")){
            if (sender instanceof Player) {
                Player p = (Player) sender;
                if(loc.get(p) != null){
                    loc.remove(p);
                    Bukkit.broadcastMessage("§a§l[[RECALL]]: "+ ChatColor.BLUE.BOLD.UNDERLINE + p.getName() +"§a§l has closed the [[RECALL]]!");
                    sender.sendMessage("§c§lClose it Successfully!"+"§c§l You can try it later!");
                    done = Calendar.MINUTE;
                    return true;
                }else{
                    sender.sendMessage("§a§l[[RECALL]]: Your [[RECALL]] isn't started!");
                    return true;
                }
            }else {
                sender.sendMessage("§4§l[[RECALL]]: ONLY Player can use this command!");
                return false;
            }
        }

        //配置命令/rchelp
        if(command.getName().equalsIgnoreCase("rchelp")){
            if(sender instanceof Player){
                Player p = (Player) sender;
                sender.sendMessage("§9§l--------------RECALL HELP---------------");
                sender.sendMessage("§c./rc Spend "+getConfig().getInt("RECALL.cost")+" starting [[RECALL]].");
                sender.sendMessage("§c./rcaccept [Playername] Accept a player's [[RECALL]].");
                sender.sendMessage("§c./rccancel Cancel your [[RECALL]] request.");
                sender.sendMessage("§c./rchelp Read [[RECALL]] help");
                sender.sendMessage("§9§l-----------------------------------------");
                sender.sendMessage("§9§l------------DEVELOPMENT TEAM-------------");
                sender.sendMessage("§aElaBosak233 ······················· Developer");
                sender.sendMessage("§aAlan_Richard ··················· Code Checker");
                sender.sendMessage("§9§l-----------------------------------------");
                return true;
            }else{
                sender.sendMessage("§4§l[[RECALL]]: ONLY Player can use this command!");
                return false;
            }

        }

        return true;
    }

}
