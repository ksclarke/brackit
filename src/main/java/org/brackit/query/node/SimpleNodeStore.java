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
package org.brackit.query.node;

import org.brackit.query.node.d2linked.D2NodeFactory;
import org.brackit.query.node.parser.DocumentParser;
import org.brackit.query.node.parser.NodeSubtreeParser;
import org.brackit.query.util.io.URIHandler;
import org.brackit.query.jdm.DocumentException;
import org.brackit.query.jdm.OperationNotSupportedException;
import org.brackit.query.jdm.Stream;
import org.brackit.query.jdm.node.Node;
import org.brackit.query.jdm.node.NodeCollection;
import org.brackit.query.jdm.node.NodeFactory;
import org.brackit.query.jdm.node.NodeStore;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Sebastian Baechle
 */
public class SimpleNodeStore implements NodeStore {
  private final Map<String, NodeCollection<?>> docs = new HashMap<>();

  @Override
  public NodeCollection<?> create(String name) throws DocumentException {
    NodeCollection<?> coll = getNodeFactory().collection(name);
    docs.put(name, coll);
    return coll;
  }

  @Override
  public NodeCollection<?> create(String name, NodeSubtreeParser parser) throws DocumentException {
    NodeCollection<?> coll = getNodeFactory().collection(name, parser);
    docs.put(name, coll);
    return coll;
  }

  @Override
  public NodeCollection<?> create(String name, Stream<NodeSubtreeParser> parsers) throws DocumentException {
    NodeCollection<?> coll = getNodeFactory().collection(name, parsers);
    docs.put(name, coll);
    return coll;
  }

  @Override
  public void drop(String name) throws DocumentException {
    if (docs.remove(name) == null) {
      throw new DocumentException("Collection %s not found", name);
    }
  }

  @Override
  public NodeCollection<?> lookup(String name) throws DocumentException {
    NodeCollection<?> coll = docs.get(name);
    if (coll != null) {
      return coll;
    }
    try {
      InputStream in = URIHandler.getInputStream(URI.create(name));
      DocumentParser p = new DocumentParser(in);
      Node<?> doc = getNodeFactory().build(p);
      coll = doc.getCollection();
      docs.put(name, coll);
      return coll;
    } catch (IOException e) {
      throw new DocumentException(e, "Collection %s not found", name);
    }
  }

  protected NodeFactory<?> getNodeFactory() {
    return new D2NodeFactory();
  }

  @Override
  public void makeDir(String path) throws DocumentException {
    throw new OperationNotSupportedException();
  }
}
