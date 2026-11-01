package nerd.amara;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import nerd.amara.tiers.GamemodeType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.Set;

public class InventoryChecker {

    public static void checkInventory(Minecraft client, boolean showMessage) {
        if (client.player == null) {
            return;
        }

        GamemodeType detected = detect(client.player.getInventory());
        if (detected != null) {
            ConfigHolder<ModConfig> holder = AutoConfig.getConfigHolder(ModConfig.class);
            ModConfig config = holder.getConfig();
            boolean changed = (config.gamemode != detected);
            if (changed) {
                config.gamemode = detected;
                holder.save();
                Keybinds.refreshAllPlayerTags();
            }
            client.player.sendOverlayMessage(Component.translatable("francetiers_tagger.kit.detected", Component.literal(detected.getDisplayName()).withStyle(s -> s.withColor(ChatFormatting.AQUA).withBold(true))));
        } else if (showMessage) {
            client.player.sendOverlayMessage(Component.translatable("francetiers_tagger.kit.not_found").withStyle(s -> s.withColor(ChatFormatting.RED)));
        }
    }

    public static GamemodeType detect(Inventory inventory) {
        if (checkVanilla(inventory)) return GamemodeType.CRYSTAL;
        if (checkSword(inventory)) return GamemodeType.SWORD;
        if (checkUhc(inventory)) return GamemodeType.UHC;
        if (checkPot(inventory)) return GamemodeType.POT;
        if (checkNethPot(inventory)) return GamemodeType.NETHPOT;
        if (checkSmp(inventory)) return GamemodeType.SMP;
        if (checkAxe(inventory)) return GamemodeType.AXE;
        if (checkMace(inventory)) return GamemodeType.MACE;
        if (checkDiamondSmp(inventory)) return GamemodeType.DIASMP;
        return null;
    }

    private static boolean checkVanilla(Inventory inventory) {
        boolean hasObsidian = false;
        boolean hasCrystal = false;
        boolean hasAnchor = false;
        boolean hasGlowstone = false;
        boolean hasSword = false;
        boolean hasHelmet = false;
        boolean hasChestplate = false;
        boolean hasLeggings = false;
        boolean hasBoots = false;

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);

