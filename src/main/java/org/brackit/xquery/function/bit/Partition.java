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

package org.brackit.xquery.function.bit;

import org.brackit.xquery.QueryContext;
import org.brackit.xquery.atomic.QNm;
import org.brackit.xquery.compiler.Bits;
import org.brackit.xquery.function.AbstractFunction;
import org.brackit.xquery.module.StaticContext;
import org.brackit.xquery.jdm.Sequence;
import org.brackit.xquery.jdm.Signature;
import org.brackit.xquery.jdm.type.Cardinality;
import org.brackit.xquery.jdm.type.AtomicType;
import org.brackit.xquery.jdm.type.DocumentType;
import org.brackit.xquery.jdm.type.SequenceType;
import org.brackit.xquery.atomic.Str;

public class Partition extends AbstractFunction {
    public static final QNm PARTITION = new QNm(Bits.BIT_NSURI, Bits.BIT_PREFIX, "partition");

    public Partition() {
        super(PARTITION,
                new Signature(new SequenceType(DocumentType.DOC, Cardinality.One),
                        new SequenceType(AtomicType.STR, Cardinality.ZeroOrOne)),
                true);
    }

    @Override
    public Sequence execute(StaticContext sctx, QueryContext ctx, Sequence[] args) {
        // TODO Auto-generated method stub
        Str minStr = (Str) args[0];
        Str maxStr = (Str) args[1];
        // Seems that the queue variable is not necessary always, so we don't need a preemptive null check
        Str queueCountStr = (Str) args[2];
        if (minStr == null && maxStr == null) {
            return null;
        }
        return null;
    }

}
