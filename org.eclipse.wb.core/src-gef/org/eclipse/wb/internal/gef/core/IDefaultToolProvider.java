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
package org.eclipse.wb.internal.gef.core;

import org.eclipse.wb.gef.core.tools.Tool;

/**
 * Interface that allows external tweaking of {@link EditDomain} {@link Tool}'s operations.
 *
 * @author scheglov_ke
 * @coverage gef.core
 */
public interface IDefaultToolProvider {
  /**
   * Allow {@link IDefaultToolProvider} override default {@link Tool} loading.
   */
  void loadDefaultTool();
}
