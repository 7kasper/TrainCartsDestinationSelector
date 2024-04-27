package nl.kaspermuller.traincartsdestinationselector;

import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.block.sign.Side;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class TrainCartsDestinationSelector extends JavaPlugin implements Listener {

	private static int STARTATTOP = -1;
	private EnumSet<Material> wallSign = EnumSet.of(
		Material.ACACIA_WALL_SIGN, 
		Material.BIRCH_WALL_SIGN, 
		Material.DARK_OAK_WALL_SIGN,
		Material.JUNGLE_WALL_SIGN,
		Material.OAK_WALL_SIGN,
		Material.SPRUCE_WALL_SIGN,
		Material.CHERRY_WALL_SIGN,
		Material.WARPED_WALL_SIGN,
		Material.BAMBOO_WALL_SIGN,
		Material.CRIMSON_WALL_SIGN,
		Material.MANGROVE_WALL_SIGN
	);
	private NamespacedKey signKey = new NamespacedKey(this, "TCDS");

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
		saveDefaultConfig();
	}

	@EventHandler
	public void onSignChange(SignChangeEvent e) {
		if (e.getLine(0).trim().equals("[TCDS]")) {
			if (e.getPlayer().hasPermission("traincartsdestinationselector.make")) {
				Sign sign = (Sign) e.getBlock().getState();
				if (e.getLine(1).trim().isEmpty() && e.getLine(2).trim().isEmpty() && e.getLine(3).trim().isEmpty()) {
					e.getPlayer().sendMessage("§eYou built a §fdestination selector§e!");
					e.getPlayer().sendMessage("§aThis sign allows users to select a train destination.");
					e.getPlayer().sendMessage("§aUse more signs in a column behind this block to add destinations.");
					sign.getPersistentDataContainer().set(signKey, PersistentDataType.INTEGER, STARTATTOP);
					sign.update();
					getServer().getScheduler().scheduleSyncDelayedTask(this, () -> cycleSign(sign, 1));
				} else {
					e.getPlayer().sendMessage("§eYou built a §fdestination list§e!");
					e.getPlayer().sendMessage("§aThis sign adds destinations to the selector.");
				}	
			} else {
				e.setLine(0, "TCDS");
				e.getPlayer().sendMessage("§4You don't have permission to make traincart selector signs!");
			}
		}
	}

	@EventHandler
	public void onClickSign(PlayerInteractEvent e) {
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK && wallSign.contains(e.getClickedBlock().getType())) {
			if (e.getPlayer().hasPermission("traincartsdestinationselector.use")) {
				Sign sign = (Sign) e.getClickedBlock().getState();
				String destination = cycleSign(sign, 1);
				if (destination != null && !destination.trim().isEmpty()) {
					e.getPlayer().performCommand("train destination " + destination);
					// Disable sign editing.
					e.setCancelled(true);
				}
			}
		}
		if (e.getAction() == Action.LEFT_CLICK_BLOCK && wallSign.contains(e.getClickedBlock().getType())) {
			if (e.getPlayer().hasPermission("traincartsdestinationselector.use")) {
				Sign sign = (Sign) e.getClickedBlock().getState();
				String destination = cycleSign(sign, -1);
				if (destination != null && !destination.trim().isEmpty()) e.getPlayer().performCommand("train destination " + destination);
			}
		}
	}

	public String cycleSign(Sign sign, int dir) {
		// Check if the sign is a destination selector.
		if (sign.getPersistentDataContainer().has(signKey, PersistentDataType.INTEGER)) {
			// Get all destinations.
			BlockFace direction = ((Directional) sign.getBlockData()).getFacing().getOppositeFace();
			List<String> options = new LinkedList<>();
			Block wall = sign.getBlock().getRelative(direction);
			afterWards(direction, wall, options);
			int length = options.size();
			// Get place in options iterator.
			int i = ((length == 0) ? 0 : sign.getPersistentDataContainer().get(signKey, PersistentDataType.INTEGER));
			// Little hack to make going back also possible from starting at top.
			if (i == STARTATTOP && dir < 0) i = 0;
			// Depending on the direction, get the next element.
			if (length > 0) { i = ((i+dir) % length); if (i < 0) i = length + i; } else { i = 0; }
			// Show destinations.
			sign.getSide(Side.FRONT).setLine(0, (length > 0) ? "> " + clampGet(i, length, options) + " <": "No Destinations!");
			sign.getSide(Side.FRONT).setLine(1, (length > 1) ? clampGet(i+1, length, options): "");
			sign.getSide(Side.FRONT).setLine(2, (length > 2) ? clampGet(i+2, length, options): "");
			sign.getSide(Side.FRONT).setLine(3, (length > 3) ? ((length > 4) ? "...": clampGet(i+3, length, options)): "");
			// Save iteration.
			sign.getPersistentDataContainer().set(signKey, PersistentDataType.INTEGER, i);
			sign.update();
			// If there is a timeout on this sign cancel it.
			if (sign.hasMetadata("timeout")) {
				getServer().getScheduler().cancelTask(sign.getMetadata("timeout").get(0).asInt());
			}
			Block bSign = sign.getBlock();
			// Put the select message after 5 seconds.
			if (getConfig().getLong("timeout") > -1) {
				sign.setMetadata("timeout", new FixedMetadataValue(this, getServer().getScheduler().scheduleSyncDelayedTask(this, ()-> {
					if (wallSign.contains(bSign.getType())) {
						Sign signn = (Sign) bSign.getState();
						List<String> lines = getConfig().getStringList("message");
						for (int j = 0; j < 4; j++) {
							String line = lines.get(j);
							if (line != null) signn.getSide(Side.FRONT).setLine(j, line.replaceAll("&", "§"));
						}
						// Also reset option list
						if (getConfig().getBoolean("reset")) {
							signn.getPersistentDataContainer().set(signKey, PersistentDataType.INTEGER, STARTATTOP);	
						}
						signn.update();
					}
				}, getConfig().getLong("timeout") * 20)));
			}
			// Return the selected option:
			if (length > 0) return options.get(i);
		}
		return null;
	}

	public String clampGet(int i, int length, List<String> options) {
		String str = options.get(i % length);
		if (str.length() > 12) return str.substring(0, 12);
		return str;
	}

	public void afterWards(BlockFace direction, Block wall, List<String> options) {
		boolean next = false;
		Block post = wall.getRelative(direction);
		next = addSignOptions(post.getRelative(getLeft(direction)), options) || next;
		next = addSignOptions(post.getRelative(getRight(direction)), options) || next;
		if (next) afterWards(direction, post, options);
	}

	public boolean addSignOptions(Block block, List<String> options) {
		if (wallSign.contains(block.getBlockData().getMaterial())) {
			Sign sign = (Sign) block.getState();
			if (sign.getSide(Side.FRONT).getLine(0).contains("[TCDS]")) {
				for (int i = 1; i < 4; i++) {
					String line = sign.getSide(Side.FRONT).getLine(i).trim();
					if (!line.isEmpty()) options.add(line);
				}
				return true;
			}
		}
		return false;
	}

	public BlockFace getLeft(BlockFace direction) {
		switch(direction) {
			case NORTH: return BlockFace.WEST;
			case EAST: return BlockFace.NORTH;
			case SOUTH: return BlockFace.EAST;
			case WEST: return BlockFace.SOUTH;
		default:
			return direction;
		}
	}

	public BlockFace getRight(BlockFace direction) {
		switch(direction) {
			case NORTH: return BlockFace.EAST;
			case EAST: return BlockFace.SOUTH;
			case SOUTH: return BlockFace.WEST;
			case WEST: return BlockFace.NORTH;
		default:
			return direction;
		}
	}

}
