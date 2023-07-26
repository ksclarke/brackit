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
package io.brackit.query.node.parser;

import java.util.ArrayList;
import java.util.List;

import io.brackit.query.jdm.DocumentException;
import io.brackit.query.jdm.node.Node;
import io.brackit.query.util.log.Logger;

/**
 * @param <E>
 * @author Sebastian Baechle
 */
public abstract class NodeSubtreeProcessor<E extends Node<E>> {
  private static final Logger log = Logger.getLogger(NodeSubtreeProcessor.class);

  private final List<NodeSubtreeListener<? super E>> listeners;

  public NodeSubtreeProcessor(final List<NodeSubtreeListener<? super E>> listeners) {
    this.listeners = new ArrayList<NodeSubtreeListener<? super E>>();
    if (listeners != null) {
      for (final NodeSubtreeListener<? super E> listener : listeners) {
        this.listeners.add(listener);
      }
    }
  }

  protected void notifyBegin() throws DocumentException {
    for (NodeSubtreeListener<? super E> listener : listeners) {
      listener.begin();
    }
  }

  protected void notifyEnd() throws DocumentException {
    for (NodeSubtreeListener<? super E> listener : listeners) {
      listener.end();
    }
  }

  protected void notifyBeginDocument() throws DocumentException {
    for (NodeSubtreeListener<? super E> listener : listeners) {
      listener.startDocument();
    }
  }

  protected void notifyEndDocument() throws DocumentException {
    for (NodeSubtreeListener<? super E> listener : listeners) {
      listener.endDocument();
    }
  }

  protected void notifyBeginFragment() throws DocumentException {
    for (NodeSubtreeListener<? super E> listener : listeners) {
      listener.beginFragment();
    }
  }

  protected void notifyEndFragment() throws DocumentException {
    for (NodeSubtreeListener<? super E> listener : listeners) {
      listener.endFragment();
    }
  }

  protected void notifyFail() {
    for (NodeSubtreeListener<? super E> listener : listeners) {
      try {
        listener.fail();
      } catch (DocumentException e) {
        log.error(e);
      }
    }
  }

  protected void notifyStartElement(E node) throws DocumentException {
    for (NodeSubtreeListener<? super E> listener : listeners) {
      listener.startElement(node);
    }
  }

  protected void notifyEndElement(E node) throws DocumentException {
    for (NodeSubtreeListener<? super E> listener : listeners) {
      listener.endElement(node);
    }
  }

  protected void notifyAttribute(E node) throws DocumentException {
    for (NodeSubtreeListener<? super E> listener : listeners) {
      listener.attribute(node);
    }
  }

  protected void notifyText(E node) throws DocumentException {
    for (NodeSubtreeListener<? super E> listener : listeners) {
      listener.text(node);
    }
  }

  protected void notifyComment(E node) throws DocumentException {
    for (NodeSubtreeListener<? super E> listener : listeners) {
      listener.comment(node);
    }
  }

  protected void notifyProcessingInstruction(E node) throws DocumentException {
    for (NodeSubtreeListener<? super E> listener : listeners) {
      listener.processingInstruction(node);
    }
  }
}