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
package org.eclipse.wb.internal.core.model.creation;

import org.eclipse.wb.core.model.JavaInfo;

/**
 * Interface for "wrapper" {@link CreationSupport}.
 *
 * @author scheglov_ke
 * @coverage core.model.creation
 */
public interface IWrapperControlCreationSupport {
  /**
   * @return the {@link JavaInfo} that wraps this {@link JavaInfo}.
   */
  JavaInfo getWrapperInfo();
}
