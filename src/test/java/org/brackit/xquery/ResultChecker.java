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
package org.brackit.xquery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.brackit.xquery.atomic.Atomic;
import org.brackit.xquery.atomic.Int32;
import org.brackit.xquery.operator.Cursor;
import org.brackit.xquery.operator.TupleImpl;
import org.brackit.xquery.jdm.DocumentException;
import org.brackit.xquery.jdm.Item;
import org.brackit.xquery.jdm.Iter;
import org.brackit.xquery.jdm.Kind;
import org.brackit.xquery.jdm.OperationNotSupportedException;
import org.brackit.xquery.jdm.Sequence;
import org.brackit.xquery.jdm.Stream;
import org.brackit.xquery.jdm.json.Array;
import org.brackit.xquery.jdm.json.Object;
import org.brackit.xquery.jdm.node.Node;

/**
 * @author Sebastian Baechle
 */
public class ResultChecker {
  public static void dCheckS(QueryContext ctx, Cursor op, Sequence... expected) throws QueryException {
    // double check result to verify that plan can be evaluated repeatedly
    checkS(ctx, op, expected);
    checkS(ctx, op, expected);
  }

  public static void checkS(QueryContext ctx, Cursor op, Sequence... expected) throws QueryException {
    Tuple[] e = new Tuple[expected.length];
    for (int i = 0; i < expected.length; i++) {
      e[i] = new TupleImpl(expected[i]);
    }
    checkT(ctx, op, e);
  }

  public static void dCheckT(QueryContext ctx, Cursor op, Tuple... expected) throws QueryException {
    // double check result to verify that plan can be evaluated repeatedly
    checkT(ctx, op, expected);
    checkT(ctx, op, expected);
  }

  public static void checkT(QueryContext ctx, Cursor op, Tuple... expected) throws QueryException {
    Tuple next;
    op.open(ctx);
    for (int i = 0; i < expected.length; i++) {
      assertNotNull("Result is empty", next = op.next(ctx));
      checkTuple(ctx, expected[i], next);
    }
    assertNull("No more results delivered", op.next(ctx));
    op.close(ctx);
  }

  public static void checkTuple(QueryContext ctx, Tuple expected, Tuple result) throws QueryException {
    int eSize = expected.getSize();
    int rSize = result.getSize();
    assertEquals("Result tuple has same size", eSize, rSize);
    for (int i = 0; i < eSize; i++) {
      Sequence eSequence = expected.get(i);
      Sequence rSequence = result.get(i);
      check(eSequence, rSequence);
    }
  }

  public static void dCheck(Sequence expected, Sequence result) throws QueryException {
    // double check result to verify that result can be evaluated repeatedly
    check(expected, result, true);
    check(expected, result, true);
  }

  public static void dCheck(Sequence expected, Sequence result, boolean nodeIdentity) throws QueryException {
    // double check result to verify that result can be evaluated repeatedly
    check(expected, result, nodeIdentity);
    check(expected, result, nodeIdentity);
  }

  public static void check(Sequence expected, Sequence result) throws QueryException {
    compare(expected, result, true);
  }

  public static void check(Sequence expected, Sequence result, boolean nodeIdentity) throws QueryException {
    compare(expected, result, nodeIdentity);
  }

  private static void compare(Sequence expected, Sequence result, boolean nodeIdentity)
      throws QueryException, AssertionError {
    if (expected == null) {
      if (result != null) // verify that result sequence has no results
      {
        Iter s = result.iterate();
        try {
          assertNull("Result sequence is empty", s.next());
        } finally {
          s.close();
        }
        assertFalse("Result has boolean value of empty sequence", result.booleanValue());
        assertTrue("Result has size of empty sequence", Int32.ZERO.cmp(result.size()) == 0);
      }
    } else {
      assertNotNull("Result sequence is not empty", result);
      Iter es = expected.iterate();
      try {
        Iter rs = result.iterate();
        try {
          Item eItem;
          Item rItem;
          while ((eItem = es.next()) != null) {
            assertNotNull("Result sequence has more results", rItem = rs.next());
            try {
              assertEquals("Result item has same type", eItem.itemType(), rItem.itemType());

              if (eItem instanceof Node<?>) {
                compareNode(eItem, rItem, nodeIdentity);
              } else if (eItem instanceof Object) {
                compareRecord(eItem, rItem);
              } else if (eItem instanceof Array) {
                compareArray(eItem, rItem);
              } else {
                compareAtomic(eItem, rItem);
              }
            } catch (AssertionError e) {
              System.err.println(String.format("Expected: '%s'\t Result: '%s'", eItem, rItem));
              throw e;
            } catch (QueryException e) {
              if (e.getCode() == ErrorCode.ERR_TYPE_INAPPROPRIATE_TYPE) {
                fail(String.format("Wrong item (type) in result. Expected: '%s'\t Result: '%s'", eItem, rItem));
              }
              throw e;
            }
          }
          assertNull("Result sequence has not more results than expected", rs.next());
        } finally {
          rs.close();
        }
      } finally {
        es.close();
      }

      try {
        boolean expectedBooleanValue = expected.booleanValue();
        boolean resultBooleanValue = false;

        try {
          resultBooleanValue = result.booleanValue();
        } catch (QueryException e) {
          fail("Result does not have defined boolean value");
        }

        assertEquals("Result has expected boolean value", expectedBooleanValue, resultBooleanValue);
      } catch (QueryException e) {
        assertEquals("Correct error code", ErrorCode.ERR_INVALID_ARGUMENT_TYPE, e.getCode());

        try {
          result.booleanValue();
          fail("Result has defined boolean value");
        } catch (QueryException e1) {
          assertEquals("Correct error code", ErrorCode.ERR_INVALID_ARGUMENT_TYPE, e1.getCode());
        }
      }
    }
  }

