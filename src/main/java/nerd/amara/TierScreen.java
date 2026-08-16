package nerd.amara;

import nerd.amara.tiers.PlayerInfo;
import nerd.amara.tiers.Tier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.Item;
import java.util.Map;

public class TierScreen extends Screen {

    private final PlayerInfo playerInfo;
    private AbstractClientPlayerEntity playerEntity;

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
    }

    @Override
    protected void init() {
        super.init();

        this.addDrawableChild(ButtonWidget.builder(net.minecraft.screen.ScreenTexts.DONE, button -> this.close())
                .dimensions(this.width / 2 - 50, this.height - 30, 100, 20)
                .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int centerX = this.width / 2;
        int listY = (int) (this.height / 2.65);
        int avatarY = this.height / 55 + 12;


        context.drawCenteredTextWithShadow(this.textRenderer, this.title, centerX, this.height / 55, 0xFFFFFFFF);


        if (this.playerEntity != null) {
            InventoryScreen.drawEntity(context, centerX - 120, avatarY + 10, centerX - 60, avatarY + 130, 55, 0.0625f, mouseX, mouseY, this.playerEntity);
        }


        int rightX = centerX;
        context.drawTextWithShadow(this.textRenderer, "Rang Global:", rightX - 44, listY - 40, 0xFFAAAAAA);
        context.drawTextWithShadow(this.textRenderer, playerInfo.global_rank != null ? "#" + playerInfo.global_rank : "N/A", rightX + 30, listY - 40, 0xFFFFAA00);
        
        context.drawTextWithShadow(this.textRenderer, "Points:", rightX - 44, listY - 24, 0xFFAAAAAA);
        context.drawTextWithShadow(this.textRenderer, playerInfo.total_points != null ? playerInfo.total_points : "0", rightX + 30, listY - 24, 0xFF55FF55);


        int yOffset = listY;
        if (playerInfo.tiers != null) {
            for (Map.Entry<String, nerd.amara.tiers.Tier> entry : playerInfo.tiers.entrySet()) {
                String modeName = entry.getKey();
                nerd.amara.tiers.Tier tierInfo = entry.getValue();
                

                Text iconText = getIconForMode(modeName);
                context.drawText(this.textRenderer, iconText, rightX, yOffset + 3, 0xFFFFFFFF, false);


                context.drawTextWithShadow(this.textRenderer, modeName, rightX + 20, yOffset, 0xFFFFFFFF);
                

                String tierText = tierInfo.tier;
                int tierColor = getTierColor(tierInfo.tier);
                int textWidth = this.textRenderer.getWidth(tierText);
                
                context.drawTextWithShadow(this.textRenderer, tierText, rightX + 114 - textWidth, yOffset, tierColor);
                yOffset += 15;
            }
        }
    }
    
    @Override
    public boolean shouldPause() {
        return false;
    }

    private Text getIconForMode(String modeName) {
        if (modeName == null) return Text.empty();
        String lower = modeName.toLowerCase();
        
        String unicode = "\uF005";
        net.minecraft.util.Identifier fontId = net.minecraft.util.Identifier.of("minecraft", "gamemodes/pvptiers");
        
        if (lower.contains("crystal")) unicode = "\uF000";
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
        if (tier == null || tier.equals("N/A")) return 0xFFFF5555;
        if (tier.startsWith("HT1") || tier.startsWith("LT1") || tier.startsWith("RLT1") || tier.startsWith("RHT1")) return 0xFFFF5555;
        if (tier.startsWith("HT2") || tier.startsWith("LT2") || tier.startsWith("RLT2") || tier.startsWith("RHT2")) return 0xFFFFAA00;
        if (tier.startsWith("HT3") || tier.startsWith("LT3")) return 0xFF55FF55;
        if (tier.startsWith("HT4") || tier.startsWith("LT4")) return 0xFF5555FF;
        if (tier.startsWith("HT5") || tier.startsWith("LT5")) return 0xFFAAAAAA;
        return 0xFFAAAAAA;
    }

    private String formatBadge(String tier) {
        return tier;
    }
}
