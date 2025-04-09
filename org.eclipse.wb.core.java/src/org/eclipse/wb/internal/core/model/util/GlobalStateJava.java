/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
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
package org.eclipse.wb.internal.core.model.util;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.model.IObjectInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.gef.core.requests.PasteRequest;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.internal.core.model.creation.IImplicitCreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.IComponentDescription;
import org.eclipse.wb.internal.core.model.description.helpers.ComponentDescriptionHelper;
import org.eclipse.wb.internal.core.model.description.helpers.DescriptionPropertiesHelper;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.core.utils.state.EditorWarning;
import org.eclipse.wb.internal.core.utils.state.GlobalState;
import org.eclipse.wb.internal.core.utils.state.IDescriptionHelper;
import org.eclipse.wb.internal.core.utils.state.ILayoutRequestValidatorHelper;
import org.eclipse.wb.internal.core.utils.state.IOrderProcessor;
import org.eclipse.wb.internal.core.utils.state.IOtherHelper;
import org.eclipse.wb.internal.core.utils.state.IParametersProvider;
import org.eclipse.wb.internal.core.utils.state.IPasteComponentProcessor;
import org.eclipse.wb.internal.core.utils.state.IPasteRequestProcessor;

import org.eclipse.gef.commands.Command;
import org.eclipse.jdt.core.IJavaProject;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementations of {@link GlobalState} for {@link JavaInfo}.
 *
 * @author scheglov_ke
 * @coverage core.model.util
 */
public final class GlobalStateJava {
	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Activates this {@link GlobalState}.
	 */
	public static void activate(JavaInfo javaInfo) {
		EditorState.setActiveJavaInfo(javaInfo);
		GlobalState.setActiveObject(javaInfo);
		if (javaInfo != null) {
			GlobalState.setToolkit(javaInfo.getDescription().getToolkit());
			GlobalState.setClassLoader(JavaInfoUtils.getClassLoader(javaInfo));
		} else {
			GlobalState.setToolkit(null);
			GlobalState.setClassLoader(null);
		}
		GlobalState.setParametersProvider(m_parametersProvider);
		GlobalState.setDescriptionHelper(m_descriptionHelper);
		GlobalState.setValidatorHelper(m_validatorHelper);
		GlobalState.setPasteRequestProcessor(m_pasteRequestProcessor);
		GlobalState.setOrderProcessor(m_orderProcessor);
		GlobalState.setOtherHelper(m_otherHelper);
	}

	/**
	 * Deactivates this {@link GlobalState}, if given {@link JavaInfo} is active.
	 */
	public static void deactivate(JavaInfo javaInfo) {
		if (GlobalState.getActiveObject() == javaInfo) {
			deactivate();
		}
	}

