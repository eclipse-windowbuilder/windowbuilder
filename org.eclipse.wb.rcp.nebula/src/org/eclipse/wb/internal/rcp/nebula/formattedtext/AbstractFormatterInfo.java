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
package org.eclipse.wb.internal.rcp.nebula.formattedtext;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.property.editor.complex.InstanceObjectPropertyEditor;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;

/**
 * Model {@link org.eclipse.nebula.widgets.formattedtext.AbstractFormatter}.
 *
 * @author sablin_aa
 * @coverage nebula.model
 */
public class AbstractFormatterInfo extends JavaInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public AbstractFormatterInfo(AstEditor editor,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(editor, description, creationSupport);
		// listener for setting property to default
		InstanceObjectPropertyEditor.installListenerForProperty(this);
	}
}
