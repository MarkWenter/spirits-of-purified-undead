package dev.purifiedundead.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.purifiedundead.api.*;
import dev.purifiedundead.config.ConfigCatalog;
import dev.purifiedundead.progress.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import static net.minecraft.commands.Commands.*;

@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid = "purified_undead")
public final class PurifiedUndeadCommands {
    private PurifiedUndeadCommands() {}

    @net.minecraftforge.eventbus.api.SubscribeEvent
    public static void register(net.minecraftforge.event.RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var root =
                literal("purifiedundead")
                        .executes(
                                c -> {
                                    reply(c.getSource(), "help");
                                    return 1;
                                });
        root.then(
                literal("status")
                        .executes(c -> status(c.getSource(), c.getSource().getPlayerOrException()))
                        .then(
                                argument("player", EntityArgument.player())
                                        .requires(s -> s.hasPermission(2))
                                        .executes(
                                                c ->
                                                        status(
                                                                c.getSource(),
                                                                EntityArgument.getPlayer(
                                                                        c, "player")))));
        var warrior = literal("warrior").requires(s -> s.hasPermission(2));
        for (String action : new String[] {"grant", "reset", "collect"}) {
            var target = argument("players", EntityArgument.players());
            for (var id : WarriorId.values())
                target.then(
                        literal(id.id())
                                .executes(
                                        c -> {
                                            var players = EntityArgument.getPlayers(c, "players");
                                            for (var p : players) {
                                                if (action.equals("grant"))
                                                    reply(
                                                            c.getSource(),
                                                            "grant",
                                                            p.getDisplayName(),
                                                            id.id(),
                                                            Component.translatable(
                                                                    "command.purified_undead."
                                                                            + ProgressApi
                                                                                    .grantWarrior(
                                                                                            p, id)
                                                                                    .name()
                                                                                    .toLowerCase(
                                                                                            java
                                                                                                    .util
                                                                                                    .Locale
                                                                                                    .ROOT)));
                                                else if (action.equals("reset")) {
                                                    ProgressApi.resetWarrior(p, id);
                                                    reply(
                                                            c.getSource(),
                                                            "reset",
                                                            p.getDisplayName(),
                                                            id.id());
                                                } else
                                                    reply(
                                                            c.getSource(),
                                                            "collect",
                                                            p.getDisplayName(),
                                                            id.id(),
                                                            ProgressManagement.collect(p, id));
                                            }
                                            return players.size();
                                        }));
            warrior.then(literal(action).then(target));
        }
        root.then(warrior);
        root.then(
                literal("contract")
                        .requires(s -> s.hasPermission(2))
                        .then(
                                literal("grant")
                                        .then(
                                                argument("players", EntityArgument.players())
                                                        .executes(
                                                                c -> {
                                                                    var players =
                                                                            EntityArgument
                                                                                    .getPlayers(
                                                                                            c,
                                                                                            "players");
                                                                    for (var p : players)
                                                                        reply(
                                                                                c.getSource(),
                                                                                "grant",
                                                                                p.getDisplayName(),
                                                                                "contract",
                                                                                Component
                                                                                        .translatable(
                                                                                                "command.purified_undead."
                                                                                                        + ProgressApi
                                                                                                                .grantContract(
                                                                                                                        p)
                                                                                                                .name()
                                                                                                                .toLowerCase(
                                                                                                                        java
                                                                                                                                .util
                                                                                                                                .Locale
                                                                                                                                .ROOT)));
                                                                    return players.size();
                                                                }))));
        root.then(
                literal("talisman")
                        .requires(s -> s.hasPermission(2))
                        .then(
                                literal("set")
                                        .then(
                                                argument("players", EntityArgument.players())
                                                        .then(
                                                                argument(
                                                                                "level",
                                                                                IntegerArgumentType
                                                                                        .integer(0))
                                                                        .executes(
                                                                                c -> {
                                                                                    int level =
                                                                                            IntegerArgumentType
                                                                                                    .getInteger(
                                                                                                            c,
                                                                                                            "level");
                                                                                    if (level
                                                                                            > WhiteWitchTalisman
                                                                                                    .maxLevel()) {
                                                                                        c.getSource()
                                                                                                .sendFailure(
                                                                                                        Component
                                                                                                                .translatable(
                                                                                                                        "command.purified_undead.level_range",
                                                                                                                        WhiteWitchTalisman
                                                                                                                                .maxLevel()));
                                                                                        return 0;
                                                                                    }
                                                                                    var players =
                                                                                            EntityArgument
                                                                                                    .getPlayers(
                                                                                                            c,
                                                                                                            "players");
                                                                                    for (var p :
                                                                                            players)
                                                                                        ProgressApi
                                                                                                .setTalismanLevel(
                                                                                                        p,
                                                                                                        level);
                                                                                    reply(
                                                                                            c
                                                                                                    .getSource(),
                                                                                            "level",
                                                                                            players
                                                                                                    .size(),
                                                                                            level);
                                                                                    return players
                                                                                            .size();
                                                                                })))));
        root.then(
                literal("rewards")
                        .requires(s -> s.hasPermission(2))
                        .then(
                                literal("retry")
                                        .then(
                                                argument("players", EntityArgument.players())
                                                        .executes(
                                                                c -> {
                                                                    var players =
                                                                            EntityArgument
                                                                                    .getPlayers(
                                                                                            c,
                                                                                            "players");
                                                                    for (var p : players)
                                                                        WarriorRewardService
                                                                                .retryPending(p);
                                                                    reply(
                                                                            c.getSource(),
                                                                            "retry",
                                                                            players.size());
                                                                    return players.size();
                                                                }))));
        var config = literal("config").requires(s -> s.hasPermission(2));
        for (String file : new String[] {"common", "slate"}) {
            config.then(
                    literal(file)
                            .executes(
                                    c -> {
                                        reply(
                                                c.getSource(),
                                                "config_list",
                                                file,
                                                String.join(
                                                        ", ", ConfigCatalog.values(file).keySet()));
                                        return 1;
                                    })
                            .then(
                                    argument("key", StringArgumentType.word())
                                            .suggests(
                                                    (c, b) ->
                                                            net.minecraft.commands
                                                                    .SharedSuggestionProvider
                                                                    .suggest(
                                                                            ConfigCatalog.values(
                                                                                            file)
                                                                                    .keySet(),
                                                                            b))
                                            .executes(
                                                    c -> {
                                                        String key =
                                                                StringArgumentType.getString(
                                                                        c, "key");
                                                        if (!ConfigCatalog.values(file)
                                                                .containsKey(key)) {
                                                            c.getSource()
                                                                    .sendFailure(
                                                                            Component.translatable(
                                                                                    "command.purified_undead.unknown_key",
                                                                                    key));
                                                            return 0;
                                                        }
                                                        reply(
                                                                c.getSource(),
                                                                "config_value",
                                                                file,
                                                                key,
                                                                String.valueOf(
                                                                        ConfigCatalog.value(
                                                                                file, key)));
                                                        return 1;
                                                    })));
        }
        config.then(
                literal("reload")
                        .then(
                                literal("foundry")
                                        .executes(
                                                c -> {
                                                    try {
                                                        int count =
                                                                dev.purifiedundead.foundry
                                                                        .FoundryRecipes
                                                                        .reloadStrict(
                                                                                c.getSource()
                                                                                        .getServer());
                                                        reply(c.getSource(), "reload", count);
                                                        return 1;
                                                    } catch (java.io.IOException
                                                            | RuntimeException e) {
                                                        c.getSource()
                                                                .sendFailure(
                                                                        Component.translatable(
                                                                                "command.purified_undead.reload_failed",
                                                                                String.valueOf(
                                                                                        e
                                                                                                .getMessage())));
                                                        return 0;
                                                    }
                                                })));
        root.then(config);
        dispatcher.register(root);
    }

    private static int status(CommandSourceStack source, ServerPlayer p) {
        var s = ProgressApi.snapshot(p);
        reply(
                source,
                "status",
                p.getDisplayName(),
                s.talismanLevel(),
                WhiteWitchTalisman.maxLevel(),
                s.eleineDrownedKills());
        for (var id : WarriorId.values()) {
            var v = s.warriors().get(id);
            reply(
                    source,
                    "state",
                    Component.translatable(id.item().getDescriptionId()),
                    v.obtained(),
                    v.pending());
        }
        reply(
                source,
                "state",
                Component.translatable("item.purified_undead.ancient_contract"),
                s.contract().obtained(),
                s.contract().pending());
        return 1;
    }

    private static void reply(CommandSourceStack source, String key, Object... args) {
        source.sendSuccess(
                () -> Component.translatable("command.purified_undead." + key, args), false);
    }
}
