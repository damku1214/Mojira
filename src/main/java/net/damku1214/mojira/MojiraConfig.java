package net.damku1214.mojira;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class MojiraConfig {
    public static final MojiraConfig CONFIG;
    public static final ModConfigSpec CONFIG_SPEC;

    public final ModConfigSpec.ConfigValue<Boolean> MC_14;
    public final ModConfigSpec.ConfigValue<Boolean> MC_63;
    public final ModConfigSpec.ConfigValue<Boolean> MC_201;
    public final ModConfigSpec.ConfigValue<Boolean> MC_577;
    public final ModConfigSpec.ConfigValue<Boolean> MC_711;
    public final ModConfigSpec.ConfigValue<Boolean> MC_779;
    public final ModConfigSpec.ConfigValue<Boolean> MC_195599;
    public final ModConfigSpec.ConfigValue<Boolean> MC_868;
    public final ModConfigSpec.ConfigValue<Boolean> MC_957;
    public final ModConfigSpec.ConfigValue<Boolean> MC_1691;
    public final ModConfigSpec.ConfigValue<Boolean> MC_2023;

    private MojiraConfig(ModConfigSpec.Builder builder) {
        MC_14 = builder
                .translation("mojira.config.mc_14")
                .define("mc_14", true);
        MC_63 = builder
                .translation("mojira.config.mc_63")
                .define("mc_63", true);
        MC_201 = builder
                .translation("mojira.config.mc_201")
                .define("mc_201", true);
        MC_577 = builder
                .translation("mojira.config.mc_577")
                .define("mc_577", true);
        MC_711 = builder
                .translation("mojira.config.mc_711")
                .define("mc_711", true);
        MC_779 = builder
                .translation("mojira.config.mc_779")
                .define("mc_779", true);
        MC_195599 = builder
                .translation("mojira.config.mc_195599")
                .define("mc_195599", true);
        MC_868 = builder
                .translation("mojira.config.mc_868")
                .define("mc_868", true);
        MC_957 = builder
                .translation("mojira.config.mc_957")
                .define("mc_957", true);
        MC_1691 = builder
                .translation("mojira.config.mc_1691")
                .define("mc_1691", true);
        MC_2023 = builder
                .translation("mojira.config.mc_2023")
                .define("mc_2023", true);
    }

    static {
        Pair<MojiraConfig, ModConfigSpec> pair = new ModConfigSpec.Builder().configure(MojiraConfig::new);

        CONFIG = pair.getLeft();
        CONFIG_SPEC = pair.getRight();
    }
}
