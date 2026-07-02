package com.gamercorpse.easypronouns.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ColorUtil {

    private static final Pattern HEX_PATTERN = Pattern.compile("(?i)(?:&#|#|\\{#)([A-Fa-f0-9]{6})(?:})?");
    private static final Pattern GRADIENT_PATTERN = Pattern.compile("(?i)<gradient:#([A-Fa-f0-9]{6}):#([A-Fa-f0-9]{6})>(.*?)</gradient>");

    private ColorUtil() {
    }

    public static Component color(String input) {
        if (input == null || input.isEmpty()) {
            return Component.empty();
        }

        input = applyLegacy(input);

        Matcher gradientMatcher = GRADIENT_PATTERN.matcher(input);
        TextComponent.Builder builder = Component.text();

        int last = 0;
        while (gradientMatcher.find()) {
            if (gradientMatcher.start() > last) {
                builder.append(colorPlain(input.substring(last, gradientMatcher.start())));
            }

            String startHex = gradientMatcher.group(1);
            String endHex = gradientMatcher.group(2);
            String text = gradientMatcher.group(3);

            builder.append(gradient(text, startHex, endHex));
            last = gradientMatcher.end();
        }

        if (last < input.length()) {
            builder.append(colorPlain(input.substring(last)));
        }

        return builder.build();
    }

    public static List<Component> colorList(List<String> input) {
        List<Component> components = new ArrayList<>();

        for (String line : input) {
            components.add(color(line));
        }

        return components;
    }

    private static Component colorPlain(String input) {
        TextComponent.Builder builder = Component.text();
        Matcher matcher = HEX_PATTERN.matcher(input);

        int last = 0;
        TextColor activeColor = null;

        while (matcher.find()) {
            if (matcher.start() > last) {
                builder.append(Component.text(input.substring(last, matcher.start()), activeColor));
            }

            activeColor = TextColor.fromHexString("#" + matcher.group(1));
            last = matcher.end();
        }

        if (last < input.length()) {
            builder.append(Component.text(input.substring(last), activeColor));
        }

        return builder.build();
    }

    private static Component gradient(String text, String startHex, String endHex) {
        TextComponent.Builder builder = Component.text();

        int startRed = Integer.parseInt(startHex.substring(0, 2), 16);
        int startGreen = Integer.parseInt(startHex.substring(2, 4), 16);
        int startBlue = Integer.parseInt(startHex.substring(4, 6), 16);

        int endRed = Integer.parseInt(endHex.substring(0, 2), 16);
        int endGreen = Integer.parseInt(endHex.substring(2, 4), 16);
        int endBlue = Integer.parseInt(endHex.substring(4, 6), 16);

        int length = Math.max(text.length() - 1, 1);

        for (int i = 0; i < text.length(); i++) {
            double ratio = (double) i / length;

            int red = (int) (startRed + (endRed - startRed) * ratio);
            int green = (int) (startGreen + (endGreen - startGreen) * ratio);
            int blue = (int) (startBlue + (endBlue - startBlue) * ratio);

            builder.append(Component.text(String.valueOf(text.charAt(i)), TextColor.color(red, green, blue)));
        }

        return builder.build();
    }

    private static String applyLegacy(String input) {
        return input
                .replace("&0", "#000000")
                .replace("&1", "#0000AA")
                .replace("&2", "#00AA00")
                .replace("&3", "#00AAAA")
                .replace("&4", "#AA0000")
                .replace("&5", "#AA00AA")
                .replace("&6", "#FFAA00")
                .replace("&7", "#AAAAAA")
                .replace("&8", "#555555")
                .replace("&9", "#5555FF")
                .replace("&a", "#55FF55")
                .replace("&b", "#55FFFF")
                .replace("&c", "#FF5555")
                .replace("&d", "#FF55FF")
                .replace("&e", "#FFFF55")
                .replace("&f", "#FFFFFF")
                .replace("&r", "#FFFFFF");
    }
}