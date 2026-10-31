package nerd.amara.tiers;

public enum TierRank {
    NA("N/A", 0, "", 0, 0xFF1F202A),
    LT6("LT6", 1, "", 5, 0xFF505A69),
    HT6("HT6", 2, "", 5, 0xFF505A69),
    LT5("LT5", 3, "\uEE00", 5, 0xFF505A69),
    HT5("HT5", 4, "\uEE01", 5, 0xFF505A69),
    LT4("LT4", 5, "\uEE02", 4, 0xFF8A7272),
    HT4("HT4", 6, "\uEE03", 4, 0xFF8A7272),
    LT3("LT3", 7, "\uEE04", 3, 0xFFD98A44),
    HT3("HT3", 8, "\uEE05", 3, 0xFFFF8C00),
    RLT2("RLT2", 9, "\uEE0a", 2, 0xFF3399FF),
    LT2("LT2", 10, "\uEE06", 2, 0xFF3399FF),
    RHT2("RHT2", 11, "\uEE0b", 2, 0xFF3399FF),
    HT2("HT2", 12, "\uEE07", 2, 0xFF3399FF),
    RLT1("RLT1", 13, "\uEE0c", 1, 0xFFFFD700),
    LT1("LT1", 14, "\uEE08", 1, 0xFFFFD700),
    RHT1("RHT1", 15, "\uEE0d", 1, 0xFFFFD700),
    HT1("HT1", 16, "\uEE09", 1, 0xFFFFD700);

    private final String code;
    private final int importance;
    private final String emoji;
    private final int level;
    private final int color;

    TierRank(String code, int importance, String emoji, int level, int color) {
        this.code = code;
        this.importance = importance;
        this.emoji = emoji;
        this.level = level;
        this.color = color;
    }

    public String getCode() {
        return code;
    }

    public int getImportance() {
        return importance;
    }

    public String getEmoji() {
        return emoji;
    }

    public int getLevel() {
        return level;
    }

    public int getColor() {
        return color;
    }

    public static TierRank fromCode(String code) {
        if (code == null || code.isEmpty() || code.equals("-")) {
            return NA;
        }
        for (TierRank rank : values()) {
            if (rank.code.equalsIgnoreCase(code)) {
                return rank;
            }
        }
        return NA;
    }
}
