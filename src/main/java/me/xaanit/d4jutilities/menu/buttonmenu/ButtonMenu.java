package me.xaanit.d4jutilities.menu.buttonmenu;

import me.xaanit.d4jutilities.menu.*;
import me.xaanit.d4jutilities.waiter.EventWaiter;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent;
import sx.blah.discord.handle.impl.obj.ReactionEmoji;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.RequestBuffer;

import java.awt.Color;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;


/**
 * @author John Grosh
 * @editor Jacob (xaanit)
 */
public class ButtonMenu extends Menu {
  private final Color color;
  private final String text;
  private final String description;
  private final List<String> choices;
  private final Consumer<ReactionEmoji> action;
  private final Runnable cancel;

  protected ButtonMenu(EventWaiter waiter, Set<IUser> users, Set<IRole> roles, long timeout, TimeUnit unit,
                       Color color, String text, String description, List<String> choices, Consumer<ReactionEmoji>
                                                                                                   action, Runnable cancel, IDiscordClient client) {
    super(waiter, users, roles, timeout, unit, client);
    this.color = color;
    this.text = text;
    this.description = description;
    this.choices = choices;
    this.action = action;
    this.cancel = cancel;
  }

  /**
   * Shows the ButtonMenu as a new {@link IMessage}
   * in the provided {@link IChannel}.
   *
   * @param channel The IChannel to send the new IMessage to
   */
  @Override
  public void display(IChannel channel) {
    initialize(RequestBuffer.request(() -> {return getMessage(channel).build();}).get());
  }

  /**
   * Displays this ButtonMenu by editing the provided {@link IMessage}.
   *
   * @param message The IMessage to display the Menu in
   */
  @Override
  public void display(IMessage message) {
    MessageBuilder builder = getMessage(message.getChannel());
    initialize(message.edit(builder.getContent(), builder.getEmbedObject()));
  }

  private void initialize(IMessage m) {
    for (int i = 0; i < choices.size(); i++) {
      IEmoji emote;
      try {
        emote = null;
        for (IGuild guild : m.getClient().getGuilds()) {
          emote = emote == null ? guild.getEmojiByID(Long.parseLong((choices.get(i)))) : emote;
        }
      } catch (Exception e) {
        emote = null;
      }
      ReactionEmoji reaction = emote == null ? ReactionEmoji.of(choices.get(i)) : ReactionEmoji.of(emote);
      if (i + 1 < choices.size()) {
        RequestBuffer.request(() -> m.addReaction(reaction));
      } else {
        RequestBuffer.request(() -> m.addReaction(reaction)).get();
        waiter.waitForEvent(ReactionAddEvent.class, e -> {
          ReactionAddEvent event = (ReactionAddEvent) e;

          if (!event.getMessage().getStringID().equals(m.getStringID())) {
            return false;
          }
          String re = !event.getReaction().getEmoji().isUnicode()
                              ? event.getReaction().getEmoji().getStringID()
                              : event.getReaction().getEmoji().getName();
          if (!choices.contains(re)) {
            return false;
          }
          return isValidUser(event);
        }, (ReactionAddEvent event) -> {
          RequestBuffer.request(() -> m.delete());
          action.accept(event.getReaction().getEmoji());
        }, timeout, unit, cancel);

      }
    }
  }

  private MessageBuilder getMessage(IChannel channel) {
    MessageBuilder builder = new MessageBuilder(client);
    builder.withChannel(channel);
    if (text != null) {
      builder.withContent(text);
    }
    if (description != null) {
      builder.withEmbed(new EmbedBuilder().withColor(color).withDesc(description).build());
    }
    return builder;
  }

}