package commands;

import java.util.Collection;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import guildunits.Guild;
import guildunits.PlayerInvitation;
import guildwars.Main;

public class GuildMemberCommand implements CommandExecutor {	
	private boolean DoHaveInvite(String nickname) {
		for (int i = 0; i < Main.invitations.size(); i++) {
			if (Main.invitations.get(i).getNickname().equals(nickname))
				return true;
		}
		return false;
	}
	
	@SuppressWarnings("unused")
	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		if (!(src instanceof Player)) {
			src.sendMessage(Text.builder("You need be player to use that command!").color(TextColors.RED).build());
			return CommandResult.success();
		}
		else {
			Collection<String> action = args.getAll("invite");
			Collection<String> name = args.getAll("name");
			String triggeredAction = (String) action.toArray()[0];
			String targetPlayer = (String) name.toArray()[0];
			Player p = (Player)src;
			//Player invitingPlayer = Sponge.getServer().getPlayer();
			//Интерпретация приглашения
			if(triggeredAction.equals("invite")) {
				boolean IsInGuildUser = false;
				for(int i = 0; i < Main.guilds.size(); i++) {
					if (Main.guilds.get(i).IsInGuild(p.getName()))
						IsInGuildUser = true;
				}
				if (!IsInGuildUser) {
					p.sendMessage(Text.of(TextColors.RED, "[Guild] You are not in guild."));
					return CommandResult.success();
				}
				
				boolean inGuildResult = false;
				for(int j = 0; j < Main.guilds.size(); j++) {
					if (Main.guilds.get(j).IsInGuild(targetPlayer)) {
						inGuildResult = true;
						break;
					}
				}
				if (targetPlayer.toUpperCase().equals(p.getName().toUpperCase())) {
					p.sendMessage(Text.of(TextColors.RED, "[Guild] You cannot invite yourself."));
					return CommandResult.success();
				}
				
				if (inGuildResult == false) {
					for (int i = 0; i < Main.guilds.size(); i++) {
						if (Main.guilds.get(i).IsLeader(p.getName())) {
							if (!Main.guilds.get(i).IsInGuild(targetPlayer)) {
								if (Sponge.getServer().getPlayer(targetPlayer).isPresent()) {
									if (this.DoHaveInvite(targetPlayer)) {
										p.sendMessage(Text.of(TextColors.RED, "[Guild] ", targetPlayer , " got invitation to guild already."));
										return CommandResult.success();
									} else {
										Sponge.getServer().getPlayer(targetPlayer).get().sendMessage(Text.of(TextColors.GREEN, p.getName(), " is inviting you to the guild ", Main.guilds.get(i).GetName()));
										Sponge.getServer().getPlayer(targetPlayer).get().sendMessage(Text.of(TextColors.GREEN, "Write \"/gok\" to enter to guild."));
										Main.invitations.add(new PlayerInvitation(targetPlayer.toUpperCase(), Main.guilds.get(i).GetId()));
										return CommandResult.success();
									}
								} else {
									p.sendMessage(Text.of(TextColors.RED, "[Guild] You cannot invite to guild offline players."));
								}
							} else {
								p.sendMessage(Text.of(TextColors.RED, "[Guild] ", targetPlayer, " is in guild already!"));
								
							}
						} else {
							if (Main.guilds.get(i).IsInGuild(targetPlayer))
								p.sendMessage(Text.of(TextColors.RED, "[Guild] You don't have enough permissions for do that."));
						}
					}
				} else {
					p.sendMessage(Text.of(TextColors.RED, "[Guild] ", targetPlayer, " is in guild already!"));
				}
			}
			//Интерпретация создания гильдии
			if (triggeredAction.equals("create")) {
				for(int i = 0; i < Main.guilds.size(); i++) {
					if (Main.guilds.get(i).GetName().equals(targetPlayer)) {
						p.sendMessage(Text.of(TextColors.RED, "[Guild] Guild with this name already exists!"));
						return CommandResult.success();
					}
				}
				for(int i = 0; i < Main.guilds.size(); i++) {
					if (Main.guilds.get(i).IsInGuild(p.getName())) {
						p.sendMessage(Text.of(TextColors.RED, "[Guild] You are in guild already!"));
						return CommandResult.success();
					}
				}
				
				Guild.CreateGuild(targetPlayer, p.getName());
				p.sendMessage(Text.of(TextColors.GREEN, "[Guild] Your guild has been created successfuly!"));
			}
			//Интерпретация кика из гильдии.
			if (triggeredAction.equals("kick")) {
				if (targetPlayer != p.getName()) {
					for (int i = 0; i < Main.guilds.size(); i++) {
						if (Main.guilds.get(i).IsInGuild(targetPlayer)) {
							if (Main.guilds.get(i).IsLeader(p.getName())) {
								if (!Main.guilds.get(i).IsLeader(targetPlayer)) {
									Main.guilds.get(i).RemoveMemberFromGuild(targetPlayer);
									p.sendMessage(Text.of(TextColors.GREEN, "[Guild] You kicked " + targetPlayer + " from the guild."));
									Sponge.getServer().getPlayer(targetPlayer).get().sendMessage(Text.of(TextColors.GREEN, "[Guild] You have been kicked from the guild."));
								} else {
									p.sendMessage(Text.of(TextColors.RED, "[Guild] You cannot kick guild leader"));
								}
							} else {
								p.sendMessage(Text.of(TextColors.RED, "[Guild] You don't have permissions to do that."));
							}
						}
					}
				} else {
					p.sendMessage(Text.of(TextColors.RED, "[Guild] You cannot kick yourself."));
					return CommandResult.success();
				}
			}
			if (triggeredAction.equals("leader")) {
				for (int i = 0; i < Main.guilds.size(); i++) {
					if (Main.guilds.get(i).IsInGuild(p.getName())) {
						if (Main.guilds.get(i).IsLeader(p.getName())) {
							if (Main.guilds.get(i).IsInGuild(targetPlayer)) {
								Main.guilds.get(i).SetLeader(targetPlayer);
								p.sendMessage(Text.of(TextColors.GREEN, "[Guild] You set " + targetPlayer + " the guild leader."));
								if (Sponge.getServer().getPlayer(targetPlayer).isPresent())
									Sponge.getServer().getPlayer(targetPlayer).get().sendMessage(Text.of(TextColors.GREEN, "[Guild] You are the guild leader now!"));
								return CommandResult.success();
							} else {
								p.sendMessage(Text.of(TextColors.RED, "[Guild] " + targetPlayer + " is not in your guild."));
								return CommandResult.success();
							}
						} else {
							p.sendMessage(Text.of(TextColors.RED, "[Guild] You're not leader of guild."));
							return CommandResult.success();
						}
					}
					p.sendMessage(Text.of(TextColors.RED, "[Guild] You're not in guild."));
					return CommandResult.success();
				}
			}
			
		}
		return CommandResult.success();
	}
}
