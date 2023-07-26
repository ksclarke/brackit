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
package org.brackit.query;

import java.util.HashMap;
import java.util.Map;

import org.brackit.query.atomic.AbstractTimeInstant;
import org.brackit.query.atomic.AnyURI;
import org.brackit.query.atomic.DTD;
import org.brackit.query.atomic.Date;
import org.brackit.query.atomic.DateTime;
import org.brackit.query.atomic.QNm;
import org.brackit.query.atomic.Time;
import org.brackit.query.jsonitem.SimpleJsonStore;
import org.brackit.query.node.SimpleNodeStore;
import org.brackit.query.node.d2linked.D2NodeFactory;
import org.brackit.query.update.UpdateList;
import org.brackit.query.update.op.UpdateOp;
import org.brackit.query.jdm.Item;
import org.brackit.query.jdm.Sequence;
import org.brackit.query.jdm.json.JsonCollection;
import org.brackit.query.jdm.json.JsonStore;
import org.brackit.query.jdm.node.Node;
import org.brackit.query.jdm.node.NodeCollection;
import org.brackit.query.jdm.node.NodeFactory;
import org.brackit.query.jdm.node.NodeStore;
import org.brackit.query.jdm.type.ItemType;

/**
 * @author Sebastian Baechle
 */
public class BrackitQueryContext implements QueryContext {
  private static final NodeFactory<?> FACTORY = new D2NodeFactory();

  private final NodeFactory<?> nodeFactory;

  private final NodeStore nodeStore;

  private final JsonStore jsonStore;

  private UpdateList updates;

  private Item extCtxItem;

  private ItemType extCtxItemType;

  private Map<QNm, Sequence> externalVars;

  private Node<?> defaultDocument;

  private NodeCollection<?> defaultNodeCollection;

  private JsonCollection<?> defaultJsonCollection;

  private DateTime dateTime;

  private Date date;

  private Time time;

  private final DTD implicitTimezone = AbstractTimeInstant.LOCAL_TIMEZONE;

  public BrackitQueryContext() {
    this.nodeFactory = FACTORY;
    this.nodeStore = new SimpleNodeStore();
    this.jsonStore = new SimpleJsonStore();
  }

  public BrackitQueryContext(NodeStore store) {
    this.nodeFactory = FACTORY;
    this.nodeStore = store;
    this.jsonStore = new SimpleJsonStore();
  }

  public BrackitQueryContext(JsonStore store) {
    this.nodeFactory = FACTORY;
    this.nodeStore = new SimpleNodeStore();
    this.jsonStore = store;
  }

  public BrackitQueryContext(NodeStore nodeStore, JsonStore jsonStore) {
    this.nodeFactory = FACTORY;
    this.nodeStore = nodeStore;
    this.jsonStore = jsonStore;
  }

  @Override
  public void addPendingUpdate(UpdateOp op) {
    if (updates == null) {
      updates = new UpdateList();
    }
    updates.append(op);
  }

  @Override
  public void applyUpdates() {
    if (updates != null) {
      updates.apply();
    }
  }

  @Override
  public UpdateList getUpdateList() {
    return updates;
  }

  @Override
  public void setUpdateList(UpdateList updates) {
    this.updates = updates;
  }

  @Override
  public void bind(QNm name, Sequence sequence) {
    if (externalVars == null) {
      externalVars = new HashMap<>(3);
    }
    externalVars.put(name, sequence);
  }

  @Override
  public Sequence resolve(QNm name) {
    return externalVars != null ? externalVars.get(name) : null;
  }

  @Override
  public boolean isBound(QNm name) {
    return externalVars != null && externalVars.containsKey(name);
  }

  @Override
  public void setContextItem(Item item) {
    extCtxItem = item;
    if (item != null) {
      extCtxItemType = item.itemType();
    }
  }

  @Override
  public Item getContextItem() {
    return extCtxItem;
  }

  @Override
  public ItemType getItemType() {
    return extCtxItemType;
  }

  @Override
  public Node<?> getDefaultDocument() {
    return defaultDocument;
  }

  @Override
  public void setDefaultDocument(Node<?> defaultDocument) {
    this.defaultDocument = defaultDocument;
  }

  @Override
  public NodeCollection<?> getDefaultNodeCollection() {
    return defaultNodeCollection;
  }

  @Override
  public JsonCollection<?> getDefaultJsonCollection() {
    return defaultJsonCollection;
  }

  @Override
  public void setDefaultNodeCollection(NodeCollection<?> defaultNodeCollection) {
    this.defaultNodeCollection = defaultNodeCollection;
  }

  @Override
  public void setDefaultJsonCollection(JsonCollection<?> defaultJsonCollection) {
    this.defaultJsonCollection = defaultJsonCollection;
  }

  @Override
  public DateTime getDateTime() {
    return dateTime != null ? dateTime : (dateTime = new DateTime(implicitTimezone));
  }

  @Override
  public Date getDate() {
    return date != null ? date : (date = new Date(getDateTime()));
  }

  @Override
  public Time getTime() {
    return time != null ? time : (time = new Time(getDateTime()));
  }

  @Override
  public DTD getImplicitTimezone() {
    return implicitTimezone;
  }

  @Override
  public AnyURI getBaseUri() {
    return AnyURI.EMPTY;
  }

  @Override
  public NodeFactory<?> getNodeFactory() {
    return nodeFactory;
  }

  @Override
  public NodeStore getNodeStore() {
    return nodeStore;
  }

  @Override
  public JsonStore getJsonItemStore() {
    return jsonStore;
  }
}