	/**
	 * Deactivates {@link GlobalState}.
	 */
	public static void deactivate() {
		EditorState.setActiveJavaInfo(null);
		GlobalState.setActiveObject(null);
		GlobalState.setToolkit(null);
		GlobalState.setClassLoader(null);
		GlobalState.setParametersProvider(null);
		GlobalState.setDescriptionHelper(null);
		GlobalState.setValidatorHelper(null);
		GlobalState.setPasteRequestProcessor(null);
		GlobalState.setOrderProcessor(null);
		GlobalState.setOtherHelper(null);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Implementations
	//
	////////////////////////////////////////////////////////////////////////////
	private static final IParametersProvider m_parametersProvider = new IParametersProvider() {
		@Override
		public java.util.Map<String, String> getParameters(Object object) {
			if (object instanceof JavaInfo) {
				return JavaInfoUtils.getParameters((JavaInfo) object);
			}
			if (object instanceof ComponentDescription) {
				return ((ComponentDescription) object).getParameters();
			}
			return null;
		}

		@Override
		public String getParameter(Object object, String name) {
			if (object instanceof JavaInfo) {
				return JavaInfoUtils.getParameter((JavaInfo) object, name);
			}
			if (object instanceof ComponentDescription) {
				return ((ComponentDescription) object).getParameter(name);
			}
			return null;
		}

		@Override
		public boolean hasTrueParameter(Object object, String name) {
			if (object instanceof JavaInfo) {
				return JavaInfoUtils.hasTrueParameter((JavaInfo) object, name);
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
			if (object instanceof JavaInfo) {
				return ((JavaInfo) object).getDescription();
			}
			return null;
		}
	};
	private static final ILayoutRequestValidatorHelper m_validatorHelper =
			new ILayoutRequestValidatorHelper() {
		@Override
		public boolean isComponent(Object object) {
			return object instanceof JavaInfo;
		}

		@Override
		public IComponentDescription getPasteComponentDescription(Object _memento) throws Exception {
			JavaInfoMemento memento = (JavaInfoMemento) _memento;
			JavaInfo activeJava = (JavaInfo) GlobalState.getActiveObject();
			return ComponentDescriptionHelper.getDescription(
					activeJava.getEditor(),
					memento.getComponentClassName());
		}

		@Override
		public Object getPasteComponent(Object _memento) throws Exception {
			JavaInfoMemento memento = (JavaInfoMemento) _memento;
			JavaInfo activeJava = (JavaInfo) GlobalState.getActiveObject();
			return memento.create(activeJava);
		}

		@Override
		public boolean canUseParentForChild(Object parent, Object child) throws Exception {
			if (parent instanceof JavaInfo parentJava && child instanceof JavaInfo) {
				JavaInfo childJava = (JavaInfo) child;
				return childJava.getCreationSupport().canUseParent(parentJava);
			}
			return true;
		}

		@Override
		public boolean canReference(Object object) {
			if (object instanceof JavaInfo javaInfo) {
				return !(javaInfo.getCreationSupport() instanceof IImplicitCreationSupport);
			}
			return false;
		}

		@Override
		public boolean canReorder(ObjectInfo component) {
			return ((JavaInfo) component).getCreationSupport().canReorder();
		}

		@Override
		public boolean canReparent(ObjectInfo component) {
			return ((JavaInfo) component).getCreationSupport().canReparent();
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
						JavaInfoMemento.apply((JavaInfo) component.getUnderlyingModel());
					}
				}
			};
		}

		@Override
		public List<IObjectInfo> getPastingComponents(final PasteRequest request) {
			@SuppressWarnings("unchecked")
			final List<JavaInfoMemento> mementos = (List<JavaInfoMemento>) request.getMemento();
			final List<IObjectInfo> components = new ArrayList<>();
			// prepare models
			ExecutionUtils.runLog(new RunnableEx() {
				@Override
				public void run() throws Exception {
					JavaInfo hierarchyModel = (JavaInfo) GlobalState.getActiveObject();
					for (JavaInfoMemento memento : mementos) {
						JavaInfo component = memento.create(hierarchyModel);
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
			JavaInfo component = (JavaInfo) _component;
			JavaInfo nextComponent = (JavaInfo) _nextComponent;
			JavaInfo container = (JavaInfo) component.getParent();
			JavaInfoUtils.move(component, null, container, nextComponent);
		}
	};
	private static final IOtherHelper m_otherHelper = new IOtherHelper() {
		@Override
		public IJavaProject getJavaProject() {
			JavaInfo javaInfo = (JavaInfo) GlobalState.getActiveObject();
			return javaInfo.getEditor().getJavaProject();
		}

		@Override
		public Object getObject(ObjectInfo model) {
			if (model instanceof JavaInfo) {
				return ((JavaInfo) model).getObject();
			}
			return null;
		}

		@Override
		public List<EditorWarning> getWarnings() {
			return getState().getWarnings();
		}

		@Override
		public void addWarning(EditorWarning warning) {
			getState().addWarning(warning);
		}

		private EditorState getState() {
			JavaInfo javaInfo = (JavaInfo) GlobalState.getActiveObject();
			return JavaInfoUtils.getState(javaInfo);
		}
	};
}
