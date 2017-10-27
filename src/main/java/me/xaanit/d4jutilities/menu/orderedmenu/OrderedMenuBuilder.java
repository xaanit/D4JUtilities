package me.xaanit.d4jutilities.menu.orderedmenu;

import sx.blah.discord.api.IDiscordClient;
import me.xaanit.d4jutilities.menu.*;


import java.awt.Color;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author John Grosh
 */
public class OrderedMenuBuilder extends MenuBuilder<OrderedMenuBuilder, OrderedMenu> {

  private Color color;
  private String text;
  private String description;
  private final List<String> choices = new LinkedList<>();
  private Consumer<Integer> action;
  private Runnable cancel = () -> {};
  private boolean useLetters = false;
  private boolean allowTypedInput = true;
  private boolean addCancel = false;
  private IDiscordClient client;

  @Override
  public OrderedMenu build() {
    if (waiter == null) {
      throw new IllegalArgumentException("Must set an EventWaiter");
    }
    if (choices.isEmpty()) {
      throw new IllegalArgumentException("Must have at least one choice");
    }
    if (choices.size() > 10) {
      throw new IllegalArgumentException("Must have no more than ten choices");
    }
    if (action == null) {
      throw new IllegalArgumentException("Must provide an action consumer");
    }
    if (text == null && description == null) {
      throw new IllegalArgumentException("Either text or description must be set");
    }
    if (client == null) {
      throw new IllegalArgumentException("Client can not be null!");
    }
    return new OrderedMenu(waiter, users, roles, timeout, unit, color, text, description, choices,
                                  action, cancel, useLetters, allowTypedInput, addCancel, client);
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
  public OrderedMenuBuilder setColor(Color color) {
    this.color = color;
    return this;
  }

  /**
   * Sets the builder to build an {@link OrderedMenu}
   * using letters for ordering and reactions (IE: A, B, C, etc.).
   * <br>As a note - by default the builder will use <b>numbers</b> not letters.
   *
   * @return This builder
   */
  public OrderedMenuBuilder useLetters() {
    this.useLetters = true;
    return this;
  }

  /**
   * Sets the builder to build an {@link OrderedMenu}
   * using numbers for ordering and reactions (IE: A, B, C, etc.).
   *
   * @return This builder
   */
  public OrderedMenuBuilder useNumbers() {
    this.useLetters = false;
    return this;
  }

  /**
   * If {@code true}, {@link sx.blah.discord.handle.obj.IUser}s can type the number or
   * letter of the input to make their selection, in addition to the reaction option.
   *
   * @param allow {@code true} if raw text input is allowed, {@code false} if it is not
   *
   * @return This builder
   */
  public OrderedMenuBuilder allowTextInput(boolean allow) {
    this.allowTypedInput = allow;
    return this;
  }

  /**
   * If {@code true}, adds a cancel button that performs the timeout action when selected.
   *
   * @param use {@code true} if the cancel button should be shown, {@code false} if it should not
   *
   * @return This builder
   */
  public OrderedMenuBuilder useCancelButton(boolean use) {
    this.addCancel = use;
    return this;
  }

  /**
   * Sets the text of the {@link sx.blah.discord.handle.obj.IMessage} to be displayed
   * when the {@link OrderedMenu} is built.
   * <p>
   * <p>This is displayed directly above the embed.
   *
   * @param text The Message content to be displayed above the embed when the OrderedMenu is built
   *
   * @return This builder
   */
  public OrderedMenuBuilder setText(String text) {
    this.text = text;
    return this;
  }

  /**
   * Sets the description to be placed in an {@link sx.blah.discord.api.internal.json.objects.EmbedObject}.
   * <br>If this is {@code null}, no MessageEmbed will be displayed
   *
   * @param description The content of the MessageEmbed's description
   *
   * @return This builder
   */
  public OrderedMenuBuilder setDescription(String description) {
    this.description = description;
    return this;
  }

  /**
   * Sets the {@link Consumer Consumer} action to perform upon selecting a option.
   *
   * @param action The Consumer action to perform upon selecting a button
   *
   * @return This builder
   */
  public OrderedMenuBuilder setAction(Consumer<Integer> action) {
    this.action = action;
    return this;
  }

  /**
   * Sets the {@link Runnable Runnable} to perform if the
   * {@link OrderedMenu} times out.
   *
   * @param cancel The Runnable action to perform if the ButtonMenu times out
   *
   * @return This builder
   */
  public OrderedMenuBuilder setCancel(Runnable cancel) {
    this.cancel = cancel;
    return this;
  }

  /**
   * Adds the String choices.
   * <br>These correspond to the button in order of addition.
   *
   * @param choices The String choices to add
   *
   * @return This builder
   */
  public OrderedMenuBuilder addChoices(String... choices) {
    this.choices.addAll(Arrays.asList(choices));
    return this;
  }

  /**
   * Sets the String choices.
   * <br>These correspond to the button in the order they are set.
   *
   * @param choices The String choices to set
   *
   * @return This builder
   */
  public OrderedMenuBuilder setChoices(String... choices) {
    this.choices.clear();
    this.choices.addAll(Arrays.asList(choices));
    return this;
  }

  /**
   * Sets the {@link IDiscordClient} for message builders.
   *
   * @param client The client to use
   *
   * @return This builder
   */
  public OrderedMenuBuilder setClient(IDiscordClient client) {
    this.client = client;
    return this;
  }
}
