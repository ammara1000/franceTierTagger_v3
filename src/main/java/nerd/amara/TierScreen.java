package nerd.amara;

import me.shedaniel.autoconfig.AutoConfig;
import nerd.amara.tiers.GamemodeType;
import nerd.amara.tiers.PlayerInfo;
import nerd.amara.tiers.TierRank;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.text.Style;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class TierScreen extends Screen {

    private final PlayerInfo playerInfo;
    private Identifier playerAvatarId;
    private boolean avatarLoaded = false;
    
    private static final Set<String> REGISTERED_AVATARS = new HashSet<>();

    public TierScreen(PlayerInfo playerInfo) {
        super(Text.translatable("francetiers_tagger.screen.profile_title", playerInfo.pseudo));
        this.playerInfo = playerInfo;
        
        CompletableFuture.runAsync(() -> {
            try {
                String urlStr = "https://mc-heads.net/body/" + playerInfo.pseudo + "/54";
                byte[] data = Http.getBytes(urlStr, Duration.ofSeconds(5));
                try (ByteArrayInputStream bais = new ByteArrayInputStream(data)) {
                    NativeImage image = NativeImage.read(bais);
                    MinecraftClient.getInstance().execute(() -> {
                        String avatarStr = "avatar_" + playerInfo.pseudo.toLowerCase(Locale.ROOT);
                        this.playerAvatarId = Identifier.of("tier", avatarStr);
                        if (!REGISTERED_AVATARS.contains(avatarStr)) {
                            NativeImageBackedTexture texture = new NativeImageBackedTexture(() -> avatarStr, image);
                            texture.upload();
                            MinecraftClient.getInstance().getTextureManager().registerTexture(this.playerAvatarId, texture);
                            REGISTERED_AVATARS.add(avatarStr);
                        } else {
                            image.close();
                        }
                        this.avatarLoaded = true;
                    });
                }
            } catch (Exception ignored) {
            }
        });
    }

    private int getWindowWidth() {
        return Math.min(350, Math.max(260, this.width - 24));
    }

    private int getWindowHeight() {
        return Math.min(210, Math.max(170, this.height - 36));
    }

    private int getStartX() {
        return (this.width - getWindowWidth()) / 2;
    }

    private int getStartY() {
        return Math.max(6, (this.height - getWindowHeight() - 26) / 2);
    }

    @Override
    protected void init() {
        super.init();
        int windowHeight = getWindowHeight();
        int startY = getStartY();
        int buttonY = Math.min(this.height - 24, startY + windowHeight + 4);
        
        this.addDrawableChild(ButtonWidget.builder(net.minecraft.screen.ScreenTexts.DONE, button -> this.close())
                .dimensions(this.width / 2 - 50, buttonY, 100, 20)
                .build());
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
        
        int windowWidth = getWindowWidth();
        int windowHeight = getWindowHeight();
        int startX = getStartX();
        int startY = getStartY();

        drawRoundedRect(context, startX, startY, windowWidth, windowHeight, 0xFF111219);
        drawRoundedRectBorder(context, startX, startY, windowWidth, windowHeight, 0xFF252636);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int windowWidth = getWindowWidth();
        int windowHeight = getWindowHeight();
        int startX = getStartX();
        int startY = getStartY();

        int leftPaneWidth = Math.max(76, (int) (windowWidth * 0.28f));
        int rightPaneWidth = windowWidth - leftPaneWidth;
        int gridCenterX = startX + leftPaneWidth + rightPaneWidth / 2;
        int avatarX = startX + (leftPaneWidth - 54) / 2;
        int avatarY = startY + 34;
        
        if (this.avatarLoaded && this.playerAvatarId != null) {
            context.drawTexture(net.minecraft.client.gl.RenderPipelines.GUI_TEXTURED, this.playerAvatarId, avatarX, avatarY, 0f, 0f, 54, 120, 54, 120);
        } else {
            context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("francetiers_tagger.screen.loading"), avatarX + 27, avatarY + 54, 0xFFAAAAAA);
        }

        Text titleText = Text.translatable("francetiers_tagger.screen.profile_title", playerInfo.pseudo);
        int titleWidth = this.textRenderer.getWidth(titleText);
        
        if (playerInfo.region != null && !playerInfo.region.isEmpty()) {
            String regionText = playerInfo.region.toUpperCase(Locale.ROOT);
            int regionWidth = this.textRenderer.getWidth(regionText);
            int badgeWidth = regionWidth + 8;
            int badgeHeight = 12;
            int totalWidth = titleWidth + 4 + badgeWidth;
            
            int titleStartX = this.width / 2 - totalWidth / 2;
            context.drawTextWithShadow(this.textRenderer, titleText, titleStartX, startY + 10, 0xFFFFFFFF);
            
            int badgeStartX = titleStartX + titleWidth + 4;
            int badgeY = startY + 9;
            
            int regionColor = switch (regionText) {
                case "EU" -> 0xFF599C4B;
                case "NA" -> 0xFF9F3A44;
                case "AS" -> 0xFFE09F3E;
                case "SA" -> 0xFF3E80E0;
                case "AF" -> 0xFFA0522D;
                case "OC" -> 0xFF800080;
                case "ME" -> 0xFFD4A017;
                default -> 0xFF555555;
            };
            
            drawRoundedRect(context, badgeStartX, badgeY, badgeWidth, badgeHeight, regionColor);
            context.drawText(this.textRenderer, regionText, badgeStartX + 4, badgeY + 2, 0xFFFFFFFF, false);
        } else {
            context.drawCenteredTextWithShadow(this.textRenderer, titleText, this.width / 2, startY + 10, 0xFFFFFFFF);
        }
        
        String rankStr = playerInfo.global_rank != null ? "#" + playerInfo.global_rank : "-";
        String ptsStr = playerInfo.total_points != null ? playerInfo.total_points : "0";
        
        Text rankText = Text.translatable("francetiers_tagger.screen.global_rank", rankStr);
        Text pointsText = Text.translatable("francetiers_tagger.screen.points", ptsStr);
        int pointsWidth = this.textRenderer.getWidth(pointsText);

        context.drawTextWithShadow(this.textRenderer, rankText, startX + 14, startY + windowHeight - 18, 0xFFAAAAAA);
        context.drawTextWithShadow(this.textRenderer, pointsText, startX + windowWidth - pointsWidth - 14, startY + windowHeight - 18, 0xFF55FF55);

        String[] row1 = {"Dia SMP", "Pot", "Nethpot", "SMP"};
        String[] row2 = {"Sword", "Vanilla", "Axe", "UHC"};
        String[] row3 = {"Mace"};
        
        int spacing = Math.min(60, (rightPaneWidth - 20) / 3);
        int rowSpacing = Math.min(45, (windowHeight - 80) / 3);
        int gridY = startY + 38;
        
        List<Text> tooltip = null;
        tooltip = drawRow(context, row1, gridCenterX, gridY, spacing, mouseX, mouseY, tooltip);
        tooltip = drawRow(context, row2, gridCenterX, gridY + rowSpacing, spacing, mouseX, mouseY, tooltip);
        tooltip = drawRow(context, row3, gridCenterX, gridY + rowSpacing * 2, spacing, mouseX, mouseY, tooltip);

        if (tooltip != null) {
            context.drawTooltip(this.textRenderer, tooltip, mouseX, mouseY);
        }
    }

    private record PeakEntry(int badgeStartX, int badgeY, int badgeWidth, int badgeHeight, String peakTierCode, int peakPoints, TierRank peakRank) {}

    private List<Text> drawRow(DrawContext context, String[] modes, int centerX, int y, int spacing, int mouseX, int mouseY, List<Text> currentTooltip) {
        int totalWidth = (modes.length - 1) * spacing;
        int startX = centerX - totalWidth / 2;
        List<Text> tooltip = currentTooltip;
        List<PeakEntry> peaks = new ArrayList<>();
        
        for (int i = 0; i < modes.length; i++) {
            String mode = modes[i];
            int x = startX + i * spacing;
            GamemodeType modeType = GamemodeType.fromName(mode);
            
            String tierText = "-";
            nerd.amara.tiers.Tier matchedTier = null;
            if (playerInfo.tiers != null) {
                for (Map.Entry<String, nerd.amara.tiers.Tier> entry : playerInfo.tiers.entrySet()) {
                    if (GamemodeType.fromName(entry.getKey()) == modeType) {
                        matchedTier = entry.getValue();
                        tierText = entry.getValue().tier;
                        break;
                    }
                }
            }
            
            ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
            boolean isOld = config != null && config.oldIcons;
            Text iconText = modeType.getIconText(isOld);
            int iconWidth = this.textRenderer.getWidth(iconText);
            
            context.drawText(this.textRenderer, iconText, x - iconWidth / 2, y, 0xFFFFFFFF, false);
            
            TierRank rank = TierRank.fromCode(tierText);
            Text badgeText;
            if (rank == TierRank.NA) {
                badgeText = Text.literal("-");
            } else {
                int level = isOld ? 0 : rank.getLevel();
                Identifier badgeFontId = isOld ? FontConstants.FONT_OLD_SMALL_ID : FontConstants.FONT_SMALL_ID;
                String iconChar = modeType.getGamemodeChar(level);
                if (!iconChar.isEmpty()) {
                    badgeText = Text.empty()
                        .append(Text.literal(iconChar).setStyle(Style.EMPTY.withFont(new StyleSpriteSource.Font(badgeFontId))))
                        .append(Text.literal(" " + tierText));
                } else {
                    badgeText = Text.literal(tierText);
                }
            }
            
            boolean hasPeak = false;
            String peakTierCode = null;
            int peakPoints = 0;
            TierRank peakRank = TierRank.NA;

            if (matchedTier != null && matchedTier.peak_tier != null) {
                String pt = matchedTier.peak_tier.trim();
                if (!pt.isEmpty() && !pt.equalsIgnoreCase("0") && !pt.equalsIgnoreCase("N/A") && !pt.equalsIgnoreCase(tierText)) {
                    peakRank = TierRank.fromCode(pt);
                    if (peakRank != TierRank.NA) {
                        hasPeak = true;
                        peakTierCode = pt;
                        peakPoints = matchedTier.peak_points != null ? matchedTier.peak_points : 0;
                    }
                }
            }

            int badgeY = y + 16;
            int textWidth = this.textRenderer.getWidth(badgeText);
            int badgeWidth = Math.max(28, textWidth + 8);
            int badgeHeight = 14;
            int badgeStartX = x - badgeWidth / 2;

            drawBadge(context, badgeStartX, badgeY, badgeWidth, badgeHeight, badgeText, textWidth, rank.getColor());

            if (hasPeak) {
                peaks.add(new PeakEntry(badgeStartX, badgeY, badgeWidth, badgeHeight, peakTierCode, peakPoints, peakRank));
            }
        }

        for (PeakEntry p : peaks) {
            context.drawText(this.textRenderer, Text.literal("★"), p.badgeStartX + p.badgeWidth + 1, p.badgeY + 2, 0xFFFFD700, true);

            if (tooltip == null) {
                int hitLeft = p.badgeStartX;
                int hitRight = p.badgeStartX + p.badgeWidth + 9;
                int hitTop = p.badgeY - 2;
                int hitBottom = p.badgeY + p.badgeHeight + 2;
                if (mouseX >= hitLeft && mouseX <= hitRight && mouseY >= hitTop && mouseY <= hitBottom) {
                    Text peakFormatted = Text.literal(p.peakTierCode).setStyle(Style.EMPTY.withColor(p.peakRank.getColor()));
                    tooltip = List.of(
                        Text.translatable("francetiers_tagger.screen.peak_tier", peakFormatted),
                        Text.translatable("francetiers_tagger.screen.peak_points", String.valueOf(p.peakPoints))
                    );
                }
            }
        }

        return tooltip;
    }

    private void drawBadge(DrawContext context, int startX, int y, int badgeWidth, int badgeHeight, Text text, int textWidth, int color) {
        drawRoundedRect(context, startX, y, badgeWidth, badgeHeight, color | 0xFF000000);
        context.drawText(this.textRenderer, text, startX + (badgeWidth - textWidth) / 2, y + 3, 0xFFFFFFFF, false);
    }

    private void drawRoundedRect(DrawContext context, int x, int y, int width, int height, int color) {
        context.fill(x + 1, y, x + width - 1, y + height, color);
        context.fill(x, y + 1, x + 1, y + height - 1, color);
        context.fill(x + width - 1, y + 1, x + width, y + height - 1, color);
    }

    private void drawRoundedRectBorder(DrawContext context, int x, int y, int width, int height, int color) {
        context.fill(x + 1, y, x + width - 1, y + 1, color);
        context.fill(x + 1, y + height - 1, x + width - 1, y + height, color);
        context.fill(x, y + 1, x + 1, y + height - 1, color);
        context.fill(x + width - 1, y + 1, x + width, y + height - 1, color);
    }
    
    @Override
    public boolean shouldPause() {
        return false;
    }
}