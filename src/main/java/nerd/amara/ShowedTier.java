package nerd.amara;

import nerd.amara.tiers.PlayerInfo;
import nerd.amara.tiers.Tier;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ShowedTier {
    private static Map<String,Integer> importance=Map.ofEntries(
            Map.entry("N/A", 0),
            Map.entry("LT6", 1),
            Map.entry("HT6", 2),
            Map.entry("LT5", 3),
            Map.entry("HT5", 4),
            Map.entry("LT4", 5),
            Map.entry("HT4", 6),
            Map.entry("LT3", 7),
            Map.entry("HT3", 8),
            Map.entry("RLT2", 9),
            Map.entry("LT2", 10),
            Map.entry("RHT2", 11),
            Map.entry("HT2", 12),
            Map.entry("RLT1", 13),
            Map.entry("LT1", 14),
            Map.entry("RHT1", 15),
            Map.entry("HT1", 16)
    );
    private static Map<String,String> tiers_emoji=Map.ofEntries(
            Map.entry("N/A", ""),
            Map.entry("LT5", "\uEE00"),
            Map.entry("HT5", "\uEE01"),
            Map.entry("LT4", "\uEE02"),
            Map.entry("HT4", "\uEE03"),
            Map.entry("LT3", "\uEE04"),
            Map.entry("HT3", "\uEE05"),
            Map.entry("LT2", "\uEE06"),
            Map.entry("HT2", "\uEE07"),
            Map.entry("LT1", "\uEE08"),
            Map.entry("HT1", "\uEE09"),
            Map.entry("RLT2", "\uEE0a"),
            Map.entry("RHT2", "\uEE0b"),
            Map.entry("RLT1", "\uEE0c"),
            Map.entry("RHT1", "\uEE0d")
    );
    private static String getGamemodeChar(String category, int level) {
        int offset = switch (category) {
            case "Mace" -> 0;
            case "SMP" -> 1;
            case "UHC" -> 2;
            case "Pot" -> 3;
            case "Crystal" -> 4;
            case "Sword" -> 5;
            case "DiaSMP" -> 6;
            case "NethPot" -> 7;
            case "Axe" -> 8;
            default -> -1;
        };
        if (offset == -1) return "";
        int base = switch (level) {
            case 5 -> 0xEC00;
            case 4 -> 0xEC10;
            case 3 -> 0xEC20;
            case 2 -> 0xEC30;
            case 1 -> 0xEC40;
            default -> 0xEF00;
        };
        return String.valueOf((char) (base + offset));
    }

    public static String showed_tier(PlayerInfo info){
        ModConfig config = me.shedaniel.autoconfig.AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        ModConfig.Gamemode currentMode = config.gamemode;
        if (currentMode == null) {
            currentMode = ModConfig.Gamemode.ALL;
            config.gamemode = currentMode;
        }
        if (!config.enabled || currentMode == ModConfig.Gamemode.MOD_OFF){
            return "";
        }
        if (info==null){
            return "";
        }

        if (currentMode != ModConfig.Gamemode.ALL) {
            String targetMode = currentMode.getDisplayName();
            if (info.tiers != null && info.tiers.containsKey(targetMode)) {
                Tier specific = info.tiers.get(targetMode);
                if (!Objects.equals(specific.tier, "N/A")) {
                    return formatBadge(specific.tier, specific.category);
                }
            }
            if (info.retired_tiers != null) {
                for (Tier element : info.retired_tiers) {
                    if (Objects.equals(element.category, targetMode)) {
                        return formatBadge(element.tier, element.category);
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
                    return formatBadge(bestRetired.tier, bestRetired.category);
                }
                Tier bestActive = info.tiers.get(bestActiveKey);
                if (importance.get(bestRetired.tier) > importance.get(bestActive.tier)) {
                    return formatBadge(bestRetired.tier, bestRetired.category);
                }
            }

            if (bestActiveKey != null) {
                Tier bestActive = info.tiers.get(bestActiveKey);
                return formatBadge(bestActive.tier, bestActive.category);
            }
        }
        return "";
    }

    public static String showed_message(PlayerInfo info){
        if (info.tiers==null) {
            return "JOUEUR NON CLASSÉ";
        }
        String msg="";
        msg=msg+"\ued09 FRANCETIERS \ued09\nPLAYER: "+info.pseudo+"\nTOP "+info.global_rank+" ("+info.total_points+" points) \nTIERS:";
        boolean hasTier=false;
        for (String key : info.tiers.keySet()) {
            Tier value = info.tiers.get(key);
            if (!Objects.equals(value.tier, "N/A")){
                //msg=msg+"\n      "+tiers_emoji.get( value.tier )+" "+gamemode_emoji.get( key )+" "+key;
                msg=msg+"\n      "+formatBadge(value.tier,key)+" "+key;
                hasTier=true;
            }
        }
        if (!hasTier){
            return "JOUEUR NON CLASSÉ";
        }
        return msg;
    }

    private static String formatBadge(String tier, String category) {
        int imp = importance.getOrDefault(tier, -1);
        int level = 0;
        if (imp >= 0 && imp < 5) level = 5;
        else if (imp >= 5 && imp < 7) level = 4;
        else if (imp >= 7 && imp < 9) level = 3;
        else if (imp >= 9 && imp < 13) level = 2;
        else if (imp >= 13) level = 1;
        
        ModConfig config = me.shedaniel.autoconfig.AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        String gamemodeChar = config.showKitIcon ? getGamemodeChar(category, level) : "";
        return "\uEEEE\uEEEE\uEEEE\uEEEE" + tiers_emoji.getOrDefault(tier, "") + gamemodeChar;
    }

    private static String bestActiveGamemode(Map<String, Tier> tiers) {
        String best = null;
        for (String key : tiers.keySet()) {
            String tier = tiers.get(key).tier;
            if (Objects.equals(tier, "N/A")) {
                continue;
            }
            if (best == null || importance.get(tiers.get(best).tier) < importance.get(tier)) {
                best = key;
            }
        }
        return best;
    }

    private static Integer bestRetiredIndex(List<Tier> retiredTiers) {
        Integer best = null;
        for (int i = 0; i < retiredTiers.size(); i++) {
            String tier = retiredTiers.get(i).tier;
            if (best == null || importance.get(retiredTiers.get(best).tier) < importance.get(tier)) {
                best = i;
            }
        }
        return best;
    }
}