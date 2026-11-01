package nerd.amara;

import me.shedaniel.autoconfig.AutoConfig;
import nerd.amara.tiers.GamemodeType;
import nerd.amara.tiers.PlayerInfo;
import nerd.amara.tiers.TierRank;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.platform.NativeImage;
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
        super(Component.translatable("francetiers_tagger.screen.profile_title", playerInfo.pseudo));
        this.playerInfo = playerInfo;
        
        CompletableFuture.runAsync(() -> {
            try {
                String urlStr = "https://mc-heads.net/body/" + playerInfo.pseudo + "/54";
                byte[] data = Http.getBytes(urlStr, Duration.ofSeconds(5));
                try (ByteArrayInputStream bais = new ByteArrayInputStream(data)) {
                    NativeImage image = NativeImage.read(bais);
                    Minecraft.getInstance().execute(() -> {
                        String avatarStr = "avatar_" + playerInfo.pseudo.toLowerCase(Locale.ROOT);
                        this.playerAvatarId = Identifier.fromNamespaceAndPath("tier", avatarStr);
                        if (!REGISTERED_AVATARS.contains(avatarStr)) {
                            DynamicTexture texture = new DynamicTexture(() -> avatarStr, image);
                            texture.upload();
                            Minecraft.getInstance().getTextureManager().register(this.playerAvatarId, texture);
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
        
        this.addRenderableWidget(Button.builder(net.minecraft.network.chat.CommonComponents.GUI_DONE, button -> this.onClose())
                .bounds(this.width / 2 - 50, buttonY, 100, 20)
                .build());
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractBackground(context, mouseX, mouseY, delta);
        
        int windowWidth = getWindowWidth();
        int windowHeight = getWindowHeight();
        int startX = getStartX();
        int startY = getStartY();

        drawRoundedRect(context, startX, startY, windowWidth, windowHeight, 0xFF111219);
        drawRoundedRectBorder(context, startX, startY, windowWidth, windowHeight, 0xFF252636);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractRenderState(context, mouseX, mouseY, delta);

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
            context.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, this.playerAvatarId, avatarX, avatarY, 0f, 0f, 54, 120, 54, 120);
        } else {
            context.centeredText(this.font, Component.translatable("francetiers_tagger.screen.loading"), avatarX + 27, avatarY + 54, 0xFFAAAAAA);
        }

        Component titleText = Component.translatable("francetiers_tagger.screen.profile_title", playerInfo.pseudo);
        int titleWidth = this.font.width(titleText);
        
        if (playerInfo.region != null && !playerInfo.region.isEmpty()) {
            String regionText = playerInfo.region.toUpperCase(Locale.ROOT);
            int regionWidth = this.font.width(regionText);
            int badgeWidth = regionWidth + 8;
            int badgeHeight = 12;
            int totalWidth = titleWidth + 4 + badgeWidth;
            
            int titleStartX = this.width / 2 - totalWidth / 2;
            context.text(this.font, titleText, titleStartX, startY + 10, 0xFFFFFFFF);
            
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
            context.text(this.font, regionText, badgeStartX + 4, badgeY + 2, 0xFFFFFFFF, false);
        } else {
            context.centeredText(this.font, titleText, this.width / 2, startY + 10, 0xFFFFFFFF);
        }
        
        String rankStr = playerInfo.global_rank != null ? "#" + playerInfo.global_rank : "-";
        String ptsStr = playerInfo.total_points != null ? playerInfo.total_points : "0";
        
        Component rankText = Component.translatable("francetiers_tagger.screen.global_rank", rankStr);
        Component pointsText = Component.translatable("francetiers_tagger.screen.points", ptsStr);
        int pointsWidth = this.font.width(pointsText);

        context.text(this.font, rankText, startX + 14, startY + windowHeight - 18, 0xFFAAAAAA);
        context.text(this.font, pointsText, startX + windowWidth - pointsWidth - 14, startY + windowHeight - 18, 0xFF55FF55);

        String[] row1 = {"Dia SMP", "Pot", "Nethpot", "SMP"};
        String[] row2 = {"Sword", "Vanilla", "Axe", "UHC"};
        String[] row3 = {"Mace"};
        
        int spacing = Math.min(60, (rightPaneWidth - 20) / 3);
        int rowSpacing = Math.min(45, (windowHeight - 80) / 3);
        int gridY = startY + 38;
        
        List<Component> tooltip = null;
        tooltip = drawRow(context, row1, gridCenterX, gridY, spacing, mouseX, mouseY, tooltip);
        tooltip = drawRow(context, row2, gridCenterX, gridY + rowSpacing, spacing, mouseX, mouseY, tooltip);
        tooltip = drawRow(context, row3, gridCenterX, gridY + rowSpacing * 2, spacing, mouseX, mouseY, tooltip);

        if (tooltip != null) {
            context.setComponentTooltipForNextFrame(this.font, tooltip, mouseX, mouseY);
        }
    }

    private record PeakEntry(int badgeStartX, int badgeY, int badgeWidth, int badgeHeight, String peakTierCode, int peakPoints, TierRank peakRank) {}

    private List<Component> drawRow(GuiGraphicsExtractor context, String[] modes, int centerX, int y, int spacing, int mouseX, int mouseY, List<Component> currentTooltip) {
        int totalWidth = (modes.length - 1) * spacing;
        int startX = centerX - totalWidth / 2;
        List<Component> tooltip = currentTooltip;
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
            Component iconText = modeType.getIconText(isOld);
            int iconWidth = this.font.width(iconText);
            
            context.text(this.font, iconText, x - iconWidth / 2, y, 0xFFFFFFFF, false);
            
            TierRank rank = TierRank.fromCode(tierText);
            Component badgeText;
            if (rank == TierRank.NA) {
                badgeText = Component.literal("-");
            } else {
                int level = isOld ? 0 : rank.getLevel();
                Identifier badgeFontId = isOld ? FontConstants.FONT_OLD_SMALL_ID : FontConstants.FONT_SMALL_ID;
                String iconChar = modeType.getGamemodeChar(level);
                if (!iconChar.isEmpty()) {
                    badgeText = Component.empty()
                        .append(Component.literal(iconChar).setStyle(Style.EMPTY.withFont(new FontDescription.Resource(badgeFontId))))
                        .append(Component.literal(" " + tierText));
                } else {
                    badgeText = Component.literal(tierText);
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
            int textWidth = this.font.width(badgeText);
            int badgeWidth = Math.max(28, textWidth + 8);
            int badgeHeight = 14;
            int badgeStartX = x - badgeWidth / 2;

            drawBadge(context, badgeStartX, badgeY, badgeWidth, badgeHeight, badgeText, textWidth, rank.getColor());

            if (hasPeak) {
                peaks.add(new PeakEntry(badgeStartX, badgeY, badgeWidth, badgeHeight, peakTierCode, peakPoints, peakRank));
            }
        }

        for (PeakEntry p : peaks) {
            context.text(this.font, Component.literal("★"), p.badgeStartX + p.badgeWidth + 1, p.badgeY + 2, 0xFFFFD700, true);

            if (tooltip == null) {
                int hitLeft = p.badgeStartX;
                int hitRight = p.badgeStartX + p.badgeWidth + 9;
                int hitTop = p.badgeY - 2;
                int hitBottom = p.badgeY + p.badgeHeight + 2;
                if (mouseX >= hitLeft && mouseX <= hitRight && mouseY >= hitTop && mouseY <= hitBottom) {
                    Component peakFormatted = Component.literal(p.peakTierCode).setStyle(Style.EMPTY.withColor(p.peakRank.getColor()));
                    tooltip = List.of(
                        Component.translatable("francetiers_tagger.screen.peak_tier", peakFormatted),
                        Component.translatable("francetiers_tagger.screen.peak_points", String.valueOf(p.peakPoints))
                    );
                }
            }
        }

        return tooltip;
    }

    private void drawBadge(GuiGraphicsExtractor context, int startX, int y, int badgeWidth, int badgeHeight, Component text, int textWidth, int color) {
        drawRoundedRect(context, startX, y, badgeWidth, badgeHeight, color | 0xFF000000);
        context.text(this.font, text, startX + (badgeWidth - textWidth) / 2, y + 3, 0xFFFFFFFF, false);
    }

    private void drawRoundedRect(GuiGraphicsExtractor context, int x, int y, int width, int height, int color) {
        context.fill(x + 1, y, x + width - 1, y + height, color);
        context.fill(x, y + 1, x + 1, y + height - 1, color);
        context.fill(x + width - 1, y + 1, x + width, y + height - 1, color);
    }

    private void drawRoundedRectBorder(GuiGraphicsExtractor context, int x, int y, int width, int height, int color) {
        context.fill(x + 1, y, x + width - 1, y + 1, color);
        context.fill(x + 1, y + height - 1, x + width - 1, y + height, color);
        context.fill(x, y + 1, x + 1, y + height - 1, color);
        context.fill(x + width - 1, y + 1, x + width, y + height - 1, color);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
}