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
package io.brackit.query.function;

import io.brackit.query.ErrorCode;
import io.brackit.query.atomic.Atomic;
import io.brackit.query.atomic.QNm;
import io.brackit.query.jdm.AbstractItem;
import io.brackit.query.jdm.Function;
import io.brackit.query.jdm.Signature;
import io.brackit.query.jdm.type.FunctionType;
import io.brackit.query.jdm.type.ItemType;
import io.brackit.query.QueryException;

/**
 * @author Sebastian Baechle
 */
public abstract class AbstractFunction extends AbstractItem implements Function {
  private final QNm name;

  private final Signature signature;

  private final boolean isUpdating;

  private final boolean isBuiltIn;

  public AbstractFunction(QNm name, Signature signature, boolean isBuiltIn) {
    this.name = name;
    this.signature = signature;
    this.isUpdating = false;
    this.isBuiltIn = isBuiltIn;
  }

  public AbstractFunction(QNm name, Signature signature, boolean isBuiltIn, boolean isUpating) {
    this.name = name;
    this.signature = signature;
    this.isUpdating = isUpating;
    this.isBuiltIn = isBuiltIn;
  }

  @Override
  public final QNm getName() {
    return name;
  }

  @Override
  public final Signature getSignature() {
    return signature;
  }

  @Override
  public boolean isUpdating() {
    return isUpdating;
  }

  @Override
  public boolean isBuiltIn() {
    return isBuiltIn;
  }

  @Override
  public final String toString() {
    return name == null ? "" + signature : name.toString() + signature;
  }

  @Override
  public Atomic atomize() throws QueryException {
    throw new QueryException(ErrorCode.ERR_ITEM_HAS_NO_TYPED_VALUE,
                             "The atomized value of function items is undefined");
  }

  @Override
  public ItemType itemType() throws QueryException {
    return new FunctionType(signature);
  }

  @Override
  public boolean booleanValue() throws QueryException {
    throw new QueryException(ErrorCode.ERR_INVALID_ARGUMENT_TYPE,
                             "The effective boolean value of function items is undefined");
  }
}
