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
package org.brackit.examples;

import org.brackit.query.BrackitQueryContext;
import org.brackit.query.QueryContext;
import org.brackit.query.QueryException;
import org.brackit.query.Query;
import org.brackit.query.jdm.Item;
import org.brackit.query.jdm.Iter;
import org.brackit.query.jdm.Sequence;

/**
 * Basic usage scenarios.
 */
public class Simple {

  public static void main(String[] args) {
    try {
      queryAndSerialize();
      System.out.println();
      queryAndIterate();
    } catch (QueryException e) {
      System.err.print("XQuery error ");
      System.err.print(e.getCode());
      System.err.print(": ");
      System.err.println(e.getMessage());
    }
  }

  private static void queryAndSerialize() throws QueryException {
    // initialize query context
    QueryContext ctx = new BrackitQueryContext();

    // run query and serialize result to std out
    System.out.println("Running query:");
    String xq = """
        for $i in (1 to 4)
        let $d := <no>{$i}</no>
        return $d""";
    System.out.println(xq);
    new Query(xq).prettyPrint().serialize(ctx, System.out);
    System.out.println();
  }

  private static void queryAndIterate() throws QueryException {
    // initialize query context
    QueryContext ctx = new BrackitQueryContext();

    // run query and serialize result to std out
    System.out.println("Running query:");
    String xq = """
        for $i in (1 to 4)
        let $d := <no>{$i}</no>
        return $d""";
    System.out.println(xq);
    Query q = new Query(xq);
    Sequence res = q.execute(ctx);
    System.out.println("result sequence size: " + res.size());
    try (final Iter it = res.iterate()) {
      Item item;
      int cnt = 0;
      while ((item = it.next()) != null) {
        System.out.print("Item ");
        System.out.print(cnt++);
        System.out.print(": ");
        System.out.print(item.itemType());
        System.out.print(" atomized value=");
        System.out.println(item.atomize());
      }
    }
  }
}
