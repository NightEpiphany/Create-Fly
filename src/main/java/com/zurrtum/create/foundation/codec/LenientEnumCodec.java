package com.zurrtum.create.foundation.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.util.StringRepresentable;

import java.util.function.Supplier;

/**
 * Like {@link StringRepresentable#fromEnum}, but also accepts legacy values written with {@code Enum#name()} (upper case).
 */
public class LenientEnumCodec {
    public static <E extends Enum<E> & StringRepresentable> Codec<E> create(Supplier<E[]> values) {
        return Codec.STRING.comapFlatMap(
            name -> {
                for (E value : values.get()) {
                    if (value.getSerializedName().equalsIgnoreCase(name) || value.name().equalsIgnoreCase(name)) {
                        return DataResult.success(value);
                    }
                }
                return DataResult.error(() -> "Unknown element name:" + name);
            }, StringRepresentable::getSerializedName
        );
    }
}
