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
package io.brackit.query.function.fn;

import io.brackit.query.atomic.DateTime;
import io.brackit.query.jdm.Sequence;
import io.brackit.query.jdm.Signature;
import io.brackit.query.module.StaticContext;
import io.brackit.query.QueryContext;
import io.brackit.query.QueryException;
import io.brackit.query.atomic.Date;
import io.brackit.query.atomic.Dbl;
import io.brackit.query.atomic.Int32;
import io.brackit.query.atomic.QNm;
import io.brackit.query.atomic.Time;
import io.brackit.query.function.AbstractFunction;

public class ExtractFromDateTime extends AbstractFunction {
  public static enum Source {
    DATE_TIME, DATE, TIME
  }

  ;

  public static enum Comp {
    YEAR, MONTH, DAY, HOURS, MINUTES, SECONDS, TIMEZONE
  }

  ;

  private Source source;
  private Comp comp;

  public ExtractFromDateTime(QNm name, Source source, Comp comp, Signature signature) {
    super(name, signature, true);
    this.source = source;
    this.comp = comp;
  }

  @Override
  public Sequence execute(StaticContext sctx, QueryContext ctx, Sequence[] args) throws QueryException {
    if (args.length == 0 || args[0] == null) {
      return null;
    }

    switch (source) {
      case DATE_TIME:
        io.brackit.query.atomic.DateTime dt = (DateTime) args[0];

        switch (comp) {
          case YEAR:
            return new Int32(dt.getYear());

          case MONTH:
            return new Int32(dt.getMonth());

          case DAY:
            return new Int32(dt.getDay());

          case HOURS:
            return new Int32(dt.getHours());

          case MINUTES:
            return new Int32(dt.getMinutes());

          case SECONDS:
            return new Dbl(dt.getMicros() / 1000000.0);

          case TIMEZONE:
            return dt.getTimezone();
        }

      case DATE:
        Date date = (Date) args[0];

        switch (comp) {
          case YEAR:
            return new Int32(date.getYear());

          case MONTH:
            return new Int32(date.getMonth());

          case DAY:
            return new Int32(date.getDay());

          case TIMEZONE:
            return date.getTimezone();
        }

      case TIME:
        Time time = (Time) args[0];

        switch (comp) {
          case HOURS:
            return new Int32(time.getHours());

          case MINUTES:
            return new Int32(time.getMinutes());

          case SECONDS:
            return new Dbl(time.getMicros() / 1000000.0);

          case TIMEZONE:
            return time.getTimezone();
        }

      default:
        return null;
    }
  }

}
