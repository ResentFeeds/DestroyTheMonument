package com.tgn.ProjectApex.DestroyTheMonument.SBA;

import org.bukkit.enchantments.Enchantment;

/**
 * Created by morpig on 2/20/14.
 */
public class SEnch {

    public static Enchantment toEnchantment(String trivialName) {

        switch (trivialName.toLowerCase()) {
            case "protection": return Enchantment.PROTECTION_ENVIRONMENTAL;
            case "fireprotection": return Enchantment.PROTECTION_FIRE;
            case "featherfalling": return Enchantment.PROTECTION_FALL;
            case "blastprotection": return Enchantment.PROTECTION_EXPLOSIONS;
            case "projectileprotection": return Enchantment.PROTECTION_PROJECTILE;
            case "respiration": return Enchantment.OXYGEN;
            case "aquaaffinity": return Enchantment.WATER_WORKER;
            case "thorns": return Enchantment.THORNS;
            case "sharpness": return Enchantment.DAMAGE_ALL;
            case "smite": return Enchantment.DAMAGE_UNDEAD;
            case "baneofarthropods": return Enchantment.DAMAGE_ARTHROPODS;
            case "knockback": return Enchantment.KNOCKBACK;
            case "fireaspect": return Enchantment.FIRE_ASPECT;
            case "looting": return Enchantment.LOOT_BONUS_MOBS;
            case "efficiency": return Enchantment.DIG_SPEED;
            case "silktouch": return Enchantment.SILK_TOUCH;
            case "unbreaking": return Enchantment.DURABILITY;
            case "fortune": return Enchantment.LOOT_BONUS_BLOCKS;
            case "power": return Enchantment.ARROW_DAMAGE;
            case "punch": return Enchantment.ARROW_KNOCKBACK;
            case "flame": return Enchantment.ARROW_FIRE;
            case "infinity": return Enchantment.ARROW_INFINITE;
            default:
                return null;
        }

    }

    public static Enchantment toEnchantment(int enchantmentId) {

        switch (enchantmentId) {
            case 0: return Enchantment.PROTECTION_ENVIRONMENTAL;
            case 1: return Enchantment.PROTECTION_FIRE;
            case 2: return Enchantment.PROTECTION_FALL;
            case 3: return Enchantment.PROTECTION_EXPLOSIONS;
            case 4: return Enchantment.PROTECTION_PROJECTILE;
            case 5: return Enchantment.OXYGEN;
            case 6: return Enchantment.WATER_WORKER;
            case 7: return Enchantment.THORNS;
            case 16: return Enchantment.DAMAGE_ALL;
            case 17: return Enchantment.DAMAGE_UNDEAD;
            case 18: return Enchantment.DAMAGE_ARTHROPODS;
            case 19: return Enchantment.KNOCKBACK;
            case 20: return Enchantment.FIRE_ASPECT;
            case 21: return Enchantment.LOOT_BONUS_MOBS;
            case 32: return Enchantment.DIG_SPEED;
            case 33: return Enchantment.SILK_TOUCH;
            case 34: return Enchantment.DURABILITY;
            case 35: return Enchantment.LOOT_BONUS_BLOCKS;
            case 48: return Enchantment.ARROW_DAMAGE;
            case 49: return Enchantment.ARROW_KNOCKBACK;
            case 50: return Enchantment.ARROW_FIRE;
            case 51: return Enchantment.ARROW_INFINITE;
            default:
                return null;
        }

    }
}