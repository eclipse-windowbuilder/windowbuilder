/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc.
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
package org.eclipse.wb.internal.core.model.property.event;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.TextDisplayPropertyEditor;
import org.eclipse.wb.internal.core.model.property.table.PropertyTable;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;

import org.eclipse.draw2d.geometry.Point;

/**
 * Implementation of {@link PropertyEditor} for {@link ListenerMethodProperty}.
 *
 * @author scheglov_ke
 * @coverage core.model.property.events
 */
final class ListenerMethodPropertyEditor extends TextDisplayPropertyEditor {
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance
	//
	////////////////////////////////////////////////////////////////////////////
	public static final PropertyEditor INSTANCE = new ListenerMethodPropertyEditor();

	private ListenerMethodPropertyEditor() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// TextDisplayPropertyEditor
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected String getText(Property property) throws Exception {
		ListenerMethodProperty methodProperty = (ListenerMethodProperty) property;
		return methodProperty.getStartPosition().map(startPosition -> {
			JavaInfo javaInfo = methodProperty.getJavaInfo();
			int line = javaInfo.getEditor().getLineNumber(startPosition);
			return "line " + line;
		}).orElse(null);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// PropertyEditor
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void doubleClick(Property property, Point location) throws Exception {
		openStubMethod(property);
	}

	@Override
	public boolean activate(PropertyTable propertyTable, Property property, Point location)
			throws Exception {
		if (location == null) {
			openStubMethod(property);
		}
		return false;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Opens stub method in "Source" page.
	 */
	private void openStubMethod(Property property) throws Exception {
		final ListenerMethodProperty methodProperty = (ListenerMethodProperty) property;
		ExecutionUtils.run(methodProperty.getJavaInfo(), new RunnableEx() {
			@Override
			public void run() throws Exception {
				methodProperty.openStubMethod();
			}
		});
	}
}
