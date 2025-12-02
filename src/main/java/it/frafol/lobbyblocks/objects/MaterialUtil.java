package it.frafol.lobbyblocks.objects;

import lombok.experimental.UtilityClass;
import org.bukkit.Material;

@UtilityClass
public class MaterialUtil {
    public static Material fromString(String name) {
        if (name == null) return null;
        Material m = null;
        try { m = Material.matchMaterial(name); } catch (Throwable ignored) {}
        if (m != null) return m;
        try {
            m = Material.getMaterial(name, true);
        } catch (NoSuchMethodError e) {
            m = Material.getMaterial(name);
        } catch (Throwable ignored) {}
        if (m != null) return m;
        try { return Material.valueOf(name.toUpperCase()); } catch (Exception ignored) {}
        String mod = name.contains(":") ? name.substring(name.indexOf(":")+1) : name;
        mod = mod.toUpperCase().replace(" ", "_");
        try { m = Material.matchMaterial(mod); } catch (Throwable ignored) {}
        if (m != null) return m;
        try { return Material.valueOf(mod); } catch (Exception ignored) {}
        return null;
    }
}
