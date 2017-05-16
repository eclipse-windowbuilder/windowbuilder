/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.nls.bundle.pure;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.nls.bundle.AbstractBundleSource;

import org.eclipse.jdt.core.dom.Expression;

/**
 * Abstract source that uses only ResourceBundle.
 *
 * @author scheglov_ke
 * @coverage core.nls
 */
public abstract class AbstractPureBundleSource extends AbstractBundleSource {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractPureBundleSource(JavaInfo root, String bundleName) throws Exception {
    super(root, bundleName);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Value access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final String getValue(Expression expression) throws Exception {
    // prepare key
    BasicExpressionInfo expressionInfo = getBasicExpressionInfo(expression);
    String key = expressionInfo.m_key;
    // return value from bundle
    return getValue(key);
  }

  @Override
  public final void setValue(Expression expression, String value) throws Exception {
    // prepare key
    BasicExpressionInfo expressionInfo = getBasicExpressionInfo(expression);
    String key = expressionInfo.m_key;
    // change value in bundle
    setValueInBundle(key, value);
  }
}
