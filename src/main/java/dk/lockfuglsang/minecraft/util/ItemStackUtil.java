package dk.lockfuglsang.minecraft.util;

import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;

import dk.lockfuglsang.minecraft.reflection.ReflectionUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Conversion to ItemStack from strings.
 */
public enum ItemStackUtil {
    ;
    private static final Pattern ITEM_AMOUNT_PATTERN = Pattern.compile("(\\{p=(?<prob>0\\.[0-9]+)\\})?(?<id>\\#?[0-9A-Za-z_]+):(?<amount>[0-9]+)\\s*(?<meta>\\{.*\\})?", Pattern.DOTALL);
    private static final Pattern ITEM_PATTERN = Pattern.compile("(?<id>[0-9A-Z_]+)\\s*(?<meta>\\{.*\\})?", Pattern.DOTALL);
    private static final Random RANDOM = new Random();

    public static List<ItemProbability> createItemsWithProbabilty(List<String> items) {
        List<ItemProbability> itemProbs = new ArrayList<>();
        for (String reward : items) {
            Matcher m = ITEM_AMOUNT_PATTERN.matcher(reward);
            if (m.matches()) {
                double p = m.group("prob") != null ? Double.parseDouble(m.group("prob")) : 1;
                String id = m.group("id");
                Material type;
                Tag<Material> tag = null;
                if (id.startsWith("#")) {
                    tag = Bukkit.getTag(Tag.REGISTRY_ITEMS, NamespacedKey.minecraft(id.substring(1).toLowerCase()), Material.class);
                    if (tag == null) {
                        throw new IllegalArgumentException("Unknown item tag: '" + tag + "' in '" + items + "'");
                    }
                    type = Material.COBBLESTONE;
                } else {
                    type = getItemType(id.toUpperCase());
                }
                int amount = Integer.parseInt(m.group("amount"), 10);
                String metaStr = m.group("meta");
                ItemStack itemStack = (metaStr == null || metaStr.isBlank()) ? new ItemStack(type, amount) : Bukkit.getItemFactory().createItemStack(type.getKey().toString() + metaStr);
                itemStack.setAmount(amount);
                itemProbs.add(new ItemProbability(p, itemStack, tag));
            } else {
                throw new IllegalArgumentException("Unknown item: '" + reward + "' in '" + items + "'");
            }
        }
        return itemProbs;

    }

    private static Material getItemType(String id) {
        if (id != null && id.matches("[0-9]*")) {
            throw new IllegalArgumentException("Bukkit 1.13+ doesn't support Item-IDs, please use Material names instead");
        } else if (id != null) {
            Material type = Material.matchMaterial(id);
            if (type != null) {
                return type;
            }
        }
        Bukkit.getLogger().log(Level.WARNING, "Unknown material: " + id, new Exception());
        return Material.COBBLESTONE;
    }

    public static List<ItemStack> createItemList(List<String> items) {
        List<ItemStack> itemList = new ArrayList<>();
        for (String reward : items) {
            if (reward != null && !reward.isEmpty()) {
                itemList.add(createItemStackAmount(reward));
            }
        }
        return itemList;
    }

    private static ItemStack createItemStackAmount(String reward) {
        if (reward == null || reward.isEmpty()) {
            return null;
        }
        Matcher m = ITEM_AMOUNT_PATTERN.matcher(reward);
        if (m.matches()) {
            Material type = getItemType(m.group("id"));
            int amount = Integer.parseInt(m.group("amount"), 10);
            String metaStr = m.group("meta");
            ItemStack itemStack = (metaStr == null || metaStr.isBlank()) ? new ItemStack(type, amount) : Bukkit.getItemFactory().createItemStack(type.getKey().toString() + metaStr);
            itemStack.setAmount(amount);
            return itemStack;
        } else {
            throw new IllegalArgumentException("Unknown item: '" + reward + "'");
        }
    }

    public static ItemStack[] createItemArray(List<ItemStack> items) {
        return items != null ? items.toArray(new ItemStack[items.size()]) : new ItemStack[0];
    }

    public static ItemStack createItemStack(String displayItem) {
        return createItemStack(displayItem, null, null);
    }

