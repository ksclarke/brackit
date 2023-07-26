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
package org.brackit.query.module;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.brackit.query.atomic.QNm;
import org.brackit.query.compiler.Bits;
import org.brackit.query.function.io.IOFun;
import org.brackit.query.function.json.JSONFun;

/**
 * @author Sebastian Baechle
 */
public class Namespaces {
  public static final String LOCAL_NSURI = "http://www.w3.org/2005/xquery-local-functions";

  public static final String FN_NSURI = "http://www.w3.org/2005/xpath-functions";

  public static final String DEFAULT_FN_NSURI = "http://jsoniq.org/default-function-namespace";

  public static final String FNMATH_NSURI = "http://www.w3.org/2005/xpath-functions/math";

  public static final String XSI_NSURI = "http://www.w3.org/2001/XMLSchema-instance";

  public static final String XS_NSURI = "http://www.w3.org/2001/XMLSchema";

  public static final String XML_NSURI = "http://www.w3.org/XML/1998/namespace";

  public static final String XMLNS_NSURI = "http://www.w3.org/2000/xmlns";

  public static final String ERR_NSURI = "http://www.w3.org/2005/xqt-errors";

  public static final String LOCAL_PREFIX = "local";

  public static final String FN_PREFIX = "fn";

  public static final String XSI_PREFIX = "xsi";

  public static final String XS_PREFIX = "xs";

  public static final String XML_PREFIX = "xml";

  public static final String XMLNS_PREFIX = "xmlns";

  public static final String ERR_PREFIX = "err";

  /* BEGIN try-catch */
  public static final QNm ERR_CODE = new QNm(Namespaces.ERR_NSURI, Namespaces.ERR_PREFIX, "code");
  public static final QNm ERR_DESCRIPTION = new QNm(Namespaces.ERR_NSURI, Namespaces.ERR_PREFIX, "description");
  public static final QNm ERR_VALUE = new QNm(Namespaces.ERR_NSURI, Namespaces.ERR_PREFIX, "value");
  public static final QNm ERR_MODULE = new QNm(Namespaces.ERR_NSURI, Namespaces.ERR_PREFIX, "module");
  public static final QNm ERR_LINE_NUMBER = new QNm(Namespaces.ERR_NSURI, Namespaces.ERR_PREFIX, "line-number");
  public static final QNm ERR_COLUMN_NUMBER = new QNm(Namespaces.ERR_NSURI, Namespaces.ERR_PREFIX, "column-number");
  /* END try-catch */

  protected static final Map<String, NamespaceDecl> predefined = new HashMap<String, NamespaceDecl>();

  protected final Map<String, NamespaceDecl> namespaces = new TreeMap<String, NamespaceDecl>();

  protected final Namespaces parent;

  protected String defaultFunctionNamespace = DEFAULT_FN_NSURI;

  protected String defaultElementNamespace = null;

  static {
    predefine(XML_PREFIX, XML_NSURI);
    predefine(XS_PREFIX, XS_NSURI);
    predefine(XSI_PREFIX, XSI_NSURI);
    predefine(FN_PREFIX, FN_NSURI);
    predefine(LOCAL_PREFIX, LOCAL_NSURI);
    predefine(ERR_PREFIX, ERR_NSURI);
    predefine(Bits.BIT_PREFIX, Bits.BIT_NSURI);
    predefine(IOFun.IO_PREFIX, IOFun.IO_NSURI);
    predefine(JSONFun.JSON_PREFIX, JSONFun.JSON_NSURI);
  }

  public Namespaces() {
    this.parent = null;
  }

  public Namespaces(Namespaces parent) {
    this.parent = parent;
    this.defaultElementNamespace = parent.defaultElementNamespace;
    this.defaultFunctionNamespace = parent.defaultFunctionNamespace;
  }

  public static void predefine(String prefix, String nsUri) {
    predefined.put(prefix, new NamespaceDecl(prefix, nsUri));
  }

  public String getDefaultFunctionNamespace() {
    return defaultFunctionNamespace;
  }

  public void setDefaultFunctionNamespace(String defaultFunctionNamespace) {
    this.defaultFunctionNamespace = defaultFunctionNamespace;
  }

  public String getDefaultElementNamespace() {
    return defaultElementNamespace;
  }

  public void setDefaultElementNamespace(String defaultElementNamespace) {
    this.defaultElementNamespace = defaultElementNamespace;
  }

  public void declare(String prefix, String nsURI) {
    namespaces.put(prefix, new NamespaceDecl(prefix, nsURI));
  }

  public String resolve(String prefix) {
    if (prefix == null || prefix.isEmpty()) {
      return null;
    }
    NamespaceDecl nsDecl = namespaces.get(prefix);

    if (nsDecl != null) {
      return nsDecl.getUri();
    }
    if (parent != null) {
      return parent.resolve(prefix);
    }
    nsDecl = predefined.get(prefix);
    if (nsDecl != null) {
      return nsDecl.getUri();
    }
    return null;
  }

  public boolean isPredefined(String prefix) {
    return predefined.containsKey(prefix);
  }
}
