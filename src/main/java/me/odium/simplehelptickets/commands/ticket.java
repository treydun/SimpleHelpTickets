package me.odium.simplehelptickets.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;

import me.odium.simplehelptickets.SimpleHelpTickets;
import me.odium.simplehelptickets.DBConnection;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ticket implements CommandExecutor {

    public SimpleHelpTickets plugin;

    public ticket(SimpleHelpTickets plugin) {
        this.plugin = plugin;
    }

    String date;
    String uuid;
    String world;
    double locX;
    double locY;
    double locZ;
    double locP;
    double locF;
    String reply;
    String status;
    String admin;
    String adminreply;
    String expire;
    String userreply;

    DBConnection service = DBConnection.getInstance();
    ResultSet rs;
    Connection con;
    java.sql.Statement stmt;

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player = null;
        if (sender instanceof Player) {
            player = (Player) sender;
        }

        if (args.length == 0) {
            sender.sendMessage(plugin.GOLD + "[ SimpleHelpTickets ]");
            sender.sendMessage(plugin.getMessage("HelpMe_Line1"));
            sender.sendMessage(plugin.getMessage("HelpMe_Line2"));
        } else if (args.length > 0) {

            // Build the command string
            StringBuilder sb = new StringBuilder();
            for (String arg : args)
                sb.append(arg + " ");
            String[] temp = sb.toString().split(" ");
            String[] temp2 = Arrays.copyOfRange(temp, 0, temp.length);
            sb.delete(0, sb.length());
            for (String details : temp2) {
                sb.append(details);
                sb.append(" ");
            }
            String details = sb.toString();

            if (player == null) {
                // SET VARIABLES FOR CONSOLE
                date = plugin.getCurrentDTG("date");
                uuid = "CONSOLE";
                world = "NONE";
                locX = 00;
                locY = 00;
                locZ = 00;
                locP = 00;
                locF = 00;
                adminreply = "NONE";
                userreply = "NONE";
                status = "OPEN";
                admin = "NONE";
                expire = "";
            } else {
                // SET VARIABLES FOR PLAYER
                date = plugin.getCurrentDTG("date");
                uuid = player.getUniqueId().toString();
                world = player.getWorld().getName();
                locX = player.getLocation().getX();
                locY = player.getLocation().getY();
                locZ = player.getLocation().getZ();
                locP = player.getLocation().getPitch();
                locF = player.getLocation().getYaw();
                adminreply = "NONE";
                userreply = "NONE";
                status = "OPEN";
                admin = "NONE";
                expire = "";
            }

            // REFERENCE CONNECTION AND ADD DATA
            if (plugin.getConfig().getBoolean("MySQL.USE_MYSQL")) {

                // CHECK MAX TICKETS
                int maxTickets = plugin.getConfig().getInt("MaxTickets"); // Get Ticket Limit from config

                try {
                    con = plugin.mysql.getConnection();
                    stmt = con.createStatement();
                    rs = stmt.executeQuery("SELECT COUNT(uuid) AS ticketTotal FROM SHT_Tickets WHERE uuid='" + uuid + "' AND status='OPEN'");
                    rs.next(); //sets pointer to first record in result set (NEED FOR MySQL)

                    int ticketTotal = rs.getInt("ticketTotal"); // GET TOTAL NUMBER OF PLAYERS TICKETS
                    if (ticketTotal >= maxTickets) { // IF MAX TICKETS REACHED
                        sender.sendMessage(plugin.getMessage("TicketMax").replace("&arg", Integer.toString(maxTickets)));
                        return true;
                    }
                } catch (SQLException e) {
                    sender.sendMessage(plugin.getMessage("Error").replace("&arg", e.toString()));
                }

                // END CHECK MAX TICKETS

                try {
                    con = plugin.mysql.getConnection();
                    stmt = con.createStatement();
                    PreparedStatement statement = con.prepareStatement("insert into SHT_Tickets(description, date, uuid, world, x, y, z, p, f, adminreply, userreply, status, admin, expiration) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?);");
                    // INSERT INTO lyrics1(name, artist) values(?, ?) [Example]

                    statement.setString(1, details);
                    statement.setString(2, date);
                    statement.setString(3, uuid);
                    statement.setString(4, world);
                    statement.setDouble(5, locX);
                    statement.setDouble(6, locY);
                    statement.setDouble(7, locZ);
                    statement.setDouble(8, locP);
                    statement.setDouble(9, locF);
                    statement.setString(10, adminreply);
                    statement.setString(11, userreply);
                    statement.setString(12, status);
                    statement.setString(13, admin);
                    statement.setString(14, expire);

                    statement.executeUpdate();
                    statement.close();

                    // Message player and finish
                    sender.sendMessage(plugin.getMessage("TicketOpen"));
                    // Notify admin of new ticket
                    Collection<? extends Player> players = Bukkit.getOnlinePlayers();
                    for (Player onlinePlayer : players) { // for every player online
                        if (onlinePlayer.hasPermission("sht.admin") && !onlinePlayer.getUniqueId().toString().equalsIgnoreCase(uuid)) { // if admin perm & not ticket owner
                            onlinePlayer.sendMessage(plugin.getMessage("TicketOpenADMIN").replace("%player", sender.getName()));
                        }
                    }


                } catch (SQLException e) {
                    sender.sendMessage(plugin.getMessage("Error").replace("&arg", e.toString()));
                    sender.sendMessage(e.getSQLState());
                    sender.sendMessage(e.getMessage());
                }

            } else {

                // CHECK MAX TICKETS
                int maxTickets = plugin.getConfig().getInt("MaxTickets"); // Get Ticket Limit from config

                try {
                    con = service.getConnection();
                    stmt = con.createStatement();
                    //ResultSet fullCount = stmt.executeQuery("SELECT *");

                    rs = stmt.executeQuery("SELECT COUNT(uuid) AS ticketTotal FROM SHT_Tickets WHERE uuid='" + uuid + "' AND status='OPEN'");

                    int ticketTotal = rs.getInt("ticketTotal"); // GET TOTAL NUMBER OF PLAYERS TICKETS
                    if (ticketTotal >= maxTickets) { // IF MAX TICKETS REACHED
                        sender.sendMessage(plugin.getMessage("TicketMax").replace("&arg", Integer.toString(maxTickets)));
                        return true;
                    }
                } catch (SQLException e) {
                    sender.sendMessage(plugin.getMessage("Error").replace("&arg", e.toString()));
                }
                // END CHECK MAX TICKETS

                try {
                    con = service.getConnection();
                    stmt = con.createStatement();
                    PreparedStatement statement = con.prepareStatement("insert into SHT_Tickets(description, date, uuid, world, x, y, z, p, f, adminreply, userreply, status, admin, expiration) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?);");

                    // description, date, owner, world, x, y, z, p, f, reply, status, admin
                    statement.setString(1, details);
                    plugin.log.info("details:" + details);
                    statement.setString(2, date);
                    plugin.log.info("date:" + date);
                    statement.setString(3, uuid);
                    plugin.log.info("uuid:" + uuid);
                    statement.setString(4, world);
                    plugin.log.info("world:" + world);
                    statement.setDouble(5, locX);
                    plugin.log.info("locX:" + locX);
                    statement.setDouble(6, locY);
                    plugin.log.info("locY:" + locY);
                    statement.setDouble(7, locZ);
                    plugin.log.info("locZ:" + locZ);
                    statement.setDouble(8, locP);
                    plugin.log.info("locP:" + locP);
                    statement.setDouble(9, locF);
                    plugin.log.info("locF:" + locF);
                    statement.setString(10, adminreply);
                    plugin.log.info("adminreply:" + adminreply);
                    statement.setString(11, userreply);
                    plugin.log.info("userreply:" + userreply);
                    statement.setString(12, status);
                    plugin.log.info("status:" + status);
                    statement.setString(13, admin);
                    plugin.log.info("admin:" + admin);
                    statement.setString(14, expire);
                    plugin.log.info("expire:" + expire);


                    plugin.log.info("" + statement);
                    statement.executeUpdate();
                    statement.close();

                    // Message player and finish
                    sender.sendMessage(plugin.getMessage("TicketOpen"));
                    // Notify admin of new ticket
                    Collection<? extends Player> players = Bukkit.getOnlinePlayers();
                    for (Player onlinePlayer : players) { // for every player online
                        if (onlinePlayer.hasPermission("sht.admin")) {     // if admin permission
                            onlinePlayer.sendMessage(plugin.getMessage("TicketOpenADMIN").replace("%player", sender.getName()));
                        }
                    }
                } catch (Exception e) {
                    sender.sendMessage(plugin.getMessage("Error").replace("&arg", e.toString()));
                    sender.sendMessage(e.getMessage());
                    //sender.sendMessage(e.getCause().toString());
                }
            }
        }
        return true;
    }
}
