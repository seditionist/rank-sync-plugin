package com.ranksync.ui;

import com.ranksync.web.RankSyncClient;
import com.ranksync.models.MemberImport;
import java.util.HashMap;
import java.util.Map;
import net.runelite.api.*;
import net.runelite.api.clan.ClanMember;
import net.runelite.api.clan.ClanRank;
import net.runelite.api.clan.ClanSettings;
import net.runelite.api.widgets.*;
import net.runelite.client.util.Text;

import java.util.ArrayList;
import java.util.List;

public class SyncMembersButton {

    private final RankSyncClient syncClient;
    private final Widget parent;

    private final List<Widget> cornersAndEdges = new ArrayList<>();
    private final ClanSettings clanSettings;


    public SyncMembersButton(Client client, RankSyncClient syncClient, int parent) {
        this.syncClient = syncClient;
        this.parent = client.getWidget(parent);
        this.clanSettings = client.getClanSettings();

        this.createWidgetWithSprite(SpriteID.EQUIPMENT_BUTTON_METAL_CORNER_TOP_LEFT, 6, 6, 9, 9);
        this.createWidgetWithSprite(SpriteID.EQUIPMENT_BUTTON_METAL_CORNER_TOP_RIGHT, 97, 6, 9, 9);
        this.createWidgetWithSprite(SpriteID.EQUIPMENT_BUTTON_METAL_CORNER_BOTTOM_LEFT, 6, 20, 9, 9);
        this.createWidgetWithSprite(SpriteID.EQUIPMENT_BUTTON_METAL_CORNER_BOTTOM_RIGHT, 97, 20, 9, 9);
        this.createWidgetWithSprite(SpriteID.EQUIPMENT_BUTTON_EDGE_LEFT, 6, 15, 9, 5);
        this.createWidgetWithSprite(SpriteID.EQUIPMENT_BUTTON_EDGE_TOP, 15, 6, 82, 9);
        this.createWidgetWithSprite(SpriteID.EQUIPMENT_BUTTON_EDGE_RIGHT, 97, 15, 9, 5);
        this.createWidgetWithSprite(SpriteID.EQUIPMENT_BUTTON_EDGE_BOTTOM, 15, 20, 82, 9);
        this.createWidgetWithText();
    }

    private void createWidgetWithSprite(int spriteId, int x, int y, int width, int height) {
        Widget w = this.parent.createChild(-1, WidgetType.GRAPHIC);
        w.setSpriteId(spriteId);
        w.setOriginalX(x);
        w.setOriginalY(y);
        w.setOriginalWidth(width);
        w.setOriginalHeight(height);
        w.setXPositionMode(WidgetPositionMode.ABSOLUTE_LEFT);
        w.setYPositionMode(WidgetPositionMode.ABSOLUTE_TOP);
        w.revalidate();
        cornersAndEdges.add(w);
    }

    private void createWidgetWithText()
    {
        Widget textWidget = this.parent.createChild(-1, WidgetType.TEXT);
        textWidget.setOriginalX(6);
        textWidget.setOriginalY(6);
        textWidget.setOriginalWidth(100);
        textWidget.setOriginalHeight(23);
        textWidget.setXPositionMode(WidgetPositionMode.ABSOLUTE_LEFT);
        textWidget.setYPositionMode(WidgetPositionMode.ABSOLUTE_TOP);
        textWidget.setXTextAlignment(WidgetTextAlignment.CENTER);
        textWidget.setYTextAlignment(WidgetTextAlignment.CENTER);
        textWidget.setText("<col=ffffff>Sync Members</col>");
        textWidget.setFontId(FontID.PLAIN_11);
        textWidget.setTextShadowed(true);

        textWidget.setHasListener(true);
        textWidget.setAction(0, "Sync Members");
        textWidget.setOnOpListener((JavaScriptCallback) e -> this.syncMembers());
        textWidget.setOnMouseOverListener((JavaScriptCallback) e -> update(true));
        textWidget.setOnMouseLeaveListener((JavaScriptCallback) e -> update(false));

        textWidget.revalidate();
    }

    private void update(boolean hovered)
    {
        for(Widget w : cornersAndEdges)
        {
            int spriteId = w.getSpriteId();
            w.setSpriteId(hovered ? spriteId + 8 : spriteId - 8);
            w.revalidate();
        }
    }

    private void syncMembers() {
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
