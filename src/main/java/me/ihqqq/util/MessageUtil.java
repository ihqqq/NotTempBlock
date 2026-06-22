package me.ihqqq.util;

import me.ihqqq.notTempBlock;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

/**
 * Tiện ích xây dựng prefix & gradient cho các thông báo của notTempBlock,
 * tham khảo phong cách prefix/màu của NotKillRank (gradient + dấu ngoặc xám).
 */
public final class MessageUtil {

    private static final TextColor GRADIENT_FROM = TextColor.color(0xFFD700);
    private static final TextColor GRADIENT_TO   = TextColor.color(0xFFA500);

    private static final String PLUGIN_NAME_SMALLCAPS = "ɴᴏᴛᴛᴇᴍᴘʙʟᴏᴄᴋ";
    private static final String PLUGIN_NAME = "NotTempBlock";

    private MessageUtil() {
    }

    /**
     * Prefix dạng: <gray>[<gradient gold→orange>ɴᴏᴛᴛᴇᴍᴘʙʟᴏᴄᴋ</gradient>]<gray>
     */
    public static Component prefix() {
        return Component.text()
                .append(Component.text("[", NamedTextColor.GRAY))
                .append(gradient(PLUGIN_NAME_SMALLCAPS, GRADIENT_FROM, GRADIENT_TO))
                .append(Component.text("] ", NamedTextColor.GRAY))
                .build();
    }

    public static Component withPrefix(Component message) {
        return prefix().append(message);
    }

    /**
     * Tên plugin dạng chữ thường (không phải small-caps), dùng cho banner /about và console.
     */
    public static Component pluginTitle() {
        return gradient(PLUGIN_NAME, GRADIENT_FROM, GRADIENT_TO);
    }

    public static Component gradient(String text, TextColor from, TextColor to) {
        var builder = Component.text();
        int length = text.length();
        for (int i = 0; i < length; i++) {
            float ratio = length <= 1 ? 0f : (float) i / (length - 1);
            builder.append(Component.text(String.valueOf(text.charAt(i)), interpolate(from, to, ratio)));
        }
        return builder.build().decoration(TextDecoration.ITALIC, false);
    }

    private static TextColor interpolate(TextColor from, TextColor to, float ratio) {
        int r = Math.round(from.red()   + ratio * (to.red()   - from.red()));
        int g = Math.round(from.green() + ratio * (to.green() - from.green()));
        int b = Math.round(from.blue()  + ratio * (to.blue()  - from.blue()));
        return TextColor.color(r, g, b);
    }


    public static void log(Component component) {
        notTempBlock.plugin.getComponentLogger().info(component);
    }
}