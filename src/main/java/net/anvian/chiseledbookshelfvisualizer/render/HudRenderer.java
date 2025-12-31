package net.anvian.chiseledbookshelfvisualizer.render;

import net.anvian.chiseledbookshelfvisualizer.ChiseledBookshelfVisualizerClient;
import net.anvian.chiseledbookshelfvisualizer.data.BookData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;


@Environment(EnvType.CLIENT)
public class HudRenderer {
    private static boolean renderCrosshair = true;
    private static float scale = ChiseledBookshelfVisualizerClient.CONFIG.hudScale;

    public static void updateScale(float newScale) {
        scale = newScale;
    }

    public static void toggleCrosshair() {
        renderCrosshair = !renderCrosshair;
    }

    public static boolean shouldRenderCrosshair() {
        return renderCrosshair;
    }

    public static void hudRender(DrawContext context, MinecraftClient client) {
        if (shouldRenderCrosshair()) {
            if (!ChiseledBookshelfVisualizerClient.modAvailable) return;

            if (client.options.hudHidden) return;

            if (client.currentScreen != null) return;

            if (ChiseledBookshelfVisualizerClient.bookShelfData.isCurrentBookDataToggled) {
                final BookData currentBookData = ChiseledBookshelfVisualizerClient.currentBookData;
                int screenWidth = client.getWindow().getScaledWidth();
                int screenHeight = client.getWindow().getScaledHeight();
                int x = screenWidth / 2;
                int y = screenHeight / 2;
                final ItemStack itemStack = currentBookData.itemStack;
                int color = 0xFFFFFFFF;
                if (itemStack.getRarity().getFormatting().getColorValue() != null) {
                    color = itemStack.getRarity().getFormatting().getColorValue();
                }
                // Thanks to justanothercorpusguy on the Fabric project Discord
                // For explaining matrix scaling for text to multiple people :P
                context.getMatrices().pushMatrix();
                context.getMatrices().scale(scale, scale);

                drawScaledCenteredText(context,client.textRenderer,itemStack.getName().getString(), x, y+10,color,scale);


                var storedComponents = itemStack.getComponents().get(DataComponentTypes.STORED_ENCHANTMENTS);

                if (storedComponents != null) {
                    int i = (int) (20 * ( scale > 1 ? scale : 1));
                    for (RegistryEntry<Enchantment> enchantment : storedComponents.getEnchantments()) {
                        drawScaledCenteredText(context, client.textRenderer, enchantment.value().description().getString() + toRomanNumeral(storedComponents.getLevel(enchantment), enchantment.value().getMaxLevel()), x, y + i, 0xFFCECECE, scale);
                        i += (int) (10 * ( scale > 1 ? scale : 1));
                    }
                }

                var writtenBookContentComponent = itemStack.getComponents().get(DataComponentTypes.WRITTEN_BOOK_CONTENT);

                if (writtenBookContentComponent != null) {
                    String authorText = Text.translatable("book.byAuthor", writtenBookContentComponent.author()).getString();
                    drawScaledCenteredText(context, client.textRenderer, authorText, x, y + 20, 0xFFCECECE, scale);
                }
                context.getMatrices().popMatrix();
            }
        }
    }
    private static void drawScaledCenteredText(DrawContext context, TextRenderer textRenderer, String text, int x, int y, int color, float scale) {
        int textWidth = textRenderer.getWidth(text);

        context.getMatrices().pushMatrix();
        context.getMatrices().scale(scale, scale);

        float scaledX = x / scale - (textWidth / 2f);
        float scaledY = y / scale;

        context.drawText(
                textRenderer,
                text,
                (int) scaledX,
                (int) scaledY,
                color,
                true
        );

        context.getMatrices().popMatrix();
    }

    public static String toRomanNumeral(Integer level, Integer maxLevel) {
        String output = "";
        if (maxLevel == 1){
            return output;
        }
        switch (level) {
            case 4:
                output += "I";
            case 5:
                output += "V";
                break;
            case 3:
                output += "I";
            case 2:
                output += "I";
            case 1:
                output += "I";
                break;
            default:
                output = level.toString();
        }
        return " " + output;
    }
}
