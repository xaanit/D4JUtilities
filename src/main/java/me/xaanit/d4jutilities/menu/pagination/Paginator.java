package me.xaanit.d4jutilities.menu.pagination;

import me.xaanit.d4jutilities.menu.Menu;
import me.xaanit.d4jutilities.waiter.EventWaiter;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent;
import sx.blah.discord.handle.impl.obj.ReactionEmoji;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.MessageBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RequestBuffer;

import java.awt.Color;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Consumer;


/**
 * @author John Grosh
 */
public class Paginator extends Menu {

  private final BiFunction<Integer, Integer, Color> color;
  private final BiFunction<Integer, Integer, String> text;
  private final int columns;
  private final int itemsPerPage;
  private final boolean showPageNumbers;
  private final boolean numberItems;
  private final List<String> strings;
  private final int pages;
  private final Consumer<IMessage> finalAction;
  private final boolean waitOnSinglePage;

  public static final String LEFT = "⬅";
  public static final String STOP = "\u23F9";
  public static final String RIGHT = "➡";

  protected Paginator(EventWaiter waiter, Set<IUser> users, Set<IRole> roles, long timeout, TimeUnit unit,
                      BiFunction<Integer, Integer, Color> color, BiFunction<Integer, Integer, String> text,
                      Consumer<IMessage> finalAction,
                      int columns, int itemsPerPage, boolean showPageNumbers, boolean numberItems, List<String>
                                                                                                           items,
                      boolean waitOnSinglePage, IDiscordClient client) {
    super(waiter, users, roles, timeout, unit, client);
    this.color = color;
    this.text = text;
    this.columns = columns;
    this.itemsPerPage = itemsPerPage;
    this.showPageNumbers = showPageNumbers;
    this.numberItems = numberItems;
    this.strings = items;
    this.pages = (int) Math.ceil((double) strings.size() / itemsPerPage);
    this.finalAction = finalAction;
    this.waitOnSinglePage = waitOnSinglePage;
  }

  /**
   * Begins pagination on page 1 as a new {@link IMessage}
   * in the provided {@link IChannel}.
   *
   * @param channel The IChannel to send the new IMessage to
   */
  @Override
  public void display(IChannel channel) {
    paginate(channel, 1);
  }

  /**
   * Begins pagination on page 1 displaying this Pagination by editing the provided
   * {@link IMessage}.
   *
   * @param message The IMessage to display the Menu in
   */
  @Override
  public void display(IMessage message) {
    paginate(message, 1);
  }

  /**
   * Begins pagination as a new {@link IMessage}
   * in the provided {@link IChannel}, starting
   * on whatever page number is provided.
   *
   * @param channel The IChannel to send the new IMessage to
   * @param pageNum The page number to begin on
   */
  public void paginate(IChannel channel, int pageNum) {
    if (pageNum < 1) {
      pageNum = 1;
    } else if (pageNum > pages) {
      pageNum = pages;
    }
    MessageBuilder msg = renderPage(pageNum, channel);
    initialize(RequestBuffer.request(() -> {
      return msg.build();
    }).get(), pageNum);
  }

  /**
   * Begins pagination displaying this Pagination by editing the provided
   * {@link IMessage}, starting on whatever
   * page number is provided.
   *
   * @param message The IChannel to send the new IMessage to
   * @param pageNum The page number to begin on
   */
  public void paginate(IMessage message, int pageNum) {
    if (pageNum < 1) {
      pageNum = 1;
    } else if (pageNum > pages) {
      pageNum = pages;
    }
    MessageBuilder msg = renderPage(pageNum, message.getChannel());
    initialize(RequestBuffer.request(() -> {
      return message.edit(msg.getContent(), msg.getEmbedObject());
    }).get(), pageNum);
  }

  private void initialize(IMessage m, int pageNum) {
    if (pages > 1) {
      RequestBuffer.request(() -> m.addReaction(ReactionEmoji.of(LEFT))).get();
      RequestBuffer.request(() -> m.addReaction(ReactionEmoji.of(STOP))).get();
      RequestBuffer.request(() -> m.addReaction(ReactionEmoji.of(RIGHT))).get();
      pagination(m, pageNum);
    } else if (waitOnSinglePage) {
      RequestBuffer.request(() -> m.addReaction(ReactionEmoji.of(STOP))).get();
      pagination(m, pageNum);
    } else {
      finalAction.accept(m);
    }
  }

  private void pagination(IMessage message, int pageNum) {
    waiter.waitForEvent(ReactionAddEvent.class, (ReactionAddEvent event) -> {
      if (!event.getMessage().getStringID().equals(message.getStringID())) {
        return false;
      }
      if (!(LEFT.equals(event.getReaction().getEmoji().getName())
                    || STOP.equals(event.getReaction().getEmoji().getName())
                    || RIGHT.equals(event.getReaction().getEmoji().getName()))) {
        return false;
      }
      return isValidUser(event);
    }, event -> {
      int newPageNum = pageNum;
      switch (event.getReaction().getEmoji().getName()) {
        case LEFT:
          if (newPageNum > 1) {
            newPageNum--;
          }
          break;
        case RIGHT:
          if (newPageNum < pages) {
            newPageNum++;
          }
          break;
        case STOP:
          finalAction.accept(message);
          return;
      }
      RequestBuffer.request(() -> {
        try {
          event.getMessage().removeReaction(event.getUser(), event.getReaction());
        } catch (MissingPermissionsException e) {
        }
      });
      int n = newPageNum;
      final int finalPageNum = newPageNum;
      final MessageBuilder builder = renderPage(finalPageNum, message.getChannel());
      IMessage m = RequestBuffer.request(() -> {
        return message.edit(builder.getContent(), builder.getEmbedObject());
      }).get();
      pagination(m, n);
    }, timeout, unit, () -> finalAction.accept(message));
  }

  private MessageBuilder renderPage(int pageNum, IChannel channel) {
    MessageBuilder mbuilder = new MessageBuilder(client).withChannel(channel);
    EmbedBuilder ebuilder = new EmbedBuilder();
    int start = (pageNum - 1) * itemsPerPage;
    int end = strings.size() < pageNum * itemsPerPage ? strings.size() : pageNum * itemsPerPage;
    switch (columns) {
      case 1:
        StringBuilder sbuilder = new StringBuilder();
        for (int i = start; i < end; i++) {
          sbuilder.append("\n").append(numberItems ? "`" + (i + 1) + ".` " : "").append(strings.get(i));
        }
        ebuilder.withDesc(sbuilder.toString());
        break;
      default:
        int per = (int) Math.ceil((double) (end - start) / columns);
        for (int k = 0; k < columns; k++) {
          StringBuilder strbuilder = new StringBuilder();
          for (int i = start + k * per; i < end && i < start + (k + 1) * per; i++) {
            strbuilder.append("\n").append(numberItems ? (i + 1) + ". " : "").append(strings.get(i));
          }
          String str = strbuilder.toString();
          ebuilder.appendField("\u200B", str.isEmpty() ? "\u200B" : str, true);
        }
    }

    ebuilder.withColor(color.apply(pageNum, pages));
    if (showPageNumbers) {
      ebuilder.withFooterText("Page " + pageNum + "/" + pages);
    }
    mbuilder.withEmbed(ebuilder.build());
    if (text != null) {
      mbuilder.withContent(text.apply(pageNum, pages));
    }
    return mbuilder;
  }
}