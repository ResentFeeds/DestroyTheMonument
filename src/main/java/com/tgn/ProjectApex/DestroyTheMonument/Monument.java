/*******************************************************************************
 * Copyright 2014 stuntguy3000 (Luke Anderson) and coasterman10.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
 ******************************************************************************/
package com.tgn.ProjectApex.DestroyTheMonument;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

import com.bergerkiller.bukkit.common.events.EntityMoveEvent;
import com.tgn.ProjectApex.DestroyTheMonument.api.GameStartEvent;
import com.tgn.ProjectApex.DestroyTheMonument.api.PhaseChangeEvent;
import com.tgn.ProjectApex.DestroyTheMonument.bar.BarUtil;
import com.tgn.ProjectApex.DestroyTheMonument.chat.ChatListener;
import com.tgn.ProjectApex.DestroyTheMonument.chat.ChatUtil;
import com.tgn.ProjectApex.DestroyTheMonument.commands.AnnihilationCommand;
import com.tgn.ProjectApex.DestroyTheMonument.commands.DistanceCommand;
import com.tgn.ProjectApex.DestroyTheMonument.commands.MapCommand;
import com.tgn.ProjectApex.DestroyTheMonument.commands.StatsCommand;
import com.tgn.ProjectApex.DestroyTheMonument.commands.TeamCommand;
import com.tgn.ProjectApex.DestroyTheMonument.commands.TeamShortcutCommand;
import com.tgn.ProjectApex.DestroyTheMonument.commands.VoteCommand;
import com.tgn.ProjectApex.DestroyTheMonument.listeners.CraftingListener;
import com.tgn.ProjectApex.DestroyTheMonument.listeners.PlayerListener;
import com.tgn.ProjectApex.DestroyTheMonument.listeners.ResourceListener;
import com.tgn.ProjectApex.DestroyTheMonument.listeners.WandListener;
import com.tgn.ProjectApex.DestroyTheMonument.listeners.WorldListener;
import com.tgn.ProjectApex.DestroyTheMonument.manager.ConfigManager;
import com.tgn.ProjectApex.DestroyTheMonument.manager.DatabaseManager;
import com.tgn.ProjectApex.DestroyTheMonument.manager.MapManager;
import com.tgn.ProjectApex.DestroyTheMonument.manager.PhaseManager;
import com.tgn.ProjectApex.DestroyTheMonument.manager.RestartHandler;
import com.tgn.ProjectApex.DestroyTheMonument.manager.ScoreboardManager;
import com.tgn.ProjectApex.DestroyTheMonument.manager.SignManager;
import com.tgn.ProjectApex.DestroyTheMonument.manager.VotingManager;
import com.tgn.ProjectApex.DestroyTheMonument.maps.MapLoader;
import com.tgn.ProjectApex.DestroyTheMonument.object.GameTeam;
import com.tgn.ProjectApex.DestroyTheMonument.object.PlayerMeta;
import com.tgn.ProjectApex.DestroyTheMonument.stats.StatType;
import com.tgn.ProjectApex.DestroyTheMonument.stats.StatsManager;

import org.apache.commons.lang.WordUtils;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Team;

public final class Monument extends JavaPlugin implements Listener {
    private ConfigManager configManager;
    private VotingManager voting;
    private MapManager maps;
    private PhaseManager timer;
    private ResourceListener resources;
    private StatsManager stats;
    private SignManager sign;
    private ScoreboardManager sb;
    private DatabaseManager db;
    private Translation translation;

    public static HashMap<String, String> messages = new HashMap<String, String>();
    public boolean useMysql = false;
    public boolean updateAvailable = false;
    public boolean motd = true;
    public String newVersion;

    public int build = 1;
    public int lastJoinPhase = 2;
    public int respawn = 10;

    public boolean runCommand = false;
    public List<String> commands = new ArrayList<String>();

    public String mysqlName = "annihilation";

