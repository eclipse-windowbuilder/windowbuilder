/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
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
