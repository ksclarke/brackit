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
package org.brackit.query.function.fn;

import org.brackit.query.QueryContext;
import org.brackit.query.QueryException;
import org.brackit.query.atomic.AbstractDuration;
import org.brackit.query.atomic.Dbl;
import org.brackit.query.atomic.Int32;
import org.brackit.query.atomic.QNm;
import org.brackit.query.function.AbstractFunction;
import org.brackit.query.module.StaticContext;
import org.brackit.query.jdm.Sequence;
import org.brackit.query.jdm.Signature;
import org.brackit.query.jdm.Type;

/**
 * Implements all functions that extract certain components of instances of
 * duration types, namely fn:years-from-duration($arg1),
 * fn:months-from-duration($arg1), fn:days-from-duration($arg1),
 * fn:hours-from-duration($arg1), fn:minutes-from-duration($arg1),
 * fn:seconds-from-duration($arg1), as per
 * http://www.w3.org/TR/xpath-functions/#func-years-from-duration ff.
 *
 * @author Max Bechtold
 */
public class ExtractFromDuration extends AbstractFunction {
  public static enum Comp {
    YEARS, MONTHS, DAYS, HOURS, MINUTES, SECONDS
  }

  ;

  private Comp comp;

  public ExtractFromDuration(QNm name, Comp comp, Signature signature) {
    super(name, signature, true);
    this.comp = comp;
  }

  @Override
  public Sequence execute(StaticContext sctx, QueryContext ctx, Sequence[] args) throws QueryException {

    if (args.length == 0 || args[0] == null) {
      return null;
    }
    AbstractDuration dur = (AbstractDuration) args[0];

    int value = 0;

    switch (comp) {
      case YEARS:
        if (dur.type().instanceOf(Type.DTD)) {
          return Int32.ZERO;
        }

        value = dur.getYears();
        break;

      case MONTHS:
        if (dur.type().instanceOf(Type.DTD)) {
          return Int32.ZERO;
        }

        value = dur.getMonths();
        break;

      case DAYS:
        if (dur.type().instanceOf(Type.YMD)) {
          return Int32.ZERO;
        }

        value = dur.getDays();
        break;

      case HOURS:
        if (dur.type().instanceOf(Type.YMD)) {
          return Int32.ZERO;
        }

        value = dur.getHours();
        break;

      case MINUTES:
        if (dur.type().instanceOf(Type.YMD)) {
          return Int32.ZERO;
        }

        value = dur.getMinutes();
        break;

      case SECONDS:
        if (dur.type().instanceOf(Type.YMD)) {
          return new Dbl(0.0);
        }

        double d = dur.getMicros() / 1000000.0;

        if (dur.isNegative()) {
          return new Dbl(d * -1);
        }
        return new Dbl(d);

    }

    if (dur.isNegative()) {
      return new Int32(value * -1);
    }

    return new Int32(value);

  }

}
