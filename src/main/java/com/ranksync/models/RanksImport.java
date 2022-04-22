package com.ranksync.models;

import lombok.Value;
import java.util.ArrayList;

@Value
public class RanksImport {
    String key;
    String name;
    ArrayList<RankImport> ranks;
}