    @Override
    public void onEnable() {

        configManager = new ConfigManager(this);
        configManager.loadConfigFiles("config.yml", "maps.yml", "shops.yml",
                "stats.yml", "messages.yml", "kit.yml");

        MapLoader mapLoader = new MapLoader(getLogger(), getDataFolder());

        runCommand = getConfig().contains("commandsToRunAtEndGame");

        if (runCommand) {
            commands = getConfig().getStringList("commandsToRunAtEndGame");
        } else commands = null;

        maps = new MapManager(this, mapLoader,
                configManager.getConfig("maps.yml"));

        File messageFile = new File("plugins/" + getDescription().getName() + "/messages.yml");
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(messageFile);

        for (String id : yml.getKeys(false))
        messages.put(id, yml.getString(id));



        stats = new StatsManager(this, configManager);
        resources = new ResourceListener(this);
        sign = new SignManager(this);
        Configuration config = configManager.getConfig("config.yml");
        timer = new PhaseManager(this, config.getInt("start-delay"),
                config.getInt("phase-period"));
        voting = new VotingManager(this);
        sb = new ScoreboardManager();

        PluginManager pm = getServer().getPluginManager();

        sign.loadSigns();

        sb.resetScoreboard(ChatColor.DARK_AQUA + "Voting" + ChatColor.WHITE
                + " | " + ChatColor.GOLD + "/vote <name>");

        build = this.getConfig().getInt("build", 5);
        lastJoinPhase = this.getConfig().getInt("lastJoinPhase", 2);
        respawn = this.getConfig().getInt("bossRespawnDelay", 10);

        pm.registerEvents(resources, this);
        pm.registerEvents(this, this);
        pm.registerEvents(new ChatListener(this), this);
        pm.registerEvents(new PlayerListener(this), this);
        pm.registerEvents(new WorldListener(), this);
        pm.registerEvents(new WandListener(this), this);
        pm.registerEvents(new CraftingListener(), this);

        getCommand("nexusgrinder").setExecutor(new AnnihilationCommand(this));
        getCommand("stats").setExecutor(new StatsCommand(stats));
        getCommand("team").setExecutor(new TeamCommand(this));
        getCommand("vote").setExecutor(new VoteCommand(voting));
        getCommand("red").setExecutor(new TeamShortcutCommand());
        getCommand("green").setExecutor(new TeamShortcutCommand());
        getCommand("yellow").setExecutor(new TeamShortcutCommand());
        getCommand("blue").setExecutor(new TeamShortcutCommand());
        getCommand("distance").setExecutor(new DistanceCommand(this));
        getCommand("map").setExecutor(new MapCommand(this, mapLoader));

        BarUtil.init(this);

        if (config.getString("stats").equalsIgnoreCase("sql"))
            useMysql = true;

        motd = config.getBoolean("enableMotd", true);

        if (useMysql) {
            String host = config.getString("MySQL.host");
            Integer port = config.getInt("MySQL.port");
            String name = config.getString("MySQL.name");
            String user = config.getString("MySQL.user");
            String pass = config.getString("MySQL.pass");
            db = new DatabaseManager(host, port, name, user, pass, this);

            db.query("CREATE TABLE IF NOT EXISTS `" + mysqlName + "` ( `username` varchar(16) NOT NULL, "
                    + "`kills` int(16) NOT NULL, `deaths` int(16) NOT NULL, `wins` int(16) NOT NULL, "
                    + "`losses` int(16) NOT NULL, `nexus_damage` int(16) NOT NULL, "
                    + "UNIQUE KEY `username` (`username`) ) ENGINE=InnoDB DEFAULT CHARSET=latin1;");
        } else
            db = new DatabaseManager(this);


        reset();

        for (Entity e : Bukkit.getWorld("lobby").getEntities()) {
            e.remove();
        }
        getLogger().info("Remove entities for NPC, success.");

        ChatUtil.setRoman(getConfig().getBoolean("roman", false));

        //sheep npc
        //BLUE
        double xblue = 11;
        double yblue = 5;
        double zblue = 0;
        Location locblue = new Location(Bukkit.getWorld("lobby"), xblue, yblue, zblue);
        //RED
        double xred = -11;
        double yred = 5;
        double zred = 0;
        Location locred = new Location(Bukkit.getWorld("lobby"), xred, yred, zred);
        //ORANGE
        double xorange = 0;
        double yorange = 5;
        double zorange = -11;
        Location locorange = new Location(Bukkit.getWorld("lobby"), xorange, yorange, zorange);
        //GREEN
        double xgreen = 0;
        double ygreen = 5;
        double zgreen = 10;
        Location locgreen = new Location(Bukkit.getWorld("lobby"), xgreen, ygreen, zgreen);



        // BLUE
        Sheep sblue = (Sheep)locblue.getWorld().spawnCreature(locblue, EntityType.SHEEP);
        sblue.setColor(DyeColor.BLUE);
        sblue.getAgeLock();
        sblue.teleport(locblue);
        sblue.isAdult();
        sblue.getLocation().setX(xblue);
        sblue.getLocation().setY(yblue);
        sblue.getLocation().setZ(zblue);
        sblue.setCustomName(ChatColor.GREEN + "Join" + ChatColor.DARK_GREEN + ">" + ChatColor.BLUE + " BLU TEAM " + ChatColor.DARK_GREEN + "<" + ChatColor.GREEN + "Join " + "0 " + "Players" );
        getLogger().info("Blue sheep, spawned.");

        //RED
        Sheep sred = (Sheep)locred.getWorld().spawnCreature(locred, EntityType.SHEEP);
        sred.setColor(DyeColor.RED);
        sred.getAgeLock();
        sred.teleport(locred);
        sred.isAdult();
        sred.getLocation().setX(xred);
        sred.getLocation().setY(yred);
        sred.getLocation().setZ(zred);
        sred.setCustomName(ChatColor.GREEN + "Join" + ChatColor.DARK_GREEN + ">" + ChatColor.RED + " RED TEAM " + ChatColor.DARK_GREEN + "<" + ChatColor.GREEN + "Join " + "0 " + "Players" );
        getLogger().info("Red sheep, spawned.");

        //ORANGE
        Sheep sorange = (Sheep)locorange.getWorld().spawnCreature(locorange, EntityType.SHEEP);
        sorange.setColor(DyeColor.ORANGE);
        sorange.getAgeLock();
        sorange.teleport(locorange);
        sorange.isAdult();
        sorange.getLocation().setX(xorange);
        sorange.getLocation().setY(yorange);
        sorange.getLocation().setZ(zorange);
        sorange.setCustomName(ChatColor.GREEN + "Join" + ChatColor.DARK_GREEN + ">" + ChatColor.YELLOW + " YELLOW TEAM" + ChatColor.DARK_GREEN + "<" + ChatColor.GREEN + "Join " + "0 " + "Players" );
        getLogger().info("Yellow sheep, spawned.");

        //GREEN
        Sheep sgreen = (Sheep)locgreen.getWorld().spawnCreature(locgreen, EntityType.SHEEP);
        sgreen.setColor(DyeColor.GREEN);
        sgreen.getAgeLock();
        sgreen.teleport(locgreen);
        sgreen.isAdult();
        sgreen.getLocation().setX(xgreen);
        sgreen.getLocation().setY(ygreen);
        sgreen.getLocation().setZ(zgreen);
        sgreen.setCustomName(ChatColor.GREEN + "Join" + ChatColor.DARK_GREEN + ">" + ChatColor.GREEN + " GREEN TEAM " + ChatColor.DARK_GREEN + "<" + ChatColor.GREEN + "Join " + "0 " + "Players" );
        getLogger().info("Green sheep, spawned.");
    }


