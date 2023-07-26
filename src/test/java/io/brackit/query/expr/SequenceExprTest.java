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

import io.brackit.query.atomic.Int32;
import io.brackit.query.operator.TupleImpl;
import io.brackit.query.BrackitQueryContext;
import io.brackit.query.QueryContext;
import io.brackit.query.ResultChecker;
import io.brackit.query.sequence.ItemSequence;
import org.junit.Test;

/**
 * @author Sebastian Baechle
 */
public class SequenceExprTest {
  QueryContext ctx = new BrackitQueryContext(null, null);

  @Test
  public void simpleSequence() {
    SequenceExpr expr = new SequenceExpr(new Int32(1), new Int32(2), new Int32(3));
    final var evaluatedSequence = expr.evaluate(ctx, new TupleImpl());
    ResultChecker.dCheck(new ItemSequence(new Int32(1), new Int32(2), new Int32(3)), evaluatedSequence);
  }

  @Test
  public void emptySequence() {
    SequenceExpr expr = new SequenceExpr();
    ResultChecker.dCheck(null, expr.evaluate(ctx, new TupleImpl()));
  }

  @Test
  public void simpleAndEmptySequence() throws Exception {
    SequenceExpr expr = new SequenceExpr(new Int32(1), new SequenceExpr(), new Int32(2), new Int32(3));
    ResultChecker.dCheck(new ItemSequence(new Int32(1), new Int32(2), new Int32(3)),
                         expr.evaluate(ctx, new TupleImpl()));
  }

  @Test
  public void nestedSequences() {
    SequenceExpr expr = new SequenceExpr(new Int32(1),
                                         new SequenceExpr(new Int32(2), new Int32(3)),
                                         new Int32(4),
                                         new SequenceExpr(new Int32(5), new Int32(6), new Int32(7)));
    ResultChecker.dCheck(new ItemSequence(new Int32(1),
                                          new Int32(2),
                                          new Int32(3),
                                          new Int32(4),
                                          new Int32(5),
                                          new Int32(6),
                                          new Int32(7)), expr.evaluate(ctx, new TupleImpl()));
  }
}