package com.gmail.mckokumin.anninexus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
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
	public Main instance;

	public void onEnable() {
		this.instance = this;
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
							ParticleEffect.FIREWORKS_SPARK.display(0, 0, 0, 0.5F, 50, bl.clone().add(0.5d, 0.5d, 0.5d), 32);
							ParticleEffect.LAVA.display(3, 2, 3, 0.5F, 20, bl.clone().add(0.5d, 0.5d, 0.5d), 32);
							getNexus().getWorld().playSound(lo, Sound.NOTE_PIANO, 32f, 2F);
							for (Player all : Bukkit.getOnlinePlayers()) {
								all.playEffect(lo, Effect.STEP_SOUND, Material.OBSIDIAN.getId());
							}
							List<Float> floatArray = new ArrayList<>();
							floatArray.add(0.5f);
							floatArray.add(0.6f);
							floatArray.add(0.7f);
							floatArray.add(0.8f);
							floatArray.add(0.9f);
							Collections.shuffle(floatArray);
							getNexus().getWorld().playSound(bl, Sound.ANVIL_LAND, 16f, floatArray.get(0));
							lo.getBlock().setType(Material.ENDER_STONE);
						}
					}, 5L);
				}
				if (hp == 0) {
					this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
						public void run() {
							p.sendMessage("§c§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
							p.sendMessage(" ");
							p.sendMessage("        §cNexusを破壊しました");
							p.sendMessage(" ");
							p.sendMessage("§c§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
							event.getBlock().getLocation().getBlock().setType(Material.BEDROCK);
							getNexus().getWorld().playSound(bl, Sound.EXPLODE, 32f, 1);
							ParticleEffect.EXPLOSION_LARGE.display(2, 2, 2, 0.1F, 30, bl, 32);
							Bukkit.getScheduler().runTaskTimer(instance, new Runnable() {
								int i = 6;

								public void run() {
									if (i > 0) {
										i--;
										ParticleEffect.EXPLOSION_LARGE.display(2, 2, 2, 0.1F, 30, bl, 32);
									}
								}
							}, 5L, 5L);
							Bukkit.getScheduler().runTaskLater(instance, () -> {
								resetNexus();
							}, 100L);
						}
					}, 5L);
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
			resetNexus();
			sender.sendMessage(ChatColor.YELLOW + "ネクサスをリセットしました");
			break;
		case "nexushp":
			if (getNexus() == null) {
				sender.sendMessage(ChatColor.RED + "ネクサスを指定していません");
				return true;
			}
			if (args.length == 1) {
				if (StringUtils.isNumeric(args[0])) {
					int sethp = Integer.valueOf(args[0]);
					if (sethp > 0) {
						sender.sendMessage("§aNexusのHPを " + hp + " から " + sethp + " にしました");
						hp = sethp;
						getNexus().getBlock().setType(Material.ENDER_STONE);
					}
					if (sethp <= 0) {
						sender.sendMessage("§c§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
						sender.sendMessage(" ");
						sender.sendMessage("        §cNexusを破壊しました");
						sender.sendMessage(" ");
						sender.sendMessage("§c§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
						getNexus().getBlock().setType(Material.BEDROCK);
						getNexus().getWorld().playSound(getNexus(), Sound.EXPLODE, 32f, 1);
						ParticleEffect.EXPLOSION_LARGE.display(2, 2, 2, 0.1F, 30, getNexus(), 32);
						Bukkit.getScheduler().runTaskTimer(instance, new Runnable() {
							int i = 3;

							public void run() {
								if (i > 0) {
									i--;
									ParticleEffect.EXPLOSION_LARGE.display(2, 2, 2, 0.1F, 30, getNexus(), 32);
								}
							}
						}, 10L, 10L);
					}
				} else {
					sender.sendMessage("§c数字をいれてください");
				}
				return true;
			}
			sender.sendMessage("§a/nexushp <hp>");
			sender.sendMessage("§aCurrent HP: " + hp);
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

	private void resetNexus() {
		if (getNexus() == null)
			return;
		getNexus().getBlock().setType(Material.ENDER_STONE);
		hp = 75;
		Bukkit.getScheduler().runTaskTimerAsynchronously(instance, new Runnable() {
			int i = 3;
			double y = -1;

			@SuppressWarnings("deprecation")
			@Override
			public void run() {
				if (i > 0) {
					for (int i = 0; i < 360; i += 45) {
						Location loc = getNexus().clone();
						double addx = 1 * Math.sin(i * (Math.PI / 180));
						double addz = 1 * Math.cos(i * (Math.PI / 180));
						loc.add(addx, y, addz);
						for (Player all : Bukkit.getOnlinePlayers()) {
							all.playEffect(loc, Effect.STEP_SOUND, Material.OBSIDIAN.getId());
							all.playEffect(loc, Effect.STEP_SOUND, Material.REDSTONE_BLOCK.getId());
						}
					}
					y++;
					i--;
				}
			}
		}, 0L, 0L);
	}

}
