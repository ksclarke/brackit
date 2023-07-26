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
package org.brackit.query.compiler.optimizer.walker.topdown;

import static org.brackit.query.compiler.XQ.ComparisonExpr;
import static org.brackit.query.compiler.XQ.GeneralCompNE;
import static org.brackit.query.compiler.XQ.NodeCompFollows;
import static org.brackit.query.compiler.XQ.NodeCompIs;
import static org.brackit.query.compiler.XQ.NodeCompPrecedes;
import static org.brackit.query.compiler.XQ.Selection;
import static org.brackit.query.compiler.XQ.ValueCompNE;

import java.util.ArrayDeque;
import java.util.Deque;

import org.brackit.query.compiler.AST;
import org.brackit.query.compiler.XQ;
import org.brackit.query.module.StaticContext;
import org.brackit.query.util.Cmp;

/**
 * @author Sebastian Baechle
 */
public class JoinRewriter extends ScopeWalker {

  private Deque<Boolean> ordered = new ArrayDeque<>();

  public JoinRewriter(StaticContext sctx) {
    super(sctx);
    ordered.push(sctx.isOrderingModeOrdered());
  }

  @Override
  protected AST visit(AST select) {
    if (select.getType() != Selection) {
      return select;
    }
    AST predicate = select.getChild(0);

    if (predicate.getType() != ComparisonExpr) {
      return select;
    }
    AST comparison = predicate.getChild(0);

    switch (comparison.getType()) {
      case NodeCompFollows:
      case NodeCompIs:
      case NodeCompPrecedes:
      case GeneralCompNE:
      case ValueCompNE:
        return select;
    }

    Cmp cmp = CmpUtil.cmp(comparison);
    boolean isGCmp = CmpUtil.isGCmp(comparison);

    // left side must not be static
    AST s1Expr = predicate.getChild(1);
    VarRef s1VarRefs = findVarRefs(s1Expr);
    if (s1VarRefs == null) {
      return select;
    }
    // right side must not be static
    AST s2Expr = predicate.getChild(2);
    VarRef s2VarRefs = findVarRefs(s2Expr);
    if (s2VarRefs == null) {
      return select;
    }
    // extract scopes of referenced variables
    Scope[] s1Scopes = sortScopes(s1VarRefs);
    Scope[] s2Scopes = sortScopes(s2VarRefs);

    if (s2Scopes[s2Scopes.length - 1].compareTo(s1Scopes[s1Scopes.length - 1]) < 0) {
      // swap left and right in comparison
      AST tmpAst = s1Expr;
      s1Expr = s2Expr;
      s2Expr = tmpAst;
      VarRef tmpMinVarRef = s1VarRefs;
      s1VarRefs = s2VarRefs;
      s2VarRefs = tmpMinVarRef;
      Scope[] tmpScopes = s1Scopes;
      s1Scopes = s2Scopes;
      s2Scopes = tmpScopes;
      cmp = cmp.swap();
    }

    // S1 and S2 may overlap
    // => trim overlapping scopes, i.e., S0
    int s1Pos = 0;
    int s2Pos = 0;
    Scope s0End = null;
    Scope s1Begin = s1Scopes[s1Pos];
    Scope s2Begin = s2Scopes[s2Pos];
    while (true) {
      if ((s1Begin.compareTo(s2Begin) >= 0)) {
        if (++s1Pos == s1Scopes.length) {
          // S1 is empty
          return select;
        }
        s0End = s1Begin;
        s1Begin = s1Scopes[s1Pos];
      } else if ((s0End != null) && ((s2Begin.compareTo(s0End) >= 0))) {
        if (++s2Pos == s2Scopes.length) {
          // S2 is empty
          return select;
        }
        s0End = s2Begin;
        s2Begin = s2Scopes[s2Pos];
      } else {
        break;
      }
    }

    // we found join semantics:
    // walk upstairs until we either find
    // a) the beginning of this pipeline or
    // b) the node defining the beginning of S2
    AST anc = select.getParent();
    while (true) {
      if ((anc.getType() == XQ.Start) || (anc.getType() == XQ.Count) || (anc.getType() == XQ.GroupBy) || (anc.getType()
          == XQ.OrderBy)) {
        return select;
      } else if (anc == s2Begin.node) {
        return convertToJoin(s2Begin.node, select, s1Expr, s2Expr, cmp, isGCmp);
      }
      anc = anc.getParent();
    }
  }

