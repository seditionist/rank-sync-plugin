package com.ranksync.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MembersSynced {

    String name;
    Integer added;
    Integer updated;
    Integer removed;
}
