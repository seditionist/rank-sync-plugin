package com.ranksync.ui;

import com.ranksync.web.RankSyncClient;
import com.ranksync.models.MemberImport;
import java.util.HashMap;
import java.util.Map;
import net.runelite.api.*;
import net.runelite.api.clan.ClanMember;
import net.runelite.api.clan.ClanRank;
import net.runelite.client.util.Text;
import java.util.ArrayList;

public class SyncMembersButton extends SyncButton {

    public SyncMembersButton(Client client, RankSyncClient syncClient, int parent) {
        super(client, syncClient, parent, "Sync Members");
    }

    protected void buttonAction() {
        Map<String, MemberImport> clanMembers = new HashMap<>();

        for (ClanMember clanMember : clanSettings.getMembers()) {
            if (clanMember.getName().startsWith("[#"))
                continue;

            String memberName = Text.toJagexName(clanMember.getName());
            ClanRank rank = clanMember.getRank();

            clanMembers.put(memberName.toLowerCase(), new MemberImport(memberName, rank));
        }

        syncClient.syncClanMembers(clanSettings.getName(), new ArrayList<>(clanMembers.values()));
    }
}
