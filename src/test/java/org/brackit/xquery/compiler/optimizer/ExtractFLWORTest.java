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
package org.brackit.xquery.compiler.optimizer;

import java.io.FileNotFoundException;

import org.brackit.xquery.ResultChecker;
import org.brackit.xquery.XQuery;
import org.brackit.xquery.XQueryBaseTest;
import org.brackit.xquery.atomic.Int32;
import org.brackit.xquery.sequence.ItemSequence;
import org.brackit.xquery.jdm.Sequence;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Sebastian Baechle
 */
public class ExtractFLWORTest extends XQueryBaseTest {

  @Test
  public void nestedInWhere() throws Exception {
    Sequence res =
        new XQuery("for $a in (1,2,3) " + "where $a > (for $b in (2,3,4) " + "			return $b) " + "return $a").execute(
            ctx);
    ResultChecker.dCheck(new Int32(3), res);
  }

  private Sequence intSequence(int... v) {
    Int32[] s = new Int32[v.length];
    for (int i = 0; i < v.length; i++) {
      s[i] = new Int32(v[i]);
    }
    return new ItemSequence(s);
  }

  @Before
  public void setUp() throws Exception, FileNotFoundException {
    super.setUp();
    DefaultOptimizer.UNNEST = true;
  }
}
