package br.com.nspick;

// Importações necessárias para manipular itens, jogadores, blocos e configurações
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.*;

public class PickaxeManager {

    // Referência ao plugin principal
    private final JavaPlugin plugin;

    // Variáveis para armazenar os dados da picareta
    private String pickaxeName;
    private List<String> pickaxeLore;
    private Map<Enchantment, Integer> pickaxeEnchants = new HashMap<>();
    private Material pickaxeMaterial;

    // Construtor que recebe a instância do plugin
    public PickaxeManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    // Carrega os valores da picareta a partir do config.yml
    public void loadConfigValues() {
        FileConfiguration config = plugin.getConfig();

        // Nome da picareta com suporte a cores (&)
        pickaxeName = ChatColor.translateAlternateColorCodes('&',
                config.getString("pickaxe.name", "&8&lPICARETA NSPICKAXE"));

        // Lore da picareta (descrição visual)
        pickaxeLore = new ArrayList<>();
        for (String line : config.getStringList("pickaxe.lore")) {
            pickaxeLore.add(ChatColor.translateAlternateColorCodes('&', line));
        }

        // Material da picareta (ex: DIAMOND_PICKAXE)
        String materialName = config.getString("pickaxe.material", "DIAMOND_PICKAXE").toUpperCase();
        try {
            pickaxeMaterial = Material.valueOf(materialName);
        } catch (Exception e) {
            pickaxeMaterial = Material.DIAMOND_PICKAXE; // fallback padrão
        }

        // Limpa e carrega os encantamentos definidos no config
        pickaxeEnchants.clear();
        if (config.isConfigurationSection("pickaxe.enchantments")) {
            for (String key : config.getConfigurationSection("pickaxe.enchantments").getKeys(false)) {
                Enchantment enchant = Enchantment.getByName(key.toUpperCase());
                int level = config.getInt("pickaxe.enchantments." + key);
                if (enchant != null) {
                    pickaxeEnchants.put(enchant, level);
                } else {
                    plugin.getLogger().warning("Encantamento inválido: " + key);
                }
            }
        }
    }

    // Dá a picareta personalizada para o jogador
    public void givePickaxe(Player player) {
        ItemStack pickaxe = new ItemStack(pickaxeMaterial);
        ItemMeta meta = pickaxe.getItemMeta();
        if (meta == null) return;

        meta.setDisplayName(pickaxeName);
        meta.setLore(pickaxeLore);

        // Aplica os encantamentos
        for (Map.Entry<Enchantment, Integer> entry : pickaxeEnchants.entrySet()) {
            meta.addEnchant(entry.getKey(), entry.getValue(), true);
        }

        pickaxe.setItemMeta(meta);
        pickaxe = tornarInquebravel(pickaxe); // Torna a picareta inquebrável via NBT
        player.getInventory().addItem(pickaxe); // Adiciona ao inventário do jogador
    }

    // Verifica se o item é a picareta do NSPick
    public boolean isNSPick(ItemStack item) {
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        return meta != null &&
                ChatColor.stripColor(meta.getDisplayName()).equals(ChatColor.stripColor(pickaxeName));
    }

    public String getPickaxeName() {
        return pickaxeName;
    }

    // Realiza a quebra em área 3x3 com base na face clicada
    public void breakArea(Block center, ItemStack item, Player player) {
        BlockFace face = getBlockFace(player, center);
        List<Block> blocks = getAffectedBlocks(center, face);

        for (Block block : blocks) {
            if (block.getType().isSolid()) {
                block.breakNaturally(item); // Quebra o bloco com drop natural
            }
        }
    }

    // Torna o item inquebrável usando NBT (compatível com versões antigas)
    private ItemStack tornarInquebravel(ItemStack item) {
        try {
            Class<?> craftItemStack = Class.forName("org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack");
            Class<?> nmsItemStack = Class.forName("net.minecraft.server.v1_8_R3.ItemStack");
            Class<?> nbtTagCompound = Class.forName("net.minecraft.server.v1_8_R3.NBTTagCompound");

            Object nmsItem = craftItemStack.getMethod("asNMSCopy", ItemStack.class).invoke(null, item);
            Object tag = nmsItemStack.getMethod("getTag").invoke(nmsItem);

            if (tag == null) {
                tag = nbtTagCompound.newInstance();
            }

            nbtTagCompound.getMethod("setBoolean", String.class, boolean.class).invoke(tag, "Unbreakable", true);
            nmsItemStack.getMethod("setTag", nbtTagCompound).invoke(nmsItem, tag);

            return (ItemStack) craftItemStack.getMethod("asBukkitCopy", nmsItemStack).invoke(null, nmsItem);
        } catch (Exception e) {
            e.printStackTrace();
            return item; // Se falhar, retorna o item original
        }
    }

    // Determina a face do bloco clicado com base na posição do jogador
    private BlockFace getBlockFace(Player player, Block block) {
        Location eye = player.getEyeLocation();
        Vector to = block.getLocation().add(0.5, 0.5, 0.5).toVector().subtract(eye.toVector());

        double x = Math.abs(to.getX());
        double y = Math.abs(to.getY());
        double z = Math.abs(to.getZ());

        if (y > x && y > z) return to.getY() > 0 ? BlockFace.UP : BlockFace.DOWN;
        if (x > z) return to.getX() > 0 ? BlockFace.EAST : BlockFace.WEST;
        return to.getZ() > 0 ? BlockFace.SOUTH : BlockFace.NORTH;
    }

    // Retorna os blocos afetados pela mineração em área 3x3
    private List<Block> getAffectedBlocks(Block center, BlockFace face) {
        List<Block> blocks = new ArrayList<>();
        Location loc = center.getLocation();
        int cx = loc.getBlockX(), cy = loc.getBlockY(), cz = loc.getBlockZ();
        World world = center.getWorld();

        // Define os eixos de varredura com base na face
        int ax1 = 0, ax2 = 0;
        switch (face) {
            case UP: case DOWN: ax1 = 0; ax2 = 2; break;
            case NORTH: case SOUTH: ax1 = 0; ax2 = 1; break;
            case EAST: case WEST: ax1 = 2; ax2 = 1; break;
            default: ax1 = 0; ax2 = 2;
        }

        // Varre os blocos em 3x3 no plano correto
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int x = cx, y = cy, z = cz;
                if (ax1 == 0) x += i;
                if (ax1 == 1) y += i;
                if (ax1 == 2) z += i;
                if (ax2 == 0) x += j;
                if (ax2 == 1) y += j;
                if (ax2 == 2) z += j;

                Block b = world.getBlockAt(x, y, z);
                blocks.add(b);
            }
        }

        return blocks;
    }
}