            hasObsidian |= hasItem(stack, Items.OBSIDIAN);
            hasCrystal |= hasItem(stack, Items.END_CRYSTAL);
            hasAnchor |= hasItem(stack, Items.RESPAWN_ANCHOR);
            hasGlowstone |= hasItem(stack, Items.GLOWSTONE);
            hasSword |= hasItem(stack, Items.NETHERITE_SWORD, true);
            hasHelmet |= hasItem(stack, Items.NETHERITE_HELMET, true);
            hasChestplate |= hasItem(stack, Items.NETHERITE_CHESTPLATE, true);
            hasLeggings |= hasItem(stack, Items.NETHERITE_LEGGINGS, true);
            hasBoots |= hasItem(stack, Items.NETHERITE_BOOTS, true);
        }

        return hasObsidian && hasCrystal && hasAnchor && hasGlowstone && hasSword && hasHelmet && hasChestplate && hasLeggings && hasBoots;
    }

    private static boolean checkSword(Inventory inventory) {
        boolean hasSword = false;
        boolean hasHelmet = false;
        boolean hasChestplate = false;
        boolean hasLeggings = false;
        boolean hasBoots = false;

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);

            hasSword |= hasItem(stack, Items.DIAMOND_SWORD);
            hasHelmet |= hasItem(stack, Items.DIAMOND_HELMET);
            hasChestplate |= hasItem(stack, Items.DIAMOND_CHESTPLATE);
            hasLeggings |= hasItem(stack, Items.DIAMOND_LEGGINGS);
            hasBoots |= hasItem(stack, Items.DIAMOND_BOOTS);

            if (SWORD_NON_ALLOWED.contains(stack.getItem())) return false;
        }

        return hasSword && hasHelmet && hasChestplate && hasLeggings && hasBoots;
    }

    private static boolean checkUhc(Inventory inventory) {
        boolean hasShield = false;
        boolean hasGaps = false;
        boolean hasLava = false;
        boolean hasWater = false;
        boolean hasCobwebs = false;
        boolean hasEnchantedBow = false;
        boolean hasEnchantedCrossbow = false;
        boolean hasEnchantedSword = false;
        boolean hasEnchantedAxe = false;
        boolean hasEnchantedHelmet = false;
        boolean hasEnchantedChestplate = false;
        boolean hasEnchantedLeggings = false;
        boolean hasEnchantedBoots = false;

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);

            hasShield |= hasItem(stack, Items.SHIELD);
            hasGaps |= hasItem(stack, Items.GOLDEN_APPLE);
            hasLava |= hasItem(stack, Items.LAVA_BUCKET);
            hasWater |= hasItem(stack, Items.WATER_BUCKET);
            hasCobwebs |= hasItem(stack, Items.COBWEB);
            hasEnchantedBow |= hasItem(stack, Items.BOW, true);
            hasEnchantedCrossbow |= hasItem(stack, Items.CROSSBOW, true);
            hasEnchantedSword |= hasItem(stack, Items.DIAMOND_SWORD, true);
            hasEnchantedAxe |= hasItem(stack, Items.DIAMOND_AXE, true);
            hasEnchantedHelmet |= hasItem(stack, Items.DIAMOND_HELMET, true);
            hasEnchantedChestplate |= hasItem(stack, Items.DIAMOND_CHESTPLATE, true);
            hasEnchantedLeggings |= hasItem(stack, Items.DIAMOND_LEGGINGS, true) || hasItem(stack, Items.IRON_LEGGINGS, true);
            hasEnchantedBoots |= hasItem(stack, Items.DIAMOND_BOOTS, true);

            if (UHC_NON_ALLOWED.contains(stack.getItem())) return false;
        }

        return hasShield && hasGaps && hasLava && hasWater && hasCobwebs && hasEnchantedBow && hasEnchantedCrossbow && hasEnchantedSword &&
                hasEnchantedAxe && hasEnchantedHelmet && hasEnchantedChestplate && hasEnchantedLeggings && hasEnchantedBoots;
    }

    private static boolean checkPot(Inventory inventory) {
        boolean hasSteak = false;
        boolean hasPotions = false;
        boolean hasEnchantedSword = false;
        boolean hasEnchantedHelmet = false;
        boolean hasEnchantedChestplate = false;
        boolean hasEnchantedLeggings = false;
        boolean hasEnchantedBoots = false;

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);

            hasSteak |= hasItem(stack, Items.COOKED_BEEF);
            hasPotions |= hasItem(stack, Items.SPLASH_POTION);
            hasEnchantedSword |= hasItem(stack, Items.DIAMOND_SWORD, true);
            hasEnchantedHelmet |= hasItem(stack, Items.DIAMOND_HELMET, true);
            hasEnchantedChestplate |= hasItem(stack, Items.DIAMOND_CHESTPLATE, true);
            hasEnchantedLeggings |= hasItem(stack, Items.DIAMOND_LEGGINGS, true);
            hasEnchantedBoots |= hasItem(stack, Items.DIAMOND_BOOTS, true);

            if (POT_NON_ALLOWED.contains(stack.getItem())) return false;
        }

        return hasSteak && hasPotions && hasEnchantedSword && hasEnchantedHelmet && hasEnchantedChestplate && hasEnchantedLeggings && hasEnchantedBoots;
    }

    private static boolean checkNethPot(Inventory inventory) {
        boolean hasGaps = false;
        boolean hasPotions = false;
        boolean hasTotem = false;
        boolean hasXp = false;
        boolean hasEnchantedSword = false;
        boolean hasEnchantedHelmet = false;
        boolean hasEnchantedChestplate = false;
        boolean hasEnchantedLeggings = false;
        boolean hasEnchantedBoots = false;

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);

            hasGaps |= hasItem(stack, Items.GOLDEN_APPLE);
            hasPotions |= hasItem(stack, Items.SPLASH_POTION);
            hasTotem |= hasItem(stack, Items.TOTEM_OF_UNDYING);
            hasXp |= hasItem(stack, Items.EXPERIENCE_BOTTLE);
            hasEnchantedSword |= hasItem(stack, Items.NETHERITE_SWORD, true);
            hasEnchantedHelmet |= hasItem(stack, Items.NETHERITE_HELMET, true);
            hasEnchantedChestplate |= hasItem(stack, Items.NETHERITE_CHESTPLATE, true);
            hasEnchantedLeggings |= hasItem(stack, Items.NETHERITE_LEGGINGS, true);
            hasEnchantedBoots |= hasItem(stack, Items.NETHERITE_BOOTS, true);

            if (NETHPOT_NON_ALLOWED.contains(stack.getItem())) return false;
        }

        return hasGaps && hasPotions && hasTotem && hasXp && hasEnchantedSword && hasEnchantedHelmet &&
                hasEnchantedChestplate && hasEnchantedLeggings && hasEnchantedBoots;
    }

    private static boolean checkSmp(Inventory inventory) {
        boolean hasGaps = false;
        boolean hasPotions = false;
        boolean hasTotem = false;
        boolean hasXp = false;
        boolean hasPearls = false;
        boolean hasShield = false;
        boolean hasEnchantedSword = false;
        boolean hasEnchantedAxe = false;
        boolean hasEnchantedHelmet = false;
        boolean hasEnchantedChestplate = false;
        boolean hasEnchantedLeggings = false;
        boolean hasEnchantedBoots = false;

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);

            hasGaps |= hasItem(stack, Items.GOLDEN_APPLE);
            hasPotions |= hasItem(stack, Items.SPLASH_POTION);
            hasTotem |= hasItem(stack, Items.TOTEM_OF_UNDYING);
            hasXp |= hasItem(stack, Items.EXPERIENCE_BOTTLE);
            hasPearls |= hasItem(stack, Items.ENDER_PEARL);
            hasShield |= hasItem(stack, Items.SHIELD);
            hasEnchantedSword |= hasItem(stack, Items.NETHERITE_SWORD, true);
            hasEnchantedAxe |= hasItem(stack, Items.NETHERITE_AXE, true);
            hasEnchantedHelmet |= hasItem(stack, Items.NETHERITE_HELMET, true);
            hasEnchantedChestplate |= hasItem(stack, Items.NETHERITE_CHESTPLATE, true);
            hasEnchantedLeggings |= hasItem(stack, Items.NETHERITE_LEGGINGS, true);
            hasEnchantedBoots |= hasItem(stack, Items.NETHERITE_BOOTS, true);

            if (SMP_NON_ALLOWED.contains(stack.getItem())) return false;
        }

        return hasGaps && hasPotions && hasTotem && hasXp && hasPearls && hasShield && hasEnchantedAxe &&
                hasEnchantedSword && hasEnchantedHelmet && hasEnchantedChestplate && hasEnchantedLeggings && hasEnchantedBoots;
    }

    private static boolean checkAxe(Inventory inventory) {
        boolean hasBow = false;
        boolean hasCrossbow = false;
        boolean hasShield = false;
        boolean hasSword = false;
        boolean hasAxe = false;
        boolean hasHelmet = false;
        boolean hasChestplate = false;
        boolean hasLeggings = false;
        boolean hasBoots = false;

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);

            hasBow |= hasItem(stack, Items.BOW, false);
            hasCrossbow |= hasItem(stack, Items.CROSSBOW, false);
            hasShield |= hasItem(stack, Items.SHIELD, false);
            hasSword |= hasItem(stack, Items.DIAMOND_SWORD, false);
            hasAxe |= hasItem(stack, Items.DIAMOND_AXE, false);
            hasHelmet |= hasItem(stack, Items.DIAMOND_HELMET, false);
            hasChestplate |= hasItem(stack, Items.DIAMOND_CHESTPLATE, false);
            hasLeggings |= hasItem(stack, Items.DIAMOND_LEGGINGS, false);
            hasBoots |= hasItem(stack, Items.DIAMOND_BOOTS, false);

            if (AXE_NON_ALLOWED.contains(stack.getItem())) return false;
        }

        return hasBow && hasCrossbow && hasShield && hasSword && hasAxe && hasHelmet && hasChestplate && hasLeggings && hasBoots;
    }

    private static boolean checkMace(Inventory inventory) {
        boolean hasGaps = false;
        boolean hasPotions = false;
        boolean hasTotem = false;
        boolean hasPearls = false;
        boolean hasWindCharge = false;
        boolean hasElytra = false;
        boolean hasShield = false;
        boolean hasEnchantedMace = false;
        boolean hasEnchantedSword = false;
        boolean hasEnchantedAxe = false;
        boolean hasEnchantedHelmet = false;
        boolean hasEnchantedChestplate = false;
        boolean hasEnchantedLeggings = false;
        boolean hasEnchantedBoots = false;

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);

            hasGaps |= hasItem(stack, Items.GOLDEN_APPLE);
            hasPotions |= hasItem(stack, Items.SPLASH_POTION);
            hasTotem |= hasItem(stack, Items.TOTEM_OF_UNDYING);
            hasPearls |= hasItem(stack, Items.ENDER_PEARL);
            hasWindCharge |= hasItem(stack, Items.WIND_CHARGE);
            hasElytra |= hasItem(stack, Items.ELYTRA);
            hasShield |= hasItem(stack, Items.SHIELD);
            hasEnchantedMace |= hasItem(stack, Items.MACE, true);
            hasEnchantedSword |= hasItem(stack, Items.NETHERITE_SWORD, true);
            hasEnchantedAxe |= hasItem(stack, Items.NETHERITE_AXE, true);
            hasEnchantedHelmet |= hasItem(stack, Items.NETHERITE_HELMET, true);
            hasEnchantedChestplate |= hasItem(stack, Items.NETHERITE_CHESTPLATE, true);
            hasEnchantedLeggings |= hasItem(stack, Items.NETHERITE_LEGGINGS, true);
            hasEnchantedBoots |= hasItem(stack, Items.NETHERITE_BOOTS, true);

            if (MACE_NON_ALLOWED.contains(stack.getItem())) return false;
        }

        return hasGaps && hasPotions && hasTotem && hasPearls && hasWindCharge && hasElytra && hasShield && hasEnchantedMace &&
                hasEnchantedSword && hasEnchantedAxe && hasEnchantedHelmet && hasEnchantedChestplate && hasEnchantedLeggings && hasEnchantedBoots;
    }

    private static boolean checkDiamondSmp(Inventory inventory) {
        boolean hasGaps = false;
        boolean hasWater = false;
        boolean hasCobweb = false;
        boolean hasChorus = false;
        boolean hasPotions = false;
        boolean hasTotem = false;
        boolean hasXp = false;
        boolean hasPearls = false;
        boolean hasShield = false;
        boolean hasEnchantedSword = false;
        boolean hasEnchantedAxe = false;
        boolean hasEnchantedHelmet = false;
        boolean hasEnchantedChestplate = false;
        boolean hasEnchantedLeggings = false;
        boolean hasEnchantedBoots = false;

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);

            hasGaps |= hasItem(stack, Items.GOLDEN_APPLE);
            hasWater |= hasItem(stack, Items.WATER_BUCKET);
            hasCobweb |= hasItem(stack, Items.COBWEB);
            hasChorus |= hasItem(stack, Items.CHORUS_FRUIT);
            hasPotions |= hasItem(stack, Items.SPLASH_POTION);
            hasTotem |= hasItem(stack, Items.TOTEM_OF_UNDYING);
            hasXp |= hasItem(stack, Items.EXPERIENCE_BOTTLE);
            hasPearls |= hasItem(stack, Items.ENDER_PEARL);
            hasShield |= hasItem(stack, Items.SHIELD);
            hasEnchantedSword |= hasItem(stack, Items.DIAMOND_SWORD, true);
            hasEnchantedAxe |= hasItem(stack, Items.DIAMOND_AXE, true);
            hasEnchantedHelmet |= hasItem(stack, Items.DIAMOND_HELMET, true);
            hasEnchantedChestplate |= hasItem(stack, Items.DIAMOND_CHESTPLATE, true);
            hasEnchantedLeggings |= hasItem(stack, Items.DIAMOND_LEGGINGS, true);
            hasEnchantedBoots |= hasItem(stack, Items.DIAMOND_BOOTS, true);

            if (DIAMOND_SMP_NON_ALLOWED.contains(stack.getItem())) return false;
        }

        return hasGaps && hasWater && hasCobweb && hasChorus && hasPotions && hasTotem && hasXp && hasPearls && hasShield && hasEnchantedAxe &&
                hasEnchantedSword && hasEnchantedHelmet && hasEnchantedChestplate && hasEnchantedLeggings && hasEnchantedBoots;
    }

    private static boolean hasItem(ItemStack stack, Item item, boolean needEnchant) {
        return stack.getItem() == item && (needEnchant == stack.isEnchanted());
    }

    private static boolean hasItem(ItemStack stack, Item item) {
        return stack.getItem() == item;
    }

    private static final Set<Item> SWORD_NON_ALLOWED = Set.of(
            Items.DIAMOND_AXE,
            Items.COBWEB,
            Items.SHIELD,
            Items.ENDER_PEARL,
            Items.EXPERIENCE_BOTTLE,
            Items.SPLASH_POTION,

            Items.NETHERITE_SWORD,
            Items.NETHERITE_AXE,
            Items.NETHERITE_PICKAXE,
            Items.NETHERITE_HELMET,
            Items.NETHERITE_CHESTPLATE,
            Items.NETHERITE_LEGGINGS,
            Items.NETHERITE_BOOTS,

            Items.END_CRYSTAL,
            Items.OBSIDIAN,
            Items.RESPAWN_ANCHOR,
            Items.GLOWSTONE
    );

    private static final Set<Item> UHC_NON_ALLOWED = Set.of(
            Items.NETHERITE_SWORD,
            Items.NETHERITE_AXE,
            Items.NETHERITE_PICKAXE,
            Items.NETHERITE_HELMET,
            Items.NETHERITE_CHESTPLATE,
            Items.NETHERITE_LEGGINGS,
            Items.NETHERITE_BOOTS,

            Items.END_CRYSTAL,
            Items.OBSIDIAN,
            Items.RESPAWN_ANCHOR,
            Items.GLOWSTONE
    );

    private static final Set<Item> POT_NON_ALLOWED = Set.of(
            Items.DIAMOND_AXE,
            Items.GOLDEN_APPLE,
            Items.SHIELD,
            Items.COBWEB,

            Items.NETHERITE_SWORD,
            Items.NETHERITE_AXE,
            Items.NETHERITE_PICKAXE,
            Items.NETHERITE_HELMET,
            Items.NETHERITE_CHESTPLATE,
            Items.NETHERITE_LEGGINGS,
            Items.NETHERITE_BOOTS,

            Items.END_CRYSTAL,
            Items.OBSIDIAN,
            Items.RESPAWN_ANCHOR,
            Items.GLOWSTONE
    );

    private static final Set<Item> NETHPOT_NON_ALLOWED = Set.of(
            Items.ENDER_PEARL,
            Items.SHIELD,

            Items.NETHERITE_AXE,
            Items.NETHERITE_PICKAXE,

            Items.DIAMOND_SWORD,
            Items.DIAMOND_AXE,
            Items.DIAMOND_PICKAXE,
            Items.DIAMOND_HELMET,
            Items.DIAMOND_CHESTPLATE,
            Items.DIAMOND_LEGGINGS,
            Items.DIAMOND_BOOTS,

            Items.END_CRYSTAL,
            Items.OBSIDIAN,
            Items.RESPAWN_ANCHOR,
            Items.GLOWSTONE
    );

    private static final Set<Item> SMP_NON_ALLOWED = Set.of(
            Items.DIAMOND_SWORD,
            Items.DIAMOND_AXE,
            Items.DIAMOND_PICKAXE,
            Items.DIAMOND_HELMET,
            Items.DIAMOND_CHESTPLATE,
            Items.DIAMOND_LEGGINGS,
            Items.DIAMOND_BOOTS,

            Items.NETHERITE_PICKAXE,

            Items.END_CRYSTAL,
            Items.OBSIDIAN,
            Items.RESPAWN_ANCHOR,
            Items.GLOWSTONE
    );

    private static final Set<Item> AXE_NON_ALLOWED = Set.of(
            Items.ENDER_PEARL,
            Items.COBWEB,
            Items.GOLDEN_APPLE,

            Items.NETHERITE_SWORD,
            Items.NETHERITE_AXE,
            Items.NETHERITE_PICKAXE,
            Items.NETHERITE_HELMET,
            Items.NETHERITE_CHESTPLATE,
            Items.NETHERITE_LEGGINGS,
            Items.NETHERITE_BOOTS,

            Items.END_CRYSTAL,
            Items.OBSIDIAN,
            Items.RESPAWN_ANCHOR,
            Items.GLOWSTONE
    );

    private static final Set<Item> MACE_NON_ALLOWED = Set.of(
            Items.EXPERIENCE_BOTTLE,

            Items.NETHERITE_PICKAXE,

            Items.DIAMOND_SWORD,
            Items.DIAMOND_AXE,
            Items.DIAMOND_PICKAXE,
            Items.DIAMOND_HELMET,
            Items.DIAMOND_CHESTPLATE,
            Items.DIAMOND_LEGGINGS,
            Items.DIAMOND_BOOTS,

            Items.END_CRYSTAL,
            Items.OBSIDIAN,
            Items.RESPAWN_ANCHOR,
            Items.GLOWSTONE
    );

    private static final Set<Item> DIAMOND_SMP_NON_ALLOWED = Set.of(
            Items.WIND_CHARGE,

            Items.NETHERITE_SWORD,
            Items.NETHERITE_AXE,
            Items.NETHERITE_HELMET,
            Items.NETHERITE_CHESTPLATE,
            Items.NETHERITE_LEGGINGS,
            Items.NETHERITE_BOOTS,

            Items.END_CRYSTAL,
            Items.OBSIDIAN,
            Items.RESPAWN_ANCHOR,
            Items.GLOWSTONE
    );
}
