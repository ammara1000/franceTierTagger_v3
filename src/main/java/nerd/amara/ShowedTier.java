package nerd.amara;

import me.shedaniel.autoconfig.AutoConfig;
import nerd.amara.tiers.GamemodeType;
import nerd.amara.tiers.PlayerInfo;
import nerd.amara.tiers.Tier;
import nerd.amara.tiers.TierRank;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ShowedTier {

    public static Tier getDisplayedTierObj(PlayerInfo info) {
        ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        return getDisplayedTierObj(info, config);
    }

    public static Tier getDisplayedTierObj(PlayerInfo info, ModConfig config) {
        if (config == null || !config.enabled || info == null) {
            return null;
        }

        GamemodeType currentMode = config.gamemode != null ? config.gamemode : GamemodeType.ALL;
        if (currentMode == GamemodeType.MOD_OFF) {
            return null;
        }

        if (currentMode != GamemodeType.ALL) {
            String targetMode = currentMode.getDisplayName();
            if (info.tiers != null) {
                Tier specific = info.tiers.get(targetMode);
                if (specific == null && currentMode == GamemodeType.CRYSTAL) {
                    specific = info.tiers.get("Vanilla");
                }
                if (specific != null && !Objects.equals(specific.tier, "N/A")) {
                    return specific;
                }
            }
            if (info.retired_tiers != null) {
                for (Tier element : info.retired_tiers) {
                    if (Objects.equals(element.category, targetMode) ||
                       (currentMode == GamemodeType.CRYSTAL && Objects.equals(element.category, "Vanilla"))) {
                        return element;
                    }
                }
            }
        }

        if (info.tiers != null) {
            String bestActiveKey = bestActiveGamemode(info.tiers);
            Integer bestRetiredIdx = info.retired_tiers != null ? bestRetiredIndex(info.retired_tiers) : null;

            if (bestRetiredIdx != null) {
                Tier bestRetired = info.retired_tiers.get(bestRetiredIdx);
                if (bestActiveKey == null) {
                    return bestRetired;
                }
                Tier bestActive = info.tiers.get(bestActiveKey);
                if (TierRank.fromCode(bestRetired.tier).getImportance() > TierRank.fromCode(bestActive.tier).getImportance()) {
                    return bestRetired;
                }
            }

            if (bestActiveKey != null) {
                return info.tiers.get(bestActiveKey);
            }
        }
        return null;
    }

    public static String showed_tier(PlayerInfo info) {
        ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        return showed_tier(info, config);
    }

    public static String showed_tier(PlayerInfo info, ModConfig config) {
        Tier best = getDisplayedTierObj(info, config);
        if (best != null) {
            return formatBadge(best.tier, best.category, config);
        }
        return "";
    }

    private static String formatBadge(String tier, String category, ModConfig config) {
        TierRank rank = TierRank.fromCode(tier);
        int level = (config != null && config.oldIcons) ? 0 : rank.getLevel();
        GamemodeType mode = GamemodeType.fromName(category);
        String gamemodeChar = (config != null && config.showKitIcon) ? mode.getGamemodeChar(level) : "";
        return rank.getEmoji() + "\uF804" + gamemodeChar;
    }

    private static String bestActiveGamemode(Map<String, Tier> tiers) {
        String best = null;
        for (String key : tiers.keySet()) {
            String tier = tiers.get(key).tier;
            if (Objects.equals(tier, "N/A")) {
                continue;
            }
            if (best == null || TierRank.fromCode(tiers.get(best).tier).getImportance() < TierRank.fromCode(tier).getImportance()) {
                best = key;
            }
        }
        return best;
    }

    private static Integer bestRetiredIndex(List<Tier> retiredTiers) {
        Integer best = null;
        for (int i = 0; i < retiredTiers.size(); i++) {
            String tier = retiredTiers.get(i).tier;
            if (best == null || TierRank.fromCode(retiredTiers.get(best).tier).getImportance() < TierRank.fromCode(tier).getImportance()) {
                best = i;
            }
        }
        return best;
    }
}
