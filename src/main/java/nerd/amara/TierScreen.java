package nerd.amara;

import nerd.amara.tiers.PlayerInfo;
import nerd.amara.tiers.Tier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import net.minecraft.text.Text;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.io.InputStream;
import java.util.Map;

public class TierScreen extends Screen {

    private final PlayerInfo playerInfo;
    private AbstractClientPlayerEntity playerEntity;
    private Identifier playerAvatarId;
    private boolean avatarLoaded = false;

    public TierScreen(PlayerInfo playerInfo) {
        super(Text.literal("Profil de " + playerInfo.pseudo));
        this.playerInfo = playerInfo;
        
        if (MinecraftClient.getInstance().world != null) {
            for (AbstractClientPlayerEntity entity : MinecraftClient.getInstance().world.getPlayers()) {
                if (entity.getName().getString().equalsIgnoreCase(playerInfo.pseudo)) {
                    this.playerEntity = entity;
                    break;
                }
            }
        }
        
        if (this.playerEntity == null) {
            CompletableFuture.runAsync(() -> {
                try {
                    String urlStr = "https://mc-heads.net/body/" + playerInfo.pseudo;
                    URL url = new java.net.URI(urlStr).toURL();
                    try (InputStream is = url.openStream()) {
                        NativeImage image = NativeImage.read(is);
                        MinecraftClient.getInstance().execute(() -> {
                            this.playerAvatarId = Identifier.of("tier", "avatar_" + playerInfo.pseudo.toLowerCase());
                            MinecraftClient.getInstance().getTextureManager().registerTexture(this.playerAvatarId, new NativeImageBackedTexture(() -> "avatar_" + playerInfo.pseudo.toLowerCase(), image));
                            this.avatarLoaded = true;
                        });
                    }
                } catch (Exception ignored) {
                }
            });
        }
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

        int gridCenterX = this.width / 2;
        if (this.playerEntity != null) {
            InventoryScreen.drawEntity(context, startX + 50, startY + 160, startX + 50 - mouseX, startY + 160 - 50 - mouseY, 60, 0.0625f, mouseX, mouseY, this.playerEntity);
            gridCenterX = startX + 210;
        } else if (this.avatarLoaded && this.playerAvatarId != null) {
            context.drawTexture(net.minecraft.client.gl.RenderPipelines.GUI_TEXTURED, this.playerAvatarId, startX + 23, startY + 40, 0f, 0f, 54, 120, 108, 240);
            gridCenterX = startX + 210;
        } else {
            gridCenterX = startX + 210;
            context.drawCenteredTextWithShadow(this.textRenderer, "Chargement...", startX + 50, startY + 100, 0xFFAAAAAA);
        }

        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, startY + 10, 0xFFFFFFFF);
        
        String rankStr = playerInfo.global_rank != null ? "#" + playerInfo.global_rank : "N/A";
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
            
            String tierText = "N/A";
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
            
            int tierColor = getTierColor(tierText);
            drawBadge(context, x, y + 16, tierText, tierColor);
        }
    }

    private void drawBadge(DrawContext context, int centerX, int y, String text, int color) {
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
        net.minecraft.util.Identifier fontId = net.minecraft.util.Identifier.of("minecraft", "gamemodes/mctiers");
        
        if (lower.contains("crystal") || lower.contains("vanilla")) unicode = "\uF000";
        else if (lower.contains("uhc")) unicode = "\uF001";
        else if (lower.contains("nethpot")) unicode = "\uF003";
        else if (lower.contains("pot")) unicode = "\uF002";
        else if (lower.contains("axe")) unicode = "\uF006";
        else if (lower.contains("mace")) unicode = "\uF007";
        else if (lower.contains("smp") && !lower.contains("dia")) unicode = "\uF004";
        else if (lower.contains("diasmp") || lower.contains("diamond")) {
            unicode = "\uF007";
            fontId = net.minecraft.util.Identifier.of("minecraft", "gamemodes/subtiers");
        }
        
        return Text.literal(unicode).setStyle(net.minecraft.text.Style.EMPTY.withFont(new net.minecraft.text.StyleSpriteSource.Font(fontId)).withColor(0xFFFFFFFF));
    }

    private int getTierColor(String tier) {
        if (tier == null || tier.equals("N/A")) return 0xFF555555;
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
