package com.ranksync.models;

import lombok.AllArgsConstructor;
import lombok.Value;
import net.runelite.api.clan.ClanRank;

@Value
@AllArgsConstructor
public class MemberImport {
    String rsn;
    Integer rank;

    public MemberImport(String rsn, ClanRank rank) { this(rsn, rank.ordinal()); }
}