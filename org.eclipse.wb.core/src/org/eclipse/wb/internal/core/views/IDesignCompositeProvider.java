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
package org.eclipse.wb.internal.core.views;

import org.eclipse.wb.internal.core.editor.DesignComposite;

/**
 * GUI editors implement this interface to provide inner {@link DesignComposite}.
 *
 * @author scheglov_ke
 * @coverage core.views
 */
public interface IDesignCompositeProvider {
  DesignComposite getDesignComposite();
}
