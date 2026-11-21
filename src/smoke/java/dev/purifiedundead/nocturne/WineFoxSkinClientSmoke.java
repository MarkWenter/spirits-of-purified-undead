package dev.purifiedundead.nocturne;

import com.github.tartaricacid.touhoulittlemaid.client.entity.GeckoMaidEntity;
import com.github.tartaricacid.touhoulittlemaid.client.resource.CustomPackLoader;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;

/** Cross-check the snapshot selectors against the model pack actually loaded by TLM. */
public final class WineFoxSkinClientSmoke {
    private static final String[] SKINS = {
            "01_taisho_maid skin skin_white", "02_new_year skin", "03_astronaut skin",
            "04_kongfu default black yellow red default_red red_more", "05_magical magic winefox ice flower blood water",
            "06_hanfu default", "07_jk skin", "08_sta texture", "09_hailuo texture", "10_zhiban default",
            "11_salesperson default blue", "12_little skin", "13_matured default", "14_momo skin_pink skin_green",
            "15_kluonoa texture", "16_tactics tactics", "17_mini texture", "18_wedding skin",
            "19_nine_tailed skin skin_white", "20_survivor mitao", "21_saint skin skinBlack", "22_elf skin"
    };

    public static void verify(ClientLevel level) {
        var maid = new EntityMaid(level);
        var rig = new GeckoMaidEntity<>(maid, maid);
        int verified = 0;
        var failures = new java.util.ArrayList<String>();
        for (String group : SKINS) {
            String[] names = group.split(" ");
            var colors = new java.util.HashSet<net.minecraft.resources.ResourceLocation>();
            for (int i = 1; i < names.length; i++) {
                var slice = new CompoundTag();
                slice.putString("ysmModel", "wine_fox/" + names[0]);
                slice.putString("ysmTexture", names[i]);
                WineFoxSkins.capture(slice, new CompoundTag());
                String key = slice.getString("geckoEcho");
                var info = CustomPackLoader.MAID_MODELS.getInfo(key).orElse(null);
                if (info == null || !info.isGeckoModel() || !info.getModelId().toString().equals(key)) {
                    failures.add(names[0] + "/" + names[i] + " unknown ID " + key);
                    continue;
                }
                rig.setMaidInfo(info);
                if (!rig.getModelLocation().equals(info.getModelId()) || !rig.updateModel()
                        || rig.getCurrentModel().rightHandBones().isEmpty() || !colors.add(rig.getTextureLocation())) {
                    failures.add(key + " missing geometry, hand or distinct texture");
                    continue;
                }
                verified++;
            }
        }
        if (!failures.isEmpty()) throw new AssertionError("Wine Fox resource checks: " + failures);
        System.out.println("NOCTURNE_WINE_FOX_PACK_OK: " + SKINS.length + " models, " + verified + " textures");
    }
}
