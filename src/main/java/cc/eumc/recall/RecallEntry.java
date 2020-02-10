package cc.eumc.recall;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Calendar;

public class RecallEntry {
    Location location;
    long expire;
    int accepted;

    /**
     * Init
     * @param loc
     * @param time in seconds
     */
    public RecallEntry(Location loc, long time) {
        this.location = loc;
        this.expire = time + System.currentTimeMillis() / 1000;
        this.accepted = 0;
    }

    public void doTeleport(Player player) {
        player.teleport(location);
        accepted ++;
    }

    public int getAccepted() {
        return accepted;
    }
}
