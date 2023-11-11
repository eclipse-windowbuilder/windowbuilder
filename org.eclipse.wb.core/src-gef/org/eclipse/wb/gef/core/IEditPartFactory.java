/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
	default org.eclipse.gef.EditPart createEditPart(org.eclipse.gef.EditPart context, Object model) {
		return createEditPart((EditPart) context, model);
	}
	/**
	 * Creates a new {@link EditPart} given the specified <i>context</i> and <i>model</i>.
	 */
	EditPart createEditPart(EditPart context, Object model);
}