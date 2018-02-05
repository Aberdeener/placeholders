/*
 * This file is part of LuckPerms, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package me.lucko.luckperms.placeholders;

import me.lucko.luckperms.api.LuckPermsApi;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import be.maximvdw.placeholderapi.PlaceholderReplaceEvent;
import be.maximvdw.placeholderapi.PlaceholderReplacer;

/**
 * MVdWPlaceholderAPI Hook for LuckPerms, implemented using the LuckPerms API.
 */
public class LuckPermsMVdWHook extends JavaPlugin implements PlaceholderReplacer {
    private LPPlaceholderProvider provider;

    @Override
    public void onEnable() {
        if (!getServer().getServicesManager().isProvidedFor(LuckPermsApi.class)) {
            throw new RuntimeException("LuckPermsApi not provided.");
        }

        LuckPermsApi api = getServer().getServicesManager().load(LuckPermsApi.class);
        this.provider = new LPPlaceholderProvider(api) {
            @Override
            protected String formatTime(int seconds) {
                if (seconds == 0) {
                    return "0s";
                }

                long minute = seconds / 60;
                seconds = seconds % 60;
                long hour = minute / 60;
                minute = minute % 60;
                long day = hour / 24;
                hour = hour % 24;

                StringBuilder time = new StringBuilder();
                if (day != 0) {
                    time.append(day).append("d ");
                }
                if (hour != 0) {
                    time.append(hour).append("h ");
                }
                if (minute != 0) {
                    time.append(minute).append("m ");
                }
                if (seconds != 0) {
                    time.append(seconds).append("s");
                }

                return time.toString().trim();
            }

            @Override
            protected String formatBoolean(boolean b) {
                return b ? "yes" : "no";
            }
        };
        PlaceholderAPI.registerPlaceholder(this, "luckperms_*", this);
    }

    @Override
    public String onPlaceholderReplace(PlaceholderReplaceEvent event) {
        String placeholder = event.getPlaceholder();
        if (!placeholder.startsWith("luckperms_")) {
            return null;
        }

        String identifier = placeholder.substring("luckperms_".length()).toLowerCase();
        Player player = event.getPlayer();

        if (player == null || this.provider == null) {
            return "";
        }

        return this.provider.handle(player, identifier);
    }

}
