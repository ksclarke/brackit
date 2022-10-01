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
package org.brackit.xquery.util;

import java.util.ArrayList;
import java.util.List;

import org.brackit.xquery.ErrorCode;
import org.brackit.xquery.QueryException;
import org.brackit.xquery.sequence.ItemSequence;
import org.brackit.xquery.xdm.Expr;
import org.brackit.xquery.xdm.Item;
import org.brackit.xquery.xdm.Iter;
import org.brackit.xquery.xdm.Sequence;

/**
 * @author Sebastian Baechle
 */
public final class ExprUtil {
  private ExprUtil() {
  }

  public final static Item asItem(Sequence res) throws QueryException {
    if (res == null || res instanceof Item) {
      return (Item) res;
    }
    Iter s = res.iterate();
    try {
      Item item = s.next();
      if (item == null) {
        return null;
      }
      if (s.next() != null) {
        throw new QueryException(ErrorCode.ERR_TYPE_INAPPROPRIATE_TYPE);
      }
      return item;
    } finally {
      s.close();
    }
  }

  public static Sequence materialize(Sequence res) throws QueryException {
    // TODO
    // how to decide cleverly if we should materialize or not???
    if (res == null || res instanceof Item) {
      return res;
    }
    final var it = res.iterate();
    try {
      Item first = it.next();
      if (first == null) {
        return null;
      }
      Item second = it.next();
      if (second == null) {
        return first;
      }
      var buffer = new ArrayList<Item>();
      buffer.add(first);
      buffer.add(second);
      Item item;
      while ((item = it.next()) != null) {
        buffer.add(item);
      }
      return new ItemSequence(buffer.toArray(new Item[0]));
    } finally {
      it.close();
    }
  }
}
