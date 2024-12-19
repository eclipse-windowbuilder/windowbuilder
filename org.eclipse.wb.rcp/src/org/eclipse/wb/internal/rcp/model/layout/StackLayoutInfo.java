/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.rcp.model.layout;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectInfoChildGraphical;
import org.eclipse.wb.internal.core.model.clipboard.ClipboardCommand;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.util.StackContainerSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.swt.model.layout.LayoutClipboardCommand;
import org.eclipse.wb.internal.swt.model.layout.LayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

import org.eclipse.swt.custom.StackLayout;

import java.util.List;

/**
 * Model for {@link StackLayout}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.layout
 */
public final class StackLayoutInfo extends LayoutInfo implements IStackLayoutInfo<ControlInfo> {
	private final StackContainerSupport<ControlInfo> m_stackContainer =
			new StackContainerSupport<>(this) {
		@Override
		protected boolean isActive() {
			return StackLayoutInfo.this.isActive();
		}

		@Override
		protected ObjectInfo getContainer() {
			return getComposite();
		}

		@Override
		protected List<ControlInfo> getChildren() {
			return getControls();
		}
	};

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public StackLayoutInfo(AstEditor editor,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(editor, description, creationSupport);
		new StackLayoutAssistant(this);
		showOnlyTopControl_graphicalChild();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Broadcasts
	//
	////////////////////////////////////////////////////////////////////////////
	private void showOnlyTopControl_graphicalChild() {
		addBroadcastListener(new ObjectInfoChildGraphical() {
			@Override
			public void invoke(ObjectInfo object, boolean[] visible) throws Exception {
				if (isManagedObject(object)) {
					ControlInfo control = (ControlInfo) object;
					if (control != getActiveControl()) {
						visible[0] = false;
					}
				}
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Refresh
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void refresh_afterCreate() throws Exception {
		super.refresh_afterCreate();
		{
			ControlInfo topControl = getActiveControl();
			if (topControl != null) {
				getLayout().topControl = topControl.getWidget();
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public ControlInfo getActiveControl() {
		return m_stackContainer.getActive();
	}

	@Override
	public ControlInfo getPrevControl() {
		return m_stackContainer.getPrev();
	}

	@Override
	public ControlInfo getNextControl() {
		return m_stackContainer.getNext();
	}

	@Override
	public void show(ControlInfo control) {
		m_stackContainer.setActive(control);
	}

	@Override
	public StackLayout getLayout() {
		return (StackLayout) super.getObject();
	}

	//////////////////////////////////////////////////////////////////////////
	//
	// Clipboard
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void clipboardCopy_addControlCommands(ControlInfo control,
			List<ClipboardCommand> commands) throws Exception {
		commands.add(new LayoutClipboardCommand<StackLayoutInfo>(control) {
			private static final long serialVersionUID = 0L;

			@Override
			protected void add(StackLayoutInfo layout, ControlInfo control) throws Exception {
				layout.command_CREATE(control, null);
			}
		});
	}
}