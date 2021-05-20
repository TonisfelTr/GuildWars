package commands.selection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.sql.SqlService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import guildwars.Main;

public class SelectorSellHallCommand implements CommandExecutor {	
	@SuppressWarnings("unused")
	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		if (!(src instanceof Player)) {
			src.sendMessage(Text.builder("You need be player to use that command!").color(TextColors.RED).build());
			return CommandResult.success();
		} else {
			for (int u = 0; u < Main.guilds.size(); u++) {
				if (Main.guilds.get(u).IsInGuild(src.getName())) {
					if (Main.guilds.get(u).IsLeader(src.getName())) {
						for (int i = 0; i < Main.halls.size(); i++ ) {
							if (Main.halls.get(i).getMasterGuild() == Main.guilds.get(u).GetId()) {
								Main.halls.get(i).setGuildMaster(0);
								String url = "jdbc:sqlite:config/guildwars/Guilds.db";
								SqlService sql;
								Connection conn;
								try {
									sql = Sponge.getServiceManager().provide(SqlService.class).get();
									conn = sql.getDataSource(url).getConnection();
									PreparedStatement stmtUpdate = conn.prepareStatement("UPDATE halls SET guildmaster = 0 WHERE name = \"" + Main.halls.get(i).getName() + "\"");
									stmtUpdate.executeUpdate();
									conn.close();
									src.sendMessage(Text.of(TextColors.GREEN, "[Guild] Guild hall \"" + Main.halls.get(i).getName() + "\" has been sold!"));
									Main.guilds.get(u).PayToGuild(Math.round(Main.halls.get(i).getPrice() / 100 * 55));
								} catch (SQLException e) {
									e.printStackTrace();
								}
								return CommandResult.success();
							}
						}
						src.sendMessage(Text.of(TextColors.RED, "[Guild] You do not have guild hall."));
						return CommandResult.success();
					}
					src.sendMessage(Text.of(TextColors.RED, "[Guild] You are not guild leader."));
					return CommandResult.success();
				}
			}
			src.sendMessage(Text.of(TextColors.RED, "[Guild] You are not in guild."));
			return CommandResult.success();
		}
	}
}
