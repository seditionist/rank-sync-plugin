package com.ranksync;

import com.google.common.base.Strings;
import com.google.inject.Binder;
import com.google.inject.Provides;

import javax.inject.Inject;

import com.ranksync.events.ErrorReceived;
import com.ranksync.events.MembersSynced;
import com.ranksync.events.KeyValidated;
import com.ranksync.events.RanksSynced;
import com.ranksync.ui.SyncMembersButton;
import com.ranksync.ui.SyncRanksButton;
import com.ranksync.web.RankSyncClient;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.clan.ClanSettings;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import java.awt.*;

@Slf4j
@PluginDescriptor(
	name = "Rank-Sync",
	tags = {"clan", "rank", "sync", "export"},
	description = "Export clan ranks and members for tracking and management."
)
public class RankSyncPlugin extends Plugin {

	static final String CONFIG_GROUP = "Rank-Sync";
	static final String KEY_VERIFIED_NAME = "keyVerified";
	static final String API_KEY_NAME = "apiKey";

	private static final int CLAN_SETTINGS_MEMBERS_PAGE_WIDGET = 693;
	private static final int CLAN_SETTINGS_MEMBERS_PAGE_WIDGET_ID = WidgetInfo.PACK(CLAN_SETTINGS_MEMBERS_PAGE_WIDGET, 2);

	private static final int CLAN_SETTINGS_RANKS_PAGE_WIDGET = 695;
	private static final int CLAN_SETTINGS_RANKS_PAGE_WIDGET_ID = WidgetInfo.PACK(CLAN_SETTINGS_RANKS_PAGE_WIDGET, 2);

	public static final Color SUCCESS = new Color(170, 255, 40);
	public static final Color ERROR = new Color(204, 66, 66);

	@Inject
	private Client client;

	@Inject
	private RankSyncConfig config;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ChatMessageManager chatMessageManager;

	@Inject
	private RankSyncClient syncClient;

	@Override
	protected void startUp() {
		log.info("Rank-Sync started!");
	}

	@Override
	protected void shutDown() {
		log.info("Rank-Sync stopped!");
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event) {
		if (!event.getGroup().equals(CONFIG_GROUP))
			return;

		if (event.getKey().contains(API_KEY_NAME)) {
			ClanSettings clanSettings = client.getClanSettings();
			syncClient.validateAPIKey(clanSettings.getName());
		}
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded widgetLoaded) {
		int groupID = widgetLoaded.getGroupId();
		if (groupID != CLAN_SETTINGS_MEMBERS_PAGE_WIDGET && groupID != CLAN_SETTINGS_RANKS_PAGE_WIDGET)
			return;

		if (!config.keyVerified() || Strings.isNullOrEmpty(config.apiKey())) {
			sendResponseToChat("Please enter a valid API Key in plugin settings.", ERROR);
			return;
		}

		switch (groupID) {
			case CLAN_SETTINGS_MEMBERS_PAGE_WIDGET:
				clientThread.invoke(() -> new SyncMembersButton(client, syncClient, CLAN_SETTINGS_MEMBERS_PAGE_WIDGET_ID));
				break;

			case CLAN_SETTINGS_RANKS_PAGE_WIDGET:
				clientThread.invoke(() -> new SyncRanksButton(client, syncClient, CLAN_SETTINGS_RANKS_PAGE_WIDGET_ID));
				break;
		}
	}

	@Subscribe
	public void onMembersSynced(MembersSynced event) {
		String message = String.format(
				"Members synced for %s. %d added, %d updated, %d removed.",
				event.getName(),
				event.getAdded(),
				event.getUpdated(),
				event.getRemoved());
		sendResponseToChat(message, SUCCESS);
	}

	@Subscribe
	public void onRanksSynced(RanksSynced event) {
		String message = String.format("Ranks synced for %s.", event.getName());
		sendResponseToChat(message, SUCCESS);
	}

	@Subscribe
	public void onKeyValidated(KeyValidated event) {
		boolean valid = event.isValid();
		config.keyVerified(valid);
		String message = valid
				? String.format("Key validated for clan: %s.", event.getClan())
				: String.format("User does not have permissions for clan: %s.", event.getClan());

		sendResponseToChat(message, valid ? SUCCESS : ERROR);
	}

	@Subscribe
	public void onErrorReceived(ErrorReceived event) {
		String message = event.getData();
		sendResponseToChat(message, ERROR);
	}

	private void sendResponseToChat(String message, Color color) {
		ChatMessageBuilder cmb = new ChatMessageBuilder();
		cmb.append(color, "Rank-Sync: " + message);

		chatMessageManager.queue(QueuedMessage.builder()
				.type(ChatMessageType.CONSOLE)
				.runeLiteFormattedMessage(cmb.build())
				.build());
	}

	@Provides
	RankSyncConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(RankSyncConfig.class);
	}

	@Override
	public void configure(Binder binder) {
		binder.bind(RankSyncClient.class);
	}
}
