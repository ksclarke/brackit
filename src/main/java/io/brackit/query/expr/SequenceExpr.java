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
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package io.brackit.query.expr;

import io.brackit.query.jsonitem.array.DArray;
import io.brackit.query.QueryContext;
import io.brackit.query.Tuple;
import io.brackit.query.sequence.FlatteningSequence;
import io.brackit.query.jdm.Expr;
import io.brackit.query.jdm.Item;
import io.brackit.query.jdm.Sequence;
import org.magicwerk.brownies.collections.GapList;

/**
 * @author Sebastian Baechle
 * @author Johannes Lichtenberger
 */
public final class SequenceExpr implements Expr {

  public final class EvalSequence extends FlatteningSequence {
    final Tuple tuple;
    final QueryContext ctx;
    final Sequence[] seqs;
    int eval;

    private EvalSequence(Tuple tuple, QueryContext ctx) {
      this.tuple = tuple;
      this.ctx = ctx;
      this.seqs = new Sequence[expr.length];
    }

    @Override
    protected Sequence sequence(int pos) {
      if (pos >= expr.length) {
        return null;
      }
      Sequence s = seqs[pos];
      if (s != null) {
        return s;
      }
      synchronized (seqs) {
        while ((s == null) && (eval < expr.length)) {
          s = seqs[pos] = expr[eval++].evaluate(ctx, tuple);
        }
      }
      return s;
    }
  }

  final Expr[] expr;

  public SequenceExpr(Expr... expr) {
    this.expr = expr;
  }

  @Override
  public Sequence evaluate(final QueryContext ctx, final Tuple tuple) {
    return new EvalSequence(tuple, ctx);
  }

  @Override
  public Item evaluateToItem(QueryContext ctx, Tuple tuple) {
    if (expr.length == 0) {
      return null;
    } else if (expr.length == 1) {
      return expr[0].evaluateToItem(ctx, tuple);
    } else {
      int i = 0;
      Item res = null;
      while (i < expr.length && (res = expr[i++].evaluateToItem(ctx, tuple)) == null)
        ;

      if (i == expr.length) {
        return res;
      }

      final var sequence = new GapList<Sequence>(expr.length);

      for (final Expr value : expr) {
        sequence.add(value.evaluateToItem(ctx, tuple));
      }

      return new DArray(sequence);
    }
  }

  @Override
  public boolean isUpdating() {
    for (Expr e : this.expr) {
      if (e.isUpdating()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isVacuous() {
    for (Expr e : this.expr) {
      if (!e.isVacuous()) {
        return false;
      }
    }
    return true;
  }

  @Override
  public String toString() {
    StringBuilder out = new StringBuilder();
    out.append("(");
    boolean first = true;
    for (Expr e : expr) {
      if (first) {
        first = false;
      } else {
        out.append(", ");
      }
      out.append(e.toString());
    }
    out.append(")");
    return out.toString();
  }
}