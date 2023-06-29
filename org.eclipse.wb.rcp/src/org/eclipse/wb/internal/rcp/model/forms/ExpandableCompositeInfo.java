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
package org.eclipse.wb.internal.rcp.model.forms;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.rcp.model.widgets.AbstractPositionCompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

import org.eclipse.ui.forms.widgets.ExpandableComposite;

/**
 * Model for {@link ExpandableComposite}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.forms
 */
public class ExpandableCompositeInfo extends AbstractPositionCompositeInfo {
	private static final String[] POSITIONS = new String[]{"setTextClient", "setClient"};

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ExpandableCompositeInfo(AstEditor editor,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		this(editor, description, creationSupport, POSITIONS);
	}

	protected ExpandableCompositeInfo(AstEditor editor,
			ComponentDescription description,
			CreationSupport creationSupport,
			String[] methods) throws Exception {
		super(editor, description, creationSupport, methods);
		// when ControlInfo added using setClient(), set "expanded" to true
		addBroadcastListener(new JavaEventListener() {
			@Override
			public void addAfter(JavaInfo parent, JavaInfo child) throws Exception {
				if (child instanceof ControlInfo
						&& parent == ExpandableCompositeInfo.this
						&& getControl("setClient") == child) {
					getPropertyByTitle("expanded").setValue(true);
				}
			}

			@Override
			public void moveAfter(JavaInfo child, ObjectInfo oldParent, JavaInfo newParent)
					throws Exception {
				if (child instanceof ControlInfo
						&& newParent == ExpandableCompositeInfo.this
						&& getControl("setClient") == child) {
					getPropertyByTitle("expanded").setValue(true);
				}
			}
		});
	}
}
