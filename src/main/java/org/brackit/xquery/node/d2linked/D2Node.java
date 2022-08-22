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
package org.brackit.xquery.node.d2linked;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import org.brackit.xquery.atomic.Atomic;
import org.brackit.xquery.atomic.QNm;
import org.brackit.xquery.node.AbstractNode;
import org.brackit.xquery.node.parser.SubtreeHandler;
import org.brackit.xquery.node.parser.SubtreeParser;
import org.brackit.xquery.node.stream.AtomStream;
import org.brackit.xquery.node.stream.EmptyStream;
import org.brackit.xquery.xdm.DocumentException;
import org.brackit.xquery.xdm.Kind;
import org.brackit.xquery.xdm.OperationNotSupportedException;
import org.brackit.xquery.xdm.Scope;
import org.brackit.xquery.xdm.Stream;
import org.brackit.xquery.xdm.node.Node;

/**
 * Abstract base class for memory nodes.
 *
 * @author Sebastian Baechle
 */
public abstract class D2Node extends AbstractNode<D2Node> {
  protected static final AtomicInteger ID_SEQUENCE = new AtomicInteger();

  public static final int NO_ADDITIONAL_STATIC_DIVISIONS = 63;

  public static final int MAX_STATIC_DIVISION = (1 + NO_ADDITIONAL_STATIC_DIVISIONS) * 2 + 1;

  public static final int[] FIRST = new int[] { 3 };

  private static final int[][] STATIC_DIVISIONS = new int[1 + NO_ADDITIONAL_STATIC_DIVISIONS][];

  static {
    STATIC_DIVISIONS[0] = FIRST;
    for (int i = 0; i < NO_ADDITIONAL_STATIC_DIVISIONS; i++) {
      STATIC_DIVISIONS[i + 1] = new int[] { (i + 2) * 2 + 1 };
    }
  }

  /**
   * Basic
   */
  public static final int NODE_CLASS_ID = 1;

  protected final ParentD2Node parent;

  protected final int[] division;

  protected D2Node sibling;

  protected int localFragmentID;

  protected D2Node(ParentD2Node parent, int[] division) {
    this.parent = parent;
    this.division = division;
    this.localFragmentID = parent == null ? localFragmentID() : parent.localFragmentID;
  }

  private D2Node getRoot() {
    D2Node parent = this;
    while (parent.parent != null) {
      parent = parent.parent;
    }
    return parent;
  }

  @Override
  public D2NodeCollection getCollection() {
    return parent == null ? null : getRoot().getCollection();
  }

  @Override
  public final int getNodeClassID() {
    return NODE_CLASS_ID;
  }

  private int localFragmentID() {
    int localFragmentID = ID_SEQUENCE.incrementAndGet();
    while (localFragmentID < 0) {
      if (ID_SEQUENCE.compareAndSet(localFragmentID, 1)) {
        localFragmentID = 1;
        return localFragmentID;
      }
      localFragmentID = ID_SEQUENCE.incrementAndGet();
    }
    return localFragmentID;
  }

  @Override
  protected final int cmpInternal(final D2Node node) {
    if (node == this) {
      return 0;
    }
    if (localFragmentID != node.localFragmentID) {
      return localFragmentID < node.localFragmentID ? -1 : 1;
    }
    D2Node c = null;
    D2Node cp = this;
    while (cp != null) {
      D2Node lcap = null;
      D2Node lca = node;
      while (lca != null) {
        if (lca == cp) {
          // found least common ancestor
          // case 0: node == this is not allowed
          // case 1: lca is this
          if (lca == this)
            return -1;
          // case 2: lca is node
          if (lca == node)
            return 1;
          // case 3: c and lcap have the same parent
          Kind kind = c.getKind();
          Kind nkind = lcap.getKind();
          if (kind == Kind.ATTRIBUTE ^ nkind == Kind.ATTRIBUTE) {
            return kind == Kind.ATTRIBUTE ? -1 : 1;
          }
          // nodes must be in the same chain -> search
          // scan from context node to n
          return compare(c.division, lcap.division);
        }
        lcap = lca;
        lca = lca.parent;
      }
      c = cp;
      cp = cp.parent;
    }
    return -1;
  }

  protected final boolean isInSubtreeOf(D2Node n) {
    D2Node a = parent;
    while (a != null) {
      if (a == n) {
        return true;
      }
      a = a.parent;
    }
    return false;
  }

