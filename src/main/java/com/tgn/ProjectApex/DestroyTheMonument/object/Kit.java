package com.tgn.ProjectApex.DestroyTheMonument.object;


import com.tgn.ProjectApex.DestroyTheMonument.Monument;
import com.tgn.ProjectApex.DestroyTheMonument.SBA.SEnch;
import com.tgn.ProjectApex.DestroyTheMonument.SBA.SItem;
import com.tgn.ProjectApex.DestroyTheMonument.SBA.SMeth;
import com.tgn.ProjectApex.DestroyTheMonument.manager.ConfigManager;
import org.bukkit.Color;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.HashMap;

/**
 * Created by morpig on 2/20/14.
 */
public class Kit {
    private Monument plugin;
    private ConfigManager config;
    public static final int UNDEF_STAT = -42;

    /**
     * Turns a string into an actual ItemStack
     * @param s (String) The string to convert
     * @return (ItemStack) The item the string represents. Returns null if the string couldn't be parsed.
     */
    public ItemStack toItemStack(String s) {
        ItemStack is = null;

        try {
            String[] item = s.split(",");

            if (item.length == 1) {
                is = new ItemStack(SItem.toMaterial(item[0].split(";")[0]), 1);
            } else if (item.length == 2) {
                is = new ItemStack(SItem.toMaterial(item[0].split(";")[0]), Integer.parseInt(item[1]));
            } else if (item.length >= 3) {
                is = new ItemStack(SItem.toMaterial(item[0].split(";")[0]), Integer.parseInt(item[1]), Short.parseShort(item[2]));
            }
            if (item.length >= 5) {
                is.addUnsafeEnchantment(SEnch.toEnchantment(item[3]), Integer.parseInt(item[4]));
            }
            if (item.length >= 7) {
                is.addUnsafeEnchantment(SEnch.toEnchantment(item[5]), Integer.parseInt(item[6]));
            }
            if (item.length >= 9) {
                is.addUnsafeEnchantment(SEnch.toEnchantment(item[7]), Integer.parseInt(item[8]));
            }
            if (item.length >= 11) {
                is.addUnsafeEnchantment(SEnch.toEnchantment(item[9]), Integer.parseInt(item[10]));
            }
            if (item.length >= 13) {
                is.addUnsafeEnchantment(SEnch.toEnchantment(item[11]), Integer.parseInt(item[12]));
            }
            if (item.length >= 15) {
                is.addUnsafeEnchantment(SEnch.toEnchantment(item[13]), Integer.parseInt(item[14]));
            }

            if (item[0].split(";").length > 1) {
                ItemMeta im = is.getItemMeta();
                im.setDisplayName(SMeth.setColours(item[0].split(";")[1]));
                is.setItemMeta(im);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return is;
    }

    public void give(Player recipent, GameTeam team) {
        PlayerInventory inv = recipent.getInventory();

        inv.clear();

        for (String s : config.getConfig("kits.yml").getStringList("kit.default.items")) {
            if (s.contains("team_helmet")) {
                ItemStack helmet = toItemStack(s.replace("team_helmet", "leatherhelmet"));
                LeatherArmorMeta im = (LeatherArmorMeta) helmet.getItemMeta();
                if (PlayerMeta.getMeta(recipent).getTeam() == GameTeam.BLUE) {
                    im.setColor(Color.fromRGB(0, 255, 255));
                } else if (PlayerMeta.getMeta(recipent).getTeam() == GameTeam.RED) {
                    im.setColor(Color.fromRGB(255, 0, 0));
                }
            } else if (s.contains("team_chestplate")) {
                ItemStack helmet = toItemStack(s.replace("team_chestplate", "leatherchestplate"));
                LeatherArmorMeta im = (LeatherArmorMeta) helmet.getItemMeta();
                if (PlayerMeta.getMeta(recipent).getTeam() == GameTeam.BLUE) {
                    im.setColor(Color.fromRGB(0, 255, 255));
                } else if (PlayerMeta.getMeta(recipent).getTeam() == GameTeam.RED) {
                    im.setColor(Color.fromRGB(255, 0, 0));
                }
            } else if (s.contains("team_leggings")) {
                ItemStack helmet = toItemStack(s.replace("team_leggings", "leatherleggings"));
                LeatherArmorMeta im = (LeatherArmorMeta) helmet.getItemMeta();
                if (PlayerMeta.getMeta(recipent).getTeam() == GameTeam.BLUE) {
                    im.setColor(Color.fromRGB(0, 255, 255));
                } else if (PlayerMeta.getMeta(recipent).getTeam() == GameTeam.RED) {
                    im.setColor(Color.fromRGB(255, 0, 0));
                }
            } else if (s.contains("team_boots")) {
                ItemStack helmet = toItemStack(s.replace("team_boots", "leatherboots"));
                LeatherArmorMeta im = (LeatherArmorMeta) helmet.getItemMeta();
                if (PlayerMeta.getMeta(recipent).getTeam() == GameTeam.BLUE) {
                    im.setColor(Color.fromRGB(0, 255, 255));
                } else if (PlayerMeta.getMeta(recipent).getTeam() == GameTeam.RED) {
                    im.setColor(Color.fromRGB(255, 0, 0));
                }
            } else {
                ItemStack is = toItemStack(s);

                //Auto-equip Armour
                int itemId = SItem.toId(is.getType());
                if (itemId >= 298 && itemId < 318) {
                    if ((itemId + 2) % 4 == 0) {
                        recipent.getInventory().setHelmet(is);
                    } else if ((itemId + 1) % 4 == 0) {
                        recipent.getInventory().setChestplate(is);
                    } else if (itemId % 4 == 0) {
                        recipent.getInventory().setLeggings(is);
                    } else {
                        recipent.getInventory().setBoots(is);
                    }
                } else {
                    //Add other items.
                    recipent.getInventory().addItem(is);
                }
            }

        }

    }
}
