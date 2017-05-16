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
package org.eclipse.wb.core.model;

import org.eclipse.jdt.core.dom.ASTNode;

/**
 * Utility for any Java-based wrapper object with {@link IWrapperInfo}.
 *
 * @author sablin_aa
 * @coverage core.model
 */
public interface IWrapper {
  /**
   * @return wrapper {@link JavaInfo}.
   */
  JavaInfo getWrapperInfo();

  /**
   * @return wrapped {@link JavaInfo}.
   */
  JavaInfo getWrappedInfo() throws Exception;

  /**
   * @return <code>true</code> if given {@link ASTNode} represents {@link #getWrappedInfo()}.
   */
  boolean isWrappedInfo(ASTNode node);
}
