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
package org.eclipse.wb.internal.xwt.model.layout;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.description.ToolkitDescription;
import org.eclipse.wb.internal.core.model.layout.absolute.BoundsProperty;
import org.eclipse.wb.internal.core.model.layout.absolute.IPreferenceConstants;
import org.eclipse.wb.internal.core.model.layout.absolute.OrderingSupport;
import org.eclipse.wb.internal.core.model.property.ComplexProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.util.AbsoluteLayoutCreationFlowSupport;
import org.eclipse.wb.internal.core.model.util.ObjectInfoAction;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.xml.editor.DesignContextMenuProvider;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.broadcast.GenericPropertySetExpression;
import org.eclipse.wb.internal.core.xml.model.broadcast.XmlObjectAddProperties;
import org.eclipse.wb.internal.core.xml.model.clipboard.ClipboardCommand;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.xml.model.description.GenericPropertyDescription;
import org.eclipse.wb.internal.core.xml.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.rcp.RcpToolkitDescription;
import org.eclipse.wb.internal.swt.Activator;
import org.eclipse.wb.internal.swt.model.layout.absolute.IAbsoluteLayoutInfo;
import org.eclipse.wb.internal.xwt.model.widgets.ControlInfo;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.preference.IPreferenceStore;

import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * Model for "null" (absolute) layout.
 *
 * @author scheglov_ke
 * @coverage XWT.model.layout
 */
