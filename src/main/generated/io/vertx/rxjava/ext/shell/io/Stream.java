/*
 * Copyright 2014 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.vertx.rxjava.ext.shell.io;

import java.util.Map;
import io.vertx.lang.rxjava.InternalHelper;
import rx.Observable;
import io.vertx.core.json.JsonObject;
import io.vertx.core.Handler;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 *
 * <p/>
 * NOTE: This class has been automatically generated from the {@link io.vertx.ext.shell.io.Stream original} non RX-ified interface using Vert.x codegen.
 */

public class Stream {

  final io.vertx.ext.shell.io.Stream delegate;

  public Stream(io.vertx.ext.shell.io.Stream delegate) {
    this.delegate = delegate;
  }

  public Object getDelegate() {
    return delegate;
  }

  public static Stream ofString(Handler<String> handler) { 
    Stream ret= Stream.newInstance(io.vertx.ext.shell.io.Stream.ofString(handler));
    return ret;
  }

  public static Stream ofJson(Handler<JsonObject> handler) { 
    Stream ret= Stream.newInstance(io.vertx.ext.shell.io.Stream.ofJson(handler));
    return ret;
  }

  public Stream write(String data) { 
    this.delegate.write(data);
    return this;
  }

  public Stream write(JsonObject data) { 
    this.delegate.write(data);
    return this;
  }


  public static Stream newInstance(io.vertx.ext.shell.io.Stream arg) {
    return arg != null ? new Stream(arg) : null;
  }
}