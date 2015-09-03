package io.vertx.ext.shell.command.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.shell.cli.CliToken;
import io.vertx.ext.shell.cli.Completion;
import io.vertx.ext.shell.command.Command;
import io.vertx.ext.shell.command.CommandManager;
import io.vertx.ext.shell.process.Process;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class CommandManagerImpl implements CommandManager {

  private static ConcurrentHashMap<Vertx, CommandManagerImpl> managers = new ConcurrentHashMap<>();

  public static CommandManager get(Vertx vertx) {
    CommandManagerImpl mgr = managers.computeIfAbsent(vertx, CommandManagerImpl::new);
    mgr.refCount.incrementAndGet();
    return mgr;
  }

  private AtomicInteger refCount = new AtomicInteger();
  private final Vertx vertx;
  private final ConcurrentHashMap<String, ManagedCommand> commandMap = new ConcurrentHashMap<>();

  public CommandManagerImpl(Vertx vertx) {
    this.vertx = vertx;
  }

  public Collection<ManagedCommand> commands() {
    return commandMap.values();
  }

  @Override
  public void registerCommand(Command command) {
    registerCommand(command, ar -> {
    });
  }

  @Override
  public void registerCommand(Command command, Handler<AsyncResult<Void>> handler) {
    Context context = vertx.getOrCreateContext();
    commandMap.put(command.name(), new ManagedCommand(vertx, context, (CommandImpl) command));
    handler.handle(Future.succeededFuture());
  }

  public ManagedCommand getCommand(String name) {
    return commandMap.get(name);
  }

  @Override
  public void release() {
    if (refCount.decrementAndGet() == 0) {
      managers.remove(vertx);
    }
  }

  @Override
  public void createProcess(String s, Handler<AsyncResult<io.vertx.ext.shell.process.Process>> handler) {
    createProcess(CliToken.tokenize(s), handler);
  }

  @Override
  public void createProcess(List<CliToken> line, Handler<AsyncResult<Process>> handler) {
    Process process;
    try {
      process = makeRequest(line);
    } catch (Exception e) {
      handler.handle(Future.failedFuture(e));
      return;
    }
    handler.handle(Future.succeededFuture(process));
  }

  private Process makeRequest(List<CliToken> s) {
    ListIterator<CliToken> tokens = s.listIterator();
    while (tokens.hasNext()) {
      CliToken token = tokens.next();
      if (token.isText()) {
        ManagedCommand command = getCommand(token.value());
        if (command == null) {
          throw new IllegalArgumentException(token.value() + ": command not found");
        }
        List<CliToken> remaining = new ArrayList<>();
        while (tokens.hasNext()) {
          remaining.add(tokens.next());
        }
        return command.createProcess(remaining);
      }
    }
    throw new IllegalArgumentException();
  }

  @Override
  public void complete(Completion completion) {
    LinkedList<CliToken> tokens = new LinkedList<>(completion.lineTokens());

    // Remove any leading white space
    while (tokens.size() > 0 && tokens.getFirst().isBlank()) {
      tokens.removeFirst();
    }

    // > 1 means it's a text token followed by something else
    if (tokens.size() > 1) {
      ListIterator<CliToken> it = tokens.listIterator();
      while (it.hasNext()) {
        CliToken ct = it.next();
        it.remove();
        if (ct.isText()) {
          List<CliToken> newTokens = new ArrayList<>();
          while (it.hasNext()) {
            newTokens.add(it.next());
          }
          StringBuilder tmp = new StringBuilder();
          newTokens.stream().forEach(token -> tmp.append(token.raw()));
          String line = tmp.toString();
          ManagedCommand command = getCommand(ct.value());
          if (command != null) {
            command.complete(new Completion() {
              @Override
              public String line() {
                return line;
              }
              @Override
              public List<CliToken> lineTokens() {
                return newTokens;
              }
              @Override
              public void complete(List<String> candidates) {
                completion.complete(candidates);
              }
              @Override
              public void complete(String value, boolean terminal) {
                completion.complete(value, terminal);
              }
            });
          } else {
            completion.complete(Collections.emptyList());
          }
        }
      }
    } else {
      String prefix = tokens.size() > 0 ? tokens.getFirst().value() : "";
      List<String> names = commands().
          stream().
          map(cmd -> cmd.command().name()).
          filter(name -> name.startsWith(prefix)).
          collect(Collectors.toList());
      if (names.size() == 1) {
        completion.complete(names.get(0).substring(prefix.length()), true);
      } else {
        String commonPrefix = Completion.findLongestCommonPrefix(names);
        if (commonPrefix.length() > prefix.length()) {
          completion.complete(commonPrefix.substring(prefix.length()), false);
        } else {
          completion.complete(names);
        }
      }
    }
  }
}
