package jackiecrazy.cloakanddagger.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import jackiecrazy.cloakanddagger.action.PermissionData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class CloakCommand {

    public static final SimpleCommandExceptionType MISSING_ARGUMENT = new SimpleCommandExceptionType(Component.translatable("wardance.command.missing"));

    public static int missingArgument(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        throw MISSING_ARGUMENT.create();
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("cloakanddagger")
                .requires(s -> s.hasPermission(2))
                .executes(CloakCommand::missingArgument)
                .then(Commands.literal("toggle")
                        .executes(CloakCommand::missingArgument)
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(CloakCommand::missingArgument)
                                .then(Commands.literal("overlay")
                                        .executes(a -> CloakCommand.getPermission(a, Permission.SEE))
                                        .then(Commands.argument("enabled", BoolArgumentType.bool())
                                                .executes(a -> CloakCommand.setPermission(a, Permission.SEE))
                                        )
                                )
                                .then(Commands.literal("stab")
                                        .executes(a -> CloakCommand.getPermission(a, Permission.STAB))
                                        .then(Commands.argument("enabled", BoolArgumentType.bool())
                                                .executes(a -> CloakCommand.setPermission(a, Permission.STAB))
                                        )
                                )
                        )
                );
        dispatcher.register(builder);
    }



    private static int getPermission(CommandContext<CommandSourceStack> ctx, Permission permission) throws CommandSyntaxException {
        Player player = EntityArgument.getPlayer(ctx, "player");
        boolean enabled = true;
        switch (permission) {
            case STAB -> enabled = PermissionData.getCap(player).canStab();
            case SEE -> enabled = PermissionData.getCap(player).canSee();
        }
        ctx.getSource().sendSuccess(Component.translatable("wardance.command.permission." + permission.name() + "." + enabled, player.getDisplayName()), false);

        return enabled ? 1 : 0;
    }

    private static int setPermission(CommandContext<CommandSourceStack> ctx, Permission permission) throws CommandSyntaxException {
        Player player = EntityArgument.getPlayer(ctx, "player");
        final boolean enabled = BoolArgumentType.getBool(ctx, "enabled");
        switch (permission) {
            case STAB -> PermissionData.getCap(player).setStab(enabled);
            case SEE -> PermissionData.getCap(player).setSee(enabled);
        }
        ctx.getSource().sendSuccess(Component.translatable("wardance.command.permission." + permission.name() + "." + enabled, player.getDisplayName()), false);
        return Command.SINGLE_SUCCESS;
    }

    private enum Permission {
        STAB,
        SEE
    }

}