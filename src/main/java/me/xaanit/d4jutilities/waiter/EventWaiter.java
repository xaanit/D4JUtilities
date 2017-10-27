package me.xaanit.d4jutilities.waiter;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.Event;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.api.events.IListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A simple object used primarily for entities found in {@link me.xaanit.d4jutilities.menu}.
 * <p>
 * <p>The EventWaiter is capable of handling specialized forms of {@link sx.blah.discord.api.events.Event Event}
 * that must meet criteria not normally specifiable without implementation of an
 * {@link sx.blah.discord.api.events.IListener IListener}.
 * <p>
 * <p>If you intend to use the EventWaiter, it is highly recommended you <b>DO NOT create multiple EventWaiters</b>!
 * Doing this will cause unnecessary increases in memory usage.
 *
 * @author John Grosh (jagrosh)
 * @editor Jacob (xaanit)
 */
public class EventWaiter implements IListener<Event> {
  private final HashMap<Class<?>, List<WaitingEvent>> waitingEvents;
  private final ScheduledExecutorService threadpool;
  private final IDiscordClient client;

  /**
   * Constructs an empty EventWaiter.
   */
  public EventWaiter(IDiscordClient client) {
    this.client = client;
    this.client.getDispatcher().registerListener(this);
    waitingEvents = new HashMap<>();
    threadpool = Executors.newSingleThreadScheduledExecutor();
  }

  /**
   * Waits an indefinite amount of time for an {@link sx.blah.discord.api.events.Event Event} that
   * returns {@code true} when tested with the provided {@link Predicate Predicate}.
   * <p>
   * <p>When this occurs, the provided {@link Consumer Consumer} will accept and
   * execute using the same Event.
   *
   * @param <T>       The type of Event to wait for
   * @param classType The {@link Class} of the Event to wait for
   * @param condition The Predicate to test when Events of the provided type are thrown
   * @param action    The Consumer to perform an action when the condition Predicate returns {@code true}
   */
  public <T extends Event> void waitForEvent(Class<T> classType, Predicate<T> condition, Consumer<T> action) {
    waitForEvent(classType, condition, action, -1, null, null);
  }

  /**
   * Waits a predetermined amount of time for an {@link sx.blah.discord.api.events.Event Event} that
   * returns {@code true} when tested with the provided {@link Predicate Predicate}.
   * <p>
   * <p>Once started, there are two possible outcomes:
   * <ul>
   * <li>The correct Event occurs within the time alloted, and the provided
   * {@link Consumer Consumer} will accept and execute using the same Event.</li>
   * <p>
   * <li>The time limit is elapsed and the provided {@link Runnable} is executed.</li>
   * </ul>
   *
   * @param <T>           The type of Event to wait for
   * @param classType     The {@link Class} of the Event to wait for
   * @param condition     The Predicate to test when Events of the provided type are thrown
   * @param action        The Consumer to perform an action when the condition Predicate returns {@code true}
   * @param timeout       The maximum amount of time to wait for
   * @param unit          The {@link TimeUnit TimeUnit} measurement of the timeout
   * @param timeoutAction The Runnable to run if the time runs out before a correct Event is thrown
   */
  public <T extends Event> void waitForEvent(Class<T> classType, Predicate<T> condition, Consumer<T> action, long timeout, TimeUnit unit, Runnable timeoutAction) {
    List<WaitingEvent> list;
    if (waitingEvents.containsKey(classType)) {
      list = waitingEvents.get(classType);
    } else {
      list = new ArrayList<>();
      waitingEvents.put(classType, list);
    }
    WaitingEvent we = new WaitingEvent<>(condition, action);
    list.add(we);
    if (timeout > 0 && unit != null) {
      threadpool.schedule(() -> {
        if (list.remove(we) && timeoutAction != null) {
          timeoutAction.run();
        }
      }, timeout, unit);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  @EventSubscriber
  public final void handle(Event event) {
    Class c = event.getClass();
    while (c.getSuperclass() != null) {
      if (waitingEvents.containsKey(c)) {
        List<WaitingEvent> list = waitingEvents.get(c);
        List<WaitingEvent> ulist = new ArrayList<>(list);
        list.removeAll(ulist.stream().filter(i -> i.attempt(event)).collect(Collectors.toList()));
      }
      c = c.getSuperclass();
    }
  }

  private class WaitingEvent<T extends Event> {
    final Predicate<T> condition;
    final Consumer<T> action;

    WaitingEvent(Predicate<T> condition, Consumer<T> action) {
      this.condition = condition;
      this.action = action;
    }

    boolean attempt(T event) {
      if (condition.test(event)) {
        action.accept(event);
        return true;
      }
      return false;
    }
  }
}