package io.vertx.ext.shell.command;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.shell.Stream;
import io.vertx.ext.shell.Tty;
import io.vertx.ext.shell.cli.CliToken;

import java.util.List;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@VertxGen
public interface CommandProcess extends Tty {

  /**
   * @return the current Vert.x instance
   */
  Vertx vertx();

  /**
   * @return the unparsed arguments tokens
   */
  List<CliToken> argsTokens();

  /**
   * @return the actual string arguments of the command
   */
  List<String> args();

  @Fluent
  CommandProcess setStdin(Handler<String> stdin);

  @Fluent
  CommandProcess eventHandler(String event, Handler<Void> handler);

  @Fluent
  CommandProcess write(String text);

  /**
   * End the process with the exit status {@literal 0}
   */
  void end();

  /**
   * End the process.
   *
   * @param status the exit status.
   */
  void end(int status);

}
