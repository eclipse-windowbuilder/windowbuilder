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
package org.eclipse.wb.internal.rcp.swing2swt.layout;

import org.eclipse.wb.core.editor.actions.assistant.AbstractAssistantPage;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.JavaInfoAddProperties;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.StringComboPropertyEditor;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.rcp.swing2swt.Messages;
import org.eclipse.wb.internal.swt.model.layout.LayoutAssistantSupport;
import org.eclipse.wb.internal.swt.model.layout.LayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

import org.eclipse.swt.widgets.Composite;

import java.util.List;

/**
 * Model for <code>BorderLayout</code>.
 *
 * @author scheglov_ke
 * @coverage rcp.swing2swt.model.layout
 */
public final class BorderLayoutInfo extends LayoutInfo {
	private final BorderLayoutInfo m_this = this;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public BorderLayoutInfo(AstEditor editor,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(editor, description, creationSupport);
		contributeProperty();
		supportLayoutAssistant();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Events
	//
	////////////////////////////////////////////////////////////////////////////
	private void supportLayoutAssistant() {
		new LayoutAssistantSupport(this) {
			@Override
			protected AbstractAssistantPage createLayoutPage(Composite parent) {
				return new BorderLayoutAssistantPage(parent, m_layout);
			}
		};
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link ControlInfo} at given region.
	 *
	 * @param region
	 *          is name of field for constraints, for example <code>WEST</code>.
	 */
	public ControlInfo getControl(String region) throws Exception {
		String fieldName = region.toLowerCase() + "Child";
		Object control = ReflectionUtils.getFieldObject(getObject(), fieldName);
		return (ControlInfo) getComposite().getChildByObject(control);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// LayoutData management
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void deleteLayoutData(ControlInfo control) throws Exception {
		super.deleteLayoutData(control);
		control.removeMethodInvocations("setLayoutData(java.lang.Object)");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Commands
	//
	////////////////////////////////////////////////////////////////////////////
	public void command_CREATE(ControlInfo control, String region) throws Exception {
		super.command_CREATE(control, null);
		command_REGION(control, region);
	}

	public void command_MOVE(ControlInfo control, String region) throws Exception {
		if (control.getParent() != getComposite()) {
			super.command_MOVE(control, null);
		}
		command_REGION(control, region);
	}

	private void command_REGION(ControlInfo control, String region) throws Exception {
		String layoutDataSource = "swing2swt.layout.BorderLayout." + region;
		control.removeMethodInvocations("setLayoutData(java.lang.Object)");
		control.addMethodInvocation("setLayoutData(java.lang.Object)", layoutDataSource);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// "Region" property
	//
	////////////////////////////////////////////////////////////////////////////
	private static final String[] REGION_TITLES = {
			Messages.BorderLayout_north,
			Messages.BorderLayout_south,
			Messages.BorderLayout_west,
			Messages.BorderLayout_east,
			Messages.BorderLayout_center};
	private static final String[] REGION_FIELDS = {"NORTH", "SOUTH", "WEST", "EAST", "CENTER"};
	private static final PropertyEditor m_regionPropertyEditor =
			new StringComboPropertyEditor(REGION_TITLES);

	/**
	 * {@link Property} for modifying region of {@link ControlInfo} on this {@link BorderLayoutInfo}.
	 */
	private final class RegionProperty extends Property {
		private final ControlInfo m_control;

		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		public RegionProperty(ControlInfo control) {
			super(m_regionPropertyEditor);
			m_control = control;
			setCategory(PropertyCategory.system(6));
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Property
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		public String getTitle() {
			return "Region";
		}

		@Override
		public Object getValue() throws Exception {
			for (int i = 0; i < REGION_FIELDS.length; i++) {
				String region = REGION_FIELDS[i];
				if (getControl(region) == m_control) {
					return REGION_TITLES[i];
				}
			}
			return "";
		}

		@Override
		public boolean isModified() throws Exception {
			return true;
		}

		@Override
		public void setValue(final Object value) throws Exception {
			ExecutionUtils.run(m_control, new RunnableEx() {
				@Override
				public void run() throws Exception {
					for (int i = 0; i < REGION_TITLES.length; i++) {
						String title = REGION_TITLES[i];
						if (title.equals(value)) {
							command_REGION(m_control, REGION_FIELDS[i]);
						}
					}
				}
			});
		}
	}

	private void contributeProperty() {
		addBroadcastListener(new JavaInfoAddProperties() {
			@Override
			public void invoke(JavaInfo javaInfo, List<Property> properties) throws Exception {
				if (javaInfo instanceof ControlInfo control && isActiveOnComposite(javaInfo.getParent())) {
					Property constraintsProperty = (Property) control.getArbitraryValue(m_this);
					if (constraintsProperty == null) {
						constraintsProperty = new RegionProperty(control);
						control.putArbitraryValue(m_this, constraintsProperty);
					}
					properties.add(constraintsProperty);
				}
			}
		});
	}
}