  protected final int[] siblingAfter(int[] p) {
    return getDivision(p[0] + ((p[0] & 1) != 0 ? 2 : 1));
  }

  protected final int[] getDivision(int value) {
    return value <= MAX_STATIC_DIVISION ? STATIC_DIVISIONS[value / 2 - 1] : new int[] { value };
  }

  protected final int[] siblingBetween(int[] p, int[] n) {
    if (n == null || n[0] - p[0] > 2) {
      return siblingAfter(p);
    }
    int length = Math.min(n.length, p.length);
    for (int i = 0; i < length; i++) {
      if (n[i] != p[i]) {
        if (n[i] < p[i]) {
          throw new IllegalArgumentException(String.format("Illegal sibling divisions: %s > %s",
                                                           Arrays.toString(p),
                                                           Arrays.toString(n)));
        } else if ((p[i] & 1) == 0) // p[i] is even
        {
          if (n[i] - p[i] > 1) // e.g. p[i] == 4 and n[i] >= 6
          {
            // There is room for a new uneven sibling between p[i]
            // and n[i]:
            // Copy p[0..i] and increase res[i] by 1 (e.g. 4->5)
            int[] r = Arrays.copyOf(p, i + 1);
            r[i] += 1;
            return r;
          } else // e.g. p[i] == 4 and n[i] == 5
          {
            // Create a new sibling under p[0..i+1]
            // Copy p[0..i+1] and increase res[i+1] by 3 (e.g.
            // 0->3),
            // 1 (e.g. 2->3) or 2 (e.g. 3->5) respectively
            int[] r = Arrays.copyOf(p, i + 2);
            r[i + 1] += r[i + 1] == 0 ? 3 : (r[i + 1] & 1) == 0 ? 1 : 2;
            return r;
          }
        } else if ((n[i] & 1) == 0) // p[i] is uneven and n[i] is even
        {
          if (n[i] - p[i] > 2) // e.g. p[i] == 5 and n[i] >= 8
          {
            // There is room for a new uneven sibling between p[i]
            // and n[i]:
            // Copy p[0..i] and increase res[i] by 2 (e.g. 5->7)
            int[] r = Arrays.copyOf(p, i + 1);
            r[i] += 2;
            return r;
          } else // e.g. p[i] == 5 and n[i] == 6
          {
            // Create a new sibling under p[0..i+1]
            // Copy p[0..i+1] and increase res[i+1] by 3 (e.g.
            // 0->3),
            // 1 (e.g. 2->3) or 2 (e.g. 3->5) respectively
            int[] r = Arrays.copyOf(p, i + 2);
            r[i + 1] += r[i + 1] == 0 ? 3 : (r[i + 1] & 1) == 0 ? 1 : 2;
            return r;
          }
        } else // p[i] and n[i] are uneven
        {
          if (n[i] - p[i] > 2) // e.g. p[i] == 5 and n[i] == 9
          {
            // There is room for a new uneven sibling between p[i]
            // and n[i]:
            // Copy p[0..i] and increase res[i] by 2 (e.g. 3->5)
            int[] r = Arrays.copyOf(p, i + 1);
            r[i] += 2;
            return r;
          } else {
            // Create a new sibling under p[0..i+1]
            // Copy p[0..i+1] and increase res[i] by 1 and set
            // res[i+1] to 3
            int[] r = Arrays.copyOf(p, i + 2);
            r[i] += 1;
            r[i + 1] = 3;
            return r;
          }
        }
      }
    }
    throw new IllegalArgumentException(String.format("Illegal sibling divisions: %s and %s",
                                                     Arrays.toString(p),
                                                     Arrays.toString(n)));
  }

  protected final int[] siblingBefore(int[] n) {
    if (n.length == 1) {
      return n[0] > 3 ? getDivision(n[0] - 2) : new int[] { 2, 3 };
    } else {
      for (int i = 0; i < n.length; i++) {
        if (n[i] > 3) {
          int[] r = Arrays.copyOf(n, i + 1);
          r[i] -= (r[i] & 1) == 0 ? 1 : 2;
          return r;
        }
      }
      int[] r = Arrays.copyOf(n, n.length + 1);
      r[r.length - 2] -= 1;
      r[r.length - 1] = 3;
      return r;
    }
  }

  private int compare(int[] value1, int[] value2) {
    int length1 = value1.length;
    int length2 = value2.length;
    int length = Math.min(length1, length2);

    int pos = -1;
    while (++pos < length) {
      int v2 = value2[pos];
      int v1 = value1[pos];

      if (v1 != v2) {
        return v1 < v2 ? -1 : 1;
      }
    }

    return length1 - length2;
  }

