package com.ranksync.models;

import lombok.Value;
import java.util.ArrayList;

@Value
public class MembersImport {
    String key;
    String name;
    ArrayList<MemberImport> members;
}