public final class AbsoluteLayoutInfo extends LayoutInfo
implements
IAbsoluteLayoutInfo<ControlInfo> {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public AbsoluteLayoutInfo(EditorContext context, CreationSupport creationSupport)
			throws Exception {
		super(context, new ComponentDescription(null), creationSupport);
		getDescription().setToolkit(RcpToolkitDescription.INSTANCE);
		getDescription().setIcon(Activator.getImageDescriptor("info/layout/absolute/layout.gif"));
		addSupport_autoSize();
		addSupport_propertyBounds();
		addSupport_contextMenu();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected List<Property> getPropertyList() throws Exception {
		return Collections.emptyList();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Broadcasts
	//
	////////////////////////////////////////////////////////////////////////////
	private void addSupport_autoSize() {
		addBroadcastListener(new GenericPropertySetExpression() {
			public void invoke(GenericPropertyImpl property,
					String[] expression,
					Object[] value,
					boolean[] shouldSet) throws Exception {
				checkForAutoSize(property);
			}

			private void checkForAutoSize(GenericPropertyImpl property) throws Exception {
				IPreferenceStore preferences = getDescription().getToolkit().getPreferences();
				if (preferences.getBoolean(IPreferenceConstants.P_AUTOSIZE_ON_PROPERTY_CHANGE)
						&& isManagedObject(property.getObject())) {
					GenericPropertyDescription propertyDescription = property.getDescription();
					if (propertyDescription != null) {
						boolean isTextProperty = propertyDescription.hasTrueTag("isText");
						boolean isImageProperty = propertyDescription.hasTrueTag("isImage");
						if (isTextProperty || isImageProperty) {
							// schedule auto-size
							final ControlInfo control = getControl(property.getObject());
							ExecutionUtils.runLater(control, new RunnableEx() {
								public void run() throws Exception {
									commandChangeBounds(control, null, control.getPreferredSize());
								}
							});
						}
					}
				}
			}
		});
	}

	private void addSupport_propertyBounds() {
		addBroadcastListener(new XmlObjectAddProperties() {
			public void invoke(XmlObjectInfo object, List<Property> properties) throws Exception {
				if (isManagedObject(object)) {
					ControlInfo control = getControl(object);
					properties.add(getBoundsProperty(control));
				}
			}
		});
	}

	private void addSupport_contextMenu() {
		addBroadcastListener(new ObjectEventListener() {
			@Override
			public void addContextMenu(List<? extends ObjectInfo> objects,
					ObjectInfo object,
					IMenuManager manager) throws Exception {
				if (isManagedObject(object)) {
					ControlInfo control = getControl(object);
					contributeControlContextMenu(manager, control);
				}
			}
		});
	}

	/**
	 * Contributes {@link Action}'s into {@link ControlInfo} context menu.
	 */
	private void contributeControlContextMenu(IMenuManager manager, final ControlInfo control) {
		// order
		{
			List<ControlInfo> controls = getComposite().getChildrenControls();
			new OrderingSupport(controls, control).contributeActions(manager);
		}
		// auto-size
		{
			IAction action =
					new ObjectInfoAction(control, "Autosize control",
							DesignerPlugin.getImageDescriptor("info/layout/absolute/fit_to_size.png")) {
				@Override
				protected void runEx() throws Exception {
					commandChangeBounds(control, null, control.getPreferredSize());
				}
			};
			manager.appendToGroup(DesignContextMenuProvider.GROUP_CONSTRAINTS, action);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Conversion
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void onSet() throws Exception {
		for (ControlInfo control : getComposite().getChildrenControls()) {
			Rectangle bounds = control.getModelBounds();
			String boundsString = bounds.x + ", " + bounds.y + ", " + bounds.width + ", " + bounds.height;
			control.setAttribute("bounds", boundsString);
		}
	}

	@Override
	protected void deleteLayoutData(ControlInfo control) throws Exception {
		super.deleteLayoutData(control);
		control.removeAttribute("location");
		control.removeAttribute("size");
		control.removeAttribute("bounds");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link AbsoluteLayoutInfo} model to use for explicit "null" layout.
	 */
	public static AbsoluteLayoutInfo createExplicitModel(EditorContext context) throws Exception {
		CreationSupport creationSupport = new AbsoluteLayoutCreationSupport();
		return new AbsoluteLayoutInfo(context, creationSupport);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Bounds Property
	//
	////////////////////////////////////////////////////////////////////////////
	private Property getBoundsProperty(ControlInfo control) {
		ComplexProperty boundsProperty = (ComplexProperty) control.getArbitraryValue(this);
		if (boundsProperty == null) {
			boundsProperty = new ComplexProperty("Bounds", null);
			boundsProperty.setCategory(PropertyCategory.system(5));
			boundsProperty.setModified(true);
			control.putArbitraryValue(this, boundsProperty);
			// x
			BoundsProperty<?> xProperty = new BoundsProperty<ControlInfo>(control, "x") {
				@Override
				public void setValue2(int value, Rectangle modelBounds) throws Exception {
					commandChangeBounds(m_component, new Point(value, modelBounds.y), null);
				}
			};
			// y
			BoundsProperty<?> yProperty = new BoundsProperty<ControlInfo>(control, "y") {
				@Override
				public void setValue2(int value, Rectangle modelBounds) throws Exception {
					commandChangeBounds(m_component, new Point(modelBounds.x, value), null);
				}
			};
			// width
			BoundsProperty<?> widthProperty = new BoundsProperty<ControlInfo>(control, "width") {
				@Override
				public void setValue2(int value, Rectangle modelBounds) throws Exception {
					commandChangeBounds(m_component, null, new Dimension(value, modelBounds.height));
				}
			};
			// height
			BoundsProperty<?> heightProperty = new BoundsProperty<ControlInfo>(control, "height") {
				@Override
				public void setValue2(int value, Rectangle modelBounds) throws Exception {
					commandChangeBounds(m_component, null, new Dimension(modelBounds.width, value));
				}
			};
			boundsProperty.setProperties(new Property[]{
					xProperty,
					yProperty,
					widthProperty,
					heightProperty});
		}
		Rectangle modelBounds = control.getModelBounds();
		if (modelBounds != null) {
			boundsProperty.setText("("
					+ modelBounds.x
					+ ", "
					+ modelBounds.y
					+ ", "
					+ modelBounds.width
					+ ", "
					+ modelBounds.height
					+ ")");
		}
		return boundsProperty;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Commands
	//
	////////////////////////////////////////////////////////////////////////////
	public void commandCreate(ControlInfo control, ControlInfo nextControl) throws Exception {
		command_CREATE(control, nextControl);
	}

	public void commandMove(ControlInfo control, ControlInfo nextControl) throws Exception {
		command_MOVE(control, nextControl);
	}

	public void commandChangeBounds(ControlInfo control, Point location, Dimension size)
			throws Exception {
		commandChangeBounds0(control, location, size);
		// apply creation flow
		if (location != null && useCreationFlow()) {
			AbsoluteLayoutCreationFlowSupport.apply(
					getComposite(),
					getControls(),
					control,
					location,
					size);
		}
	}

	private void commandChangeBounds0(ControlInfo control, Point location, Dimension size)
			throws Exception {
		Assert.isLegal(location != null || size != null, "Either location or size may not be null.");
		// try to find "location" and "size"
		if (location == null && hasAttribute(control, "location")) {
			location = control.getModelBounds().getLocation();
		}
		if (size == null && hasAttribute(control, "size")) {
			size = control.getModelBounds().getSize();
		}
		// "bounds"
		if (hasAttribute(control, "bounds")) {
			if (location != null) {
				setExpression(control, "bounds", 0, location.x);
				setExpression(control, "bounds", 1, location.y);
			}
			if (size != null) {
				setExpression(control, "bounds", 2, size.width);
				setExpression(control, "bounds", 3, size.height);
			}
			control.removeAttribute("location");
			control.removeAttribute("size");
			return;
		}
		// no existing attributes
		DocumentElement element = control.getCreationSupport().getElement();
		if (location != null && size != null) {
			String attributeString =
					location.x + ", " + location.y + ", " + size.width + ", " + size.height;
			element.setAttribute("bounds", attributeString);
			control.removeAttribute("location");
			control.removeAttribute("size");
		} else if (location != null) {
			String attributeString = location.x + ", " + location.y;
			element.setAttribute("location", attributeString);
		} else if (size != null) {
			String attributeString = size.width + ", " + size.height;
			element.setAttribute("size", attributeString);
		}
	}

	private static boolean hasAttribute(ControlInfo control, String attribute) {
		DocumentElement element = control.getCreationSupport().getElement();
		return element.getAttribute(attribute) != null;
	}

	/**
	 * Set value of "index" part of property.
	 */
	private static void setExpression(ControlInfo control, String attribute, int index, int value) {
		DocumentElement element = control.getCreationSupport().getElement();
		// prepare parts
		String attributeString = element.getAttribute(attribute);
		String[] attributeParts = StringUtils.split(attributeString, ", ");
		// set part
		attributeParts[index] = Integer.toString(value);
		// set attribute
		element.setAttribute(attribute, StringUtils.join(attributeParts, ", "));
	}

	private boolean useCreationFlow() {
		return getToolkit().getPreferences().getBoolean(IPreferenceConstants.P_CREATION_FLOW);
	}

	private ToolkitDescription getToolkit() {
		return getDescription().getToolkit();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Clipboard
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void clipboardCopy_addControlCommands(ControlInfo control,
			List<ClipboardCommand> commands) throws Exception {
		final Rectangle bounds = control.getModelBounds();
		commands.add(new LayoutClipboardCommand<AbsoluteLayoutInfo>(control) {
			private static final long serialVersionUID = 0L;

			@Override
			protected void add(AbsoluteLayoutInfo layout, ControlInfo control) throws Exception {
				layout.command_CREATE(control, null);
				layout.commandChangeBounds(control, bounds.getLocation(), bounds.getSize());
			}
		});
	}
}
