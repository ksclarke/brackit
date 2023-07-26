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

import io.brackit.query.atomic.Str;
import io.brackit.query.ErrorCode;
import io.brackit.query.QueryContext;
import io.brackit.query.QueryException;
import io.brackit.query.Tuple;
import io.brackit.query.jdm.Expr;
import io.brackit.query.jdm.Item;
import io.brackit.query.jdm.Iter;
import io.brackit.query.jdm.Kind;
import io.brackit.query.jdm.Sequence;
import io.brackit.query.jdm.node.Node;

/**
 * @author Sebastian Baechle
 */
public class CommentExpr extends ConstructedNodeBuilder implements Expr {
  protected final Expr expr;

  protected final boolean appendOnly;

  public CommentExpr(Expr expr, boolean appendOnly) {
    this.expr = expr;
    this.appendOnly = appendOnly;
  }

  @Override
  public final Sequence evaluate(QueryContext ctx, Tuple tuple) {
    return evaluateToItem(ctx, tuple);
  }

  @Override
  public Item evaluateToItem(QueryContext ctx, Tuple tuple) {
    // See XQuery 3.7.3.6 Computed Comment Constructors
    Sequence sequence = expr.evaluate(ctx, tuple);
    StringBuilder buf = new StringBuilder("");

    if (sequence != null) {
      if (sequence instanceof Item) {
        String s = ((Item) sequence).atomize().stringValue();
        buf.append(s);
      } else {
        boolean first = true;
        Iter it = sequence.iterate();
        try {
          Item item;
          while ((item = it.next()) != null) {
            String s = item.atomize().stringValue();
            if (!s.isEmpty()) {
              if (!first) {
                buf.append(' ');
              }
              first = false;
              buf.append(s);
            }
          }
        } finally {
          it.close();
        }
      }
    }

    String content = buf.toString();

    if (content.contains("--") || content.endsWith("-")) {
      throw new QueryException(ErrorCode.ERR_COMMENT_WOULD_CONTAIN_ILLEGAL_HYPHENS,
                               "Comment node contains illegal hyphens: '%s'",
                               content);
    }

    if (appendOnly) {
      ((Node<?>) tuple.get(tuple.getSize() - 1)).append(Kind.COMMENT, null, new Str(content));
      return null;
    }

    Node<?> node = ctx.getNodeFactory().comment(new Str(content));
    return node;
  }

  @Override
  public boolean isUpdating() {
    return expr.isUpdating();
  }

  @Override
  public boolean isVacuous() {
    return false;
  }
}
