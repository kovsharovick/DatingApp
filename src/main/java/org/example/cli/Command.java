package org.example.cli;

public interface Command {
    String getName();

    String getDescription();

    boolean isAdminOnly();

    void execute(CommandContext ctx, String[] args);
}
