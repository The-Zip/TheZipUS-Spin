package us.thezip.spin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

@SuppressWarnings("unused")
public class Main extends JavaPlugin implements Listener {
    private WeakHashMap<UUID, Boolean> cannotTake = new WeakHashMap<>();
    private String title = ChatColor.YELLOW + "Havoc Lottery";
    private List<ItemStack> rewards = new ArrayList<>();
    private Inventory base = null;

    @Override
    public void onEnable() {
        loadConfig();
        base = Bukkit.createInventory(null, 9, title == null ? ChatColor.YELLOW + "Havoc Lottery" : title);
    }

    public void doRandomInventory(final Player player) {
        if(player.getOpenInventory() != null)
            player.closeInventory();
        cannotTake.put(player.getUniqueId(), true);
        final Inventory inventory = base;
        player.openInventory(inventory);

        new BukkitRunnable() {
            int counter = 0;
            @Override
            public void run() {
                if(counter < 5 && rewards.size() > 0) {
                    for (int i = 0; i < 5; i++) {
                        int thisRandom = getRandom(rewards.size());
                        inventory.setItem(2 + i, rewards.get(thisRandom));
                    }
                    counter++;
                } else {
                    cannotTake.put(player.getUniqueId(), false);
                    this.cancel();
                }
            }

        }.runTaskTimer(this, 0, 10L);
    }

    @EventHandler
    public void takeItems(InventoryClickEvent event)
    {
        if(event.getSlotType() == InventoryType.SlotType.CONTAINER)
        {
            if(cannotTake.get(event.getWhoClicked().getUniqueId())) {
                event.setCursor(null);
                event.setCancelled(true);
            }
        }
    }

    /**
     * Entire random number, configurable too.
     * @param maximum
     * @return
     */
    private Integer getRandom(Integer maximum) {
        Random random = new Random();
        return (random.nextInt(maximum) + random.nextInt(maximum) + random.nextInt(maximum) + random.nextInt(maximum)) / 4;
    }

    /**
     * Load the item list and quantities, etc, add to HashMap and set window title.
     */
    private void loadConfig() {
        this.saveDefaultConfig();
        title = getConfig().getString("spin.title");
        title = title.contains("&") ? ChatColor.translateAlternateColorCodes('&', title) : title;
        Bukkit.getServer().getPluginManager().registerEvents(this, this);

        for(String s : getConfig().getStringList("spin.items"))
        {
            String item = "COAL";
            String parseInt = "1";
            int quantity = 1;
            try {
                String[] lines = s.split(":");
                item = lines[0].toUpperCase();
                parseInt = lines[1];
                getLogger().info("Added " + parseInt + "x " + item + " to reward list.");
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                try {
                    Material material = Material.getMaterial(item);
                    Integer.parseInt(parseInt);
                    if (material != null && quantity > 0)
                        rewards.add(new ItemStack(material, quantity));
                } catch (NumberFormatException ex) {
                    getLogger().warning("Couldn't parse \"" + item + "\" defaulting to 1.");
                } catch (Exception ex) {
                    getLogger().warning("Err.. working on this one I'm afraid.");
                }
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String alias, String[] strings) {
        if(command.getName().equalsIgnoreCase("spin") || alias.equalsIgnoreCase("lotto"))
        {
            if(commandSender instanceof Player) {
                Player player = (Player) commandSender;
                if(!hasCooldown(player))
                    doRandomInventory(player);
            } else {
                if(strings.length < 1) {
                    commandSender.sendMessage(ChatColor.DARK_RED + "Insufficient arguments provided!");
                    commandSender.sendMessage(ChatColor.YELLOW + "/spin <player>");
                    return true;
                }
                try {
                    Player player = Bukkit.getOfflinePlayer(strings[0]).getPlayer();

                    if(player.isOnline()) {
                        doRandomInventory(player);
                    } else {
                        commandSender.sendMessage(ChatColor.DARK_RED + "Player is offline!");
                    }

                    return true;
                } catch (NullPointerException ex) {
                    commandSender.sendMessage(ChatColor.DARK_RED + "Player is invalid or offline.");
                }
            }
            return true;
        }
        return false;
    }

    public boolean hasCooldown(Player player) {
        if (Cooldown.tryCooldown(player, "Spin", 28800000)) {
            cannotTake.put(player.getUniqueId(), true);
            return false;
        } else {
            player.sendMessage(ChatColor.RED + "You have " + ChatColor.GOLD + (Cooldown.getCooldown(player, "Spin") / 1000) + ChatColor.RED + " seconds left.");
            return true;
        }
    }
}
