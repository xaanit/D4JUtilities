package me.xaanit.d4jutilities.menu;

import me.xaanit.d4jutilities.waiter.EventWaiter;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;

import java.awt.Color;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author John Grosh
 * @editor Jacob (xaanit)
 */
public abstract class MenuBuilder<T extends MenuBuilder<T, V>, V extends Menu> {
  protected EventWaiter waiter;
  protected Set<IUser> users = new HashSet<>();
  protected Set<IRole> roles = new HashSet<>();
  protected long timeout = -1;
  protected TimeUnit unit = TimeUnit.MINUTES;

  /**
   * Builds the {@link me.xaanit.d4jutilities.menu.Menu Menu} corresponding to
   * this {@link me.xaanit.d4jutilities.menu.MenuBuilder MenuBuilder}.
   * <br>After doing this, no modifications of the displayed Menu can be made.
   *
   * @return The built Menu of corresponding type to this MenuBuilder.
   */
  public abstract V build();

  /**
   * Sets the {@link Color Color} of the {@link sx.blah.discord.api.internal.json.objects.EmbedObject},
   * if description of the MessageEmbed is set.
   *
   * @param color The Color of the MessageEmbed
   *
   * @return This builder
   */
  public abstract T setColor(Color color);

  /**
   * Sets the {@link EventWaiter}
   * that will do {@link Menu} operations.
   * <p>
   * <p><b>NOTE:</b> All Menus will only work with an EventWaiter set!
   * <br>Not setting an EventWaiter means the Menu will not work.
   *
   * @param waiter The EventWaiter
   *
   * @return This builder
   */
  public final T setEventWaiter(EventWaiter waiter) {
    this.waiter = waiter;
    return (T) this;
  }

  /**
   * Adds {@link IUser}s that are allowed to use the
   * {@link Menu} that will be built.
   *
   * @param users The IUsers allowed to use the Menu
   *
   * @return This builder
   */
  public final T addUsers(IUser... users) {
    this.users.addAll(Arrays.asList(users));
    return (T) this;
  }

  /**
   * Sets {@link net.dv8tion.jda.core.entities.IUser IUser}s that are allowed to use the
   * {@link com.jagrosh.jdautilities.menu.Menu Menu} that will be built.
   * <br>This clears any IUsers already registered before adding the ones specified.
   *
   * @param users The IUsers allowed to use the Menu
   *
   * @return This builder
   */
  public final T setUsers(IUser... users) {
    this.users.clear();
    this.users.addAll(Arrays.asList(users));
    return (T) this;
  }

  /**
   * Adds {@link net.dv8tion.jda.core.entities.IRole IRole}s that are allowed to use the
   * {@link com.jagrosh.jdautilities.menu.Menu Menu} that will be built.
   *
   * @param roles The IRoles allowed to use the Menu
   *
   * @return This builder
   */
  public final T addRoles(IRole... roles) {
    this.roles.addAll(Arrays.asList(roles));
    return (T) this;
  }

  /**
   * Sets {@link net.dv8tion.jda.core.entities.IRole IRole}s that are allowed to use the
   * {@link com.jagrosh.jdautilities.menu.Menu Menu} that will be built.
   * <br>This clears any IRoles already registered before adding the ones specified.
   *
   * @param roles The IRoles allowed to use the Menu
   *
   * @return This builder
   */
  public final T setRoles(IRole... roles) {
    this.roles.clear();
    this.roles.addAll(Arrays.asList(roles));
    return (T) this;
  }

  /**
   * Sets the timeout that the {@link com.jagrosh.jdautilities.menu.Menu Menu} should
   * stay available.
   * <p>
   * <p>After this has expired, the a final action in the form of a
   * {@link Runnable} may execute.
   *
   * @param timeout The amount of time for the Menu to stay available
   * @param unit    The {@link TimeUnit TimeUnit} for the timeout
   *
   * @return This builder
   */
  public final T setTimeout(long timeout, TimeUnit unit) {
    this.timeout = timeout;
    this.unit = unit;
    return (T) this;
  }
}