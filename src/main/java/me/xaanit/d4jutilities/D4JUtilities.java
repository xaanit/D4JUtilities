package me.xaanit.d4jutilities;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.modules.IModule;

public class D4JUtilities implements IModule {
  @Override
  public boolean enable(IDiscordClient iDiscordClient) {
    System.out.println("D4J-Utilities. A D4J Port of https://github.com/JDA-Applications/JDA-Utilities");
    System.out.println("Made by " + getAuthor());
    return true;
  }

  @Override
  public void disable() {
  }

  @Override
  public String getName() {
    return "D4J-Utilities";
  }

  @Override
  public String getAuthor() {
    return "xaanit";
  }

  @Override
  public String getVersion() {
    return "1.0.0";
  }

  @Override
  public String getMinimumDiscord4JVersion() {
    return "2.9.1";
  }
}
