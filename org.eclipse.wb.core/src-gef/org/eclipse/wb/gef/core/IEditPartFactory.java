/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
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
package org.eclipse.wb.gef.core;

import org.eclipse.gef.EditPartViewer;

/**
 * A factory for creating new EditParts. {@link EditPartViewer} can be configured with an
 * <code>{@link IEditPartFactory}</code>. Whenever an <code>{@link EditPart}</code> in that viewer
 * needs to create another {@link EditPart}, it can use the Viewer's factory. The factory is also
 * used by the viewer whenever {@link EditPartViewer#setModel(Object)} is called.
 *
 * @author lobas_av
 * @coverage gef.core
 */
public interface IEditPartFactory extends org.eclipse.gef.EditPartFactory {
	/**
	 * Creates a new {@link EditPart} given the specified <i>context</i> and <i>model</i>.
	 */
	@Override
	EditPart createEditPart(org.eclipse.gef.EditPart context, Object model);
}