    @EventHandler
    public void sheepMoveEvent(EntityMoveEvent event) {


        if (event.getEntity() instanceof Sheep) {

            //BLUE
            double xblue = 11;
            double yblue = 5;
            double zblue = 0;
            Location locblue = new Location(Bukkit.getWorld("lobby"), xblue, yblue, zblue);
            //RED
            double xred = -11;
            double yred = 5;
            double zred = 0;
            Location locred = new Location(Bukkit.getWorld("lobby"), xred, yred, zred);


            Sheep sheep = (Sheep) event.getEntity();


            if (sheep.getColor() == DyeColor.BLUE) {
                sheep.teleport(locblue);
                for (GameTeam t : GameTeam.teams()) {
                    int size = 0;

                    for (Player p : Bukkit.getOnlinePlayers()) {
                        PlayerMeta metas = PlayerMeta.getMeta(p);
                        if (metas.getTeam() == GameTeam.BLUE)
                            size++;
                    }

                    if (size != 1) {
                        sheep.setCustomName(ChatColor.GREEN + "Join" + ChatColor.DARK_GREEN + ">" + ChatColor.BLUE + " BLU TEAM " + ChatColor.DARK_GREEN + "<" + ChatColor.GREEN + "Join " + size + " Players" );
                    } else {
                        sheep.setCustomName(ChatColor.GREEN + "Join" + ChatColor.DARK_GREEN + ">" + ChatColor.BLUE + " BLU TEAM " + ChatColor.DARK_GREEN + "<" + ChatColor.GREEN + "Join " + size + " Players");
                    }
                }


            } else if (sheep.getColor() == DyeColor.RED) {
                sheep.teleport(locred);
                for (GameTeam t : GameTeam.teams()) {
                    int size = 0;

                    for (Player p : Bukkit.getOnlinePlayers()) {
                        PlayerMeta metas = PlayerMeta.getMeta(p);
                        if (metas.getTeam() == GameTeam.RED)
                            size++;
                    }

                    if (size != 1) {
                        sheep.setCustomName(ChatColor.GREEN + "Join" + ChatColor.DARK_GREEN + ">" + ChatColor.RED + " RED TEAM " + ChatColor.DARK_GREEN + "<" + ChatColor.GREEN + "Join " + size + " Players" );
                    } else {
                        sheep.setCustomName(ChatColor.GREEN + "Join" + ChatColor.DARK_GREEN + ">" + ChatColor.RED + " RED TEAM " + ChatColor.DARK_GREEN + "<" + ChatColor.GREEN + "Join " + size + " Players");
                    }
                }



                }
            }


        }
    @EventHandler
    public void sheepRightClick(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Sheep && event.getPlayer() instanceof Player) {
            Sheep s = (Sheep) event.getRightClicked();
            Player player = event.getPlayer();
            PlayerMeta meta = PlayerMeta.getMeta(player);
            GameTeam target;
            if (s.getColor() == DyeColor.BLUE) {
                player.performCommand("team blue");
                for (GameTeam t : GameTeam.teams()) {
                    int size = 0;

                    for (Player p : Bukkit.getOnlinePlayers()) {
                        PlayerMeta metas = PlayerMeta.getMeta(p);
                        if (meta.getTeam() == GameTeam.BLUE)
                            size++;
                    }

                    if (size != 1) {
                        s.setCustomName(ChatColor.GREEN + "Join" + ChatColor.DARK_GREEN + ">" + ChatColor.BLUE + " BLU TEAM " + ChatColor.DARK_GREEN + "<" + ChatColor.GREEN + "Join " + size + " Players" );
                    } else {
                        s.setCustomName(ChatColor.GREEN + "Join" + ChatColor.DARK_GREEN + ">" + ChatColor.BLUE + " BLU TEAM " + ChatColor.DARK_GREEN + "<" + ChatColor.GREEN + "Join " + size + " Players");
                    }

                }

            } else if (s.getColor() == DyeColor.RED) {
                player.performCommand("team red");
                for (GameTeam t : GameTeam.teams()) {
                    int size = 0;

                    for (Player p : Bukkit.getOnlinePlayers()) {
                        PlayerMeta metas = PlayerMeta.getMeta(p);
                        if (meta.getTeam() == GameTeam.RED)
                            size++;
                    }

                    if (size != 1) {
                        s.setCustomName(ChatColor.GREEN + "Join" + ChatColor.DARK_GREEN + ">" + ChatColor.RED + " RED TEAM " + ChatColor.DARK_GREEN + "<" + ChatColor.GREEN + "Join " + size + " Players" );
                    } else {
                        s.setCustomName(ChatColor.GREEN + "Join" + ChatColor.DARK_GREEN + ">" + ChatColor.RED + " RED TEAM " + ChatColor.DARK_GREEN + "<" + ChatColor.GREEN + "Join " + size + " Players");
                    }

                }


            }
        }
    }

    //WATER DAMAGE



    public boolean startTimer() {
        if (timer.isRunning())
            return false;

        timer.start();

        return true;
    }

    public void loadMap(final String map) {
        FileConfiguration config = configManager.getConfig("maps.yml");
        ConfigurationSection section = config.getConfigurationSection(map);

        World w = getServer().getWorld(map);

        for (GameTeam team : GameTeam.teams()) {
            String name = team.name().toLowerCase();
            if (section.contains("spawns." + name)) {
                for (String s : section.getStringList("spawns." + name))
                    team.addSpawn(Util.parseLocation(getServer().getWorld(map),
                            s));
            }
            if (section.contains("monument." + name)) {
                Location loc = Util.parseLocation(w,
                        section.getString("monument." + name));
                team.loadNexus(loc, 75);
            }
        }
    }

    public void startGame() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            for (Player pp : Bukkit.getOnlinePlayers()) {
                p.showPlayer(pp);
                pp.showPlayer(p);
            }
        }

        Bukkit.getPluginManager().callEvent(
                new GameStartEvent(maps.getCurrentMap()));
        sb.scores.clear();

        for (OfflinePlayer score : sb.sb.getPlayers())
            sb.sb.resetScores(score);

        sb.obj.setDisplayName(ChatColor.DARK_AQUA + "Map: "
                + WordUtils.capitalize(voting.getWinner()));

        for (GameTeam t : GameTeam.teams()) {
            sb.scores.put(t.name(), sb.obj.getScore(Bukkit
                    .getOfflinePlayer(WordUtils.capitalize(t.name()
                            .toLowerCase() + " Nexus"))));
            sb.scores.get(t.name()).setScore(t.getNexus().getHealth());

            Team sbt = sb.sb.registerNewTeam(t.name() + "SB");
            sbt.addPlayer(Bukkit.getOfflinePlayer(WordUtils
                    .capitalize(WordUtils.capitalize(t.name().toLowerCase()
                            + " Nexus"))));
            sbt.setPrefix(t.color().toString());
        }

        sb.obj.setDisplayName(ChatColor.DARK_AQUA + "Map: "
                + WordUtils.capitalize(voting.getWinner()));

        for (Player p : getServer().getOnlinePlayers())
            if (PlayerMeta.getMeta(p).getTeam() != GameTeam.NONE)
                Util.sendPlayerToGame(p, this);

        sb.update();

        getServer().getScheduler().runTaskTimer(this, new Runnable() {
            public void run() {
                for (GameTeam t : GameTeam.values()) {
                    if (t != GameTeam.NONE && t.getNexus().isAlive()) {
                        Location nexus = t.getNexus().getLocation().clone();
                        nexus.add(0.5, 0, 0.5);
                        Util.ParticleEffects.sendToLocation(Util.ParticleEffects.ENDER, nexus, 1F, 1F, 1F, 0, 20);
                        Util.ParticleEffects.sendToLocation(Util.ParticleEffects.ENCHANTMENT_TABLE, nexus, 1F, 1F, 1F, 0, 20);
                    }
                }
            }
        }, 100L, 5L);
    }

    @EventHandler
    public void onPlayerSwim(PlayerMoveEvent e) {
        if (e.getTo().getBlock().getType().equals(Material.STATIONARY_WATER)) {
            e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 1000, 10));
        }
    }

    public void advancePhase() {
        ChatUtil.phaseMessage(timer.getPhase());

        if (timer.getPhase() == 3)
            resources.spawnDiamonds();

        Bukkit.getPluginManager().callEvent(
                new PhaseChangeEvent(timer.getPhase()));

        getSignHandler().updateSigns(GameTeam.RED);
        getSignHandler().updateSigns(GameTeam.BLUE);
    }

    public void onSecond() {
        long time = timer.getTime();

        if (time == -5L) {

            String winner = voting.getWinner();
            voting.end();
            getServer().broadcastMessage(ChatColor.GOLD + "Voting is now closed!");
            maps.selectMap(winner);
            getServer().broadcastMessage(
                    ChatColor.GREEN + WordUtils.capitalize(winner)
                            + " was chosen!");
            loadMap(winner);

            for (Player p : Bukkit.getOnlinePlayers()) {
                p.playSound(p.getLocation(), Sound.ENDERDRAGON_WINGS, 10, 1);
                p.playSound(p.getLocation(), Sound.ENDERDRAGON_GROWL, 10, 1);
            }
        }

        if (time <= -5L) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.playSound(p.getLocation(), Sound.ENDERDRAGON_WINGS, 10, 1);
                p.playSound(p.getLocation(), Sound.ENDERDRAGON_GROWL, 10, 1);
                p.playSound(p.getLocation(), Sound.NOTE_PLING, 10, 2F);
                p.playSound(p.getLocation(), Sound.NOTE_BASS_GUITAR, 10, 2F);
                p.playSound(p.getLocation(), Sound.NOTE_PIANO, 10, 2F);
            }
        }

        if (time == 0L)
            startGame();
    }

    public int getPhase() {
        return timer.getPhase();
    }

    public MapManager getMapManager() {
        return maps;
    }

    public StatsManager getStatsManager() {
        return stats;
    }

    public DatabaseManager getDatabaseHandler() {
        return db;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public int getPhaseDelay() {
        return configManager.getConfig("config.yml").getInt("phase-period");
    }

    public void log(String m, Level l) {
        getLogger().log(l, m);
    }

    public VotingManager getVotingManager() {
        return voting;
    }

    public ScoreboardManager getScoreboardHandler() {
        return sb;
    }

    public void endGame(GameTeam winner) {
        if (winner == null)
            return;

        ChatUtil.winMessage(winner);
        timer.stop();

        for (Player p : getServer().getOnlinePlayers()) {
            if (PlayerMeta.getMeta(p).getTeam() == winner)
                stats.incrementStat(StatType.WINS, p);
        }

        long restartDelay = configManager.getConfig("config.yml").getLong(
                "restart-delay");
        RestartHandler rs = new RestartHandler(this, restartDelay);
        rs.start(timer.getTime(), winner.getColor(winner));
    }

    public void reset() {
        sb.resetScoreboard(ChatColor.DARK_AQUA + "Voting" + ChatColor.WHITE
                + " | " + ChatColor.GOLD + "/vote <name>");
        maps.reset();
        PlayerMeta.reset();
        timer.reset();
        for (Player p : getServer().getOnlinePlayers()) {
            PlayerMeta.getMeta(p).setTeam(GameTeam.NONE);
            p.teleport(maps.getLobbySpawnPoint());
            BarUtil.setMessageAndPercent(p, ChatColor.DARK_AQUA
                    + "Welcome to NexusGrinder!", 0.01F);
            p.setMaxHealth(20D);
            p.setHealth(20D);
            p.setFoodLevel(20);
            p.setSaturation(20F);
        }

        voting.start();
        sb.update();

        for (Player p : Bukkit.getOnlinePlayers()) {
            for (Player pp : Bukkit.getOnlinePlayers()) {
                p.showPlayer(pp);
                pp.showPlayer(p);
            }
        }

        Bukkit.getScheduler().runTaskLater(this, new Runnable() {
            @SuppressWarnings("deprecation")
            public void run() {
                for (Player p : getServer().getOnlinePlayers()) {
                    PlayerInventory inv = p.getInventory();
                    inv.setHelmet(null);
                    inv.setChestplate(null);
                    inv.setLeggings(null);
                    inv.setBoots(null);

                    p.getInventory().clear();

                    for (PotionEffect effect : p.getActivePotionEffects())
                        p.removePotionEffect(effect.getType());

                    p.setLevel(0);
                    p.setExp(0);
                    p.setSaturation(20F);

                    ItemStack selector = new ItemStack(Material.FEATHER);
                    ItemMeta itemMeta = selector.getItemMeta();
                    itemMeta.setDisplayName(ChatColor.AQUA
                            + "Right click to select class");
                    selector.setItemMeta(itemMeta);

                    p.getInventory().setItem(0, selector);

                    p.updateInventory();
                }

                for (GameTeam t : GameTeam.values())
                    if (t != GameTeam.NONE)
                        sign.updateSigns(t);

                checkStarting();
            }
        }, 2L);
    }

    public void checkWin() {
        int alive = 0;
        GameTeam aliveTeam = null;
        for (GameTeam t : GameTeam.teams()) {
            if (t.getNexus().isAlive()) {
                alive++;
                aliveTeam = t;
            }
        }
        if (alive == 1) {
            endGame(aliveTeam);
        }
    }

    public SignManager getSignHandler() {
        return sign;
    }

    public void setSignHandler(SignManager sign) {
        this.sign = sign;
    }

    public void checkStarting() {
        if (!timer.isRunning()) {
            if (Bukkit.getOnlinePlayers().length >= getConfig().getInt(
                    "requiredToStart"))
                timer.start();
        }
    }


    public PhaseManager getPhaseManager() {
        return timer;
    }

    public void listTeams(CommandSender sender) {
        sender.sendMessage(ChatColor.GRAY + "============[ "
                + ChatColor.DARK_AQUA + "Teams" + ChatColor.GRAY
                + " ]============");
        for (GameTeam t : GameTeam.teams()) {
            int size = 0;

            for (Player p : Bukkit.getOnlinePlayers()) {
                PlayerMeta meta = PlayerMeta.getMeta(p);
                if (meta.getTeam() == t)
                    size++;
            }

            if (size != 1) {
                sender.sendMessage(t.coloredName() + " - " + size + " " + Translation._("INFO_TEAM_LIST_PLAYERS") + Translation._("DYNAMIC_S"));
            } else {
                sender.sendMessage(t.coloredName() + " - " + size + " " + Translation._("INFO_TEAM_LIST_PLAYERS"));
            }
        }
        sender.sendMessage(ChatColor.GRAY + "===============================");
    }

    public void joinTeam(Player player, String team) {
        PlayerMeta meta = PlayerMeta.getMeta(player);
        if (meta.getTeam() != GameTeam.NONE && !player.hasPermission("annihilation.bypass.teamlimitor")) {
            player.sendMessage(ChatColor.GOLD + Translation._("NEXUSGRINDER_PREFIX") + ChatColor.DARK_AQUA + Translation._("ERROR_PLAYER_NOSWITCHTEAM"));
            return;
        }

        GameTeam target;
        try {
            target = GameTeam.valueOf(team.toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.GOLD + Translation._("NEXUSGRINDER_PREFIX") + ChatColor.RED + Translation._("ERROR_GAME_INVALIDTEAM"));
            listTeams(player);
            return;
        }

        if (Util.isTeamTooBig(target)
                && !player.hasPermission("annihilation.bypass.teamlimit")) {
            player.sendMessage(ChatColor.GOLD + Translation._("NEXUSGRINDER_PREFIX") + ChatColor.RED + Translation._("ERROR_GAME_TEAMFULL"));
            return;
        }

        if (target.getNexus() != null) {
            if (target.getNexus().getHealth() == 0 && getPhase() > 1) {
                player.sendMessage(ChatColor.GOLD + Translation._("NEXUSGRINDER_PREFIX") + ChatColor.RED + Translation._("ERROR_GAME_TEAMNONEXUS"));
                return;
            }
        }

        if (getPhase() > lastJoinPhase
                && !player.hasPermission("annhilation.bypass.phaselimiter")) {
            player.kickPlayer(ChatColor.GOLD + Translation._("NEXUSGRINDER_PREFIX") + ChatColor.RED
                    + "You cannot join the game in this phase!");
            return;
        }

        player.sendMessage(ChatColor.GOLD + Translation._("NEXUSGRINDER_PREFIX") + ChatColor.DARK_AQUA + "You joined "
                + target.coloredName());
        meta.setTeam(target);

        getScoreboardHandler().teams.get(team.toUpperCase()).addPlayer(
                player);

        if (getPhase() > 0) {
            Util.sendPlayerToGame(player, this);
        }

        getSignHandler().updateSigns(GameTeam.RED);
        getSignHandler().updateSigns(GameTeam.BLUE);
    }
}
