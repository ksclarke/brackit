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
package org.brackit.query.function.bit;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import org.brackit.query.QueryContext;
import org.brackit.query.atomic.QNm;
import org.brackit.query.compiler.Bits;
import org.brackit.query.function.AbstractFunction;
import org.brackit.query.module.StaticContext;
import org.brackit.query.operator.TupleImpl;
import org.brackit.query.sequence.BaseIter;
import org.brackit.query.sequence.FlatteningSequence;
import org.brackit.query.sequence.LazySequence;
import org.brackit.query.util.annotation.FunctionAnnotation;
import org.brackit.query.jdm.Item;
import org.brackit.query.jdm.Iter;
import org.brackit.query.jdm.Sequence;
import org.brackit.query.jdm.Signature;
import org.brackit.query.jdm.json.Array;
import org.brackit.query.jdm.type.ArrayType;
import org.brackit.query.jdm.type.Cardinality;
import org.brackit.query.jdm.type.SequenceType;

/**
 * @author Sebastian Baechle
 */
@FunctionAnnotation(description = "Returns the values of the given array.", parameters = "$array")
public class ArrayValues extends AbstractFunction {

  public static final QNm DEFAULT_NAME = new QNm(Bits.BIT_NSURI, Bits.BIT_PREFIX, "array-values");

  public ArrayValues() {
    this(DEFAULT_NAME);
  }

  public ArrayValues(QNm name) {
    super(name,
          new Signature(SequenceType.ITEM_SEQUENCE, new SequenceType(ArrayType.ARRAY, Cardinality.ZeroOrOne)),
          true);
  }

  @Override
  public Sequence execute(StaticContext sctx, QueryContext ctx, Sequence[] args) {
    final Array array = (Array) args[0];

    if (array == null)
      return null;

    return new LazySequence() {
      @Override
      public Iter iterate() {
        return new BaseIter() {
          private List<Sequence> sequences;
          private Deque<Item> flatteningSequences = new ArrayDeque<>();

          private int index;

          @Override
          public Item next() {
            if (sequences == null) {
              sequences = array.values();
            } else if (!flatteningSequences.isEmpty()) {
              return flatteningSequences.removeFirst();
            }

            if (index < sequences.size()) {
              final var sequence = sequences.get(index++);
              if (sequence instanceof FlatteningSequence) {
                final var iter = sequence.iterate();
                Item item;
                while ((item = iter.next()) != null) {
                  flatteningSequences.addLast(item.evaluateToItem(ctx, new TupleImpl()));
                }
                return flatteningSequences.removeFirst();
              }
              return (Item) sequence;
            }

            return null;
          }

          @Override
          public void close() {
          }
        };
      }
    };
  }
}
