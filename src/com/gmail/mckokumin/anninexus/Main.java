package com.gmail.mckokumin.anninexus;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener, CommandExecutor {

	public ArrayList<String> setnexus = new ArrayList<String>();
	public int hp = 75;
	public int nexusX;
	public int nexusY;
	public int nexusZ;
	public String nexusWorld;
	public String nexusDamageMessage;

	public void onEnable() {
		this.saveDefaultConfig();
		this.saveConfig();
		this.getServer().getPluginManager().registerEvents(this, this);
		if (this.getConfig().isSet("nexus.X"))
			this.nexusX = this.getConfig().getInt("nexus.X");
		if (this.getConfig().isSet("nexus.Y"))
			this.nexusY = this.getConfig().getInt("nexus.y");
		if (this.getConfig().isSet("nexus.Z"))
			this.nexusZ = this.getConfig().getInt("nexus.Z");
		if (this.getConfig().isSet("nexus.world"))
			this.nexusWorld = this.getConfig().getString("nexus.world");
		if (this.getConfig().isSet("nexus.nexusDamage"))
			this.nexusDamageMessage = this.getConfig().getString("messages.nexusDamage");
	}

	public void onDisable() {

	}

	@EventHandler(ignoreCancelled = true)
	public void onNexus(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Player p = event.getPlayer();
			Block b = event.getClickedBlock();
			if (setnexus.contains(p.getName())) {
				setNexus(b.getX(), b.getY(), b.getZ(), p.getWorld().getName());
				saveConfig();
				setnexus.remove(p.getName());
				p.sendMessage(ChatColor.YELLOW + "ネクサスの位置を設定しました(エンドストーンであれば破壊できます)");
			}
		}
	}

	@EventHandler
	public void onNexusDamage(BlockBreakEvent event) {
		if (nexusWorld == null) {
			return;
		}
		Player p = event.getPlayer();
		Block b = event.getBlock();
		if (b.getType() == Material.ENDER_STONE) {
			Location bl = b.getLocation();
			if (bl.getBlockX() == nexusX && bl.getBlockY() == nexusY && bl.getBlockZ() == nexusZ
					&& p.getWorld().getName().equalsIgnoreCase(nexusWorld)) {
				hp--;
				event.setCancelled(true);
				bl.getBlock().setType(Material.AIR);
				if (hp > 0) {
					this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
						@SuppressWarnings("deprecation")
						public void run() {
							p.sendMessage("§cNexusに攻撃しました 残り: " + hp);
							if (nexusDamageMessage != null) {
								Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', nexusDamageMessage));
							}
							Location lo = new Location(p.getWorld(), nexusX, nexusY, nexusZ);
							for (Player all : Bukkit.getOnlinePlayers()) {
								ParticleEffect.FIREWORKS_SPARK.display(0, 0, 0, 0.5F, 50, bl, 32);
								ParticleEffect.LAVA.display(3, 2, 3, 0.5F, 20, bl, 32);
								all.playSound(lo, Sound.NOTE_PIANO, 32f, 2F);
								all.playEffect(lo, Effect.STEP_SOUND, Material.OBSIDIAN.getId());
								int[] i = { 1, 2, 3, 4, 5 };
								int result = i[(int) Math.floor(Math.random() * i.length)];
								if (result == 1) {
									all.playSound(bl, Sound.ANVIL_LAND, 16f, 0.5f);
								}
								if (result == 2) {
									all.playSound(bl, Sound.ANVIL_LAND, 16f, 0.6f);
								}
								if (result == 3) {
									all.playSound(bl, Sound.ANVIL_LAND, 16f, 0.7f);
								}
								if (result == 4) {
									all.playSound(bl, Sound.ANVIL_LAND, 16f, 0.8f);
								}
								if (result == 5) {
									all.playSound(bl, Sound.ANVIL_LAND, 16f, 0.9f);
								}
							}
							lo.getBlock().setType(Material.ENDER_STONE);
						}
					}, 6L);
				}
				if (hp == 0) {
					this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
						public void run() {
							p.sendMessage("§c§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
							p.sendMessage(" ");
							p.sendMessage("        §cNexusを破壊しました");
							p.sendMessage(" ");
							p.sendMessage("§c§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
							event.setCancelled(true);
							event.getBlock().getLocation().getBlock().setType(Material.BEDROCK);
							for (Player all : Bukkit.getOnlinePlayers()) {
								all.playSound(bl, Sound.EXPLODE, 32f, 1);
								ParticleEffect.EXPLOSION_LARGE.display(2, 2, 2, 0.1F, 30, bl, 32);
							}
						}
					}, 6L);
				}
			}
		}
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		switch (cmd.getName()) {
		case "nexus":
			if (setnexus.contains(sender.getName())) {
				sender.sendMessage(ChatColor.RED + "ネクサスをクリックしてください!!");
				return true;
			}
			setnexus.add(sender.getName());
			sender.sendMessage(ChatColor.GOLD + "ネクサスをクリックしてください...");
			break;
		case "nexuscancel":
			if (setnexus.contains(sender.getName())) {
				sender.sendMessage(ChatColor.GOLD + "ネクサス選択モードから解除されました");
				setnexus.remove(sender.getName());
				return true;
			}
			sender.sendMessage(ChatColor.RED + "あなたはネクサス選択モードではありません");
			break;
		case "nexusdelete":
			getConfig().set("nexus.X", null);
			getConfig().set("nexus.Y", null);
			getConfig().set("nexus.Z", null);
			getConfig().set("nexus.world", null);
			nexusX = 0;
			nexusY = 0;
			nexusZ = 0;
			nexusWorld = null;
			saveConfig();
			sender.sendMessage(ChatColor.YELLOW + "ネクサスを解除しました");
			break;
		case "nexusmsg":
			if (args.length == 0) {
				sender.sendMessage(ChatColor.RED + "使い方: /nexusmsg <message>");
			} else {
				String m = "";
				for (int i = 0; i < args.length; ++i) {
					m += args[i] + " ";
				}
				getConfig().set("messages.nexusDamage", m);
				sender.sendMessage(ChatColor.GREEN + "メッセージを指定しました");
			}
			break;
		case "nexusreset":
			if (getNexus() == null) {
				sender.sendMessage(ChatColor.RED + "ネクサスを指定していません");
				return true;
			}
			getNexus().getBlock().setType(Material.ENDER_STONE);
			hp = 75;
			sender.sendMessage(ChatColor.YELLOW + "ネクサスをリセットしました");
			break;
		}
		return true;
	}

	private Location getNexus() {
		if (nexusWorld != null) {
			return new Location(Bukkit.getWorld(nexusWorld), nexusX, nexusY, nexusZ);
		}
		int x = getConfig().getInt("nexus.X");
		int y = getConfig().getInt("nexus.Y");
		int z = getConfig().getInt("nexus.Z");
		String w1 = getConfig().getString("nexus.world");
		World w = Bukkit.getWorld(w1);
		nexusX = x;
		nexusY = y;
		nexusZ = z;
		nexusWorld = w1;
		return new Location(w, x, y, z);
	}

	private void setNexus(int x, int y, int z, String world) {
		getConfig().set("nexus.X", x);
		getConfig().set("nexus.Y", y);
		getConfig().set("nexus.Z", z);
		getConfig().set("nexus.world", world);
		nexusX = x;
		nexusY = y;
		nexusZ = z;
		nexusWorld = world;
	}

}
