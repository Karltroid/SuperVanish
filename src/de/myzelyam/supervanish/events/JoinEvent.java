/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *   License, v. 2.0. If a copy of the MPL was not distributed with this
 *   file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package de.myzelyam.supervanish.events;

import de.myzelyam.supervanish.SuperVanish;
import de.myzelyam.supervanish.hooks.EssentialsHook;
import de.myzelyam.supervanish.utils.ProtocolLibPacketUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class JoinEvent implements EventExecutor, Listener {
    private final SuperVanish plugin;

    public JoinEvent(SuperVanish plugin) {
        this.plugin = plugin;
    }

    private FileConfiguration getSettings() {
        return plugin.settings;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void execute(Listener listener, Event event) {
        try {
            if (event instanceof PlayerJoinEvent) {
                PlayerJoinEvent e = (PlayerJoinEvent) event;
                final Player p = e.getPlayer();
                final List<String> invisiblePlayers = plugin.getAllInvisiblePlayers();
                // join message
                if (getSettings().getBoolean(
                        "Configuration.Messages.HideNormalJoinAndLeaveMessagesWhileInvisible", true)
                        && invisiblePlayers.contains(p.getUniqueId().toString())) {
                    e.setJoinMessage(null);
                }
                // vanished:
                if (invisiblePlayers.contains(p.getUniqueId().toString())) {
                    // Essentials
                    if (plugin.getServer().getPluginManager()
                            .getPlugin("Essentials") != null
                            && getSettings().getBoolean("Configuration.Hooks.EnableEssentialsHook")) {
                        EssentialsHook.hidePlayer(p);
                    }
                    // reminding message
                    if (getSettings().getBoolean("Configuration.Messages.RemindInvisiblePlayersOnJoin")) {
                        p.sendMessage(plugin.convertString(
                                plugin.getMsg("RemindingMessage"), p));
                    }
                    // hide
                    plugin.getVisibilityAdjuster().getHider().hideToAll(p);
                    // metadata
                    p.setMetadata("vanished", new FixedMetadataValue(plugin, true));
                    // re-add action bar
                    if (plugin.getActionBarMgr() != null
                            && getSettings().getBoolean("Configuration.Messages.DisplayActionBarsToInvisiblePlayers")) {
                        plugin.getActionBarMgr().addActionBar(p);
                    }
                    // packet night vision
                    if (plugin.packetNightVision && getSettings().getBoolean("Configuration.Players.AddNightVision"))
                        plugin.getProtocolLibPacketUtils().sendAddPotionEffect(p, new PotionEffect(
                                PotionEffectType.NIGHT_VISION, ProtocolLibPacketUtils.INFINITE_POTION_LENGTH, 0));

                }
                // not necessarily vanished:
                //
                // hide vanished players to player
                plugin.getVisibilityAdjuster().getHider().hideAllInvisibleTo(p);
            }
        } catch (Exception er) {
            plugin.printException(er);
        }
    }
}