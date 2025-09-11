/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *    Marcel du Preez - updated "restore default value" feature
 *******************************************************************************/
package org.eclipse.wb.internal.swing.model.layout;

import org.eclipse.wb.core.editor.constants.CoreImages;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.association.AssociationObject;
import org.eclipse.wb.core.model.association.AssociationObjects;
import org.eclipse.wb.core.model.association.InvocationChildAssociation;
import org.eclipse.wb.core.model.broadcast.BroadcastSupport;
import org.eclipse.wb.core.model.broadcast.JavaEventListener;
import org.eclipse.wb.core.model.broadcast.JavaInfoAddProperties;
import org.eclipse.wb.core.model.broadcast.ObjectInfoDelete;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.clipboard.ClipboardCommand;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.LayoutDescription;
import org.eclipse.wb.internal.core.model.description.helpers.LayoutDescriptionHelper;
import org.eclipse.wb.internal.core.model.layout.GeneralLayoutData;
import org.eclipse.wb.internal.core.model.layout.GeneralLayoutData.HorizontalAlignment;
import org.eclipse.wb.internal.core.model.layout.GeneralLayoutData.VerticalAlignment;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.model.property.ComplexProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.property.editor.presentation.ButtonPropertyEditorPresentation;
import org.eclipse.wb.internal.core.model.property.table.PropertyTable;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.core.utils.ui.UiUtils;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.component.menu.JPopupMenuInfo;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Menu;

import org.osgi.service.prefs.Preferences;

import java.awt.Component;
import java.awt.Container;
import java.awt.LayoutManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Abstract model for {@link LayoutManager}.
 *
 * @author scheglov_ke
 * @author Marcel du Preez - default layout
 * @coverage swing.model.layout
 */