  private static void compareArray(Item eItem, Item rItem) {
  }

  private static void compareAtomic(Item eItem, Item rItem) throws QueryException {
    assertTrue("Result item is atomic", rItem instanceof Atomic);
    assertTrue("Result atomic is equal to expected", ((Atomic) eItem).eq((Atomic) rItem));
  }

  private static void compareRecord(Item eItem, Item rItem) throws DocumentException {
    assertTrue("Result item is record", rItem instanceof Object);
    Object eNode = (Object) eItem;
    Object rNode = (Object) rItem;

    assertEquals(eNode.size(), rNode.size());

    // TODO
  }

  private static void compareNode(Item eItem, Item rItem, boolean nodeIdentity) throws DocumentException {
    assertTrue("Result item is node", rItem instanceof Node<?>);
    Node<?> eNode = (Node<?>) eItem;
    Node<?> rNode = (Node<?>) rItem;
    if (nodeIdentity) {
      assertTrue("Result node is equal to expected", eNode.isSelfOf(rNode));
    } else {
      compareNode(eNode, rNode);
    }
  }

  private static void compareNode(Node<?> eNode, Node<?> rNode)
      throws DocumentException, OperationNotSupportedException {
    assertEquals("Node kind is correct", eNode.getKind(), rNode.getKind());
    if (eNode.getKind() == Kind.DOCUMENT) {
      compareChildren(eNode, rNode);
    } else {
      assertEquals("Node name is correct", eNode.getName(), rNode.getName());
      if (eNode.getKind() == Kind.ELEMENT) {
        compareAttributes(eNode, rNode);
        compareChildren(eNode, rNode);
      } else {
        assertEquals("Node value correct", eNode.getValue(), rNode.getValue());
      }
    }
  }

  private static void compareChildren(Node<?> eNode, Node<?> rNode) throws DocumentException {
    Stream<? extends Node<?>> eChildren = eNode.getChildren();
    try {
      Stream<? extends Node<?>> rChildren = rNode.getChildren();
      try {
        Node<?> eChild;
        Node<?> rChild;
        while ((eChild = eChildren.next()) != null) {
          assertNotNull("Child is in result", rChild = rChildren.next());
          compareNode(eChild, rChild);
        }
        assertNull("Result has no further children", rChildren.next());
      } finally {
        rChildren.close();
      }
    } finally {
      eChildren.close();
    }
  }

  private static void compareAttributes(Node<?> eNode, Node<?> rNode) throws DocumentException {
    Stream<? extends Node<?>> eAtts = eNode.getAttributes();
    try {
      Stream<? extends Node<?>> rAtts = rNode.getAttributes();
      try {
        Node<?> eAtt;
        Node<?> rAtt;
        while ((eAtt = eAtts.next()) != null) {
          assertNotNull("Attribute is in result", rAtt = rAtts.next());
          assertEquals("Node kind is correct", Kind.ATTRIBUTE, rAtt.getKind());
          assertEquals("Node name is correct", eAtt.getName(), rAtt.getName());
          assertEquals("Node name value correct", eAtt.getValue(), rAtt.getValue());
        }
        assertNull("Result has no further attributes", rAtts.next());
      } finally {
        rAtts.close();
      }
    } finally {
      eAtts.close();
    }
  }
}
