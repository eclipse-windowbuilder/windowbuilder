/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.swt.model.layout;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.association.EmptyAssociation;
import org.eclipse.wb.core.model.association.ImplicitObjectAssociation;
import org.eclipse.wb.core.model.broadcast.BroadcastSupport;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.core.model.broadcast.JavaInfoAddProperties;
import org.eclipse.wb.core.model.broadcast.ObjectInfoChildAddAfter;
import org.eclipse.wb.core.model.broadcast.ObjectInfoChildAddBefore;
import org.eclipse.wb.core.model.broadcast.ObjectInfoDelete;
import org.eclipse.wb.core.model.broadcast.ObjectInfoTreeComplete;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.clipboard.ClipboardCommand;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.IImplicitCreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.model.property.ComplexProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.property.editor.presentation.ButtonPropertyEditorPresentation;
import org.eclipse.wb.internal.core.model.property.table.PropertyTable;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.ui.UiUtils;
import org.eclipse.wb.internal.swt.IExceptionConstants;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;
import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;
import org.eclipse.wb.internal.swt.support.ContainerSupport;
import org.eclipse.wb.internal.swt.support.ControlSupport;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Menu;

import org.apache.commons.lang3.NotImplementedException;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract model for {@link Layout}.
 *
 * @author lobas_av
 * @coverage swt.model.layout
 */
