package nerd.amara;

import nerd.amara.tiers.GamemodeType;
import nerd.amara.tiers.PlayerInfo;
import nerd.amara.tiers.Tier;
import nerd.amara.tiers.TierRank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class ShowedTierTest {

    private ModConfig config;
    private PlayerInfo playerInfo;

    @BeforeEach
    public void setUp() {
        config = new ModConfig();
        config.enabled = true;
        config.gamemode = GamemodeType.ALL;
        config.showKitIcon = true;
        config.oldIcons = false;

        playerInfo = new PlayerInfo();
        playerInfo.pseudo = "TestPlayer";
        playerInfo.tiers = new HashMap<>();
        playerInfo.retired_tiers = new ArrayList<>();
    }

    @Test
    public void testDisabledConfigReturnsNull() {
        config.enabled = false;
        Tier result = ShowedTier.getDisplayedTierObj(playerInfo, config);
        assertNull(result);
        assertEquals("", ShowedTier.showed_tier(playerInfo, config));
    }

    @Test
    public void testModOffReturnsNull() {
        config.gamemode = GamemodeType.MOD_OFF;
        Tier result = ShowedTier.getDisplayedTierObj(playerInfo, config);
        assertNull(result);
        assertEquals("", ShowedTier.showed_tier(playerInfo, config));
    }

    @Test
    public void testNullInfoReturnsNull() {
        assertNull(ShowedTier.getDisplayedTierObj(null, config));
        assertEquals("", ShowedTier.showed_tier(null, config));
    }

    @Test
    public void testBestActiveTierSelectedInAllMode() {
        Tier swordTier = new Tier();
        swordTier.category = "Sword";
        swordTier.tier = "LT3";
        playerInfo.tiers.put("Sword", swordTier);

        Tier potTier = new Tier();
        potTier.category = "NethPot";
        potTier.tier = "HT1";
        playerInfo.tiers.put("NethPot", potTier);

        Tier smpTier = new Tier();
        smpTier.category = "SMP";
        smpTier.tier = "HT2";
        playerInfo.tiers.put("SMP", smpTier);

        Tier result = ShowedTier.getDisplayedTierObj(playerInfo, config);
        assertNotNull(result);
        assertEquals("NethPot", result.category);
        assertEquals("HT1", result.tier);
    }

    @Test
    public void testRetiredTierTakesPrecedenceIfHigher() {
        Tier activeTier = new Tier();
        activeTier.category = "Sword";
        activeTier.tier = "LT2";
        playerInfo.tiers.put("Sword", activeTier);

        Tier retiredTier = new Tier();
        retiredTier.category = "SMP";
        retiredTier.tier = "HT1";
        playerInfo.retired_tiers.add(retiredTier);

        Tier result = ShowedTier.getDisplayedTierObj(playerInfo, config);
        assertNotNull(result);
        assertEquals("SMP", result.category);
        assertEquals("HT1", result.tier);
    }

    @Test
    public void testActiveTierTakesPrecedenceIfRetiredIsLower() {
        Tier activeTier = new Tier();
        activeTier.category = "Sword";
        activeTier.tier = "HT1";
        playerInfo.tiers.put("Sword", activeTier);

        Tier retiredTier = new Tier();
        retiredTier.category = "SMP";
        retiredTier.tier = "LT3";
        playerInfo.retired_tiers.add(retiredTier);

        Tier result = ShowedTier.getDisplayedTierObj(playerInfo, config);
        assertNotNull(result);
        assertEquals("Sword", result.category);
        assertEquals("HT1", result.tier);
    }

    @Test
    public void testSpecificGamemodeFiltering() {
        Tier swordTier = new Tier();
        swordTier.category = "Sword";
        swordTier.tier = "HT1";
        playerInfo.tiers.put("Sword", swordTier);

        Tier potTier = new Tier();
        potTier.category = "NethPot";
        potTier.tier = "LT2";
        playerInfo.tiers.put("NethPot", potTier);

        config.gamemode = GamemodeType.NETHPOT;

        Tier result = ShowedTier.getDisplayedTierObj(playerInfo, config);
        assertNotNull(result);
        assertEquals("NethPot", result.category);
        assertEquals("LT2", result.tier);
    }

    @Test
    public void testCrystalAliasesVanillaInSpecificGamemode() {
        Tier vanillaTier = new Tier();
        vanillaTier.category = "Vanilla";
        vanillaTier.tier = "HT2";
        playerInfo.tiers.put("Vanilla", vanillaTier);

        config.gamemode = GamemodeType.CRYSTAL;

        Tier result = ShowedTier.getDisplayedTierObj(playerInfo, config);
        assertNotNull(result);
        assertEquals("Vanilla", result.category);
        assertEquals("HT2", result.tier);
    }

    @Test
    public void testRetiredCrystalAliasesVanilla() {
        Tier retiredVanilla = new Tier();
        retiredVanilla.category = "Vanilla";
        retiredVanilla.tier = "LT1";
        playerInfo.retired_tiers.add(retiredVanilla);

        config.gamemode = GamemodeType.CRYSTAL;

        Tier result = ShowedTier.getDisplayedTierObj(playerInfo, config);
        assertNotNull(result);
        assertEquals("Vanilla", result.category);
        assertEquals("LT1", result.tier);
    }

    @Test
    public void testFormatBadgeWithAndWithoutKitIcon() {
        Tier potTier = new Tier();
        potTier.category = "NethPot";
        potTier.tier = "HT1";
        playerInfo.tiers.put("NethPot", potTier);

        config.showKitIcon = true;
        String badgeWithIcon = ShowedTier.showed_tier(playerInfo, config);
        TierRank rank = TierRank.HT1;
        String expectedGamemodeChar = GamemodeType.NETHPOT.getGamemodeChar(rank.getLevel());
        assertEquals(rank.getEmoji() + "\uF804" + expectedGamemodeChar, badgeWithIcon);

        config.showKitIcon = false;
        String badgeWithoutIcon = ShowedTier.showed_tier(playerInfo, config);
        assertEquals(rank.getEmoji() + "\uF804", badgeWithoutIcon);
    }

    @Test
    public void testFormatBadgeOldIconsUsesLevelZero() {
        Tier potTier = new Tier();
        potTier.category = "NethPot";
        potTier.tier = "HT1";
        playerInfo.tiers.put("NethPot", potTier);

        config.showKitIcon = true;
        config.oldIcons = true;
        String badgeOld = ShowedTier.showed_tier(playerInfo, config);
        String expectedOldChar = GamemodeType.NETHPOT.getGamemodeChar(0);
        assertEquals(TierRank.HT1.getEmoji() + "\uF804" + expectedOldChar, badgeOld);
    }

    @Test
    public void testIgnoresNAActiveTier() {
        Tier naTier = new Tier();
        naTier.category = "Sword";
        naTier.tier = "N/A";
        playerInfo.tiers.put("Sword", naTier);

        Tier validTier = new Tier();
        validTier.category = "SMP";
        validTier.tier = "LT4";
        playerInfo.tiers.put("SMP", validTier);

        Tier result = ShowedTier.getDisplayedTierObj(playerInfo, config);
        assertNotNull(result);
        assertEquals("SMP", result.category);
        assertEquals("LT4", result.tier);
    }
}
