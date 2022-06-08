package dk.lockfuglsang.minecraft.nbt;

import static dk.lockfuglsang.minecraft.reflection.ReflectionUtil.exec;
import static dk.lockfuglsang.minecraft.reflection.ReflectionUtil.execStatic;
import static dk.lockfuglsang.minecraft.reflection.ReflectionUtil.getCraftBukkitVersion;
import static dk.lockfuglsang.minecraft.reflection.ReflectionUtil.getPackageName;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.inventory.ItemStack;

/**
 * An NBTItemStackTagger using reflection for CraftBukkit based servers.
 */
public class CraftBukkitNBTTagger implements NBTItemStackTagger {
    private static final Logger log = Logger.getLogger(CraftBukkitNBTTagger.class.getName());

    @Override
    public String getNBTTag(ItemStack itemStack) {
        if (itemStack == null) {
            return "";
        }
        Object nmsItem = execStatic(getCraftItemStackClass(), "asNMSCopy", itemStack);
        Object nbtTag = exec(nmsItem, "getTag");
        return nbtTag != null ? "" + nbtTag : "";
    }

    @Override
    public ItemStack setNBTTag(ItemStack itemStack, String nbtTagString) {
        if (itemStack == null || nbtTagString == null || nbtTagString.isEmpty()) {
            return itemStack;
        }
        Object nmsItem = execStatic(getCraftItemStackClass(), "asNMSCopy", itemStack);
        Object nbtTag = execStatic(getNBTTagParser(), "a", nbtTagString);
        exec(nmsItem, "c", nbtTag);
        Object item = execStatic(getCraftItemStackClass(), "asBukkitCopy", nmsItem);
        if (item instanceof ItemStack) {
            return (ItemStack) item;
        }
        return itemStack;
    }

    @Override
    public ItemStack addNBTTag(ItemStack itemStack, String nbtTagString) {
        if (itemStack == null || nbtTagString == null || nbtTagString.isEmpty()) {
            return itemStack;
        }
        Object nmsItem = execStatic(getCraftItemStackClass(), "asNMSCopy", itemStack);
        Object nbtTag = exec(nmsItem, "s");
        Object nbtTagNew = execStatic(getNBTTagParser(), "a", nbtTagString);
        nbtTag = merge(nbtTagNew, nbtTag);
        exec(nmsItem, "c", nbtTag);
        Object item = execStatic(getCraftItemStackClass(), "asBukkitCopy", nmsItem);
        if (item instanceof ItemStack) {
            return (ItemStack) item;
        }
        return itemStack;
    }

    /**
     * Merges two NBTTagCompound objects
     */
    private static Object merge(Object src, Object tgt) {
        if (tgt == null) {
            return src;
        }
        try {
            Field mapField = src.getClass().getDeclaredField("x");
            mapField.setAccessible(true);
            Map<String, Object> map = (Map<String, Object>) mapField.get(src);
            Class<?> NBTBase = Class.forName(getPackageName(tgt) + ".NBTBase");
            for (String key : map.keySet()) {
                Object val = exec(src, "c", new Class[] { String.class }, key);
                exec(tgt, "a", new Class[] { String.class, NBTBase }, key, val);
            }
            return tgt;
        } catch (IllegalAccessException | ClassNotFoundException | NoSuchFieldException e) {
            log.log(Level.WARNING, "Unable to transfer NBTTag from " + src + " to " + tgt + ": ", e);
        }
        return tgt;
    }

    private static Class<?> getNBTTagParser() {
        try {
            return Class.forName("net.minecraft.nbt.MojangsonParser");
        } catch (ClassNotFoundException e) {
            log.log(Level.WARNING, "Unable to instantiate MojangsonParser: ", e);
        }
        return null;
    }

    private static Class<?> getCraftItemStackClass() {
        String version = getCraftBukkitVersion();
        try {
            return Class.forName("org.bukkit.craftbukkit." + version + ".inventory.CraftItemStack");
        } catch (Exception e) {
            log.log(Level.WARNING, "Unable to find CraftItemStack: ", e);
        }
        return null;
    }
}