    public static ItemStack createItemStackSkull(String texture, String name, String description) {
        String metaStr = String.format("{display:{Name:\"%s\"},SkullOwner:{Id:\"%s\",Properties:{textures:[{Value:\"%s\"}]}}}", name, createUniqueId(texture, name, description), texture);
        ItemStack itemStack = (metaStr == null || metaStr.isBlank()) ? new ItemStack(Material.PLAYER_HEAD, 1) : Bukkit.getItemFactory().createItemStack(Material.PLAYER_HEAD.getKey().toString() + metaStr);
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            if (name != null) {
                meta.setDisplayName(FormatUtil.normalize(name));
            }
            List<String> lore = new ArrayList<>();
            if (description != null) {
                lore.addAll(FormatUtil.wordWrap(FormatUtil.normalize(description), 30, 30));
            }
            meta.setLore(lore);
            itemStack.setItemMeta(meta);
        }
        return itemStack;
    }

    public static UUID createUniqueId(String texture, String name, String description) {
        return new UUID(texture.hashCode(), ("" + name + description).hashCode());
    }

    public static ItemStack createItemStack(String displayItem, String name, String description) {
        Material type = Material.DIRT;
        String metaStr = null;
        if (displayItem != null) {
            Matcher matcher = ITEM_PATTERN.matcher(displayItem);
            if (matcher.matches()) {
                type = getItemType(matcher.group("id"));
                metaStr = matcher.group("meta");
            }
        }
        if (type == null) {
            Bukkit.getLogger().warning("Invalid material " + displayItem + " supplied!");
            type = Material.COBBLESTONE;
        }
        ItemStack itemStack = (metaStr == null || metaStr.isBlank()) ? new ItemStack(type, 1) : Bukkit.getItemFactory().createItemStack(type.getKey().toString() + metaStr);
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            if (name != null) {
                meta.setDisplayName(FormatUtil.normalize(name));
            }
            List<String> lore = new ArrayList<>();
            if (description != null) {
                lore.addAll(FormatUtil.wordWrap(FormatUtil.normalize(description), 30, 30));
            }
            meta.setLore(lore);
            itemStack.setItemMeta(meta);
        }
        return itemStack;
    }

    public static List<ItemStack> clone(List<ItemStack> items) {
        if (items == null) {
            return null;
        }
        List<ItemStack> copy = new ArrayList<>();
        for (ItemStack item : items) {
            copy.add(item.clone());
        }
        return copy;
    }

    public static boolean isValidInventoryItem(ItemStack itemStack) {
        return itemStack.getType().isItem();
    }

    public static Builder builder(ItemStack stack) {
        return new Builder(stack);
    }

    public static String asString(ItemStack item) {
        return item.getType().name() + ":" + item.getAmount();
    }

    public static String asShortString(List<ItemStack> items) {
        List<String> shorts = new ArrayList<>();
        for (ItemStack item : items) {
            shorts.add(asShortString(item));
        }
        return "[" + FormatUtil.join(shorts, ", ") + "]";
    }

    public static String asShortString(ItemStack item) {
        if (item == null) {
            return "";
        }
        return item.getAmount() > 1
                ? tr("\u00a7f{0}x \u00a77{1}", item.getAmount(), getItemName(item))
                : tr("\u00a77{0}", getItemName(item));
    }

    public static ItemStack asDisplayItem(ItemStack item) {
        ItemStack copy = new ItemStack(item);
        ItemMeta itemMeta = copy.getItemMeta();
        // Hide all enchants (if possible).
        try {
            Class<?> aClass = Class.forName("org.bukkit.inventory.ItemFlag");
            Object allValues = ReflectionUtil.execStatic(aClass, "values");
            ReflectionUtil.exec(itemMeta, "addItemFlags", allValues);
        } catch (ClassNotFoundException e) {
            // Ignore - only available for 1.9 and above
        }
        copy.setItemMeta(itemMeta);
        return copy;
    }

    public static String getItemName(ItemStack stack) {
        if (stack != null) {
            if (stack.getItemMeta() != null && stack.getItemMeta().getDisplayName() != null && !stack.getItemMeta().getDisplayName().trim().isEmpty()) {
                return stack.getItemMeta().getDisplayName();
            }
            /*
             * Vault isn't 1.13 compatible (yet)
             * ItemInfo itemInfo = Items.itemByStack(stack);
             * return itemInfo != null ? itemInfo.getName() : "" + stack.getType();
             */
            return tr(FormatUtil.camelcase(stack.getType().name()).replaceAll("([A-Z])", " $1").trim());
        }
        return null;
    }

    public static String getMaterialName(Material block) {
        return block == null ? null : tr(FormatUtil.camelcase(block.name()).replaceAll("([A-Z])", " $1").trim());
    }

    /**
     * Builder for ItemStack
     */
    public static class Builder {
        private ItemStack itemStack;

        public Builder(ItemStack itemStack) {
            this.itemStack = itemStack != null ? itemStack.clone() : new ItemStack(Material.AIR);
        }

        public Builder type(Material mat) {
            itemStack.setType(mat);
            return this;
        }

        public Builder amount(int amount) {
            itemStack.setAmount(amount);
            return this;
        }

        public Builder displayName(String name) {
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(name);
            itemStack.setItemMeta(itemMeta);
            return this;
        }

        public Builder enchant(Enchantment enchantment, int level) {
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.addEnchant(enchantment, level, false);
            itemStack.setItemMeta(itemMeta);
            return this;
        }

        public Builder select(boolean b) {
            return b ? select() : deselect();
        }

        public Builder select() {
            return enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1).add(ItemFlag.HIDE_ENCHANTS);
        }

        public Builder deselect() {
            return remove(Enchantment.PROTECTION_ENVIRONMENTAL).remove(ItemFlag.HIDE_ENCHANTS);
        }

        public Builder add(ItemFlag... flags) {
            ItemMeta meta = itemStack.getItemMeta();
            meta.addItemFlags(flags);
            itemStack.setItemMeta(meta);
            return this;
        }

        public Builder remove(ItemFlag... flags) {
            ItemMeta meta = itemStack.getItemMeta();
            meta.removeItemFlags(flags);
            itemStack.setItemMeta(meta);
            return this;
        }

        private Builder remove(Enchantment enchantment) {
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.removeEnchant(enchantment);
            itemStack.setItemMeta(itemMeta);
            return this;
        }

        public Builder lore(String lore) {
            return lore(Collections.singletonList(FormatUtil.normalize(lore)));
        }

        public Builder lore(List<String> lore) {
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta != null) {
                if (itemMeta.getLore() == null) {
                    itemMeta.setLore(lore);
                } else {
                    List<String> oldLore = itemMeta.getLore();
                    oldLore.addAll(lore);
                    itemMeta.setLore(oldLore);
                }
                itemStack.setItemMeta(itemMeta);
            }
            return this;
        }

        public ItemStack build() {
            return itemStack;
        }
    }

    public static class ItemProbability {
        private final double probability;
        private final ItemStack item;
        private final Material[] randomMaterial;

        public ItemProbability(double probability, ItemStack item) {
            this(probability, item, null);
        }

        public ItemProbability(double probability, ItemStack item, Tag<Material> itemTag) {
            this.probability = probability;
            this.item = item;
            this.randomMaterial = itemTag == null ? null : itemTag.getValues().toArray(new Material[0]);
        }

        public double getProbability() {
            return probability;
        }

        public ItemStack getItem() {
            if (randomMaterial != null) {
                ItemStack randomItem = item.clone();
                randomItem.setType(randomMaterial[RANDOM.nextInt(randomMaterial.length)]);
                return randomItem;
            }
            return item;
        }

        @Override
        public String toString() {
            return "ItemProbability{" +
                    "probability=" + probability +
                    ", item=" + item +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ItemProbability that = (ItemProbability) o;
            return Double.compare(that.probability, probability) == 0 &&
                    Objects.equals(asString(item), asString(that.item));
        }

        @Override
        public int hashCode() {
            return Objects.hash(probability, item);
        }
    }
}
