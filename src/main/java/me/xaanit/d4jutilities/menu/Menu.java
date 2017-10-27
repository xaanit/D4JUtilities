package me.xaanit.d4jutilities.menu;

import me.xaanit.d4jutilities.waiter.EventWaiter;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent;
import sx.blah.discord.handle.impl.obj.Channel;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;

import java.util.Set;
import java.util.concurrent.TimeUnit;


/**
 * @author John Grosh
 * @editor Jacob (xaanit)
 */
public abstract class Menu {
  protected final EventWaiter waiter;
  protected final Set<IUser> users;
  protected final Set<IRole> roles;
  protected final long timeout;
  protected final TimeUnit unit;
  protected final IDiscordClient client;

  protected Menu(EventWaiter waiter, Set<IUser> users, Set<IRole> roles, long timeout, TimeUnit unit, IDiscordClient client) {
    this.waiter = waiter;
    this.users = users;
    this.roles = roles;
    this.timeout = timeout;
    this.unit = unit;
    this.client = client;
  }

  /**
   * Displays this Menu in a {@link sx.blah.discord.handle.obj.IChannel IChannel}.
   *
   * @param channel The IChannel to display this Menu in
   */
  public abstract void display(IChannel channel);

  /**
   * Displays this Menu as a designated {@link sx.blah.discord.handle.obj.IMessage IMessage}.
   * <br>The Message provided must be one sent by the bot! Trying to provided a Message
   * authored by another {@link sx.blah.discord.handle.obj.IUser IUser} will prevent the
   * Menu from being displayed!
   *
   * @param message The Message to display this Menu as
   */
  public abstract void display(IMessage message);

  protected boolean isValidUser(ReactionAddEvent event) {
    if (event.getUser().isBot()) {
      return false;
    }
    if (users.isEmpty() && roles.isEmpty()) {
      return true;
    }
    if (users.contains(event.getUser())) {
      return true;
    }
    if (!(event.getChannel() instanceof Channel)) {
      return false;
    }
    return event.getUser().getRolesForGuild(event.getGuild()).stream().anyMatch(r -> roles.contains(r));
  }

  protected boolean isValidUser(MessageReceivedEvent event) {
    if (event.getAuthor().isBot()) {
      return false;
    }
    if (users.isEmpty() && roles.isEmpty()) {
      return true;
    }
    if (users.contains(event.getAuthor())) {
      return true;
    }
    if (!(event.getChannel() instanceof Channel)) {
      return false;
    }
    return event.getAuthor().getRolesForGuild(event.getGuild()).stream().anyMatch(r -> roles.contains(r));
  }
}