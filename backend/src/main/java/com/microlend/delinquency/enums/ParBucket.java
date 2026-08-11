package com.microlend.delinquency.enums;

// Portfolio-at-Risk aging buckets derived from DPD (Days Past Due).
public enum ParBucket {
    CURRENT,   // DPD == 0 (not yet at risk)
    PAR30,     // 1–30 DPD
    PAR60,     // 31–60 DPD
    PAR90,     // 61–90 DPD
    PAR180     // 91+ DPD
}
