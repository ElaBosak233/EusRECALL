package cc.eumc.recall;

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

    public Map<Player,Location> loc = new HashMap<>();

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
        //配置命令/recall
        if (command.getName().equalsIgnoreCase("recall")) {
            if (sender instanceof Player) {
                //判断是不是玩家
                Player p = (Player) sender;
                if (args.length > 0) {
                    //判断命令长短
                    sender.sendMessage("§9§l[[RECALL]]: Too many arguments!");
                    return false;
                } else {
                    int now = Calendar.MINUTE; //获取当前时间
                    int need = Math.abs(now - done); //求上一次完成和现在时间的差值的绝对值
                    //判断时间差值是否≥1
                    if(need >= 1){
                        double money = econ.getBalance(p);
                        int cost = getConfig().getInt("RECALL.cost");
                        //判断金钱
                        if(money >= cost){
                            econ.withdrawPlayer(p,cost);
                            loc.put(p,p.getLocation()); //记录玩家名、坐标
                            Bukkit.broadcastMessage("§9§l[[RECALL]] Request:" + " "+ ChatColor.AQUA.UNDERLINE + p.getName() + "§a§l is starting the [[RECALL]]!"+"§c§l§n Use /rcaccept " +ChatColor.RED.UNDERLINE.BOLD +p.getName() + " " + "§9§lto accept!");
                            sender.sendMessage("§9§lYour [[RECALL]] location is "+ChatColor.RED.BOLD.UNDERLINE+loc+"§9§l Please wait for your friend~");
                            done = Calendar.MINUTE; //输入指令并运行的时间
                            int now1 = Calendar.MINUTE;
                            int need1 = Math.abs(now1 - done);
                            if(need1 >= 1){
                                loc.remove(p);
                            }
                            return true;
                        }else{
                            sender.sendMessage("§9§l[[RECALL]]: You don't have enough money to use [[RECALL]]!");
                            return false;
                        }
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
            Player p = (Player) sender;
            if (sender instanceof Player) {
                if(args.length == 0){
                    sender.sendMessage("§e§l[[RECALL]]: Use /rcaccept [PlayerName] to accept!");
                }else{
                    if(args.length == 1){
                        Player target = Bukkit.getPlayer(args[1]);
                        if(target != null){
                            //玩家在线
                            if(loc.get(target) == null){
                                sender.sendMessage("§e§l[[RECALL]]: Player didn't use [[RECALL]]!");
                                return false;
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
                                    return false;
                                }
                            }
                        }else{
                            //玩家不在线
                            sender.sendMessage("§e§l[[RECALL]]: Player is not online!");
                            return false;
                        }
                    }else{
                        sender.sendMessage("§9§l[[RECALL]]: Too many arguments!");
                    }
                    //设置目标玩家target
                }
            }else{
                sender.sendMessage("§4§l[[RECALL]]: ONLY Player can use this command!");
                return false;
            }
        }

        //配置命令/rccancel
        if(command.getName().equalsIgnoreCase("rccancel")){
            Player p = (Player) sender;
            if (sender instanceof Player) {
                if(loc.get(p) != null){
                    loc.remove(p);
                    Bukkit.broadcastMessage("§a§l[[RECALL]]: "+ ChatColor.BLUE.BOLD.UNDERLINE + p.getName() +"§a§l has closed the [[RECALL]]!");
                    p.sendMessage("§c§lClose it Successfully!"+"§c§l You can try it later!");
                }else{
                    sender.sendMessage("§a§l[[RECALL]]: Your [[RECALL]] is closed!");
                }
            }else {
                sender.sendMessage("§4§l[[RECALL]]: ONLY Player can use this command!");
                return false;
            }
        }

        return false;
    }

}
