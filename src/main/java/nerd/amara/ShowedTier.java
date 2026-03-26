package nerd.amara;

import nerd.amara.tiers.PlayerInfo;
import nerd.amara.tiers.Tier;

import java.util.HashMap;
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
    private static Map<String,String> gamemode_emoji=Map.ofEntries(
            Map.entry("Mace", "\uEF00"),
            Map.entry("SMP", "\uEF01"),
            Map.entry("UHC", "\uEF02"),
            Map.entry("Pot", "\uEF03"),
            Map.entry("Crystal", "\uEF04"),
            Map.entry("Sword", "\uEF05"),
            Map.entry("DiaSMP", "\uEF06"),
            Map.entry("NethPot", "\uEF07"),
            Map.entry("Axe", "\uEF08")
    );
    public static String showed_tier(PlayerInfo info){
        ModConfig config = ConfigManager.getConfig();
        if (Objects.equals(config.gamemode, "Mod Off")){
            return "";
        }
        if (info==null){
            return "";
        }
        //System.out.println("--------------1"+info.pseudo);//--------------------
        String best_actual_gamemode=null;
        Integer best_retired_gamemode=null;
        if (!Objects.equals(config.gamemode, "All")) {
            if (info.tiers != null) {
                if (!Objects.equals(info.tiers.get(config.gamemode).tier, "N/A")) {
                    return "\uEEEE\uEEEE\uEEEE\uEEEE" + tiers_emoji.get(info.tiers.get(config.gamemode).tier) + "" + gamemode_emoji.get(info.tiers.get(config.gamemode).category);
                }
            }
            if (info.retired_tiers != null) {
                for (int i = 0; i < info.retired_tiers.size(); i++) {
                    Tier element = info.retired_tiers.get(i);
                    if (Objects.equals(element.category, config.gamemode)) {
                        return "\uEEEE\uEEEE\uEEEE\uEEEE" + tiers_emoji.get(info.retired_tiers.get(i).tier) + "" + gamemode_emoji.get(info.retired_tiers.get(i).category);
                    }
                }
            }
        }
        if (info.tiers!=null) {
            //System.out.println("--------------2" + info.pseudo);//---------------------
            for (String key : info.tiers.keySet()) {
                Tier value = info.tiers.get(key);
                if (best_actual_gamemode==null & !Objects.equals(info.tiers.get(key).tier, "N/A")){
                    best_actual_gamemode = key;
                }
                else {
                    //System.out.println("key: "+key);//---------------------
                    //System.out.println("best: "+best_actual_gamemode);//---------------------
                    //System.out.println(info.tiers.get(best_actual_gamemode));//---------------------
                    //System.out.println(info.tiers.get(key));//---------------------
                    if (!Objects.equals(info.tiers.get(key).tier, "N/A")){
                        if (importance.get(info.tiers.get(best_actual_gamemode).tier) < importance.get(info.tiers.get(key).tier)) {
                            best_actual_gamemode = key;
                        }
                    }
                }
                //System.out.println(best_actual_gamemode);//---------------------
            }
            if (info.retired_tiers != null) {
                //System.out.println("--------------3" + info.pseudo);//---------------------
                for (int i = 0; i < info.retired_tiers.size(); i++) {
                    Tier element = info.retired_tiers.get(i);
                    if (best_retired_gamemode == null){
                        best_retired_gamemode = i;
                    }
                    else{
                        if (importance.get(info.retired_tiers.get(best_retired_gamemode).tier) < importance.get(info.retired_tiers.get(i).tier)) {
                            best_retired_gamemode = i;
                        }
                    }
                }
                //System.out.println(best_retired_gamemode);//---------------------
            }
            //System.out.println("--------------01" + info.pseudo);//---------------------
            if (best_retired_gamemode != null) {
                //System.out.println("--------------4" + info.pseudo);//---------------------
                if (best_actual_gamemode == null){
                    //System.out.println("--------------5" + info.pseudo);//---------------------
                    return "\uEEEE\uEEEE\uEEEE\uEEEE" + tiers_emoji.get( info.retired_tiers.get(best_retired_gamemode).tier ) + "" + gamemode_emoji.get( info.retired_tiers.get(best_retired_gamemode).category );
                }
                else {
                    //System.out.println();//---------------------
                    if (importance.get(info.retired_tiers.get(best_retired_gamemode).tier) > importance.get(info.tiers.get(best_actual_gamemode).tier)) {
                        System.out.println("--------------5" + info.pseudo);//---------------------
                        return "\uEEEE\uEEEE\uEEEE\uEEEE" + tiers_emoji.get( info.retired_tiers.get(best_retired_gamemode).tier ) + "" + gamemode_emoji.get( info.retired_tiers.get(best_retired_gamemode).category );
                    }
                }
            }
            //System.out.println("--------------02" + info.pseudo);//---------------------
            if (best_actual_gamemode != null) {
                //System.out.println("--------------6" + info.pseudo);
                return "\uEEEE\uEEEE\uEEEE\uEEEE" + tiers_emoji.get( info.tiers.get(best_actual_gamemode).tier ) + "" + gamemode_emoji.get( info.tiers.get(best_actual_gamemode).category );
            }
        }
        //System.out.println("--------------7" + info.pseudo);//---------------------
        return "";
    }
    public static String showed_message(PlayerInfo info){
        String msg="";
        msg=msg+"\ued09 FRANCETIERS \ued09\nPLAYER: "+info.pseudo+"\nTOP "+info.global_rank+" ("+info.total_points+" points) \nTIERS:";
        if (info.tiers!=null) {
            for (String key : info.tiers.keySet()) {
                Tier value = info.tiers.get(key);
                if (!Objects.equals(info.tiers.get(key).tier, "N/A")){
                    msg=msg+"\n      "+tiers_emoji.get( value.tier )+" "+gamemode_emoji.get( key )+" "+key;
                }
            }
        }
        else {
            return "PLAYER UNRANKED";
        }
        return msg;
    }
}
