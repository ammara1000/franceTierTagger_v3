package nerd.amara;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;

public class UpdateNotification {
    private static String latestVersion = null;
    private static String currentVersion = null;
    private static String updateUrl = "https://modrinth.com/mod/FranceTiersTagger";
    private static long startTime = 0;
    private static boolean dismissed = false;
    private static boolean usedWalksy = false;

    public static final int HEIGHT = 20;
    private static final int DURATION_MS = 5000;
    private static final int ANIM_MS = 350;
    private static final float TEXT_SCALE = 0.65f;

    private static int currentRenderedX = -1000;
    private static float animatedY = 8f;

    private static String pendingLatest = null;
    private static String pendingCurrent = null;
    private static String pendingUrl = null;
    private static boolean hasShownInGame = false;

    public static void setPending(String latest, String current, String url) {
        pendingLatest = latest;
        pendingCurrent = current;
        pendingUrl = url;
    }

    public static void triggerPendingIfAny() {
        if (!hasShownInGame && pendingLatest != null) {
            hasShownInGame = true;
            trigger(pendingLatest, pendingCurrent, pendingUrl);
            pendingLatest = null;
            pendingCurrent = null;
            pendingUrl = null;
        }
    }

    public static void trigger(String latest, String current, String url) {
        latestVersion = latest;
        currentVersion = current;
        if (url != null && !url.isEmpty()) {
            updateUrl = url;
        }
        dismissed = false;
        if (tryWalksyToast(latest, current, updateUrl)) {
            usedWalksy = true;
            return;
        }
        usedWalksy = false;
        startTime = System.currentTimeMillis();
        animatedY = 8f;
    }

    public static boolean isVisible() {
        if (usedWalksy || dismissed || latestVersion == null || startTime == 0) {
            return false;
        }
        return (System.currentTimeMillis() - startTime) < (DURATION_MS + ANIM_MS);
    }

    public static void openModrinth() {
        dismissed = true;
        try {
            Util.getOperatingSystem().open(URI.create(updateUrl));
        } catch (Exception ignored) {
        }
    }

    public static void dismiss() {
        dismissed = true;
    }

    public static int getX() {
        return currentRenderedX;
    }

    public static int getY() {
        return Math.round(animatedY);
    }

