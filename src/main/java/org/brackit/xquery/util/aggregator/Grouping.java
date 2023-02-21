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
package org.brackit.xquery.util.aggregator;

import org.brackit.xquery.QueryException;
import org.brackit.xquery.Tuple;
import org.brackit.xquery.atomic.Atomic;
import org.brackit.xquery.expr.Cast;
import org.brackit.xquery.operator.TupleImpl;
import org.brackit.xquery.util.ExprUtil;
import org.brackit.xquery.jdm.Item;
import org.brackit.xquery.jdm.Sequence;
import org.brackit.xquery.jdm.Type;

/**
 * @author Sebastian Baechle
 */
public class Grouping {
  final int[] groupSpecs;
  final int[] addAggsSpecs;
  final Aggregate defaultAgg;
  final Aggregate[] additionalAggs;

  volatile int tupleSize = -1;
  Aggregate[] aggSpecs;
  Atomic[] gk; // current grouping key
  Aggregator[] aggs;
  boolean[] onlyFirst;
  int size;

  public Grouping(int[] groupSpecs, int[] addAggsSpecs, Aggregate defaultAgg,
      Aggregate[] additionalAggs) {
    this.groupSpecs = groupSpecs;
    this.addAggsSpecs = addAggsSpecs;
    this.defaultAgg = defaultAgg;
    this.additionalAggs = additionalAggs;
  }

  public Grouping(int[] groupSpecs, int[] addAggsSpecs, Aggregate defaultAgg,
      Aggregate[] additionalAggs, int tupleSize) {
    this.groupSpecs = groupSpecs;
    this.addAggsSpecs = addAggsSpecs;
    this.defaultAgg = defaultAgg;
    this.additionalAggs = additionalAggs;
    init(tupleSize);
  }

  private synchronized void init(int tupleSize) {
    if (this.tupleSize != -1) {
      return;
    }
    int len = tupleSize + additionalAggs.length;
    this.aggSpecs = new Aggregate[len];
    this.aggs = new Aggregator[len];
    this.onlyFirst = new boolean[len];
    // init each incoming binding position to default aggregation
    for (int pos = 0; pos < tupleSize; pos++) {
      aggSpecs[pos] = defaultAgg;
    }
    if (defaultAgg == Aggregate.SINGLE) {
      for (int pos = 0; pos < tupleSize; pos++) {
        onlyFirst[pos] = true;
      }
    }
    // override aggregation for grouping variables
    for (int pos : groupSpecs) {
      aggSpecs[pos] = Aggregate.SINGLE;
      onlyFirst[pos] = true;
    }
    // init additional aggregation bindings
    for (int pos = tupleSize; pos < len; pos++) {
      aggSpecs[pos] = additionalAggs[pos - tupleSize];
      onlyFirst[pos] = (aggSpecs[pos] == Aggregate.SINGLE);
    }
    clear();
    this.tupleSize = tupleSize;
  }

  public int getSize() {
    return size;
  }

  public static Atomic[] groupingKeys(int[] groupSpecs, Tuple t)
      throws QueryException {
    Atomic[] gk = new Atomic[groupSpecs.length];
    for (int i = 0; i < groupSpecs.length; i++) {
      Sequence seq = t.get(groupSpecs[i]);
      if (seq != null) {
        Item item = ExprUtil.asItem(seq);
        if (item != null) {
          gk[i] = item.atomize();
          if (gk[i].type().instanceOf(Type.UNA)) {
            gk[i] = Cast.cast(null, gk[i], Type.STR);
          }
        }
      }
    }
    return gk;
  }

  public boolean cmp(Atomic[] gk1, Atomic[] gk2) {
    for (int i = 0; i < groupSpecs.length; i++) {
      if (gk1[i] == null) {
        if (gk2[i] != null) {
          return false;
        }
      } else if ((gk2[i] == null) || (gk1[i].atomicCmp(gk2[i]) != 0)) {
        return false;
      }
    }
    return true;
  }

  public synchronized void clear() {
    for (int i = 0; i < aggSpecs.length; i++) {
      if (aggs[i] != null) {
        aggs[i].clear();
      } else {
        aggs[i] = aggSpecs[i].aggregator();
      }
    }
    size = 0;
  }

  public boolean add(Tuple t) throws QueryException {
    if (tupleSize == -1) {
      init(t.getSize());
    }
    Atomic[] pgk = gk;
    gk = groupingKeys(groupSpecs, t);
    if ((pgk != null) && (!cmp(pgk, gk))) {
      return false;
    }
    addInternal(t);
    return true;
  }

  public boolean add(Atomic[] gk, Tuple t) throws QueryException {
    if (tupleSize == -1) {
      init(t.getSize());
    }
    Atomic[] pgk = this.gk;
    if ((pgk != null) && (!cmp(pgk, gk))) {
      return false;
    }
    this.gk = gk;
    addInternal(t);
    return true;
  }

  private void addInternal(Tuple t) throws QueryException {
    for (int i = 0; i < tupleSize; i++) {
      if ((size > 0) && (onlyFirst[i])) {
        continue;
      }
      Sequence s = t.get(i);
      if (s == null) {
        continue;
      }
      synchronized (aggs[i]) {
        aggs[i].add(s);
      }
    }
    for (int i = 0; i < addAggsSpecs.length; i++) {
      if ((size > 0) && (onlyFirst[tupleSize + i])) {
        continue;
      }
      Sequence s = t.get(addAggsSpecs[i]);
      if (s == null) {
        continue;
      }
      synchronized (aggs[tupleSize + i]) {
        aggs[tupleSize + i].add(s);
      }
    }
    size++;
  }

  public Tuple emit() throws QueryException {
    Sequence[] groupings = new Sequence[aggs.length];
    for (int i = 0; i < aggs.length; i++) {
      groupings[i] = aggs[i].getAggregate();
    }
    return new TupleImpl(groupings);
  }

  public Tuple singleEmit(Tuple t) throws QueryException {
    //		if (additionalAggs.length == 0) {
    //			return t;
    //		}
    if (tupleSize == -1) {
      init(t.getSize());
    }
    Sequence[] padding = new Sequence[additionalAggs.length];
    for (int i = 0; i < additionalAggs.length; i++) {
      padding[i] = aggs[tupleSize + i].getAggregate();
    }
    return t.concat(padding);
  }
}