  @Override
  public Scope getScope() {
    return null;
  }

  @Override
  public D2Node getParent() throws DocumentException {
    return parent;
  }

  @Override
  public final boolean isSelfOf(Node<?> node) {
    return node == this;
  }

  @Override
  public boolean isAncestorOf(Node<?> node) {
    return false;
  }

  @Override
  public boolean isAncestorOrSelfOf(Node<?> node) {
    // check only for self; overridden in parent node
    // TODO: fix for sun's compiler bug using generics parent == node
    return this == node;
  }

  @Override
  public boolean isAttributeOf(Node<?> node) {
    return false;
  }

  @Override
  public boolean isChildOf(Node<?> node) {
    // TODO: fix for sun's compiler bug using generics parent == node
    return node != null && parent == node;
  }

  @Override
  public boolean isDescendantOf(Node<?> node) {
    // TODO: fix for sun's compiler bug using generics parent == node
    return node != null && (parent == node || parent != null && parent.isDescendantOf(node));
  }

  @Override
  public boolean isDescendantOrSelfOf(Node<?> node) {
    // TODO: fix for sun's compiler bug using generics parent == node
    return node != null && (this == node || parent == node || parent != null && parent.isDescendantOrSelfOf(
        node));
  }

  @Override
  public boolean isParentOf(Node<?> node) {
    return false;
  }

  @Override
  public boolean isFollowingOf(Node<?> node) {
    if (node == this || !(node instanceof D2Node n) || getKind() == Kind.ATTRIBUTE) {
      return false;
    }
    if (cmpInternal(n) <= 0) {
      return false;
    }
    D2Node c = this.parent;
    while (c != null) {
      if (c == n) {
        // n is ancestor
        return false;
      }
      c = c.parent;
    }
    return true;
  }

  @Override
  public boolean isFollowingSiblingOf(Node<?> node) {
    if (parent == null || node == this || !(node instanceof D2Node n) || node.getKind() == Kind.ATTRIBUTE) {
      return false;
    }
    if (parent != n.parent) {
      return false;
    }
    return compare(division, n.division) > 0;
  }

  @Override
  public boolean isPrecedingOf(Node<?> node) {
    if (node == this || !(node instanceof D2Node n) || getKind() == Kind.ATTRIBUTE) {
      return false;
    }
    if (cmpInternal(n) >= 0) {
      return false;
    }
    n = n.parent;
    while (n != null) {
      if (n == this) {
        // n is ancestor
        return false;
      }
      n = n.parent;
    }
    return true;
  }

  @Override
  public boolean isPrecedingSiblingOf(Node<?> node) {
    if (parent == null || node == this || !(node instanceof D2Node n) || node.getKind() == Kind.ATTRIBUTE) {
      return false;
    }
    if (parent != n.parent) {
      return false;
    }
    return compare(division, n.division) < 0;
  }

  @Override
  public final boolean isRoot() {
    return getKind() == Kind.ELEMENT && parent != null && parent.getKind() == Kind.DOCUMENT;
  }

  @Override
  public boolean isSiblingOf(Node<?> node) {
    return node != null
        // TODO: fix for sun's compiler bug using generics parent ==
        // node
        && node != this && parent != null && node.isChildOf(parent);
  }

  @Override
  public final boolean isDocumentRoot() {
    return parent == null;
  }

  @Override
  public boolean isDocumentOf(Node<?> node) {
    return false;
  }

  @Override
  public boolean hasAttributes() throws DocumentException {
    return false;
  }

  @Override
  public boolean hasChildren() throws DocumentException {
    return false;
  }

  @Override
  public void delete() throws DocumentException {
    if (parent != null) {
      if (getKind() == Kind.ATTRIBUTE) {
        parent.deleteAttribute(getName());
      } else {
        parent.deleteChild(this);
      }
    }
  }

  @Override
  public void parse(SubtreeHandler handler) throws DocumentException {
    new D2NodeParser(this).parse(handler);
  }

  @Override
  public D2Node getNextSibling() throws DocumentException {
    if (parent == null) {
      return null;
    }
    return parent.nextSiblingOf(this);
  }

  @Override
  public D2Node getPreviousSibling() throws DocumentException {
    if (parent == null) {
      return null;
    }
    return parent.previousSiblingOf(this);
  }

  @Override
  public D2Node getFirstChild() throws DocumentException {
    return null;
  }

