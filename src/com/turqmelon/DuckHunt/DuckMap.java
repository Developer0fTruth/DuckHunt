package com.turqmelon.DuckHunt;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public class DuckMap {

    private static DuckMap loadedMap = null;

    public static DuckMap getLoadedMap() {
        return loadedMap;
    }

    public static void setLoadedMap(DuckMap loadedMap) {
        DuckMap.loadedMap = loadedMap;
    }

    public static void loadMap(File file) throws IOException, ParseException {
        if (getLoadedMap() != null) {
            return;
        }
        JSONParser parser = new JSONParser();
        JSONObject object = (JSONObject) parser.parse(new FileReader(file));
        setLoadedMap(new DuckMap(object));
        Bukkit.broadcastMessage("§7Loading " + getLoadedMap().getName() + " ...");
    }

    private String name;
    private Location playerSpawn;
    private Location hunterSpawn;
    private List<Location> walls;
    private Location goal;
    private double goalRange;

    public DuckMap(String name, Location playerSpawn, Location hunterSpawn, List<Location> walls, Location goal, double goalRange) {
        this.name = name;
        this.playerSpawn = playerSpawn;
        this.hunterSpawn = hunterSpawn;
        this.walls = walls;
        this.goal = goal;
        this.goalRange = goalRange;
    }

    public void unload() {
        if (getLoadedMap() == null || !getLoadedMap().getName().equals(getName())) {
            return;
        }
        Bukkit.broadcastMessage("§7Unloading " + getName() + " ...");
        setLoadedMap(null);
    }

    public void save(File file) throws IOException {

        FileWriter writer = new FileWriter(file);
        writer.write(toJSON().toJSONString());
        writer.flush();
        writer.close();

    }

    public DuckMap(JSONObject map) {
        this.name = (String) map.get("name");
        JSONObject spawns = (JSONObject) map.get("spawns");
        this.playerSpawn = new JSONLocation((JSONObject) spawns.get("player")).toLocation();
        this.hunterSpawn = new JSONLocation((JSONObject) spawns.get("hunter")).toLocation();

        JSONArray walls = (JSONArray) map.get("walls");
        this.walls = new ArrayList<>();
        for (Object w : walls) {
            getWalls().add(new JSONLocation((JSONObject) w).toLocation());
        }
        JSONObject goal = (JSONObject) map.get("goal");
        this.goal = new JSONLocation((JSONObject) goal.get("location")).toLocation();
        this.goalRange = (double) goal.get("range");
    }

    @Override
    public String toString() {
        return toJSON().toJSONString();
    }

    public JSONObject toJSON() {
        JSONObject map = new JSONObject();
        map.put("name", getName());

        JSONObject spawns = new JSONObject();
        spawns.put("player", new JSONLocation(getPlayerSpawn()).toJSON());
        spawns.put("hunter", new JSONLocation(getHunterSpawn()).toJSON());
        map.put("spawns", spawns);

        JSONArray walls = new JSONArray();
        for (Location location : getWalls()) {
            walls.add(new JSONLocation(location).toJSON());
        }

        map.put("walls", walls);

        JSONObject goal = new JSONObject();
        goal.put("location", new JSONLocation(getGoal()).toJSON());
        goal.put("range", getGoalRange());

        map.put("goal", goal);
        return map;
    }

    public String getName() {
        return name;
    }

    public Location getPlayerSpawn() {
        return playerSpawn;
    }

    public Location getHunterSpawn() {
        return hunterSpawn;
    }

    public List<Location> getWalls() {
        return walls;
    }

    public Location getGoal() {
        return goal;
    }

    public double getGoalRange() {
        return goalRange;
    }

    public void setPlayerSpawn(Location playerSpawn) {
        this.playerSpawn = playerSpawn;
    }

    public void setHunterSpawn(Location hunterSpawn) {
        this.hunterSpawn = hunterSpawn;
    }

    public void setWalls(List<Location> walls) {
        this.walls = walls;
    }

    public void setGoal(Location goal) {
        this.goal = goal;
    }

    public void setGoalRange(double goalRange) {
        this.goalRange = goalRange;
    }

    @SuppressWarnings("unchecked")
    public static class JSONLocation {

        private Location location = null;
        private JSONObject object = null;

        public JSONLocation(Location location) {
            this.location = location;
        }

        public JSONLocation(JSONObject object) {
            this.object = object;
        }

        public Location toLocation() {
            if (getObject() == null) return null;
            World world = Bukkit.getWorld((String) getObject().get("world"));
            if (world == null) return null;
            double x = (double) getObject().get("x");
            double y = (double) getObject().get("y");
            double z = (double) getObject().get("z");
            float yaw = (float) ((double) getObject().get("yaw"));
            float pitch = (float) ((double) getObject().get("pitch"));
            return new Location(world, x, y, z, yaw, pitch);
        }

        public JSONObject toJSON() {
            if (getLocation() == null) return null;
            JSONObject l = new JSONObject();
            l.put("world", getLocation().getWorld().getName());
            l.put("x", getLocation().getX());
            l.put("y", getLocation().getY());
            l.put("z", getLocation().getZ());
            l.put("yaw", getLocation().getYaw());
            l.put("pitch", getLocation().getPitch());
            return l;
        }

        public Location getLocation() {
            return location;
        }

        public JSONObject getObject() {
            return object;
        }
    }

}
