package me.xaanit.d4jutilities.menu.orderedmenu;

import me.xaanit.d4jutilities.menu.Menu;
import me.xaanit.d4jutilities.waiter.EventWaiter;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.Event;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent;
import sx.blah.discord.handle.impl.obj.ReactionEmoji;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RequestBuffer;

import java.awt.Color;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author John Grosh
 * @editor Jacob (xaanit)
 */
public class OrderedMenu extends Menu {
  private final Color color;
  private final String text;
  private final String description;
  private final List<String> choices;
  private final Consumer<Integer> action;
  private final Runnable cancel;
  private final boolean useLetters;
  private final boolean allowTypedInput;
  private final boolean useCancel;

  public final static String[] NUMBERS = new String[] { "1\u20E3", "2\u20E3", "3\u20E3",
          "4\u20E3", "5\u20E3", "6\u20E3", "7\u20E3", "8\u20E3", "9\u20E3", "\uD83D\uDD1F" };
  public final static String[] LETTERS = new String[] { "\uD83C\uDDE6", "\uD83C\uDDE7", "\uD83C\uDDE8",
          "\uD83C\uDDE9", "\uD83C\uDDEA", "\uD83C\uDDEB", "\uD83C\uDDEC", "\uD83C\uDDED", "\uD83C\uDDEE",
          "\uD83C\uDDEF" };
  public final static String CANCEL = "\u274C";

  protected OrderedMenu(EventWaiter waiter, Set<IUser> users, Set<IRole> roles, long timeout, TimeUnit unit,
                        Color color, String text, String description, List<String> choices, Consumer<Integer> action,
                        Runnable cancel,
                        boolean useLetters, boolean allowTypedInput, boolean useCancel, IDiscordClient client) {
    super(waiter, users, roles, timeout, unit, client);
    this.color = color;
    this.text = text;
    this.description = description;
    this.choices = choices;
    this.action = action;
    this.cancel = cancel;
    this.useLetters = useLetters;
    this.allowTypedInput = allowTypedInput;
    this.useCancel = useCancel;
  }

  /**
   * Shows the OrderedMenu as a new {@link IMessage}
   * in the provided {@link IChannel}.
   *
   * @param channel The IChannel to send the new IMessage to
   */
  @Override
  public void display(IChannel channel) {
    if (!channel.isPrivate()
                && !allowTypedInput
                && !channel.getModifiedPermissions(client.getOurUser()).contains(Permissions.ADD_REACTIONS)) {
      throw new MissingPermissionsException("Must be able to add reactions if not allowing typed input!", EnumSet.of(Permissions.ADD_REACTIONS));
    }
    initialize(RequestBuffer.request(() -> {return getMessage(channel).build();}).get());
  }

  /**
   * Displays this OrderedMenu by editing the provided
   * {@link IMessage}.
   *
   * @param message The IMessage to display the Menu in
   */
  @Override
  public void display(IMessage message) {
    if (!message.getChannel().isPrivate()
                && !allowTypedInput
                && !message.getChannel().getModifiedPermissions(client.getOurUser()).contains(Permissions
                                                                                                      .ADD_REACTIONS)) {
      throw new MissingPermissionsException("Must be able to add reactions if not allowing typed input!", EnumSet.of(Permissions.ADD_REACTIONS));
    }
    MessageBuilder builder = getMessage(message.getChannel());
    initialize(RequestBuffer.request(() -> {
      return message.edit(builder.getContent(), builder.getEmbedObject());
    }).get());
  }

  private void initialize(IMessage m) {
    try {
      for (int i = 1; i <= choices.size(); i++) {
        if (i < choices.size()) {
          ReactionEmoji reaction = ReactionEmoji.of(getEmoji(i));
          RequestBuffer.request(() -> m.addReaction(reaction));
        } else {
          ReactionEmoji reaction = ReactionEmoji.of(getEmoji(i));
          RequestBuffer.IVoidRequest request = () -> m.addReaction(reaction);
          if (useCancel) {
            RequestBuffer.request(request);
            request = () -> m.addReaction(ReactionEmoji.of(CANCEL));
          }
          RequestBuffer.request(request).get();
          if (allowTypedInput) {
            waitGeneric(m);
          } else {
            waitReactionOnly(m);
          }
        }
      }
    } catch (MissingPermissionsException ex) {
      if (allowTypedInput) {
        waitGeneric(m);
      } else {
        waitReactionOnly(m);
      }
    }
  }

