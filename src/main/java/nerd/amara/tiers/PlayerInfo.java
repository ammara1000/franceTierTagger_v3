package nerd.amara.tiers;

import java.util.List;
import java.util.Map;

public class PlayerInfo {
    public String pseudo;
    public String total_points;
    public String global_rank;
    public String avatar_url;
    public Map<String,Tier> tiers;
    public List<Tier> retired_tiers;
    public String background_color;
    public String text_color;
}
