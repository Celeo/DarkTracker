package net.thedarktide.celeo;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class Listeners implements Listener
{

	private final DarkTrackerCore plugin;

	public Listeners(DarkTrackerCore instance)
	{
		plugin = instance;
	}

	@EventHandler
	public void logBlockBreak(BlockBreakEvent event)
	{
		if ((plugin.trackBlockBreak == null || plugin.trackBlockBreak.isEmpty())
				&& !plugin.trackAllBlockBreaks)
			return;
		Player player = event.getPlayer();
		Block block = event.getBlock();
		if (player == null)
			return;
		if (plugin.trackBlockBreak.contains(Integer.valueOf(block.getTypeId())))
			plugin.logInformation("blockBreak " + block.getType().name()
					+ " at " + plugin.formatLocation(block.getLocation()), player);
	}

	@EventHandler
	public void logBlockPlace(BlockPlaceEvent event)
	{
		if ((plugin.trackBlockPlace == null || plugin.trackBlockPlace.isEmpty())
				&& !plugin.trackAllBlockPlaces)
			return;
		Player player = event.getPlayer();
		Block block = event.getBlock();
		if (player == null)
			return;
		if (plugin.trackBlockPlace.contains(Integer.valueOf(block.getTypeId())))
			plugin.logInformation("blockPlace " + block.getType().name()
					+ " at " + plugin.formatLocation(block.getLocation()), player);
	}

	@EventHandler
	public void logPlayerDeath(EntityDeathEvent event)
	{
		if (!plugin.trackPlayerDeath)
			return;
		if (!(event.getEntity() instanceof Player))
			return;
		Player player = (Player) event.getEntity();
		if (player == null)
			return;
		plugin.logInformation("died", player);
	}

	@EventHandler
	public void logPlayerKillOtherPlayer(EntityDeathEvent event)
	{
		if (!plugin.trackPlayerKill)
			return;
		if (!(event.getEntity() instanceof Player))
			return;
		Player dead = (Player) event.getEntity();
		if (!(event.getEntity().getLastDamageCause().getCause().equals(DamageCause.ENTITY_ATTACK)))
			return;
		if (event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent)
		{
			EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event.getEntity().getLastDamageCause();
			if (e.getDamager() instanceof Player)
			{
				Player killer = (Player) e.getDamager();
				plugin.logInformation("killed " + dead.getName(), killer);
			}
		}
	}

	@EventHandler
	public void playerUseBucket(PlayerBucketEmptyEvent event)
	{
		if (event.isCancelled())
			return;
		plugin.logInformation("bucket "
				+ event.getBucket().name().toLowerCase().replace("_bucket", "")
				+ " "
				+ plugin.formatLocation(event.getBlockClicked().getLocation()), event.getPlayer());
	}

	@EventHandler
	public void logPlayerGameModeSwitch(PlayerGameModeChangeEvent event)
	{
		if (!plugin.trackGameModeSwitch)
			return;
		plugin.logInformation(event.getNewGameMode().name(), event.getPlayer());
	}

	@EventHandler
	public void onPlayerJoinServer(PlayerJoinEvent event)
	{
		if (!plugin.trackLogin)
			return;
		Player player = event.getPlayer();
		plugin.logInformation("join "
				+ player.getAddress().toString().split(":")[0].replaceAll("/", ""), player);
	}

	@EventHandler
	public void onPlayerQuitServer(PlayerQuitEvent event)
	{
		if (!plugin.trackLogout)
			return;
		plugin.logInformation("quit", event.getPlayer());
	}

	@EventHandler
	public void onPlayerOpenChest(PlayerInteractEvent event)
	{
		if (!plugin.trackOpenChest)
			return;
		Player player = event.getPlayer();
		Block block = event.getClickedBlock();
		if (player == null || block == null
				|| !block.getType().equals(Material.CHEST))
			return;
		plugin.logInformation("openChest "
				+ plugin.formatLocation(block.getLocation()), player);
	}

	@EventHandler
	public void onPlayerEnterCommand(PlayerCommandPreprocessEvent event)
	{
		if (!plugin.trackPlayerCommand)
			return;
		Player player = event.getPlayer();
		String command = event.getMessage();
		if (player == null || command == null)
			return;
		plugin.logInformation("command: " + command, player);
	}

	public DarkTrackerCore getPlugin()
	{
		return plugin;
	}

}