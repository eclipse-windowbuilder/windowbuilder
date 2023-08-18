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
package org.eclipse.wb.internal.xwt.model.layout;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectInfoChildGraphical;
import org.eclipse.wb.internal.core.model.util.StackContainerSupport;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.rcp.model.layout.IStackLayoutInfo;
import org.eclipse.wb.internal.rcp.model.layout.StackLayoutAssistant;
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;

import org.eclipse.swt.custom.StackLayout;

import java.util.List;

/**
 * Model for {@link StackLayout}.
 *
 * @author scheglov_ke
 * @coverage XWT.model.layout
 */
public final class StackLayoutInfo extends GenericFlowLayoutInfo
implements
IStackLayoutInfo<ControlInfo> {
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
	public StackLayoutInfo(EditorContext context,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(context, description, creationSupport);
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
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean isHorizontal() {
		return true;
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
				ReflectionUtils.setField(getObject(), "topControl", topControl.getObject());
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
}