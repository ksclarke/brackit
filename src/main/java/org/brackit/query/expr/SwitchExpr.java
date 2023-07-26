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
package org.brackit.query.expr;

import org.brackit.query.QueryContext;
import org.brackit.query.QueryException;
import org.brackit.query.Tuple;
import org.brackit.query.atomic.Atomic;
import org.brackit.query.function.fn.DeepEqual;
import org.brackit.query.util.ExprUtil;
import org.brackit.query.jdm.Expr;
import org.brackit.query.jdm.Item;
import org.brackit.query.jdm.Sequence;

/**
 * @author Sebastian Baechle
 */
public class SwitchExpr implements Expr {
  final Expr operand;
  final Expr[][] cases;
  final Expr dftValue;

  public SwitchExpr(Expr operand, Expr[][] cases, Expr dftValue) {
    this.operand = operand;
    this.cases = cases;
    this.dftValue = dftValue;
  }

  @Override
  public Sequence evaluate(QueryContext ctx, Tuple tuple) throws QueryException {
    Item oi = operand.evaluateToItem(ctx, tuple);
    Atomic oa = (oi != null) ? oi.atomize() : null;
    for (final var aCase : cases) {
      for (int j = 0; j < aCase.length - 1; j++) {
        Item cij = aCase[j].evaluateToItem(ctx, tuple);
        Atomic ca = (cij != null) ? cij.atomize() : null;
        if (DeepEqual.deepEquals(oa, ca).booleanValue()) {
          return aCase[aCase.length - 1].evaluate(ctx, tuple);
        }
      }
    }
    return dftValue.evaluate(ctx, tuple);
  }

  @Override
  public Item evaluateToItem(QueryContext ctx, Tuple tuple) throws QueryException {
    return ExprUtil.asItem(evaluate(ctx, tuple));
  }

  @Override
  public boolean isUpdating() {
    if (operand.isUpdating() || dftValue.isUpdating()) {
      return true;
    }
    for (final Expr[] aCase : cases) {
      for (Expr expr : aCase) {
        if (expr.isUpdating()) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public boolean isVacuous() {
    if (operand.isVacuous() || dftValue.isVacuous()) {
      return true;
    }
    for (final Expr[] aCase : cases) {
      for (final Expr expr : aCase) {
        if (expr.isVacuous()) {
          return true;
        }
      }
    }
    return false;
  }
}
