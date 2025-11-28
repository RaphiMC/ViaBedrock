// THIS FILE IS AUTO-GENERATED. DO NOT EDIT!

package net.raphimc.viabedrock.protocol.data.enums.bedrock.generated;

import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;

public enum ShowStoreOfferRedirectType {

    MarketplaceOffer(0),
    DressingRoomOffer(1),
    ThirdPartyServerPage(2);

    private static final Int2ObjectMap<ShowStoreOfferRedirectType> BY_VALUE = new Int2ObjectOpenHashMap<>();

    static {
        for (ShowStoreOfferRedirectType value : values()) {
            if (!BY_VALUE.containsKey(value.value)) BY_VALUE.put(value.value, value);
        }
    }

    public static ShowStoreOfferRedirectType getByValue(final int value) {
        return BY_VALUE.get(value);
    }

    public static ShowStoreOfferRedirectType getByValue(final int value, final ShowStoreOfferRedirectType fallback) {
        return BY_VALUE.getOrDefault(value, fallback);
    }

    private final int value;

    ShowStoreOfferRedirectType(final ShowStoreOfferRedirectType value) {
        this(value.value);
    }

    ShowStoreOfferRedirectType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

}
