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
package org.eclipse.wb.internal.core.xml.model.utils;

import com.google.common.collect.ImmutableMap;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.model.IObjectInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.requests.PasteRequest;
import org.eclipse.wb.internal.core.model.creation.IImplicitCreationSupport;
import org.eclipse.wb.internal.core.model.description.IComponentDescription;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.state.EditorWarning;
import org.eclipse.wb.internal.core.utils.state.GlobalState;
import org.eclipse.wb.internal.core.utils.state.IDescriptionHelper;
import org.eclipse.wb.internal.core.utils.state.ILayoutRequestValidatorHelper;
import org.eclipse.wb.internal.core.utils.state.IOrderProcessor;
import org.eclipse.wb.internal.core.utils.state.IOtherHelper;
import org.eclipse.wb.internal.core.utils.state.IParametersProvider;
import org.eclipse.wb.internal.core.utils.state.IPasteComponentProcessor;
import org.eclipse.wb.internal.core.utils.state.IPasteRequestProcessor;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.association.OrderAssociation;
import org.eclipse.wb.internal.core.xml.model.clipboard.XmlObjectMemento;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescriptionHelper;
import org.eclipse.wb.internal.core.xml.model.description.DescriptionPropertiesHelper;

import org.eclipse.jdt.core.IJavaProject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Implementations of {@link GlobalState} for {@link XmlObjectInfo}.
 *
 * @author scheglov_ke
 * @coverage XML.model.util
 */
public final class GlobalStateXml {
	private static EditorContext m_context;

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Activates this {@link GlobalState}.
	 */
	public static void activate(XmlObjectInfo object) {
		GlobalState.setActiveObject(object);
		if (object != null) {
			GlobalState.setToolkit(object.getDescription().getToolkit());
			GlobalState.setClassLoader(object.getContext().getClassLoader());
			setEditorContext(object.getContext());
		} else {
			GlobalState.setToolkit(null);
			GlobalState.setClassLoader(null);
			setEditorContext(null);
		}
		GlobalState.setParametersProvider(m_parametersProvider);
		GlobalState.setDescriptionHelper(m_descriptionHelper);
		GlobalState.setValidatorHelper(m_validatorHelper);
		GlobalState.setPasteRequestProcessor(m_pasteRequestProcessor);
		GlobalState.setOrderProcessor(m_orderProcessor);
		GlobalState.setOtherHelper(m_otherHelper);
	}

	/**
	 * Deactivates this {@link GlobalState}, if given {@link XmlObjectInfo} is active.
	 */
	public static void deactivate(XmlObjectInfo object) {
		if (GlobalState.getActiveObject() == object) {
			deactivate();
		}
	}

