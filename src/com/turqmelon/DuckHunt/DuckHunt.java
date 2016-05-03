package com.turqmelon.DuckHunt;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DuckHunt extends JavaPlugin implements Listener {

    private static DuckHunt instance;
    private static Map<UUID, Location> wallPoint1 = new HashMap<>();
    private static Map<UUID, Location> wallPoint2 = new HashMap<>();

    public static DuckHunt getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        getServer().getPluginManager().registerEvents(this, this);
    }

    public static File getMapFile(String name) throws IOException {
        name = name.toLowerCase().replace(" ", "_");
        File dir = getInstance().getDataFolder();
        if (!dir.exists()) {
            dir.mkdir();
        }
        File file = new File(dir, name + ".json");
        if (!file.exists()) {
            file.createNewFile();
        }
        return file;
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (DuckMap.getLoadedMap() == null) return;
        DuckMap map = DuckMap.getLoadedMap();
        event.setRespawnLocation(player.isOp() ?
                map.getHunterSpawn() : map.getPlayerSpawn());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        new BukkitRunnable() {

            @Override
            public void run() {
                player.sendMessage("§eDuck Hunt plugin by Turqmelon");
            }
        }.runTaskLater(this, 20L);
    }

    private static Map<UUID, Location> lastSafe = new HashMap<>();

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (DuckMap.getLoadedMap() == null) return;
        Player player = event.getPlayer();
        Location location = player.getLocation();
        boolean close = false;
        for (Location wall : DuckMap.getLoadedMap().getWalls()) {
            if (wall.getWorld().getName().equalsIgnoreCase(location.getWorld().getName()) &&
                    wall.getBlockX() == location.getBlockX() &&
                    wall.getBlockY() == location.getBlockY() &&
                    wall.getBlockZ() == location.getBlockZ()) {
                close = true;
                break;
            }
        }
        if (close) {
            if (lastSafe.containsKey(player.getUniqueId())) {
                player.teleport(lastSafe.get(player.getUniqueId()));
            } else {
                player.teleport(player.isOp() ? DuckMap.getLoadedMap().getHunterSpawn() : DuckMap.getLoadedMap().getPlayerSpawn());
            }
            player.sendMessage("§4A mysterious force blocks the way.");
            return;
        } else {
            lastSafe.put(player.getUniqueId(), location);
        }

        Location goal = DuckMap.getLoadedMap().getGoal();
        if (!player.isOp() && goal.distance(location) <= DuckMap.getLoadedMap().getGoalRange()) {
            player.teleport(DuckMap.getLoadedMap().getHunterSpawn());
            Bukkit.broadcastMessage("§6" + player.getName() + " reached the goal, and has begun to attack the hunters!");
            ItemStack item = new ItemStack(Material.DIAMOND_SWORD, 1);
            item.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 10);
            player.getInventory().addItem(item);
        }

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (command.getName().equalsIgnoreCase("loadmap")) {
            if (sender.isOp()) {
                if (args.length == 1) {
                    if (DuckMap.getLoadedMap() == null) {
                        try {
                            DuckMap.loadMap(getMapFile(args[0]));
                        } catch (IOException | ParseException e) {
                            sender.sendMessage("§cError: " + e.getMessage());
                        }
                    } else {
                        sender.sendMessage("Map currently loaded. Please /unloadmap first.");
                    }
                } else {
                    sender.sendMessage("/loadmap <Name>");
                }
            } else {
                sender.sendMessage("No permission.");
            }
        } else if (command.getName().equalsIgnoreCase("warpall")) {
            if (sender.isOp()) {
                DuckMap map = DuckMap.getLoadedMap();
                if (map != null) {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.teleport(player.isOp() ? map.getHunterSpawn() : map.getPlayerSpawn());
                    }
                    sender.sendMessage("§aTeleported");
                } else {
                    sender.sendMessage("No map loaded.");
                }
            } else {
                sender.sendMessage("No permission.");
            }
        } else if (command.getName().equalsIgnoreCase("setwall")) {
            if (sender.isOp()) {
                if ((sender instanceof Player)) {
                    Player player = (Player) sender;
                    if (wallPoint1.containsKey(player.getUniqueId()) && wallPoint2.containsKey(player.getUniqueId())) {

                        if (args.length == 1) {

                            if (DuckMap.getLoadedMap() != null) {

                                if (args[0].equalsIgnoreCase("clear")) {
                                    DuckMap.getLoadedMap().getWalls().clear();
                                } else {
                                    try {
                                        Material material = Material.valueOf(args[0].toUpperCase());

                                        Location loc1 = wallPoint1.get(player.getUniqueId());
                                        Location loc2 = wallPoint2.get(player.getUniqueId());

                                        int minx = loc1.getBlockX() <= loc2.getBlockX() ? loc1.getBlockX() : loc2.getBlockX();
                                        int miny = loc1.getBlockY() <= loc2.getBlockY() ? loc1.getBlockY() : loc2.getBlockY();
                                        int minz = loc1.getBlockZ() <= loc2.getBlockZ() ? loc1.getBlockZ() : loc2.getBlockZ();

                                        int maxx = loc1.getBlockX() >= loc2.getBlockX() ? loc1.getBlockX() : loc2.getBlockX();
                                        int maxy = loc1.getBlockY() >= loc2.getBlockY() ? loc1.getBlockY() : loc2.getBlockY();
                                        int maxz = loc1.getBlockZ() >= loc2.getBlockZ() ? loc1.getBlockZ() : loc2.getBlockZ();

                                        int count = 0;
                                        for (int x = minx; x <= maxx; x++) {
                                            for (int y = miny; y <= maxy; y++) {
                                                for (int z = minz; z <= maxz; z++) {
                                                    Block block = loc1.getWorld().getBlockAt(x, y, z);
                                                    if (block.getType() == material) {
                                                        count++;
                                                        DuckMap.getLoadedMap().getWalls().add(block.getLocation());
                                                        block.setType(Material.AIR);
                                                    }
                                                }
                                            }
                                        }

                                        if (count == 0) {
                                            sender.sendMessage("No walls added.");
                                            return true;
                                        } else {
                                            sender.sendMessage("§aAdded " + count + " walls.");
                                        }

                                    } catch (Exception ex) {
                                        sender.sendMessage("§cError: " + ex.getMessage());
                                        return true;
                                    }
                                }

                                try {
                                    DuckMap.getLoadedMap().save(getMapFile(DuckMap.getLoadedMap().getName()));
                                    sender.sendMessage("§aDone");
                                } catch (IOException e) {
                                    sender.sendMessage("§cError: " + e.getMessage());
                                }

                            } else {
                                sender.sendMessage("No loaded map.");
                            }


                        } else {
                            sender.sendMessage("/setwall <Material>");
                        }

                    } else {
                        sender.sendMessage("Both locations not set.");
                    }
                } else {
                    sender.sendMessage("You must be a player.");
                }
            } else {
                sender.sendMessage("No permission.");
            }
        } else if (command.getName().equalsIgnoreCase("setwallpoint2")) {
            if (sender.isOp()) {
                if ((sender instanceof Player)) {
                    Player player = (Player) sender;
                    wallPoint2.put(player.getUniqueId(), player.getLocation());
                    sender.sendMessage("§aPoint 2 set to current location.");
                } else {
                    sender.sendMessage("You must be a player.");
                }
            } else {
                sender.sendMessage("No permission.");
            }
        } else if (command.getName().equalsIgnoreCase("setwallpoint1")) {
            if (sender.isOp()) {
                if ((sender instanceof Player)) {
                    Player player = (Player) sender;
                    wallPoint1.put(player.getUniqueId(), player.getLocation());
                    sender.sendMessage("§aPoint 1 set to current location.");
                } else {
                    sender.sendMessage("You must be a player.");
                }
            } else {
                sender.sendMessage("No permission.");
            }
        } else if (command.getName().equalsIgnoreCase("unloadmap")) {
            if (sender.isOp()) {
                if (DuckMap.getLoadedMap() != null) {
                    DuckMap.getLoadedMap().unload();
                } else {
                    sender.sendMessage("No loaded map.");
                }
            } else {
                sender.sendMessage("No permission.");
            }
        } else if (command.getName().equalsIgnoreCase("sethunterspawn")) {
            if (sender.isOp()) {
                if ((sender instanceof Player)) {
                    Player player = (Player) sender;
                    if (DuckMap.getLoadedMap() != null) {
                        DuckMap map = DuckMap.getLoadedMap();
                        map.setHunterSpawn(player.getLocation());
                        try {
                            map.save(getMapFile(map.getName()));
                            sender.sendMessage("§aHunter spawn set.");
                        } catch (IOException e) {
                            sender.sendMessage("§cError: " + e.getMessage());
                        }
                    } else {
                        sender.sendMessage("No loaded map.");
                    }
                } else {
                    sender.sendMessage("You must be a player.");
                }
            } else {
                sender.sendMessage("No permission.");
            }
        } else if (command.getName().equalsIgnoreCase("setgoal")) {
            if (sender.isOp()) {
                if ((sender instanceof Player)) {
                    Player player = (Player) sender;
                    if (DuckMap.getLoadedMap() != null) {
                        DuckMap map = DuckMap.getLoadedMap();
                        if (args.length == 1) {
                            try {
                                double radius = Double.parseDouble(args[0]);
                                if (radius < 1) {
                                    throw new NumberFormatException();
                                }
                                map.setGoal(player.getLocation());
                                map.setGoalRange(radius);
                                map.save(getMapFile(map.getName()));
                                sender.sendMessage("§aGoal set. (Radius = " + radius + ")");
                            } catch (NumberFormatException ex) {
                                sender.sendMessage("Enter a valid radius. (> 0)");
                            } catch (IOException e) {
                                sender.sendMessage("§cError: " + e.getMessage());
                            }
                        } else {
                            sender.sendMessage("/setgoal <Radius>");
                        }
                    } else {
                        sender.sendMessage("No loaded map.");
                    }
                } else {
                    sender.sendMessage("You must be a player.");
                }
            } else {
                sender.sendMessage("No permission.");
            }
        } else if (command.getName().equalsIgnoreCase("setplayerspawn")) {
            if (sender.isOp()) {
                if ((sender instanceof Player)) {
                    Player player = (Player) sender;
                    if (DuckMap.getLoadedMap() != null) {
                        DuckMap map = DuckMap.getLoadedMap();
                        map.setPlayerSpawn(player.getLocation());
                        try {
                            map.save(getMapFile(map.getName()));
                            sender.sendMessage("§aPlayer spawn set.");
                        } catch (IOException e) {
                            sender.sendMessage("§cError: " + e.getMessage());
                        }
                    } else {
                        sender.sendMessage("No loaded map.");
                    }
                } else {
                    sender.sendMessage("You must be a player.");
                }
            } else {
                sender.sendMessage("No permission.");
            }
        } else if (command.getName().equalsIgnoreCase("newmap")) {
            if (sender.isOp()) {
                if (args.length == 1) {
                    if (DuckMap.getLoadedMap() == null) {

                        if ((sender instanceof Player)) {

                            Player player = (Player) sender;
                            Location location = player.getLocation();
                            DuckMap map = new DuckMap(args[0], location, location, new ArrayList<>(), location, 0);
                            DuckMap.setLoadedMap(map);
                            sender.sendMessage("§aMap created!");
                            try {
                                map.save(getMapFile(args[0]));
                                sender.sendMessage("§aNext: §f/setPlayerSpawn, /setHunterSpawn, /setWallPoint1, /setWallPoint2, /setWall <Material>, /setGoal <Range>");
                            } catch (IOException e) {
                                sender.sendMessage("§cError: " + e.getMessage());
                            }

                        } else {
                            sender.sendMessage("You must be a player.");
                        }

                    } else {
                        sender.sendMessage("Map currently loaded. Please /unloadmap first.");
                    }
                } else {
                    sender.sendMessage("/newmap <Name>");
                }
            } else {
                sender.sendMessage("No permission.");
            }
        }

        return true;
    }
}
