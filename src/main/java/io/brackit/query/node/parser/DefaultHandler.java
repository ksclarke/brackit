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

import io.brackit.query.atomic.Atomic;
import io.brackit.query.atomic.QNm;
import io.brackit.query.jdm.DocumentException;

/**
 * @author Sebastian Baechle
 */
public class DefaultHandler implements NodeSubtreeHandler {
  @Override
  public void attribute(QNm name, Atomic value) throws DocumentException {
  }

  @Override
  public void begin() throws DocumentException {
  }

  @Override
  public void beginFragment() throws DocumentException {
  }

  @Override
  public void comment(Atomic content) throws DocumentException {
  }

  @Override
  public void end() throws DocumentException {
  }

  @Override
  public void endDocument() throws DocumentException {
  }

  @Override
  public void endElement(QNm name) throws DocumentException {
  }

  @Override
  public void endFragment() throws DocumentException {
  }

  @Override
  public void fail() throws DocumentException {
  }

  @Override
  public void processingInstruction(QNm target, Atomic content) throws DocumentException {
  }

  @Override
  public void startDocument() throws DocumentException {
  }

  @Override
  public void startElement(QNm name) throws DocumentException {
  }

  @Override
  public void text(Atomic content) throws DocumentException {
  }

  @Override
  public void endMapping(String prefix) throws DocumentException {
  }

  @Override
  public void startMapping(String prefix, String uri) throws DocumentException {
  }
}
