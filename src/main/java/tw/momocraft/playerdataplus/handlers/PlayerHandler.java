package tw.momocraft.playerdataplus.handlers;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.UUID;

public class PlayerHandler {
    public static Player getPlayerString(String playerName) {
        Player args = null;
        try {
            args = Bukkit.getPlayer(UUID.fromString(playerName));
        } catch (Exception e) {
            ServerHandler.sendDebugTrace(e);
        }
        if (args == null) {
            return Bukkit.getPlayer(playerName);
        }
        return args;
    }

    public static String getPlayerUUID(Player player) {
        if (player != null) {
            return player.getUniqueId().toString();
        }
        return "";
    }

    public static String getOfflinePlayerID(OfflinePlayer player) {
        if (player != null) {
            return player.getUniqueId().toString();
        }
        return "";
    }

    public static OfflinePlayer getOfflinePlayer(String playerName) {
        Collection<?> playersOnlineNew;
        OfflinePlayer[] playersOnlineOld;
        try {
            if (Bukkit.class.getMethod("getOfflinePlayers", new Class<?>[0]).getReturnType() == Collection.class) {
                playersOnlineNew = ((Collection<?>) Bukkit.class.getMethod("getOfflinePlayers", new Class<?>[0]).invoke(null, new Object[0]));
                for (Object objPlayer : playersOnlineNew) {
                    Player player = ((Player) objPlayer);
                    if (player.getName().equalsIgnoreCase(playerName)) {
                        return player;
                    }
                }
            } else {
                playersOnlineOld = ((OfflinePlayer[]) Bukkit.class.getMethod("getOfflinePlayers", new Class<?>[0]).invoke(null, new Object[0]));
                for (OfflinePlayer player : playersOnlineOld) {
                    if (player.getName().equalsIgnoreCase(playerName)) {
                        return player;
                    }
                }
            }
        } catch (Exception e) {
            ServerHandler.sendDebugTrace(e);
        }
        return null;
    }
}
