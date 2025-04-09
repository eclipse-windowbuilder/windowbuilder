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
package org.eclipse.wb.internal.rcp.nebula.collapsiblebuttons;

import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.factory.ImplicitFactoryCreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

import org.eclipse.swt.widgets.Shell;

/**
 * Model {@link CollapsibleButtons}
 *
 * @author sablin_aa
 * @coverage nebula.model
 */
public final class CollapsibleButtonsInfo extends CompositeInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public CollapsibleButtonsInfo(AstEditor editor,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(editor, description, creationSupport);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Refresh
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void refresh_afterCreate2() throws Exception {
		makeAddedButtonsVisible();
		super.refresh_afterCreate2();
	}

	/**
	 * {@link CollapsibleButtonsInfo} performs layout only when created {@link CustomButton} is
	 * visible. But Designer works usually on invisible {@link Shell}, so we have to make it visible
	 * to force layout.
	 */
	private void makeAddedButtonsVisible() throws Exception {
		// FIXME: remove this code after the author fixed the 'isVisible()' problem
		Shell shell = getWidget().getShell();
		shell.setVisible(true);
		shell.layout();
	}

	/**
	 * Adds new "button" on {@link CollapsibleButtons} widget.
	 *
	 * @return the added button {@link ControlInfo}.
	 */
	public static ControlInfo createButton(final CollapsibleButtonsInfo collapsibleButtons,
			ControlInfo nextButton) throws Exception {
		AstEditor editor = collapsibleButtons.getEditor();
		// prepare CreationSupport
		CreationSupport creationSupport;
		{
			String signature =
					"addButton(java.lang.String,java.lang.String,"
							+ "org.eclipse.swt.graphics.Image,"
							+ "org.eclipse.swt.graphics.Image)";
			String source = "addButton(\"New Button\", \"New CollapsibleButton\", null, null)";
			creationSupport = new ImplicitFactoryCreationSupport(signature, source);
		}
		// do add
		ControlInfo button =
				(ControlInfo) JavaInfoUtils.createJavaInfo(
						editor,
						"org.eclipse.nebula.widgets.collapsiblebuttons.CustomButton",
						creationSupport);
		JavaInfoUtils.add(button, null, collapsibleButtons, nextButton);
		return button;
	}

	/**
	 * Moves "button" on {@link CollapsibleButtons} widget.
	 */
	public static void moveButton(ControlInfo button, ControlInfo nextButton) throws Exception {
		JavaInfoUtils.move(button, null, button.getParentJava(), nextButton);
	}
}
