package com.ranksync.ui;

import com.ranksync.models.RankImport;
import com.ranksync.web.RankSyncClient;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.clan.ClanRank;
import net.runelite.api.clan.ClanTitle;
import java.util.ArrayList;

@Slf4j
public class SyncRanksButton extends SyncButton {

    public SyncRanksButton(Client client, RankSyncClient syncClient, int parent) {
        super(client, syncClient, parent, "Sync Ranks");
    }

    @Override
    protected void buttonAction() {
        ArrayList<RankImport> clanRanks = new ArrayList<>();

        for (ClanRank rank : ClanRank.values()) {
            ClanTitle title = clanSettings.titleForRank(rank);
            RankImport clanRank = new RankImport(rank, title);
            clanRanks.add(clanRank);
        }

        syncClient.syncClanRanks(clanSettings.getName(), clanRanks);
    }
}
