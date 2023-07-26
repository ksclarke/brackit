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
package org.brackit.query.module;

import java.util.*;

import org.brackit.query.atomic.QNm;
import org.brackit.query.compiler.Bits;
import org.brackit.query.expr.DeclVariable;
import org.brackit.query.expr.DefaultCtxItem;
import org.brackit.query.expr.DefaultCtxPos;
import org.brackit.query.expr.DefaultCtxSize;
import org.brackit.query.expr.Variable;
import org.brackit.query.jdm.type.SequenceType;

/**
 * @author Sebastian Baechle
 */
public class Variables {

  protected final Map<QNm, Variable> vars = new TreeMap<>();
  protected final DefaultCtxItem dftItem = new DefaultCtxItem();
  protected final DefaultCtxPos dftPos = new DefaultCtxPos(dftItem);
  protected final DefaultCtxSize dftSize = new DefaultCtxSize(dftItem);
  protected final List<Variables> imports = new LinkedList<>();

  public Variables() {
  }

  public boolean isDeclared(QNm name) {
    return vars.containsKey(name);
  }

  public DefaultCtxItem getDftCtxItem() {
    return dftItem;
  }

  public DefaultCtxPos getDftCtxPos() {
    return dftPos;
  }

  public DefaultCtxSize getDftCtxSize() {
    return dftSize;
  }

  public Variable resolve(QNm name) {
    Variable var = vars.get(name);
    if (var != null) {
      return var;
    } else if (name.equals(Bits.FS_DOT)) {
      return dftItem;
    } else if (name.equals(Bits.FS_POSITION)) {
      return dftPos;
    } else if (name.equals(Bits.FS_LAST)) {
      // var is null at this point.
      return null;
    }
    for (Variables v : imports) {
      // TODO check only public vars!
      var = v.resolve(name);
      if (var != null) {
        return var;
      }
    }
    return null;
  }

  public void importVariables(Variables variables) {
    imports.add(variables);
  }

  public DeclVariable declare(QNm name, SequenceType type, boolean external) {
    DeclVariable var = new DeclVariable(name, type);
    vars.put(name, var);
    return var;
  }

  public Collection<Variable> getDeclaredVariables() {
    return Collections.unmodifiableCollection(vars.values());
  }
}
