package nerd.amara;

import nerd.amara.tiers.PlayerInfo;
import nerd.amara.tiers.Tier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;

import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import net.minecraft.text.Text;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.io.InputStream;
import java.util.Map;

public class TierScreen extends Screen {

    private final PlayerInfo playerInfo;

    private Identifier playerAvatarId;
    private boolean avatarLoaded = false;
    
    private static final java.util.Set<String> REGISTERED_AVATARS = new java.util.HashSet<>();
    
    private String getGamemodeIconChar(String tierText, String mode) {
        if (tierText.equals("-") || tierText.equals("N/A")) return "";
        
        int imp = -1;
        if (tierText.equals("LT6")) imp = 1;
        else if (tierText.equals("HT6")) imp = 2;
        else if (tierText.equals("LT5")) imp = 3;
        else if (tierText.equals("HT5")) imp = 4;
        else if (tierText.equals("LT4")) imp = 5;
        else if (tierText.equals("HT4")) imp = 6;
        else if (tierText.equals("LT3")) imp = 7;
        else if (tierText.equals("HT3")) imp = 8;
        else if (tierText.equals("RLT2")) imp = 9;
        else if (tierText.equals("LT2")) imp = 10;
        else if (tierText.equals("RHT2")) imp = 11;
        else if (tierText.equals("HT2")) imp = 12;
        else if (tierText.equals("RLT1")) imp = 13;
        else if (tierText.equals("LT1")) imp = 14;
        else if (tierText.equals("RHT1")) imp = 15;
        else if (tierText.equals("HT1")) imp = 16;
        
        int level = 0;
        if (imp >= 0 && imp < 5) level = 5;
        else if (imp >= 5 && imp < 7) level = 4;
        else if (imp >= 7 && imp < 9) level = 3;
        else if (imp >= 9 && imp < 13) level = 2;
        else if (imp >= 13) level = 1;
        
        String cleanMode = mode.toLowerCase().replace(" ", "");
        int offset = -1;
        if (cleanMode.equals("mace")) offset = 0;
        else if (cleanMode.equals("smp")) offset = 1;
        else if (cleanMode.equals("uhc")) offset = 2;
        else if (cleanMode.equals("pot")) offset = 3;
        else if (cleanMode.equals("crystal") || cleanMode.equals("vanilla")) offset = 4;
        else if (cleanMode.equals("sword")) offset = 5;
        else if (cleanMode.equals("diasmp")) offset = 6;
        else if (cleanMode.equals("nethpot")) offset = 7;
        else if (cleanMode.equals("axe")) offset = 8;
        
        if (offset == -1) return "";
        
        int base = 0xEF00;
        if (level == 5) base = 0xEC00;
        else if (level == 4) base = 0xEC10;
        else if (level == 3) base = 0xEC20;
        else if (level == 2) base = 0xEC30;
        else if (level == 1) base = 0xEC40;
        
        return String.valueOf((char) (base + offset));
    }

    private net.minecraft.client.network.OtherClientPlayerEntity dummyPlayer;

