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
package org.brackit.xquery.expr;

import org.brackit.xquery.ResultChecker;
import org.brackit.xquery.XQuery;
import org.brackit.xquery.XQueryBaseTest;
import org.brackit.xquery.atomic.Bool;
import org.brackit.xquery.jdm.Sequence;
import org.junit.Test;

/**
 * @author Sebastian Baechle
 */
public class CmpExprTest extends XQueryBaseTest {

  @Test
  public void generalComparison() {
    Sequence result = new XQuery("1 > 2").execute(ctx);
    ResultChecker.dCheck(Bool.FALSE, result);
  }

  @Test
  public void generalComparisonNodeAndAtomics() {
    // the content must be converted to numeric double
    Sequence result = new XQuery("(<a>12</a> < 24) and (<b>122</b> > 24)").execute(ctx);
    ResultChecker.dCheck(Bool.TRUE, result);
  }

  @Test
  public void valueComparison() {
    Sequence result = new XQuery("1 lt 2").execute(ctx);
    ResultChecker.dCheck(Bool.TRUE, result);
  }

  @Test
  public void compareDates() {
    Sequence res = new XQuery("xs:date('2002-10-10+13:00') eq xs:date('2002-10-09-11:00')").execute(ctx);
    ResultChecker.dCheck(Bool.TRUE, res);
  }

  @Test
  public void compareDates2() {
    Sequence res = new XQuery("xs:date('2002-10-10+14:00') eq xs:date('2002-10-09-11:00')").execute(ctx);
    ResultChecker.dCheck(Bool.FALSE, res);
  }
}