    public static int getWidth() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.textRenderer == null) {
            return 120;
        }
        Text title = getTitle();
        Text sub = getSubtitle();
        int tWidth = client.textRenderer.getWidth(title);
        int sWidth = client.textRenderer.getWidth(sub);
        int maxTextWidth = Math.round(Math.max(tWidth, sWidth) * TEXT_SCALE);
        return maxTextWidth + 26;
    }

    public static void render(DrawContext context, int mouseX, int mouseY) {
        if (usedWalksy || !isVisible()) {
            currentRenderedX = -1000;
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        int screenWidth = client.getWindow().getScaledWidth();
        int width = getWidth();
        int targetX = screenWidth - width - 8;

        int targetY = 8;
        animatedY += (targetY - animatedY) * 0.25f;
        int y = Math.round(animatedY);

        long elapsed = System.currentTimeMillis() - startTime;
        float xPos = targetX;
        if (elapsed < ANIM_MS) {
            float progress = (float) elapsed / ANIM_MS;
            float ease = 1.0f - (float) Math.pow(1.0f - progress, 3);
            xPos = screenWidth + (targetX - screenWidth) * ease;
        } else if (elapsed > DURATION_MS) {
            float progress = (float) (elapsed - DURATION_MS) / ANIM_MS;
            float ease = (float) Math.pow(Math.min(1.0f, progress), 2);
            xPos = targetX + (screenWidth + 15 - targetX) * ease;
        }

        int x = Math.round(xPos);
        currentRenderedX = x;

        boolean hovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + HEIGHT;
        if (hovered && elapsed < DURATION_MS) {
            startTime = System.currentTimeMillis() - ANIM_MS - 50;
        }

        drawModernPill(context, x, y, width, HEIGHT, 0xCC0F172A, 0x4560A5FA);

        int iconCX = x + 10;
        int iconCY = y + 10;
        drawCircle(context, iconCX, iconCY, 5, 0xFF0284C7);
        drawDownloadIcon(context, iconCX, iconCY);

        Text title = getTitle();
        Text subtitle = getSubtitle();

        context.getMatrices().pushMatrix();
        context.getMatrices().translate((float) (x + 18), (float) (y + 3));
        context.getMatrices().scale(TEXT_SCALE, TEXT_SCALE);
        context.drawText(client.textRenderer, title, 0, 0, 0xFFFFFFFF, false);
        context.drawText(client.textRenderer, subtitle, 0, 11, 0xFF94A3B8, false);
        context.getMatrices().popMatrix();
    }

    private static void drawModernPill(DrawContext context, int x, int y, int width, int height, int bgColor, int borderColor) {
        float r = height / 2.0f;
        for (int i = 0; i < height; i++) {
            float dy = (i + 0.5f) - r;
            float halfW = (float) Math.sqrt(Math.max(0, r * r - dy * dy));
            int rowStart = Math.round(x + r - halfW);
            int rowEnd = Math.round(x + width - r + halfW);
            if (rowEnd > rowStart) {
                context.fill(rowStart, y + i, rowEnd, y + i + 1, bgColor);
                if (borderColor != 0) {
                    context.fill(rowStart, y + i, rowStart + 1, y + i + 1, borderColor);
                    context.fill(rowEnd - 1, y + i, rowEnd, y + i + 1, borderColor);
                }
            }
        }
        if (borderColor != 0) {
            int topStart = Math.round(x + r);
            int topEnd = Math.round(x + width - r);
            context.fill(topStart, y, topEnd, y + 1, borderColor);
            context.fill(topStart, y + height - 1, topEnd, y + height, borderColor);
        }
    }

    private static void drawCircle(DrawContext context, int cx, int cy, int radius, int color) {
        for (int dy = -radius; dy <= radius; dy++) {
            float halfW = (float) Math.sqrt(Math.max(0, radius * radius - dy * dy));
            int x1 = Math.round(cx - halfW);
            int x2 = Math.round(cx + halfW);
            context.fill(x1, cy + dy, x2, cy + dy + 1, color);
        }
    }

    private static void drawDownloadIcon(DrawContext context, int cx, int cy) {
        context.fill(cx - 1, cy - 3, cx + 1, cy + 1, 0xFFFFFFFF);
        context.fill(cx - 2, cy - 1, cx - 1, cy, 0xFFFFFFFF);
        context.fill(cx + 1, cy - 1, cx + 2, cy, 0xFFFFFFFF);
        context.fill(cx - 2, cy + 2, cx + 2, cy + 3, 0xFFFFFFFF);
    }

    private static boolean tryWalksyToast(String latest, String current, String url) {
        if (!FabricLoader.getInstance().isModLoaded("walksylib")) {
            return false;
        }
        try {
            Class<?> wlClass = Class.forName("main.walksy.lib.core.WalksyLib");
            Object wl = null;
            try {
                wl = wlClass.getMethod("getInstanceOrNull").invoke(null);
            } catch (Throwable ignored) {
            }
            if (wl == null) {
                try {
                    wl = wlClass.getMethod("getInstance").invoke(null);
                } catch (Throwable ignored) {
                }
            }
            if (wl == null) {
                return false;
            }
            Object toastMgr = wlClass.getMethod("getToastManager").invoke(wl);
            if (toastMgr == null) {
                return false;
            }

            Class<?> wlToastClass = Class.forName("main.walksy.lib.core.toast.WLToast");
            String titleStr = Text.translatable("francetiers_tagger.update.title", latest != null ? latest : "").getString();
            String subStr = Text.translatable("francetiers_tagger.update.subtitle", current != null ? current : "").getString();

            Method ofMethod = wlToastClass.getMethod("of", String.class, String.class);
            Object toast = ofMethod.invoke(null, titleStr, subStr);

            try {
                Class<?> iconsClass = Class.forName("main.walksy.lib.core.render.texture.Icons");
                Field updateIconField = iconsClass.getField("UPDATE");
                Object updateIcon = updateIconField.get(null);
                Class<?> iconTypeClass = updateIconField.getType();
                Method iconMethod = wlToastClass.getMethod("icon", iconTypeClass);
                toast = iconMethod.invoke(toast, updateIcon);
            } catch (Throwable ignored) {
            }

            try {
                Method idMethod = wlToastClass.getMethod("id", Object.class);
                toast = idMethod.invoke(toast, "francetiers_tagger:update");
            } catch (Throwable ignored) {
            }

            try {
                Method secondsMethod = wlToastClass.getMethod("seconds", float.class);
                toast = secondsMethod.invoke(toast, 5.0f);
            } catch (Throwable ignored) {
            }

            try {
                Method onDismissedMethod = wlToastClass.getMethod("onDismissed", Runnable.class);
                toast = onDismissedMethod.invoke(toast, (Runnable) UpdateNotification::openModrinth);
            } catch (Throwable ignored) {
            }

            try {
                Method infoMethod = wlToastClass.getMethod("info");
                toast = infoMethod.invoke(toast);
            } catch (Throwable ignored) {
            }

            Method submitMethod = toastMgr.getClass().getMethod("submit", wlToastClass);
            submitMethod.invoke(toastMgr, toast);
            return true;
        } catch (Throwable t) {
            Francetiers_tagger.LOGGER.debug("WalksyLib toast failed to submit: {}", t.getMessage());
            return false;
        }
    }

    private static Text getTitle() {
        String ver = latestVersion != null ? latestVersion : "";
        return Text.translatable("francetiers_tagger.update.title", ver);
    }

    private static Text getSubtitle() {
        String cur = currentVersion != null ? currentVersion : "";
        return Text.translatable("francetiers_tagger.update.subtitle", cur);
    }
}