  @Override
  public D2Node getLastChild() throws DocumentException {
    return null;
  }

  @Override
  public Stream<D2Node> getChildren() throws DocumentException {
    return new EmptyStream<>();
  }

  @Override
  public D2Node getAttribute(QNm name) throws DocumentException {
    return null;
  }

  @Override
  public Stream<D2Node> getAttributes() throws DocumentException {
    return new EmptyStream<>();
  }

  @Override
  public Stream<D2Node> getSubtree() throws DocumentException {
    return new AtomStream<>(this);
  }

  @Override
  public Stream<? extends D2Node> getDescendantOrSelf() throws DocumentException {
    return new AtomStream<>(this);
  }

  @Override
  public void setName(QNm name) throws DocumentException {
    throw new OperationNotSupportedException();
  }

  @Override
  public void setValue(Atomic value) throws DocumentException {
    throw new OperationNotSupportedException();
  }

  @Override
  public D2Node setAttribute(Node<?> attribute) throws DocumentException {
    throw new OperationNotSupportedException();
  }

  @Override
  public D2Node setAttribute(QNm name, Atomic value) throws DocumentException {
    throw new OperationNotSupportedException();
  }

  @Override
  public boolean deleteAttribute(QNm name) throws DocumentException {
    throw new OperationNotSupportedException();
  }

  @Override
  public D2Node append(Node<?> child) throws DocumentException {
    throw new OperationNotSupportedException();
  }

  @Override
  public D2Node append(SubtreeParser parser) throws DocumentException {
    throw new OperationNotSupportedException();
  }

  @Override
  public D2Node append(Kind kind, QNm name, Atomic value) throws DocumentException {
    throw new OperationNotSupportedException();
  }

  @Override
  public D2Node prepend(Kind kind, QNm name, Atomic value) throws DocumentException {
    throw new OperationNotSupportedException();
  }

  @Override
  public D2Node prepend(Node<?> child) throws DocumentException {
    throw new OperationNotSupportedException();
  }

  @Override
  public D2Node prepend(SubtreeParser parser) throws DocumentException {
    throw new OperationNotSupportedException();
  }

  @Override
  public D2Node insertAfter(Kind kind, QNm name, Atomic value)
      throws DocumentException {
    if (parent == null) {
      throw new DocumentException("%s has no parent", this);
    }
    return parent.insertAfter(kind, name, value);
  }

  @Override
  public D2Node insertAfter(Node<?> child) throws DocumentException {
    if (parent == null) {
      throw new DocumentException("%s has no parent", this);
    }
    return parent.insertAfter(this, child);
  }

  @Override
  public D2Node insertAfter(SubtreeParser parser) throws DocumentException {
    if (parent == null) {
      throw new DocumentException("%s has no parent", this);
    }
    return parent.insertAfter(this, parser);
  }

  @Override
  public D2Node insertBefore(Kind kind, QNm name, Atomic value)
      throws DocumentException {
    if (parent == null) {
      throw new DocumentException("%s has no parent", this);
    }
    return parent.insertBefore(kind, name, value);
  }

  @Override
  public D2Node insertBefore(Node<?> child) throws DocumentException {
    if (parent == null) {
      throw new DocumentException("%s has no parent", this);
    }
    return parent.insertBefore(this, child);
  }

  @Override
  public D2Node insertBefore(SubtreeParser parser) throws DocumentException {
    if (parent == null) {
      throw new DocumentException("%s has no parent", this);
    }
    return parent.insertBefore(this, parser);
  }

  @Override
  public D2Node replaceWith(Kind kind, QNm name, Atomic value)
      throws DocumentException {
    if (parent == null) {
      throw new DocumentException("Cannot replace node without parent");
    }

    return parent.replace(this, kind, name, value);
  }

  @Override
  public D2Node replaceWith(Node<?> node) throws DocumentException {
    if (parent == null) {
      throw new DocumentException("Cannot replace node without parent");
    }

    return parent.replace(this, node);
  }

  @Override
  public D2Node replaceWith(SubtreeParser parser) throws DocumentException {
    final D2Node me = this;
    D2NodeBuilder builder = new D2NodeBuilder() {
      @Override
      D2Node first(Kind kind, QNm name, Atomic value) throws DocumentException {
        if (parent == null) {
          throw new DocumentException("Cannot replace node without parent");
        }

        return parent.replace(me, kind, name, value);
      }
    };
    parser.parse(builder);
    return builder.root();
  }
}
