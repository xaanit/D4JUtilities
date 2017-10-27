package me.xaanit.d4jutilities.menu.pagination;

import me.xaanit.d4jutilities.menu.MenuBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.RequestBuffer;

import java.awt.Color;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;


/**
 * @author John Grosh
 */
public class PaginatorBuilder extends MenuBuilder<PaginatorBuilder, Paginator> {

  private BiFunction<Integer, Integer, Color> color = (page, pages) -> null;
  private BiFunction<Integer, Integer, String> text = (page, pages) -> "";
  private Consumer<IMessage> finalAction = m -> RequestBuffer.request(() -> m.delete());
  private int columns = 1;
  private int itemsPerPage = 12;
  private boolean showPageNumbers = true;
  private boolean numberItems = false;
  private boolean waitOnSinglePage = false;
  private IDiscordClient client;

  private final List<String> strings = new LinkedList<>();

  @Override
  public Paginator build() {
    if (waiter == null) {
      throw new IllegalArgumentException("Must set an EventWaiter");
    }
    if (strings.isEmpty()) {
      throw new IllegalArgumentException("Must include at least one item to paginate");
    }
    if (client == null) {
      throw new IllegalArgumentException("Client can not be null");
    }
    return new Paginator(waiter, users, roles, timeout, unit, color, text, finalAction,
                                columns, itemsPerPage, showPageNumbers, numberItems, strings, waitOnSinglePage, client);
  }

  /**
   * Sets the {@link Color Color} of the {@link sx.blah.discord.api.internal.json.objects.EmbedObject},
   * if description of the MessageEmbed is set.
   *
   * @param color The Color of the MessageEmbed
   *
   * @return This builder
   */
  @Override
  public PaginatorBuilder setColor(Color color) {
    this.color = (i0, i1) -> color;
    return this;
  }

  /**
   * Sets the {@link Color Color} of the {@link sx.blah.discord.api.internal.json.objects.EmbedObject},
   * relative to the total page number and the current page as determined by the provided
   * {@link BiFunction BiFunction}.
   * <br>As the page changes, the BiFunction will re-process the current page number and the total
   * page number, allowing for the color of the embed to change depending on the page number.
   *
   * @param colorBiFunction A BiFunction that uses both current and total page numbers to get a Color for the
   *                        MessageEmbed
   *
   * @return This builder
   */
  public PaginatorBuilder setColor(BiFunction<Integer, Integer, Color> colorBiFunction) {
    this.color = colorBiFunction;
    return this;
  }

  /**
   * Sets the text of the {@link IMessage} to be displayed
   * when the {@link Paginator} is built.
   * <p>
   * <p>This is displayed directly above the embed.
   *
   * @param text The Message content to be displayed above the embed when the Paginator is built
   *
   * @return This builder
   */
  public PaginatorBuilder setText(String text) {
    this.text = (i0, i1) -> text;
    return this;
  }

  /**
   * Sets the text of the {@link IMessage} to be displayed
   * relative to the total page number and the current page as determined by the provided
   * {@link BiFunction BiFunction}.
   * <br>As the page changes, the BiFunction will re-process the current page number and the total
   * page number, allowing for the displayed text of the Message to change depending on the page number.
   *
   * @param textBiFunction The BiFunction that uses both current and total page numbers to get text for the Message
   *
   * @return This builder
   */
  public PaginatorBuilder setText(BiFunction<Integer, Integer, String> textBiFunction) {
    this.text = textBiFunction;
    return this;
  }

  /**
   * Sets the {@link Consumer Consumer} to perform if the
   * {@link Paginator} times out.
   *
   * @param finalAction The Consumer action to perform if the Paginator times out
   *
   * @return This builder
   */
  public PaginatorBuilder setFinalAction(Consumer<IMessage> finalAction) {
    this.finalAction = finalAction;
    return this;
  }

  /**
   * Sets the number of columns each page will have.
   * <br>By default this is 1.
   *
   * @param columns The number of columns
   *
   * @return This builder
   */
  public PaginatorBuilder setColumns(int columns) {
    if (columns < 1 || columns > 3) {
      throw new IllegalArgumentException("Only 1, 2, or 3 columns are supported");
    }
    this.columns = columns;
    return this;
  }

  /**
   * Sets the number of items that will appear on each page.
   *
   * @param num Always positive, never-zero number of items per page
   *
   * @return This builder
   *
   * @throws IllegalArgumentException If the provided number is less than 1
   */
  public PaginatorBuilder setItemsPerPage(int num) {
    if (num < 1) {
      throw new IllegalArgumentException("There must be at least one item per page");
    }
    this.itemsPerPage = num;
    return this;
  }

  /**
   * Sets whether or not the page number will be shown.
   *
   * @param show {@code true} if the page number should be shown, {@code false} if it should not
   *
   * @return This builder
   */
  public PaginatorBuilder showPageNumbers(boolean show) {
    this.showPageNumbers = show;
    return this;
  }

  /**
   * Sets whether or not the items will be automatically numbered.
   *
   * @param number {@code true} if the items should be numbered, {@code false} if it should not
   *
   * @return This builder
   */
  public PaginatorBuilder useNumberedItems(boolean number) {
    this.numberItems = number;
    return this;
  }

  /**
   * Sets whether the {@link Paginator} will instantly
   * timeout, and possibly run a provided {@link Runnable Runnable}, if only a single slide is available to
   * display.
   *
   * @param wait {@code true} if the Paginator will still generate
   *
   * @return This builder
   */
  public PaginatorBuilder waitOnSinglePage(boolean wait) {
    this.waitOnSinglePage = wait;
    return this;
  }

  /**
   * Clears the list of String items to paginate.
   *
   * @return This builder
   */
  public PaginatorBuilder clearItems() {
    strings.clear();
    return this;
  }

  /**
   * Adds String items to the list of items to paginate.
   *
   * @param items The String list of items to add
   *
   * @return This builder
   */
  public PaginatorBuilder addItems(String... items) {
    strings.addAll(Arrays.asList(items));
    return this;
  }

  /**
   * Sets the String list of items to paginate.
   * <br>This method clears all previously set items before setting.
   *
   * @param items The String list of items to paginate
   *
   * @return This builder
   */
  public PaginatorBuilder setItems(String... items) {
    strings.clear();
    strings.addAll(Arrays.asList(items));
    return this;
  }

  /**
   * Sets the {@link IDiscordClient} for message builders.
   *
   * @param client The client to use
   *
   * @return This builder
   */
  public PaginatorBuilder setClient(IDiscordClient client) {
    this.client = client;
    return this;
  }
}