	/**
	 * Deactivates {@link GlobalState}.
	 */
	public static void deactivate() {
		GlobalState.setActiveObject(null);
		GlobalState.setToolkit(null);
		GlobalState.setClassLoader(null);
		GlobalState.setParametersProvider(null);
		GlobalState.setDescriptionHelper(null);
		GlobalState.setValidatorHelper(null);
		GlobalState.setPasteRequestProcessor(null);
		GlobalState.setOrderProcessor(null);
		GlobalState.setOtherHelper(null);
		setEditorContext(null);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Editor context
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Set the editor context instance.
	 */
	public static void setEditorContext(EditorContext context) {
		m_context = context;
	}

	/**
	 * @return the current editor context. Cannot be <code>null</code> if the GlobalState activated.
	 */
	public static EditorContext getEditorContext() {
		return m_context;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Implementations
	//
	////////////////////////////////////////////////////////////////////////////
	private static final IParametersProvider m_parametersProvider = new IParametersProvider() {
		@Override
		public Map<String, String> getParameters(Object object) {
			if (object instanceof XmlObjectInfo) {
				return XmlObjectUtils.getParameters((XmlObjectInfo) object);
			}
			if (object instanceof ComponentDescription) {
				return ((ComponentDescription) object).getParameters();
			}
			return ImmutableMap.of();
		}

		@Override
		public String getParameter(Object object, String name) {
			if (object instanceof XmlObjectInfo) {
				return XmlObjectUtils.getParameter((XmlObjectInfo) object, name);
			}
			if (object instanceof ComponentDescription) {
				return ((ComponentDescription) object).getParameter(name);
			}
			return null;
		}

		@Override
		public boolean hasTrueParameter(Object object, String name) {
			if (object instanceof XmlObjectInfo) {
				return XmlObjectUtils.hasTrueParameter((XmlObjectInfo) object, name);
			}
			return false;
		}
	};
	private static final IDescriptionHelper m_descriptionHelper = new IDescriptionHelper() {
		@Override
		public PropertyEditor getEditorForType(Class<?> type) throws Exception {
			return DescriptionPropertiesHelper.getEditorForType(type);
		}

		@Override
		public IComponentDescription getDescription(Object object) {
			if (object instanceof XmlObjectInfo) {
				return ((XmlObjectInfo) object).getDescription();
			}
			return null;
		}
	};
	private static final ILayoutRequestValidatorHelper m_validatorHelper =
			new ILayoutRequestValidatorHelper() {
		@Override
		public boolean isComponent(Object object) {
			return object instanceof XmlObjectInfo;
		}

		@Override
		public IComponentDescription getPasteComponentDescription(Object _memento) throws Exception {
			XmlObjectMemento memento = (XmlObjectMemento) _memento;
			XmlObjectInfo activeObject = (XmlObjectInfo) GlobalState.getActiveObject();
			return ComponentDescriptionHelper.getDescription(
					activeObject.getContext(),
					memento.getComponentClassName());
		}

		@Override
		public Object getPasteComponent(Object _memento) throws Exception {
			XmlObjectMemento memento = (XmlObjectMemento) _memento;
			XmlObjectInfo activeObject = (XmlObjectInfo) GlobalState.getActiveObject();
			return memento.create(activeObject);
		}

		@Override
		public boolean canUseParentForChild(Object parent, Object child) throws Exception {
			return true;
		}

		@Override
		public boolean canReference(Object object) {
			if (object instanceof XmlObjectInfo xmlObject) {
				return !(xmlObject.getCreationSupport() instanceof IImplicitCreationSupport);
			}
			return false;
		}

		@Override
		public boolean canReorder(ObjectInfo component) {
			return true;
		}

		@Override
		public boolean canReparent(ObjectInfo component) {
			return true;
		}
	};
	private static final IPasteRequestProcessor m_pasteRequestProcessor =
			new IPasteRequestProcessor() {
		@Override
		public Command getPasteCommand(PasteRequest request,
				final IPasteComponentProcessor componentProcessor) {
			final List<IObjectInfo> components = getPastingComponents(request);
			if (components.isEmpty()) {
				return null;
			}
			// create command
			ObjectInfo hierarchyObject = GlobalState.getActiveObject();
			return new EditCommand(hierarchyObject) {
				@Override
				protected void executeEdit() throws Exception {
					for (IObjectInfo component : components) {
						componentProcessor.process(component);
						XmlObjectMemento.apply((XmlObjectInfo) component.getUnderlyingModel());
					}
				}
			};
		}

		@Override
		public List<IObjectInfo> getPastingComponents(PasteRequest request) {
			@SuppressWarnings("unchecked")
			final List<XmlObjectMemento> mementos = (List<XmlObjectMemento>) request.getMemento();
			final List<IObjectInfo> components = new ArrayList<>();
			// prepare models
			ExecutionUtils.runLog(new RunnableEx() {
				@Override
				public void run() throws Exception {
					XmlObjectInfo hierarchyModel = (XmlObjectInfo) GlobalState.getActiveObject();
					for (XmlObjectMemento memento : mementos) {
						XmlObjectInfo component = memento.create(hierarchyModel);
						components.add(component);
					}
				}
			});
			// set objects for selection
			request.setObjects(components);
			return components;
		}
	};
	private static final IOrderProcessor m_orderProcessor = new IOrderProcessor() {
		@Override
		public void move(Object _component, Object _nextComponent) throws Exception {
			XmlObjectInfo component = (XmlObjectInfo) _component;
			XmlObjectInfo nextComponent = (XmlObjectInfo) _nextComponent;
			XmlObjectInfo container = (XmlObjectInfo) component.getParent();
			XmlObjectUtils.move(component, OrderAssociation.INSTANCE, container, nextComponent);
		}
	};
	private static final IOtherHelper m_otherHelper = new IOtherHelper() {
		@Override
		public IJavaProject getJavaProject() {
			XmlObjectInfo object = (XmlObjectInfo) GlobalState.getActiveObject();
			return object.getContext().getJavaProject();
		}

		@Override
		public Object getObject(ObjectInfo model) {
			if (model instanceof XmlObjectInfo) {
				return ((XmlObjectInfo) model).getObject();
			}
			return null;
		}

		@Override
		public List<EditorWarning> getWarnings() {
			return getContext().getWarnings();
		}

		@Override
		public void addWarning(EditorWarning warning) {
			getContext().addWarning(warning);
		}

		private EditorContext getContext() {
			return ((XmlObjectInfo) GlobalState.getActiveObject()).getContext();
		}
	};
}
