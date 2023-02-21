package org.brackit.xquery.jdm.type;

import org.brackit.xquery.atomic.QNm;
import org.brackit.xquery.jdm.Item;
import org.brackit.xquery.jdm.Kind;
import org.brackit.xquery.jdm.Type;
import org.brackit.xquery.jdm.json.JsonItem;

public abstract class JsonItemType extends StructuredItemType {
  @Override
  public boolean isAnyItem() {
    return false;
  }

  @Override
  public boolean isAtomic() {
    return false;
  }

  @Override
  public boolean isNode() {
    return true;
  }

  @Override
  public boolean isStructuredItem() {
    return true;
  }

  @Override
  public boolean isJsonItem() {
    return true;
  }

  @Override
  public boolean isFunction() {
    return false;
  }

  @Override
  public boolean isArray() {
    return false;
  }

  @Override
  public boolean isObject() {
    return false;
  }

  /**
   * null indicates any node kind
   */
  @Override
  public Kind getNodeKind() {
    return null;
  }

  /**
   * null indicates any name
   */
  @Override
  public QNm getQName() {
    return null;
  }

  /**
   * null indicates any type
   */
  @Override
  public Type getType() {
    return null;
  }

  @Override
  public boolean matches(Item item) {
    return (item instanceof JsonItem);
  }
}
