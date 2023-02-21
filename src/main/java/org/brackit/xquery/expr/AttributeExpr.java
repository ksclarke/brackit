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

import org.brackit.xquery.ErrorCode;
import org.brackit.xquery.QueryContext;
import org.brackit.xquery.QueryException;
import org.brackit.xquery.Tuple;
import org.brackit.xquery.atomic.QNm;
import org.brackit.xquery.atomic.Str;
import org.brackit.xquery.atomic.Una;
import org.brackit.xquery.module.StaticContext;
import org.brackit.xquery.jdm.Expr;
import org.brackit.xquery.jdm.Item;
import org.brackit.xquery.jdm.Sequence;
import org.brackit.xquery.jdm.node.Node;

/**
 * @author Sebastian Baechle
 */
public class AttributeExpr extends ConstructedNodeBuilder implements Expr {
  protected final StaticContext sctx;
  protected final Expr nameExpr;
  protected final Expr[] valueExpr;
  protected final boolean appendOnly;
  protected final QNm name;

  public AttributeExpr(StaticContext sctx, Expr nameExpr, Expr[] valueExpr, boolean appendOnly) {
    this.sctx = sctx;
    this.nameExpr = nameExpr;
    this.valueExpr = valueExpr;
    this.appendOnly = appendOnly;
    this.name = (QNm) (nameExpr instanceof QNm ? nameExpr : null);
  }

  @Override
  public final Sequence evaluate(QueryContext ctx, Tuple tuple) {
    return evaluateToItem(ctx, tuple);
  }

  @Override
  public Item evaluateToItem(QueryContext ctx, Tuple tuple) {
    // See XQuery 3.0 3.8.3.2 Computed Attribute Constructors
    QNm name = this.name != null ? this.name : buildAttributeName(sctx, nameExpr.evaluateToItem(ctx, tuple));

    if ("xmlns".equals(name.getPrefix())) {
      throw new QueryException(ErrorCode.ERR_ILLEGAL_NAME_OF_CONSTRUCTED_ATTRIBUTE,
                               "Attribute name prefix must not be \"xmlns\"");
    }
    if (name.getPrefix() == null && "xmlns".equals(name.getLocalName())) {
      throw new QueryException(ErrorCode.ERR_ILLEGAL_NAME_OF_CONSTRUCTED_ATTRIBUTE,
                               "Attribute name must not be \"xmlns\"");
    }
    if ("http://www.w3.org/2000/xmlns/".equals(name.getNamespaceURI())) {
      throw new QueryException(ErrorCode.ERR_ILLEGAL_NAME_OF_CONSTRUCTED_ATTRIBUTE,
                               "Attribute name namespace URI must not be \"http://www.w3.org/2000/xmlns/\"");
    }
    if ("xml".equals(name.getPrefix())) {
      if (!"http://www.w3.org/XML/1998/namespace".equals(name.getNamespaceURI())) {
        throw new QueryException(ErrorCode.ERR_ILLEGAL_NAME_OF_CONSTRUCTED_ATTRIBUTE,
                                 "Namespace prefix \"xml\" must be bound to namespace URI other "
                                     + "than \"http://www.w3.org/XML/1998/namespace\"");
      }
    } else if ("http://www.w3.org/XML/1998/namespace".equals(name.getNamespaceURI())) {
      throw new QueryException(ErrorCode.ERR_ILLEGAL_NAME_OF_CONSTRUCTED_ATTRIBUTE,
                               "Namespace prefix \"xml\" must be bound to namespace URI other "
                                   + "than \"http://www.w3.org/XML/1998/namespace\"");
    }
    String stringValue = "";
    for (Expr e : valueExpr) {
      Sequence content = e.evaluate(ctx, tuple);
      stringValue += buildAttributeContent(content);
    }

    if (appendOnly) {
      ((Node<?>) tuple.get(tuple.getSize() - 1)).setAttribute(name, new Una(stringValue));
      return null;
    }

    Node<?> attribute = ctx.getNodeFactory().attribute(name, new Str(stringValue));
    return attribute;
  }

  @Override
  public boolean isUpdating() {
    if (nameExpr.isUpdating()) {
      return isUpdating();
    }
    for (Expr e : valueExpr) {
      if (e.isUpdating()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isVacuous() {
    return false;
  }

  @Override
  public String toString() {
    return "attribute";
  }
}
