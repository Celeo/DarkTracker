package net.thedarktide.celeo;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * <b>DarkTracker</b><br>
 * Started on May 3rd, 2012.
 * 
 * @author Celeo
 */
public class DarkTrackerCore extends JavaPlugin
{

	private static final Logger log = Logger.getLogger("Minecraft");
	private static final boolean DEBUGGING = true;
	private File databaseFile = null;
	private Connection connection = null;

	boolean trackLogin;
	boolean trackLogout;
	List<Integer> trackBlockBreak;
	List<Integer> trackBlockPlace;
	boolean trackGameModeSwitch;
	boolean trackPlayerKill;
	boolean trackPlayerDeath;
	boolean trackOpenChest;
	boolean trackPlayerCommand;
	boolean trackAllBlockBreaks;
	boolean trackAllBlockPlaces;
	boolean trackBucketUse;

	@Override
	public void onEnable()
	{
		log("Initiating ...");
		setupDatabase();
		getServer().getPluginManager().registerEvents(new Listeners(this), this);
		getCommand("darktracker").setExecutor(this);
		if (!new File(getDataFolder(), "/config.yml").exists())
			saveDefaultConfig();
		load();
		log("Enabled");
	}

	public void load()
	{
		trackLogin = getConfig().getBoolean("trackLogin", false);
		trackLogout = getConfig().getBoolean("trackLogout", false);
		trackBlockBreak = getConfig().getIntegerList("trackBlockBreak");
		trackBlockPlace = getConfig().getIntegerList("trackBlockPlace");
		trackGameModeSwitch = getConfig().getBoolean("trackGameModeSwitch", true);
		trackPlayerKill = getConfig().getBoolean("trackPlayerKill", false);
		trackPlayerDeath = getConfig().getBoolean("trackPlayerDeath", false);
		trackOpenChest = getConfig().getBoolean("trackOpenChest", true);
		trackPlayerCommand = getConfig().getBoolean("trackPlayerCommand", false);
		trackAllBlockBreaks = getConfig().getBoolean("trackAllBlockBreaks", true);
		trackAllBlockPlaces = getConfig().getBoolean("trackAllBlockPlaces", false);
		trackBucketUse = getConfig().getBoolean("trackBucketUse", true);
		checkLists();
	}

	@SuppressWarnings("boxing")
	public void checkLists()
	{
		if (trackBlockBreak == null)
		{
			trackBlockBreak = new ArrayList<Integer>();
			trackBlockBreak.add(14);
			trackBlockBreak.add(15);
			trackBlockBreak.add(56);
			trackBlockBreak.add(129);
		}
		if (trackBlockPlace == null)
			trackBlockPlace = new ArrayList<Integer>();
	}

	@SuppressWarnings("boxing")
	public void save()
	{
		getConfig().set("trackLogin", trackLogin);
		getConfig().set("trackLogout", trackLogout);
		getConfig().set("trackBlockBreak", trackBlockBreak);
		getConfig().set("trackBlockPlace", trackBlockPlace);
		getConfig().set("trackGameModeSwitch", trackGameModeSwitch);
		getConfig().set("trackPlayerKill", trackPlayerKill);
		getConfig().set("trackPlayerDeath", trackPlayerDeath);
		getConfig().set("trackOpenChest", trackOpenChest);
		getConfig().set("trackPlayerCommand", trackPlayerCommand);
		getConfig().set("trackAllBlockBreaks", trackAllBlockBreaks);
		getConfig().set("trackAllBlockPlaces", trackAllBlockPlaces);
		getConfig().set("trackBucketUse", trackBucketUse);
		saveConfig();
	}

	@Override
	public void onDisable()
	{
		try
		{
			connection.close();
		}
		catch (Exception e)
		{}
		log("Disabled");
	}

	@SuppressWarnings("boxing")
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		if (args == null || args.length == 0)
			return false;
		String param = args[0].toLowerCase();
		if (param.equals("reload"))
		{
			reloadConfig();
			load();
			sender.sendMessage("Reloaded from configuration");
		}
		if (param.equals("save"))
		{
			save();
			sender.sendMessage("Saved to configuration");
		}
		if (param.equals("defaultconfig"))
		{
			saveDefaultConfig();
			load();
			sender.sendMessage("Done");
		}
		if (param.equals("list"))
		{
			if (args.length != 2)
				sender.sendMessage("/darktracker list [type]");
			else
			{
				if (args[1].equals("break"))
				{
					String types = "";
					for (Integer i : trackBlockBreak)
					{
						if (types.equals(""))
							types = i.toString();
						else
							types += " " + i.toString();
					}
					sender.sendMessage("Blocks being logged: " + types);
				}
				else if (args[1].equals("place"))
				{
					String types = "";
					for (Integer i : trackBlockPlace)
					{
						if (types.equals(""))
							types = i.toString();
						else
							types += " " + i.toString();
					}
					sender.sendMessage("Blocks being logged: " + types);
				}
				else if (args[1].equals("all"))
					sender.sendMessage(String.format("login: %b logout: %b gamemode: %b kill: %b death: %b openchest: %b command: %b allbreak: %b allplace %b", trackLogin, trackLogout, trackGameModeSwitch, trackPlayerKill, trackPlayerDeath, trackOpenChest, trackPlayerCommand, trackAllBlockBreaks, trackAllBlockPlaces));
			}
		}
		return true;
	}

	public void setupDatabase()
	{
		getDataFolder().mkdirs();
		databaseFile = new File(getDataFolder(), "/Logs.db");
		if (!databaseFile.exists())
		{
			try
			{
				databaseFile.createNewFile();
				debug("Created new file for the database.");
			}
			catch (IOException e)
			{
				log("ERROR: Could not create the database file!");
			}
		}
		try
		{
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:"
					+ databaseFile);
			debug("Connection established to the database.");
			Statement stat = connection.createStatement();
			stat.executeUpdate("CREATE TABLE IF NOT EXISTS `logs` (name TEXT, time TEXT, type TEXT, location TEXT)");
			stat.close();
			log("Connected to SQLite database and initiated.");
		}
		catch (ClassNotFoundException e)
		{
			log("ERROR: No SQLite driver found!");
		}
		catch (SQLException e)
		{
			log("ERROR: Error with the SQL used to create the table!");
		}
	}

	@SuppressWarnings("static-method")
	public void log(String messsage)
	{
		log.info("[DarkTracker] " + messsage);
	}

	public void debug(String message)
	{
		if (DEBUGGING)
			log("<DEBUG> " + message);
	}

	@SuppressWarnings("static-method")
	public String formatLocation(Location location)
	{
		return formatDouble(location.getX()) + " "
				+ formatDouble(location.getY()) + " "
				+ formatDouble(location.getZ());
	}

	public static String formatDouble(double doubleToFormat)
	{
		return new DecimalFormat("#.##").format(doubleToFormat);
	}

	public void logInformation(String type, Player player)
	{
		String time = Long.valueOf(System.currentTimeMillis() / 1000).toString();
		String sql = "Insert into `logs` (`name`, `time`, `type`, `location`) values ('"
				+ player.getName()
				+ "', "
				+ time
				+ ", '"
				+ type
				+ "', '"
				+ formatLocation(player.getLocation()) + "')";
		try
		{
			Statement statement = connection.createStatement();
			statement.executeUpdate(sql);
			statement.close();
		}
		catch (SQLException e)
		{
			debug("An error occurred running the SQL statement: '" + sql + "'");
		}
	}

}