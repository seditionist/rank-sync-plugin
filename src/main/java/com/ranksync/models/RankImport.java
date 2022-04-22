package com.ranksync.models;

import lombok.AllArgsConstructor;
import lombok.Value;
import net.runelite.api.clan.ClanRank;
import net.runelite.api.clan.ClanTitle;

@Value
@AllArgsConstructor
public class RankImport {
    Integer rank;
    Integer id;

    public RankImport(ClanRank rank, ClanTitle title) { this(rank.ordinal(), title.getId()); }
}