public class LayoutInfo extends JavaInfo {
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
		addBroadcastListener(new ObjectInfoDelete() {
			@Override
			public void before(ObjectInfo parent, ObjectInfo child) throws Exception {
				// delete this layout
				if (child == LayoutInfo.this) {
					onDelete();
				}
				// delete child from this container
				if (isManagedObject(child)) {
					ComponentInfo component = (ComponentInfo) child;
					removeComponentConstraints(getContainer(), component);
				}
			}
		});
		addBroadcastListener(new JavaEventListener() {
			@Override
			public void moveBefore(JavaInfo child, ObjectInfo oldParent, JavaInfo newParent)
					throws Exception {
				// move FROM this layout
				if (isManagedObject(child) && newParent != oldParent) {
					ComponentInfo component = (ComponentInfo) child;
					removeComponentConstraints(getContainer(), component);
				}
			}

			@Override
			public void clipboardCopy(JavaInfo javaInfo, List<ClipboardCommand> commands)
					throws Exception {
				if (isActiveOnContainer(javaInfo)) {
					clipboardCopy_addContainerCommands(commands);
				}
			}
		});
		addBroadcastListener(new JavaInfoAddProperties() {
			@Override
			public void invoke(JavaInfo javaInfo, List<Property> properties) throws Exception {
				if (isActiveOnContainer(javaInfo)) {
					event_addLayoutProperties(properties);
				}
			}
		});
	}

	/**
	 * @return <code>true</code> if given {@link Object} is managed by this {@link LayoutInfo}.
	 */
	public boolean isManagedObject(Object object) {
		if (isManagedClass(object)) {
			ComponentInfo component = (ComponentInfo) object;
			if (JavaInfoUtils.isIndirectlyExposed(component)) {
				return false;
			}
			ObjectInfo container = component.getParent();
			return isActiveOnContainer(container);
		}
		return false;
	}

	/**
	 * @return <code>true</code> if given {@link Object} is {@link ComponentInfo}.
	 */
	private static boolean isManagedClass(Object object) {
		return object instanceof ComponentInfo && !(object instanceof JPopupMenuInfo);
	}

	/**
	 * @return <code>true</code> if this {@link LayoutInfo} is active on its {@link ContainerInfo}.
	 */
	protected final boolean isActiveOnContainer(ObjectInfo container) {
		return container != null && container.getChildren().contains(this);
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
	 * @return the {@link ContainerInfo} that contains this {@link LayoutInfo}.
	 */
	public final ContainerInfo getContainer() {
		return (ContainerInfo) getParent();
	}

	/**
	 * @return the {@link ComponentInfo} children of container.
	 */
	public final List<ComponentInfo> getComponents() {
		List<ComponentInfo> components = new ArrayList<>();
		for (ObjectInfo child : getContainer().getChildren()) {
			if (isManagedObject(child)) {
				ComponentInfo component = (ComponentInfo) child;
				components.add(component);
			}
		}
		return components;
	}

	/**
	 * @return <code>true</code> if this {@link LayoutInfo} is active on its {@link ContainerInfo}.
	 *         For example implicit {@link LayoutInfo}'s replaced by "real" {@link LayoutInfo} are
	 *         inactive.
	 */
	public final boolean isActive() {
		ContainerInfo container = getContainer();
		return isActiveOnContainer(container);
	}

	/**
	 * This method is invoked when this {@link LayoutInfo} is set on its {@link ContainerInfo}.
	 */
	public void onSet() throws Exception {
	}

	/**
	 * This method is invoked when this {@link LayoutInfo} is deleted from its {@link ContainerInfo}.
	 */
	protected void onDelete() throws Exception {
		ContainerInfo container = getContainer();
		for (ComponentInfo componentInfo : container.getChildrenComponents()) {
			storeLayoutData(componentInfo);
			removeComponentConstraints(container, componentInfo);
		}
	}

	/**
	 * May be overridden by subclasses to cast the layout manager to its explicit
	 * type.
	 *
	 * <b>Important</b> This method should <i>always</i> return a the same object as
	 * {@link #getObject()}.
	 *
	 * @return the {@link LayoutManager} created for this {@link LayoutInfo}.
	 */
	public LayoutManager getLayoutManager() {
		return (LayoutManager) getObject();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// "Layout" property
	//
	////////////////////////////////////////////////////////////////////////////
	private ComplexProperty m_layoutComplexProperty;

	/**
	 * Adds properties of this {@link LayoutInfo} to the properties of its {@link ContainerInfo}.
	 */
	private void event_addLayoutProperties(List<Property> properties) throws Exception {
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
							setDefaultLayout();
						}
					}
				};
				m_layoutComplexProperty.setCategory(PropertyCategory.system(5));
				if (getContainer().canSetLayout()) {
					m_layoutComplexProperty.setEditorPresentation(new ButtonPropertyEditorPresentation() {
						@Override
						protected Image getImage() {
							return CoreImages.getSharedImage(CoreImages.PROPERTIES_DOWN);
						}

						@Override
						protected void onClick(PropertyTable propertyTable, Property property)
								throws Exception {
							MenuManager manager = new MenuManager();
							getContainer().fillLayoutsManager(manager);
							Menu menu = manager.createContextMenu(propertyTable.getControl());
							UiUtils.showAndDisposeOnHide(menu);
						}
					});
				}
			}
			m_layoutComplexProperty.setProperties(layoutProperties);
		}
		// add property
		properties.add(m_layoutComplexProperty);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Components/constraints
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Removes constraints for given {@link ComponentInfo}.
	 * <p>
	 * By default it tries to remove constraints in {@link Container#add(Component, Object)}.
	 */
	protected void removeComponentConstraints(ContainerInfo container, ComponentInfo component)
			throws Exception {
		if (component.getAssociation() instanceof InvocationChildAssociation) {
			InvocationChildAssociation association =
					(InvocationChildAssociation) component.getAssociation();
			MethodInvocation invocation = association.getInvocation();
			String signature = AstNodeUtils.getMethodSignature(invocation);
			if (signature.equals("add(java.awt.Component,java.lang.Object)")) {
				getEditor().removeInvocationArgument(invocation, 1);
			}
			if (signature.equals("add(java.lang.String,java.awt.Component)")) {
				getEditor().removeInvocationArgument(invocation, 0);
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Adds given {@link ComponentInfo} using {@link Container#add(Component, Object)} or
	 * {@link Container#add(Component)}.
	 */
	protected final void add(ComponentInfo component,
			String constraintsSource,
			ComponentInfo nextComponent) throws Exception {
		AssociationObject association = getAssociation(constraintsSource);
		JavaInfoUtils.add(component, association, getContainer(), nextComponent);
	}

	/**
	 * Moves {@link ComponentInfo} to this container.
	 */
	protected final void move(ComponentInfo component,
			String constraintsSource,
			ComponentInfo nextComponent) throws Exception {
		ContainerInfo container = getContainer();
		AssociationObject association = getAssociation(constraintsSource);
		JavaInfoUtils.move(component, association, container, nextComponent);
	}

	/**
	 * @return the {@link AssociationObject} for standard Swing parent/child association - using
	 *         methods {@link Container#add(Component, Object)} or {@link Container#add(Component)}
	 *
	 * @param constraintsSource
	 *          the source for constraints object for {@link Container#add(Component, Object)} or
	 *          <code>null</code> if {@link Container#add(Component)} should be used.
	 */
	private static AssociationObject getAssociation(String constraintsSource) throws Exception {
		if (constraintsSource != null) {
			return AssociationObjects.invocationChild(
					"%parent%.add(%child%, " + constraintsSource + ")",
					false);
		} else {
			return AssociationObjects.invocationChild("%parent%.add(%child%)", false);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Clipboard
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Adds commands for coping parent {@link ContainerInfo}.
	 */
	protected void clipboardCopy_addContainerCommands(List<ClipboardCommand> commands)
			throws Exception {
		for (ComponentInfo component : getContainer().getChildrenComponents()) {
			if (!JavaInfoUtils.isImplicitlyCreated(component)) {
				clipboardCopy_addComponentCommands(component, commands);
			}
		}
	}

	/**
	 * Adds commands for coping {@link ComponentInfo} on parent {@link ContainerInfo}.
	 */
	protected void clipboardCopy_addComponentCommands(ComponentInfo component,
			List<ClipboardCommand> commands) throws Exception {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Manage general layout data.
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Store general layout data properties for {@link ComponentInfo}.
	 */
	protected void storeLayoutData(ComponentInfo component) throws Exception {
	}

	protected void storeLayoutDataDefault(ComponentInfo component) throws Exception {
		if (isManagedObject(component)) {
			GeneralLayoutData generalLayoutData = new GeneralLayoutData();
			generalLayoutData.gridX = null;
			generalLayoutData.gridY = null;
			generalLayoutData.spanX = null;
			generalLayoutData.spanY = null;
			generalLayoutData.horizontalGrab = null;
			generalLayoutData.verticalGrab = null;
			// alignments
			{
				float alignmentX = (Float) component.getPropertyByTitle("alignmentX").getValue();
				if (alignmentX <= 0.3) {
					generalLayoutData.horizontalAlignment = HorizontalAlignment.LEFT;
				} else if (alignmentX >= 0.6) {
					generalLayoutData.horizontalAlignment = HorizontalAlignment.RIGHT;
				} else {
					generalLayoutData.horizontalAlignment = HorizontalAlignment.CENTER;
				}
			}
			{
				float alignmentY = (Float) component.getPropertyByTitle("alignmentY").getValue();
				if (alignmentY <= 0.3) {
					generalLayoutData.verticalAlignment = VerticalAlignment.TOP;
				} else if (alignmentY >= 0.6) {
					generalLayoutData.verticalAlignment = VerticalAlignment.BOTTOM;
				} else {
					generalLayoutData.verticalAlignment = VerticalAlignment.CENTER;
				}
			}
			generalLayoutData.putToInfo(component);
		}
	}

	/**
	 * Removes the previous layout and sets the default layout as specified in the Windowbuilder
	 * layout preferences
	 *
	 * When the Implicit (default) layout option is selected the layout defaults to the GridLayout
	 *
	 * @throws Exception
	 */
	private void setDefaultLayout() throws Exception {
		Preferences prefs = InstanceScope.INSTANCE.getNode("org.eclipse.wb.swing");
		//when the preferences are set to "Implicit (default) layout" the returned value is null
		//therefore the default value for that would then default to flowLayout
		String defaultValue = prefs.get("layout.default", "gridLayout");
		List<LayoutDescription> descriptions =
				LayoutDescriptionHelper.get(getDescription().getToolkit());
		String creationId = null;
		ClassLoader editorLoader = null;
		Class<?> layoutClass = null;
		for (LayoutDescription description : descriptions) {
			if (Objects.equals(defaultValue, description.getId())) {
				creationId = description.getCreationId();
				editorLoader = EditorState.get(getContainer().getEditor()).getEditorLoader();
				layoutClass = editorLoader.loadClass(description.getLayoutClassName());
			}
		}
		LayoutInfo defaultLayoutInfo = (LayoutInfo) JavaInfoUtils.createJavaInfo(
				getContainer().getEditor(),
				layoutClass,
				new ConstructorCreationSupport(creationId, true));
		getContainer().setLayout(defaultLayoutInfo);
	}
}
