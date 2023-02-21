/*
 * [New BSD License]
 * Copyright (c) 2011-2012, Brackit Project Team <info@brackit.org>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Brackit Project Team nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.brackit.xquery.block;

import org.brackit.xquery.QueryContext;
import org.brackit.xquery.QueryException;
import org.brackit.xquery.Tuple;
import org.brackit.xquery.jdm.Expr;

/**
 * @author Sebastian Baechle
 */
public class LetBind implements Block {
  final Expr expr;

  public LetBind(Expr expr) {
    this.expr = expr;
  }

  @Override
  public Sink create(QueryContext ctx, Sink sink) throws QueryException {
    return new LetBindSink(ctx, sink);
  }

  @Override
  public int outputWidth(int inputWidth) {
    return inputWidth + 1;
  }

  private class LetBindSink implements Sink {
    final QueryContext ctx;
    final Sink sink;

    public LetBindSink(QueryContext ctx, Sink sink) {
      this.ctx = ctx;
      this.sink = sink;
    }

    @Override
    public void output(Tuple[] buf, int len) throws QueryException {
      for (int i = 0; i < len; i++) {
        Tuple t = buf[i];
        buf[i] = t.concat(expr.evaluate(ctx, t));
      }
      sink.output(buf, len);
    }

    @Override
    public Sink fork() {
      return new LetBindSink(ctx, sink.fork());
    }

    @Override
    public Sink partition(Sink stopAt) {
      return new LetBindSink(ctx, sink.partition(stopAt));
    }

    @Override
    public void end() throws QueryException {
      sink.end();
    }

    @Override
    public void begin() throws QueryException {
      sink.begin();
    }

    @Override
    public void fail() throws QueryException {
      sink.fail();
    }
  }
}