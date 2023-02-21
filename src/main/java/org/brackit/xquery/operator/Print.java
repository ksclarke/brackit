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
package org.brackit.xquery.operator;

import java.io.PrintStream;
import java.util.Arrays;

import org.brackit.xquery.QueryContext;
import org.brackit.xquery.QueryException;
import org.brackit.xquery.Tuple;
import org.brackit.xquery.jdm.Sequence;

/**
 * @author Sebastian Baechle
 */
public class Print implements Operator {
  private final Operator in;
  private final PrintStream out;

  public Print(Operator in, PrintStream out) {
    this.in = in;
    this.out = out;
  }

  protected class PrintCursor implements Cursor {
    private static final int MAX_SIZE = 20;

    private final Cursor c;
    private final Tuple[] t;
    private final PrintStream out;

    private int count;

    public PrintCursor(Cursor c, Tuple[] t, PrintStream out) {
      this.c = c;
      this.t = t;
      this.out = out;
    }

    @Override
    public void close(QueryContext ctx) {
      c.close(ctx);
      out.println("---");
      out.print(count);
      out.println(" results");
      out.flush();
    }

    @Override
    public Tuple next(QueryContext ctx) throws QueryException {
      Tuple next = c.next(ctx);
      if (next != null) {
        count++;
        int size = next.getSize();
        out.print("|");
        for (int i = 0; i < size; i++) {
          out.print(' ');
          Sequence sequence = next.get(i);
          String s = (sequence != null) ? asString(ctx, sequence) : "()";
          s = shrinkOrPad(s);
          out.print(s);
          out.print(" |");
        }
        out.print('\n');
      }
      return next;
    }

    private String shrinkOrPad(String s) {
      int length = s.length();

      if (length == MAX_SIZE) {
        return s;
      }
      if (length > MAX_SIZE) {
        return s.substring(0, MAX_SIZE);
      }

      int toAdd = (MAX_SIZE - length);
      char[] result = new char[MAX_SIZE];

      int i = 0;
      while (i < (toAdd / 2)) {
        result[i++] = ' ';
      }
      System.arraycopy(s.toCharArray(), 0, result, i, length);
      int j = i + length;
      while (i++ < toAdd) {
        result[j++] = ' ';
      }
      return new String(result);

    }

    @Override
    public void open(QueryContext ctx) throws QueryException {
      c.open(ctx);
      count = 0;
      out.print("--- ");
      out.println(Arrays.toString(t));
    }
  }

  @Override
  public Cursor create(QueryContext ctx, Tuple tuple) throws QueryException {
    return new PrintCursor(in.create(ctx, tuple), new Tuple[] { tuple }, out);
  }

  @Override
  public Cursor create(QueryContext ctx, Tuple[] buf, int len) throws QueryException {
    return new PrintCursor(in.create(ctx, buf, len), buf, out);
  }

  @Override
  public int tupleWidth(int initSize) {
    return in.tupleWidth(initSize);
  }

  public String asString(QueryContext ctx, Sequence sequence) throws QueryException {
    return sequence.toString();
  }
}