  private AST convertToJoin(AST rightInRoot, AST select, AST s1Expr, AST s2Expr, Cmp cmp, boolean isGCmp) {
    // assemble left input pipeline
    AST leftIn = new AST(XQ.Start);
    AST s1End = new AST(XQ.End);
    s1End.addChild(s1Expr.copyTree());
    leftIn.addChild(s1End);

    // copy start of right (nested) input pipeline
    AST rorig = rightInRoot;
    AST rightIn = new AST(XQ.Start);
    AST copy = rightIn;

    while (rorig != select) {
      AST toAdd = rorig.copy();
      for (int i = 0; i < rorig.getChildCount() - 1; i++) {
        toAdd.addChild(rorig.getChild(i).copyTree());
      }
      copy.addChild(toAdd);
      copy = toAdd;
      rorig = rorig.getLastChild();
    }
    AST s2End = new AST(XQ.End);
    s2End.addChild(s2Expr.copyTree());
    copy.addChild(s2End);

    AST join = new AST(XQ.Join);
    join.addChild(leftIn);
    join.setProperty("cmp", cmp);
    join.setProperty("GCmp", isGCmp);
    if (!ordered.peek()) {
      join.setProperty("skipSort", Boolean.TRUE);
    }
    join.addChild(rightIn);
    AST postStart = new AST(XQ.Start);
    postStart.addChild(new AST(XQ.End));
    join.addChild(postStart);
    join.addChild(select.getLastChild().copyTree());

    AST parent = rightInRoot.getParent();
    parent.replaceChild(rightInRoot.getChildIndex(), join);

    snapshot();
    refreshScopes(parent, true);

    parent = pushRightInput(join);
    snapshot();

    refreshScopes(parent, true);
    return parent;
  }

  protected AST pushRightInput(AST join) {
    VarRef rRefs = findVarRefs(join.getChild(1));
    VarRef[] sortedRRefs = sortVarRefs(rRefs);
    Scope joinScope = findScope(join);

    AST target = null;
    Scope targetScope = null;
    for (int i = sortedRRefs.length - 1; i >= 0; i--) {
      Scope s = sortedRRefs[i].var.scope;
      if (s.compareTo(joinScope) < 0) {
        target = s.node;
        targetScope = s;
        break;
      }
    }

    if (target == null) {
      // TODO right input is completely independent
      // goto start?
      target = join.getParent();
      while (target.getType() != XQ.Start) {
        target = target.getParent();
      }
    } else {
      VarRef lRefs = findVarRefs(join.getChild(0));
      VarRef[] sortedLRefs = sortVarRefs(lRefs);
      for (VarRef sortedLRef : sortedLRefs) {
        Scope s = sortedLRef.var.scope;
        if (s.compareTo(targetScope) > 0) {
          target = s.node.getParent();
        }
      }
    }

    /*
     * CAVEAT: Ensure that begin of left
     * input is inside current pipeline!!!
     */
    AST leftInRoot = join.getParent();
    while ((leftInRoot.getType() != XQ.Start) && (leftInRoot != target)) {
      leftInRoot = leftInRoot.getParent();
    }

    // create copy of pipeline section between
    // target and this node
    AST copy = new AST(XQ.Start);
    AST copyEnd = copy;
    AST tmp = leftInRoot.getLastChild();
    if (tmp == join) {
      // stop if there's nothing to do
      return join;
    }
    while (tmp != join) {
      AST clone = tmp.copy();
      for (int i = 0; i < tmp.getChildCount() - 1; i++) {
        clone.addChild(tmp.getChild(i).copyTree());
      }
      copyEnd.addChild(clone);
      copyEnd = clone;
      tmp = tmp.getLastChild();
    }

    // now append copy of current left input
    copyEnd.addChild(join.getChild(0).getChild(0).copyTree());
    // replace left input with copy
    join.replaceChild(0, copy);
    // cut-out moved pipeline section
    leftInRoot.replaceChild(leftInRoot.getChildCount() - 1, join);

    return leftInRoot;
  }
}