public class LayoutInfo extends JavaInfo implements ILayoutInfo<ControlInfo> {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public LayoutInfo(AstEditor editor,
			ComponentDescription description,
			CreationSupport creationSupport) throws Exception {
		super(editor, description, creationSupport);
		addBroadcastListeners();
		new LayoutNameSupport(this);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Broadcast events
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Adds listeners to the {@link BroadcastSupport}.
	 */
	private void addBroadcastListeners() {
		addBroadcastListener(new ObjectInfoTreeComplete() {
			@Override
			public void invoke() throws Exception {
				CompositeInfo composite = getComposite();
				// validate LayoutData
				if (isActiveOnComposite(composite)) {
					validateCorrespondence_Layout_LayoutData();
				}
				// create virtual layout data
				if (isActiveOnComposite(composite)) {
					for (ControlInfo control : composite.getChildrenControls()) {
						ensureLayoutData(control);
					}
				}
			}
		});
		addBroadcastListener(new ObjectInfoChildAddBefore() {
			@Override
			public void invoke(ObjectInfo parent, ObjectInfo child, ObjectInfo[] nextChild)
					throws Exception {
				// add new LayoutData - remove existing one
				if (parent instanceof ControlInfo control
						&& child instanceof LayoutDataInfo
						&& isActiveOnComposite(parent.getParent())) {
					LayoutDataInfo existingLayoutData = getLayoutData(control);
					if (existingLayoutData != null) {
						control.removeChild(existingLayoutData);
					}
				}
			}
		});
		addBroadcastListener(new ObjectInfoChildAddAfter() {
			@Override
			public void invoke(ObjectInfo parent, ObjectInfo child) throws Exception {
				CompositeInfo composite = getComposite();
				// add this layout
				if (child == LayoutInfo.this) {
					// implicit layouts are bound to its parent
					if (getCreationSupport() instanceof IImplicitCreationSupport) {
						targetBroadcastListener(parent);
					}
					// create virtual LayoutData's
					for (ControlInfo control : composite.getChildrenControls()) {
						ensureLayoutData(control);
					}
				}
			}
		});
		addBroadcastListener(new ObjectInfoDelete() {
			@Override
			public void before(ObjectInfo parent, ObjectInfo child) throws Exception {
				// delete this layout
				if (child == LayoutInfo.this) {
					onDelete();
				}
				// delete ControlInfo from this composite
				if (isActiveOnComposite(parent) && child instanceof ControlInfo) {
					onControlRemoveBefore((ControlInfo) child);
				}
			}

			@Override
			public void after(ObjectInfo parent, ObjectInfo child) throws Exception {
				// delete layout data - create virtual
				if (parent instanceof ControlInfo control
						&& child instanceof LayoutDataInfo
						&& shouldCreateLayoutData((ControlInfo) parent)
						&& isActiveOnComposite(parent.getParent())) {
					ensureLayoutData(control);
				}
				// delete ControlInfo from this composite
				if (isActiveOnComposite(parent) && child instanceof ControlInfo control) {
					if (control.isDeleted()) {
						onControlRemoveAfter(control);
					}
				}
			}
		});
		addBroadcastListener(new JavaEventListener() {
			@Override
			public void addAfter(JavaInfo parent, JavaInfo child) throws Exception {
				// new ControlInfo added, ensure LayoutDataInfo
				if (isActiveOnComposite(parent) && child instanceof ControlInfo) {
					ensureLayoutData((ControlInfo) child);
				}
			}

			@Override
			public void moveBefore(JavaInfo child, ObjectInfo oldParent, JavaInfo newParent)
					throws Exception {
				// move ControlInfo FROM this composite
				if (isActiveOnComposite(oldParent)
						&& child instanceof ControlInfo control
						&& newParent != oldParent) {
					onControlRemoveBefore(control);
					deleteLayoutData(control);
				}
			}

			@Override
			public void moveAfter(JavaInfo child, ObjectInfo oldParent, JavaInfo newParent)
					throws Exception {
				// move ControlInfo FROM this composite
				if (isActiveOnComposite(oldParent)
						&& child instanceof ControlInfo
						&& newParent != oldParent) {
					onControlRemoveAfter((ControlInfo) child);
				}
				// move ControlInfo TO this composite
				if (isActiveOnComposite(newParent)
						&& child instanceof ControlInfo
						&& newParent != oldParent) {
					ensureLayoutData((ControlInfo) child);
				}
			}

			@Override
			public void clipboardCopy(JavaInfo javaInfo, List<ClipboardCommand> commands)
					throws Exception {
				if (isActiveOnComposite(javaInfo)) {
					clipboardCopy_addCompositeCommands(commands);
				}
			}
		});
		addBroadcastListener(new JavaInfoAddProperties() {
			@Override
			public void invoke(JavaInfo javaInfo, List<Property> properties) throws Exception {
				// add layout properties to composite properties
				if (isActiveOnComposite(javaInfo)) {
					addLayoutProperties(properties);
				}
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// LayoutData compatibility validation
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Ensures that {@link Control}'s have <code>LayoutData</code> that corresponds to {@link Layout}.
	 */
	private void validateCorrespondence_Layout_LayoutData() throws Exception {
		CompositeInfo composite = getComposite();
		Object[] controls = ContainerSupport.getChildren(composite.getObject());
		if (hasLayoutData()) {
			Class<?> expectedLayoutDataClass = getLayoutDataClass();
			for (Object control : controls) {
				Object layoutData = ControlSupport.getLayoutData(control);
				if (!ReflectionUtils.isAssignableFrom(expectedLayoutDataClass, layoutData)) {
					throw new DesignerException(IExceptionConstants.INCOMPATIBLE_LAYOUT_DATA,
							getShortClassName(layoutData),
							getShortComponentName(composite.getChildByObject(control)),
							getShortComponentName(composite),
							getShortClassName(getDescription().getComponentClass()),
							getShortClassName(expectedLayoutDataClass));
				}
			}
		}
	}

	private static String getShortClassName(Object o) {
		Class<?> clazz = o.getClass();
		return getShortClassName(clazz);
	}

	private static String getShortClassName(Class<?> clazz) {
		if (clazz == null) {
			return "<null>";
		}
		return CodeUtils.getShortClass(clazz.getName());
	}

	private static String getShortComponentName(JavaInfo javaInfo) {
		if (javaInfo == null) {
			return "<null>";
		}
		return javaInfo.getVariableSupport().getComponentName();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public final IObjectPresentation getPresentation() {
		return new LayoutPresentation(this);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link CompositeInfo} that contains this {@link LayoutInfo}.
	 */
	@Override
	public final CompositeInfo getComposite() {
		return (CompositeInfo) getParent();
	}

	@Override
	public final boolean isActive() {
		CompositeInfo composite = getComposite();
		return isActiveOnComposite(composite);
	}

	/**
	 * @return <code>true</code> if this {@link LayoutInfo} is active on its {@link CompositeInfo}.
	 */
	protected final boolean isActiveOnComposite(ObjectInfo composite) {
		return composite != null && composite.getChildren().contains(this);
	}

	@Override
	public final List<ControlInfo> getControls() {
		List<ControlInfo> controls = new ArrayList<>();
		CompositeInfo composite = getComposite();
		if (isActiveOnComposite(composite)) {
			for (ControlInfo control : composite.getChildrenControls()) {
				if (isManagedObject(control)) {
					controls.add(control);
				}
			}
		}
		return controls;
	}

	@Override
	public boolean isManagedObject(Object object) {
		if (object instanceof ControlInfo control
				&& isActive()
				&& getComposite().getChildren().contains(object)) {
			if (JavaInfoUtils.isIndirectlyExposed(control)) {
				return false;
			}
			return true;
		}
		return false;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// "Layout" property
	//
	////////////////////////////////////////////////////////////////////////////
	private ComplexProperty m_layoutComplexProperty;

	/**
	 * Adds properties of this {@link LayoutInfo} to the properties of its {@link CompositeInfo}.
	 */
	private void addLayoutProperties(List<Property> properties) throws Exception {
		// prepare layout complex property
		{
			Property[] layoutProperties = getProperties();
			if (m_layoutComplexProperty == null) {
				String text;
				{
					Class<?> componentClass = getDescription().getComponentClass();
					if (componentClass != null) {
						text = "(" + componentClass.getName() + ")";
					} else {
						text = "(absolute)";
					}
				}
				//
				m_layoutComplexProperty = new ComplexProperty("Layout", text) {
					@Override
					public boolean isModified() throws Exception {
						return true;
					}

					@Override
					public void setValue(Object value) throws Exception {
						if (value == UNKNOWN_VALUE) {
							delete();
						}
					}
				};
				m_layoutComplexProperty.setCategory(PropertyCategory.system(5));
				m_layoutComplexProperty.setEditorPresentation(new ButtonPropertyEditorPresentation() {
					@Override
					protected Image getImage() {
						return DesignerPlugin.getImage("properties/down.png");
					}

					@Override
					protected void onClick(PropertyTable propertyTable, Property property) throws Exception {
						MenuManager manager = new MenuManager();
						getComposite().fillLayoutsManager(manager);
						Menu menu = manager.createContextMenu(propertyTable.getControl());
						UiUtils.showAndDisposeOnHide(menu);
					}
				});
			}
			m_layoutComplexProperty.setProperties(layoutProperties);
		}
		// add property
		properties.add(m_layoutComplexProperty);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Layout notifications
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * This method is invoked when this {@link LayoutInfo} is set on its {@link CompositeInfo}.
	 */
	public void onSet() throws Exception {
	}

	/**
	 * This method is invoked when this {@link LayoutInfo} is deleted from its {@link CompositeInfo}.
	 */
	protected void onDelete() throws Exception {
		for (ControlInfo control : getComposite().getChildrenControls()) {
			deleteLayoutData(control);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Control notifications
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Notification that given {@link ControlInfo} will be removed from composite.
	 */
	protected void onControlRemoveBefore(ControlInfo control) throws Exception {
	}

	/**
	 * Notification that given {@link ControlInfo} was removed from composite.
	 */
	protected void onControlRemoveAfter(ControlInfo control) throws Exception {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// LayoutData management
	//
	////////////////////////////////////////////////////////////////////////////
	private static String KEY_EXPOSED_HAS_DATA = "exposed control: already has LayoutData";
	private static String KEY_EXPOSED_DATA = "exposed control: its LayoutData";
	private static String KEY_DONT_CREATE_VIRTUAL_DATA =
			"don't create virtual LayoutData for this ControlInfo";

	/**
	 * We may be {@link ControlInfo} that virtual {@link LayoutDataInfo} should not be created for it,
	 * when we intentionally delete {@link LayoutDataInfo}, for example during process of moving this
	 * {@link ControlInfo} from this {@link LayoutInfo} or deleting this {@link LayoutInfo}.
	 *
	 * @return <code>true</code> if for given {@link ControlInfo} we should create
	 *         {@link LayoutDataInfo}.
	 */
	private boolean shouldCreateLayoutData(ControlInfo control) {
		return control.getArbitraryValue(KEY_DONT_CREATE_VIRTUAL_DATA) == null;
	}

	/**
	 * Delete {@link LayoutDataInfo} associated with given {@link ControlInfo}.
	 * <p>
	 * Note that this is different than {@link LayoutDataInfo#delete()} because we don't remove
	 * implicit/virtual {@link LayoutDataInfo} from list of children in
	 * {@link CreationSupport#delete()}. {@link CreationSupport#delete()} has to remove only
	 * {@link ASTNode}'s related with {@link LayoutDataInfo}. So, we need separate operation to remove
	 * {@link LayoutDataInfo} from list of children.
	 */
	protected void deleteLayoutData(ControlInfo control) throws Exception {
		control.putArbitraryValue(KEY_DONT_CREATE_VIRTUAL_DATA, Boolean.TRUE);
		try {
			LayoutDataInfo layoutData = getLayoutData(control);
			if (layoutData != null) {
				storeLayoutData(control, layoutData);
				layoutData.delete();
				// if implicit/virtual, so still alive, force remove from children
				if (!layoutData.isDeleted()) {
					control.removeChild(layoutData);
				}
			}
		} finally {
			control.removeArbitraryValue(KEY_DONT_CREATE_VIRTUAL_DATA);
		}
	}

	/**
	 * @return {@link LayoutDataInfo} associated with given {@link ControlInfo}, or <code>null</code>
	 *         if no {@link LayoutDataInfo} expected for parent {@link LayoutInfo}.
	 */
	public static LayoutDataInfo getLayoutData(ControlInfo control) {
		// select only layout data
		List<LayoutDataInfo> objects = new ArrayList<>();
		for (ObjectInfo object : control.getChildren()) {
			if (object instanceof LayoutDataInfo) {
				objects.add((LayoutDataInfo) object);
			}
		}
		// check for no layout data
		if (objects.isEmpty()) {
			return null;
		}
		// only one layout data can be set
		Assert.isTrue(objects.size() == 1);
		return objects.get(0);
	}

	@Override
	public ILayoutDataInfo getLayoutData2(IControlInfo control) {
		return getLayoutData((ControlInfo) control);
	}

	/**
	 * Ensure that if {@link LayoutDataInfo} should exist for given component, there is "real"
	 * {@link LayoutDataInfo}, or create "virtual"/"implicit" {@link LayoutDataInfo}.
	 */
	private void ensureLayoutData(ControlInfo control) throws Exception {
		if (JavaInfoUtils.isIndirectlyExposed(control)) {
			return;
		}
		if (hasLayoutData()) {
			LayoutDataInfo layoutData = getLayoutData(control);
			if (layoutData != null) {
				return;
			}
			// create layout data
			if (control.getCreationSupport() instanceof IImplicitCreationSupport) {
				// prepare layout data, ask only first time, during initialize()
				Object layoutDataObject;
				if (control.getArbitraryValue(KEY_EXPOSED_HAS_DATA) != null) {
					layoutDataObject = control.getArbitraryValue(KEY_EXPOSED_DATA);
				} else {
					layoutDataObject = ControlSupport.getLayoutData(control.getObject());
					control.putArbitraryValue(KEY_EXPOSED_HAS_DATA, Boolean.TRUE);
					control.putArbitraryValue(KEY_EXPOSED_DATA, layoutDataObject);
				}
				// if no layout data, then virtual
				if (layoutDataObject == null) {
					createVirtualLayoutData(control);
				} else {
					createImplicitLayoutData(control);
				}
			} else {
				createVirtualLayoutData(control);
			}
		}
	}

	/**
	 * Creates implicit {@link LayoutDataInfo} for given {@link ControlInfo}.
	 * <p>
	 * "Implicit" {@link LayoutDataInfo} is wrapper for "layout data" object that exists for
	 * "implicit" {@link ControlInfo}, for example when exposed {@link Control} has already
	 * "layout data" set during creation of exposing container.
	 */
	private void createImplicitLayoutData(ControlInfo control) throws Exception {
		// create model
		JavaInfo layoutData;
		{
			CreationSupport creationSupport = new ImplicitLayoutDataCreationSupport(control);
			layoutData = JavaInfoUtils.createJavaInfo(getEditor(), getLayoutDataClass(), creationSupport);
		}
		// configure
		layoutData.setVariableSupport(new ImplicitLayoutDataVariableSupport(layoutData));
		layoutData.setAssociation(new ImplicitObjectAssociation(control));
		// add to control
		control.addChild(layoutData);
	}

	/**
	 * Creates virtual {@link LayoutDataInfo} for given {@link ControlInfo}.
	 * <p>
	 * "Virtual" {@link LayoutDataInfo} is placeholder for "layout data" when "layout data" should
	 * exist, but does not exist yet in source code. Most layout managers in this case use
	 * "layout data" with some default values. So, we show these values in properties and allow to
	 * change them, at this moment we "materialize" {@link LayoutDataInfo} in source code.
	 */
	private void createVirtualLayoutData(ControlInfo control) throws Exception {
		Object dataObject = getDefaultVirtualDataObject();
		// create model
		JavaInfo layoutData;
		{
			CreationSupport creationSupport = new VirtualLayoutDataCreationSupport(control, dataObject);
			layoutData = JavaInfoUtils.createJavaInfo(getEditor(), getLayoutDataClass(), creationSupport);
		}
		// configure
		layoutData.setVariableSupport(new VirtualLayoutDataVariableSupport(layoutData));
		layoutData.setAssociation(new EmptyAssociation());
		// add to control
		control.addChild(layoutData);
	}

	/**
	 * @return default object used for "virtual" {@link LayoutDataInfo}.
	 */
	protected Object getDefaultVirtualDataObject() throws Exception {
		throw new NotImplementedException(getClass().getName());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Virtual Layout Data Support
	//
	////////////////////////////////////////////////////////////////////////////
	private static final String KEY_LAYOUT_DATA_HAS = "layout-data.has";
	private static final String KEY_LAYOUT_DATA_CLASS = "layout-data.class";
	private Class<?> m_layoutDataClass;

	/**
	 * @return <code>true</code> if this layout has layout data, for user.
	 */
	private boolean hasLayoutData() {
		return JavaInfoUtils.hasTrueParameter(this, KEY_LAYOUT_DATA_HAS);
	}

	/**
	 * @return {@link Class} of layout data objects.
	 */
	private Class<?> getLayoutDataClass() throws Exception {
		if (m_layoutDataClass == null) {
			// extract class name
			String layoutDataClassName = JavaInfoUtils.getParameter(this, KEY_LAYOUT_DATA_CLASS);
			Assert.isNotNull(layoutDataClassName);
			Assert.isTrue(layoutDataClassName.length() != 0);
			// load class
			m_layoutDataClass = JavaInfoUtils.getClassLoader(this).loadClass(layoutDataClassName);
		}
		return m_layoutDataClass;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Add {@link ControlInfo} to host {@link CompositeInfo}.
	 */
	public final void command_CREATE(ControlInfo control, ControlInfo nextControl) throws Exception {
		startEdit();
		try {
			JavaInfoUtils.add(control, null, getComposite(), nextControl);
		} finally {
			endEdit();
		}
	}

	/**
	 * Moves {@link ControlInfo} to host {@link CompositeInfo}.
	 */
	public final void command_MOVE(ControlInfo control, ControlInfo nextControl) throws Exception {
		startEdit();
		try {
			JavaInfoUtils.move(control, null, getComposite(), nextControl);
		} finally {
			endEdit();
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Clipboard
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Adds commands for coping parent {@link CompositeInfo}.
	 */
	protected void clipboardCopy_addCompositeCommands(List<ClipboardCommand> commands)
			throws Exception {
		for (ControlInfo control : getComposite().getChildrenControls()) {
			if (!JavaInfoUtils.isImplicitlyCreated(control)) {
				clipboardCopy_addControlCommands(control, commands);
			}
		}
	}

	/**
	 * Adds commands for coping {@link ControlInfo} on parent {@link CompositeInfo}.
	 */
	protected void clipboardCopy_addControlCommands(ControlInfo control,
			List<ClipboardCommand> commands) throws Exception {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Manage general layout data.
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Store general layout data properties for {@link ControlInfo}.
	 */
	protected void storeLayoutData(ControlInfo control, LayoutDataInfo layoutData) throws Exception {
	}
}