  private void waitGeneric(IMessage m) {
    waiter.waitForEvent(Event.class, e -> {
      if (e instanceof ReactionAddEvent) {
        return isValidReaction(m, (ReactionAddEvent) e);
      }
      if (e instanceof MessageReceivedEvent) {
        return isValidIMessage(m, (MessageReceivedEvent) e);
      }
      return false;
    }, e -> {
      RequestBuffer.request(() -> m.delete());
      if (e instanceof ReactionAddEvent) {
        ReactionAddEvent event = (ReactionAddEvent) e;
        if (event.getReaction().getEmoji().getName().equals(CANCEL)) {
          cancel.run();
        } else {
          action.accept(getNumber(event.getReaction().getEmoji().getName()));
        }
      } else if (e instanceof MessageReceivedEvent) {
        MessageReceivedEvent event = (MessageReceivedEvent) e;
        int num = getIMessageNumber(event.getMessage().getContent());
        if (num < 0 || num > choices.size()) {
          cancel.run();
        } else {
          action.accept(num);
        }
      }
    }, timeout, unit, cancel);
  }

  private void waitReactionOnly(IMessage m) {
    waiter.waitForEvent(ReactionAddEvent.class, e -> {
      return isValidReaction(m, e);
    }, e -> {
      RequestBuffer.request(() -> m.delete());
      if (e.getReaction().getEmoji().getName().equals(CANCEL)) {
        cancel.run();
      } else {
        action.accept(getNumber(e.getReaction().getEmoji().getName()));
      }
    }, timeout, unit, cancel);
  }

  private MessageBuilder getMessage(IChannel channel) {
    MessageBuilder mbuilder = new MessageBuilder(client);
    if (text != null) {
      mbuilder.withContent(text);
    }
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < choices.size(); i++) {
      sb.append("\n").append(getEmoji(i + 1)).append(" ").append(choices.get(i));
    }
    mbuilder.withEmbed(new EmbedBuilder().withColor(color).withDesc(description == null ? sb.toString() :
                                                                            description + sb.toString()).build());
    mbuilder.withChannel(channel);
    return mbuilder;
  }

  private boolean isValidReaction(IMessage m, ReactionAddEvent e) {
    if (!e.getMessage().getStringID().equals(m.getStringID())) {
      return false;
    }
    if (!isValidUser(e)) {
      return false;
    }
    if (e.getReaction().getEmoji().getName().equals(CANCEL)) {
      return true;
    }
    int num = getNumber(e.getReaction().getEmoji().getName());
    return !(num < 0 || num > choices.size());
  }

  private boolean isValidIMessage(IMessage m, MessageReceivedEvent e) {
    if (!e.getChannel().equals(m.getChannel())) {
      return false;
    }
    return isValidUser(e);
  }

  private String getEmoji(int number) {
    if (useLetters) {
      return LETTERS[number - 1];
    } else {
      return NUMBERS[number - 1];
    }
  }

  private int getNumber(String emoji) {
    String[] array = useLetters ? LETTERS : NUMBERS;
    for (int i = 0; i < array.length; i++) {
      if (array[i].equals(emoji)) {
        return i + 1;
      }
    }
    return -1;
  }

  private int getIMessageNumber(String message) {
    if (useLetters) {
      return message.length() == 1 ? " abcdefghij".indexOf(message.toLowerCase()) : -1;
    } else {
      if (message.length() == 1) {
        return " 123456789".indexOf(message);
      }
      return message.equals("10") ? 10 : -1;
    }
  }
}