package com.ranksync;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(RankSyncPlugin.CONFIG_GROUP)
public interface RankSyncConfig extends Config {

	@ConfigItem(
		keyName = RankSyncPlugin.API_KEY_NAME,
		name = "API Key",
		description = "API Key for your Rank-Sync clan",
		secret = true
	)
	default String apiKey() { return ""; }

	@ConfigItem(
			keyName = RankSyncPlugin.KEY_VERIFIED_NAME,
			name = "",
			description = "",
			hidden = true
	)
	default boolean keyVerified() { return false; }

	@ConfigItem(
			keyName = RankSyncPlugin.KEY_VERIFIED_NAME,
			name = "",
			description = "",
			hidden = true
	)
	void keyVerified(boolean value);
}
