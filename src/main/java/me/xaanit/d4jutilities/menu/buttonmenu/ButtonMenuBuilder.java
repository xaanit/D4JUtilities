package me.xaanit.d4jutilities.menu.buttonmenu;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.obj.ReactionEmoji;
import sx.blah.discord.handle.obj.IEmoji;
import me.xaanit.d4jutilities.menu.*;


import java.awt.Color;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;


/**
 * @author John Grosh
 * @edit Jacob (xaanit)
 */
public class ButtonMenuBuilder extends MenuBuilder<ButtonMenuBuilder, ButtonMenu> {

  private Color color;
  private String text;
  private String description;
  private final List<String> choices = new LinkedList<>();
  private Consumer<ReactionEmoji> action;
  private Runnable cancel = () -> {};
  private IDiscordClient client;

  @Override
  public ButtonMenu build() {
    if (waiter == null) {
      throw new IllegalArgumentException("Must set an EventWaiter");
    }
    if (choices.isEmpty()) {
      throw new IllegalArgumentException("Must have at least one choice");
    }
    if (action == null) {
      throw new IllegalArgumentException("Must provide an action consumer");
    }
    if (text == null && description == null) {
      throw new IllegalArgumentException("Either text or description must be set");
    }
    if (client == null) {
      throw new IllegalArgumentException("Client can not be null");
    }
    return new ButtonMenu(waiter, users, roles, timeout, unit, color, text, description, choices, action, cancel,
                                 client);
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
  public ButtonMenuBuilder setColor(Color color) {
    this.color = color;
    return this;
  }

  /**
   * Sets the text of the {@link sx.blah.discord.handle.obj.IMessage} to be displayed
   * when the {@link ButtonMenu} is built.
   * <p>
   * <p>This is displayed directly above the embed.
   *
   * @param text The Message content to be displayed above the embed when the ButtonMenu is built
   *
   * @return This builder
   */
  public ButtonMenuBuilder setText(String text) {
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
  public ButtonMenuBuilder setDescription(String description) {
    this.description = description;
    return this;
  }

  /**
   * Sets the {@link Consumer Consumer} action to perform upon selecting a button.
   *
   * @param action The Consumer action to perform upon selecting a button
   *
   * @return This builder
   */
  public ButtonMenuBuilder setAction(Consumer<ReactionEmoji> action) {
    this.action = action;
    return this;
  }

  /**
   * Sets the {@link Runnable Runnable} to perform if the {@link ButtonMenu} times out.
   *
   * @param cancel The Runnable action to perform if the ButtonMenu times out
   *
   * @return This builder
   */
  public ButtonMenuBuilder setCancel(Runnable cancel) {
    this.cancel = cancel;
    return this;
  }

  /**
   * Adds String unicode emojis as button choices.
   * <p>
   * <p>Any non-unicode {@link sx.blah.discord.handle.obj.IEmoji}s should be
   * added using {@link ButtonMenuBuilder#addChoices(IEmoji...)}.
   *
   * @param emojis The String unicode emojis to add
   *
   * @return This builder
   */
  public ButtonMenuBuilder addChoices(String... emojis) {
    this.choices.addAll(Arrays.asList(emojis));
    return this;
  }

  /**
   * Adds custom {@link IEmoji}s as button choices.
   * <p>
   * <p>Any regular unicode emojis should be added using {@link ButtonMenuBuilder#addChoices(String...)}.
   *
   * @param emotes The Emote objects to add
   *
   * @return This builder
   */
  public ButtonMenuBuilder addChoices(IEmoji... emotes) {
    Arrays.asList(emotes).stream().map(e -> e.getStringID()).forEach(e -> this.choices.add(e));
    return this;
  }

  /**
   * Sets the String unicode emojis as button choices.
   * <p>
   * <p>Any non-unicode {@link IEmoji}s should be
   * set using {@link ButtonMenuBuilder#setChoices(IEmoji...)}.
   *
   * @param emojis The String unicode emojis to set
   *
   * @return This builder
   */
  public ButtonMenuBuilder setChoices(String... emojis) {
    this.choices.clear();
    this.choices.addAll(Arrays.asList(emojis));
    return this;
  }

  /**
   * Sets the {@link IEmoji}s as button choices.
   * <p>
   * <p>Any regular unicode emojis should be set using {@link ButtonMenuBuilder#addChoices(String...)}.
   *
   * @param emotes The Emote objects to set
   *
   * @return This builder
   */
  public ButtonMenuBuilder setChoices(IEmoji... emotes) {
    this.choices.clear();
    Arrays.asList(emotes).stream().map(e -> e.getStringID()).forEach(e -> this.choices.add(e));
    return this;
  }

  /**
   * Sets the {@link IDiscordClient} for message builders.
   *
   * @param client The client to use
   *
   * @return This builder
   */
  public ButtonMenuBuilder setClient(IDiscordClient client) {
    this.client = client;
    return this;
  }
}