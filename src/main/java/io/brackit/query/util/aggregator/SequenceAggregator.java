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
package io.brackit.query.util.aggregator;

import java.util.Arrays;

import io.brackit.query.QueryException;
import io.brackit.query.sequence.NestedSequence;
import io.brackit.query.jdm.Sequence;

/**
 * Aggregator for sequence concatenation
 *
 * @author Sebastian Baechle
 */
public class SequenceAggregator implements Aggregator {
  private Sequence[] buf = new Sequence[5];
  private int len;

  @Override
  public void add(Sequence s) throws QueryException {
    if (s == null) {
      return;
    }
    if (len == buf.length) {
      buf = Arrays.copyOf(buf, ((buf.length * 3) / 2 + 1));
    }
    buf[len++] = s;
  }

  @Override
  public Sequence getAggregate() {
    if (len == 0) {
      return null;
    }
    if (len == 1) {
      return buf[0];
    }
    if (buf.length != len) {
      buf = Arrays.copyOf(buf, len);
    }
    return new NestedSequence(buf);
  }

  @Override
  public void clear() {
    if (len > 1) {
      buf = new Sequence[5];
    }
    len = 0;
  }
}