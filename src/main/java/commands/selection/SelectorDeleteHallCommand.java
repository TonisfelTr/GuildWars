package commands.selection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.service.sql.SqlService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import guildwars.Main;

public class SelectorDeleteHallCommand implements CommandExecutor {	
	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		Collection<String> hallNameCollection = args.getAll("hall_name");
		String hallName = (String) hallNameCollection.toArray()[0];
		for (int i = 0; i < Main.halls.size(); i++) {
			if (Main.halls.get(i).getName() == hallName) {
				Main.halls.remove(i);
				String url = "jdbc:sqlite:config/guildwars/Guilds.db";
				SqlService sql;
				Connection conn;
				try {
					sql = Sponge.getServiceManager().provide(SqlService.class).get();
					conn = sql.getDataSource(url).getConnection();
					PreparedStatement stmtInsert = conn.prepareStatement("DELETE FROM `halls` WHERE `name`=" + hallName);
					stmtInsert.executeUpdate();
					conn.close();
					src.sendMessage(Text.of(TextColors.GREEN, "[Guild] Guild hall \"" + hallName + "\" has been deleted!"));
				} catch (SQLException e) {
					e.printStackTrace();
				}
				return CommandResult.success();
			}
		}
		src.sendMessage(Text.of(TextColors.RED, "[Guild] Guild hall with that name does not exist."));
		return CommandResult.success();
	}
}
