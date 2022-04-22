package com.ranksync.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class KeyValidated {
    String clan;
    boolean valid;
}
