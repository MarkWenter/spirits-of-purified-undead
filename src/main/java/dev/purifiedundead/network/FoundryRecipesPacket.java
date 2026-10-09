package dev.purifiedundead.network;

import dev.purifiedundead.foundry.FoundryRecipes;
import net.minecraft.network.FriendlyByteBuf;
import java.util.List;

public record FoundryRecipesPacket(List<FoundryRecipes.Recipe> recipes) {
    static void encode(FoundryRecipesPacket p, FriendlyByteBuf b) {
        FoundryRecipes.write(b, p.recipes);
    }

    static FoundryRecipesPacket decode(FriendlyByteBuf b) {
        return new FoundryRecipesPacket(FoundryRecipes.read(b));
    }

    static void handle(
            FoundryRecipesPacket p,
            java.util.function.Supplier<net.minecraftforge.network.NetworkEvent.Context> c) {
        dev.purifiedundead.client.FoundryClientRecipes.accept(p.recipes);
        c.get().setPacketHandled(true);
    }
}