    public TierScreen(PlayerInfo playerInfo) {
        super(Text.literal("Profil de " + playerInfo.pseudo));
        this.playerInfo = playerInfo;
        
        CompletableFuture.runAsync(() -> {
            try {
                String urlStr = "https://mc-heads.net/body/" + playerInfo.pseudo + "/54";
                URL url = new java.net.URI(urlStr).toURL();
                try (InputStream is = url.openStream()) {
                    NativeImage image = NativeImage.read(is);
                    MinecraftClient.getInstance().execute(() -> {
                        String avatarStr = "avatar_" + playerInfo.pseudo.toLowerCase();
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

    @Override
    protected void init() {
        super.init();
        this.addDrawableChild(ButtonWidget.builder(net.minecraft.screen.ScreenTexts.DONE, button -> this.close())
                .dimensions(this.width / 2 - 50, this.height - 30, 100, 20)
                .build());
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
        
        int windowWidth = 340;
        int windowHeight = 210;
        int startX = (this.width - windowWidth) / 2;
        int startY = (this.height - windowHeight) / 2;

        drawRoundedRect(context, startX, startY, windowWidth, windowHeight, 0xFF111219);
        drawRoundedRectBorder(context, startX, startY, windowWidth, windowHeight, 0xFF252636);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int windowWidth = 340;
        int windowHeight = 210;
        int startX = (this.width - windowWidth) / 2;
        int startY = (this.height - windowHeight) / 2;

        int gridCenterX = startX + 210;
        
        if (this.avatarLoaded && this.playerAvatarId != null) {
            context.drawTexture(net.minecraft.client.gl.RenderPipelines.GUI_TEXTURED, this.playerAvatarId, startX + 23, startY + 40, 0f, 0f, 54, 120, 54, 120);
        } else {
            context.drawCenteredTextWithShadow(this.textRenderer, "Chargement...", startX + 50, startY + 100, 0xFFAAAAAA);
        }

        String titleText = "Profil de " + playerInfo.pseudo;
        int titleWidth = this.textRenderer.getWidth(titleText);
        
        if (playerInfo.region != null && !playerInfo.region.isEmpty()) {
            String regionText = playerInfo.region.toUpperCase();
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
        
        context.drawTextWithShadow(this.textRenderer, "Global: " + rankStr, startX + 15, startY + windowHeight - 20, 0xFFAAAAAA);
        context.drawTextWithShadow(this.textRenderer, "Points: " + ptsStr, startX + windowWidth - 75, startY + windowHeight - 20, 0xFF55FF55);

        String[] row1 = {"Dia SMP", "Pot", "Nethpot", "SMP"};
        String[] row2 = {"Sword", "Vanilla", "Axe", "UHC"};
        String[] row3 = {"Mace"};
        
        int gridY = startY + 45;
        drawRow(context, row1, gridCenterX, gridY);
        drawRow(context, row2, gridCenterX, gridY + 45);
        drawRow(context, row3, gridCenterX, gridY + 90);
    }

    private void drawRow(DrawContext context, String[] modes, int centerX, int y) {
        int spacing = 50;
        int totalWidth = (modes.length - 1) * spacing;
        int startX = centerX - totalWidth / 2;
        
        for (int i = 0; i < modes.length; i++) {
            String mode = modes[i];
            int x = startX + i * spacing;
            
            String tierText = "-";
            if (playerInfo.tiers != null) {
                for (Map.Entry<String, nerd.amara.tiers.Tier> entry : playerInfo.tiers.entrySet()) {
                    String cleanKey = entry.getKey().toLowerCase().replace(" ", "");
                    String cleanMode = mode.toLowerCase().replace(" ", "");
                    if (cleanKey.equals(cleanMode)) {
                        tierText = entry.getValue().tier;
                        break;
                    }
                }
            }
            
            Text iconText = getIconForMode(mode);
            int iconWidth = this.textRenderer.getWidth(iconText);
            
            context.drawText(this.textRenderer, iconText, x - iconWidth / 2, y, 0xFFFFFFFF, false);
            
            Text badgeText;
            if (tierText.equals("-") || tierText.equals("N/A")) {
                badgeText = Text.literal("-");
            } else {
                String iconChar = getGamemodeIconChar(tierText, mode);
                if (!iconChar.isEmpty()) {
                    badgeText = Text.empty()
                        .append(Text.literal(iconChar).setStyle(net.minecraft.text.Style.EMPTY.withFont(new net.minecraft.text.StyleSpriteSource.Font(net.minecraft.util.Identifier.of("frtl", "lol_small")))))
                        .append(Text.literal(" " + tierText));
                } else {
                    badgeText = Text.literal(tierText);
                }
            }
            
            int tierColor = getTierColor(tierText);
            drawBadge(context, x, y + 16, badgeText, tierColor);
        }
    }

    private void drawBadge(DrawContext context, int centerX, int y, Text text, int color) {
        int textWidth = this.textRenderer.getWidth(text);
        int badgeWidth = Math.max(34, textWidth + 12);
        int badgeHeight = 14;
        int startX = centerX - badgeWidth / 2;
        
        drawRoundedRect(context, startX, y, badgeWidth, badgeHeight, color | 0xFF000000);
        context.drawText(this.textRenderer, text, centerX - textWidth / 2, y + 3, 0xFFFFFFFF, false);
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

    private Text getIconForMode(String modeName) {
        if (modeName == null) return Text.empty();
        String lower = modeName.toLowerCase();
        
        String unicode = "\uF005";
        net.minecraft.util.Identifier fontId = net.minecraft.util.Identifier.of("frtl", "gamemodes/mctiers");
        
        if (lower.contains("crystal") || lower.contains("vanilla")) unicode = "\uF000";
        else if (lower.contains("uhc")) unicode = "\uF001";
        else if (lower.contains("nethpot")) unicode = "\uF003";
        else if (lower.contains("pot")) unicode = "\uF002";
        else if (lower.contains("axe")) unicode = "\uF006";
        else if (lower.contains("mace")) unicode = "\uF007";
        else if (lower.contains("smp") && !lower.contains("dia")) unicode = "\uF004";
        else if (lower.contains("diasmp") || lower.contains("diamond")) {
            unicode = "\uF007";
            fontId = net.minecraft.util.Identifier.of("frtl", "gamemodes/subtiers");
        }
        
        return Text.literal(unicode).setStyle(net.minecraft.text.Style.EMPTY.withFont(new net.minecraft.text.StyleSpriteSource.Font(fontId)).withColor(0xFFFFFFFF));
    }

    private int getTierColor(String tier) {
        if (tier == null || tier.equals("-") || tier.equals("N/A")) return 0xFF1F202A;
        if (tier.startsWith("HT1") || tier.startsWith("RHT1")) return 0xFFFFD700;
        if (tier.startsWith("LT1") || tier.startsWith("RLT1")) return 0xFFFFD700;
        if (tier.startsWith("HT2") || tier.startsWith("RHT2")) return 0xFF3399FF;
        if (tier.startsWith("LT2") || tier.startsWith("RLT2")) return 0xFF3399FF;
        if (tier.startsWith("HT3")) return 0xFFFF8C00;
        if (tier.startsWith("LT3")) return 0xFFD98A44;
        if (tier.startsWith("HT4")) return 0xFF8A7272;
        if (tier.startsWith("LT4")) return 0xFF8A7272;
        if (tier.startsWith("HT5") || tier.startsWith("LT5")) return 0xFF505A69;
        return 0xFFAAAAAA;
    }
}
