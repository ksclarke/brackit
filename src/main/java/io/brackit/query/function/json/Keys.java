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
package io.brackit.query.function.json;

import io.brackit.query.atomic.QNm;
import io.brackit.query.jdm.Item;
import io.brackit.query.jdm.Iter;
import io.brackit.query.jdm.Sequence;
import io.brackit.query.jdm.Signature;
import io.brackit.query.jdm.json.Object;
import io.brackit.query.jdm.type.SequenceType;
import io.brackit.query.module.StaticContext;
import io.brackit.query.sequence.BaseIter;
import io.brackit.query.sequence.LazySequence;
import io.brackit.query.util.annotation.FunctionAnnotation;
import io.brackit.query.QueryContext;
import io.brackit.query.function.AbstractFunction;

import java.util.*;

/**
 * @author Sebastian Baechle
 */
@FunctionAnnotation(description = "Returns an array with the field values of the given record.", parameters = "$record")
public class Keys extends AbstractFunction {

  public static final QNm DEFAULT_NAME = new QNm(JSONFun.JSON_NSURI, JSONFun.JSON_PREFIX, "keys");

  public Keys() {
    this(DEFAULT_NAME);
  }

  public Keys(QNm name) {
    super(name, new Signature(SequenceType.ITEM_SEQUENCE, SequenceType.ITEM_SEQUENCE), true);
  }

  @Override
  public Sequence execute(StaticContext sctx, QueryContext ctx, Sequence[] args) {
    final var sequence = args[0];

    return new LazySequence() {
      @Override
      public Iter iterate() {
        return new BaseIter() {
          Iter s;
          final Set<String> stringKeys = new HashSet<>();
          final List<Item> keys = new ArrayList<>();

          @Override
          public Item next() {
            if (s == null) {
              s = sequence.iterate();
            }

            Item item;
            while ((item = s.next()) != null) {
              if (item instanceof Object object) {
                try (final var nameIter = object.names().iterate()) {
                  Item name;
                  while ((name = nameIter.next()) != null) {
                    if (stringKeys.add(name.atomize().stringValue())) {
                      keys.add(name);
                    }
                  }
                }
              }
            }

            if (!keys.isEmpty()) {
              return keys.remove(0);
            }

            return null;
          }

          @Override
          public void close() {
            if (s != null) {
              s.close();
            }
          }
        };
      }
    };
  }
}