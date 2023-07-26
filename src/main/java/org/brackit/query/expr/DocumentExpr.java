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
package org.brackit.query.expr;

import org.brackit.query.ErrorCode;
import org.brackit.query.QueryContext;
import org.brackit.query.QueryException;
import org.brackit.query.Tuple;
import org.brackit.query.jdm.Expr;
import org.brackit.query.jdm.Item;
import org.brackit.query.jdm.Sequence;
import org.brackit.query.jdm.node.Node;

/**
 * @author Sebastian Baechle
 */
public class DocumentExpr extends ConstructedNodeBuilder implements Expr {
  protected final Expr contentExpr;

  protected final boolean bind;

  public DocumentExpr(Expr contentExpr, boolean bind) {
    this.contentExpr = contentExpr;
    this.bind = bind;
  }

  @Override
  public final Sequence evaluate(QueryContext ctx, Tuple tuple) {
    return evaluateToItem(ctx, tuple);
  }

  @Override
  public Item evaluateToItem(QueryContext ctx, Tuple tuple) {
    // See XQuery 3.7.3.3 Computed Document Constructors
    final Node<?> document = ctx.getNodeFactory().document(null);

    ContentSink sink = new ContentSink() {
      @Override
      public Node<?> addNode(QueryContext ctx, Node<?> node) {
        return document.append(node);
      }

      @Override
      public Node<?> addAttribute(QueryContext ctx, Node<?> attribute) {
        throw new QueryException(ErrorCode.ERR_TYPE_INAPPROPRIATE_TYPE, "Cannot add attribute to document node");
      }
    };

    Tuple t = tuple.concat(document);
    Sequence content = contentExpr.evaluate(ctx, t);
    buildContentSequence(ctx, sink, content);
    return document;
  }

  @Override
  public boolean isUpdating() {
    return contentExpr.isUpdating();
  }

  @Override
  public boolean isVacuous() {
    return false;
  }

  public String toString() {
    return "document";
  }
}