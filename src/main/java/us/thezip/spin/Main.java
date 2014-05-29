package us.thezip.spin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

@SuppressWarnings("unused")
public class Main extends JavaPlugin {
    private List<ItemStack> rewards = new ArrayList<ItemStack>();
    private Properties properties = new Properties();
    private Inventory base = null;
    private int randomness = 6;
    private String title = "";

    @Override
    public void onEnable() {
        loadConfig();
        base = Bukkit.createInventory(null, 9, properties.getProperty("title"));
    }

    public void doRandomInventory(final Player player) {
        if(player.getOpenInventory() != null)
            player.closeInventory();
        final Inventory inventory = base;

        player.openInventory(inventory);
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 5; i++) {
                    int thisRandom = getRandom(rewards.size());
                    inventory.setItem(2 + i, rewards.get(thisRandom));
                }
            }
        }, 0, 5L);
    }

    /**
     * Entire random number, configurable too.
     * @param maximum
     * @return
     */
    private Integer getRandom(Integer maximum) {
        Random random = new Random();
        int x = 0;
        for(int i = 0; i < randomness; i++)
            x += random.nextInt(maximum);
        return (x / randomness);
    }

    /**
     * Load the item list and quantities, etc, add to HashMap and set window title.
     */
    private void loadConfig() {
        title = getConfig().getString("title");
        for(String s : getConfig().getStringList("items"))
        {
            String item = "COAL";
            String parseInt = "1";
            int quantity = 1;

            try {
                String[] lines = s.split(":");
                item = lines[0].toUpperCase();
                parseInt = lines[1];
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
        if(command.getName().equalsIgnoreCase("spin"))
        {
            if(commandSender instanceof Player) {
                Player player = (Player) commandSender;
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
}
