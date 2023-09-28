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
package org.eclipse.wb.internal.swing.databinding;

import com.google.common.collect.Maps;

import org.eclipse.wb.core.editor.IDesignPageSite;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.databinding.model.IBindingInfo;
import org.eclipse.wb.internal.core.databinding.model.IDatabindingsProvider;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.model.ObserveTypeContainer;
import org.eclipse.wb.internal.core.databinding.model.SynchronizeManager;
import org.eclipse.wb.internal.core.databinding.ui.BindingDesignPage;
import org.eclipse.wb.internal.core.databinding.ui.ObserveType;
import org.eclipse.wb.internal.core.databinding.ui.UiUtils;
import org.eclipse.wb.internal.core.databinding.ui.editor.IPageListener;
import org.eclipse.wb.internal.core.databinding.ui.editor.IUiContentProvider;
import org.eclipse.wb.internal.core.databinding.ui.filter.AllPropertiesFilter;
import org.eclipse.wb.internal.core.databinding.ui.filter.PropertyFilter;
import org.eclipse.wb.internal.core.databinding.ui.providers.ObserveDecoratingLabelProvider;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;
import org.eclipse.wb.internal.swing.databinding.model.DataBindingsRootInfo;
import org.eclipse.wb.internal.swing.databinding.model.JavaInfoDecorator;
import org.eclipse.wb.internal.swing.databinding.model.JavaInfoDeleteManager;
import org.eclipse.wb.internal.swing.databinding.model.ObserveCreationType;
import org.eclipse.wb.internal.swing.databinding.model.ObserveInfo;
import org.eclipse.wb.internal.swing.databinding.model.bindings.AutoBindingInfo;
import org.eclipse.wb.internal.swing.databinding.model.bindings.BindingInfo;
import org.eclipse.wb.internal.swing.databinding.model.bindings.ColumnBindingInfo;
import org.eclipse.wb.internal.swing.databinding.model.bindings.DetailBindingInfo;
import org.eclipse.wb.internal.swing.databinding.model.bindings.JComboBoxBindingInfo;
import org.eclipse.wb.internal.swing.databinding.model.bindings.JListBindingInfo;
import org.eclipse.wb.internal.swing.databinding.model.bindings.JTableBindingInfo;
import org.eclipse.wb.internal.swing.databinding.model.bindings.UpdateStrategyInfo;
import org.eclipse.wb.internal.swing.databinding.model.bindings.VirtualBindingInfo;
import org.eclipse.wb.internal.swing.databinding.model.properties.PropertyInfo;
import org.eclipse.wb.internal.swing.databinding.ui.filters.HideAdvancedPropertyFilter;
import org.eclipse.wb.internal.swing.databinding.ui.filters.ShowAdvancedPropertyFilter;
import org.eclipse.wb.internal.swing.databinding.ui.filters.TypesPropertyFilter;
import org.eclipse.wb.internal.swing.databinding.ui.property.JavaInfoPropertiesManager;
import org.eclipse.wb.internal.swing.databinding.ui.providers.BindingLabelProvider;
import org.eclipse.wb.internal.swing.databinding.ui.providers.TypeImageProvider;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.ToolBar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * {@link IDatabindingsProvider} for support Swing beans bindings API.
 *
 * @author lobas_av
 * @coverage bindings.swing.model
 */
public final class DatabindingsProvider implements IDatabindingsProvider {
	private static List<PropertyFilter> m_filters;
	private final JavaInfo m_javaInfoRoot;
	private final DataBindingsRootInfo m_rootInfo = new DataBindingsRootInfo(this);
	private final List<ObserveTypeContainer> m_containers;
	private final List<ObserveType> m_types = new ArrayList<>();
	private final Map<ObserveType, ObserveTypeContainer> m_typeToContainer = Maps.newHashMap();
	private BindingDesignPage m_bindingPage;
	private ObserveType m_targetStartType;
	private ObserveType m_modelStartType;
	private JavaInfoDeleteManager m_javaInfoDeleteManager;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public DatabindingsProvider(JavaInfo javaInfoRoot) throws Exception {
		m_javaInfoRoot = javaInfoRoot;
		// load containers
		m_containers =
				ExternalFactoriesHelper.getElementsInstances(
						ObserveTypeContainer.class,
						"org.eclipse.wb.swing.databinding.observeTypeContainer",
						"container");
		// prepare containers
		for (ObserveTypeContainer container : m_containers) {
			ObserveType observeType = container.getObserveType();
			m_types.add(observeType);
			m_typeToContainer.put(observeType, container);
			//
			if (container.isTargetStartType()) {
				m_targetStartType = observeType;
			}
			if (container.isModelStartType()) {
				m_modelStartType = observeType;
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public JavaInfo getJavaInfoRoot() {
		return m_javaInfoRoot;
	}

	public List<ObserveTypeContainer> getContainers() {
		return m_containers;
	}

	public ObserveTypeContainer getContainer(ObserveType type) {
		return m_typeToContainer.get(type);
	}

	public DataBindingsRootInfo getRootInfo() {
		return m_rootInfo;
	}

	public void hookJavaInfoEvents() throws Exception {
		// update observes
		new SynchronizeManager(this, m_javaInfoRoot);
		// handle delete info's
		m_javaInfoDeleteManager = new JavaInfoDeleteManager(this);
		// decorate info's
		new JavaInfoDecorator(this);
		// properties
		new JavaInfoPropertiesManager(this, m_javaInfoRoot);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Bindings
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void configureBindingViewer(IDialogSettings settings, TableViewer viewer) {
		// prepare table
		Table table = viewer.getTable();
		// binding type image column
		TableColumn bindingColumn = new TableColumn(table, SWT.NONE);
		bindingColumn.setWidth(23);
		bindingColumn.setResizable(false);
		// target column
		TableColumn targetColumn = UiUtils.createSmartColumn(table, settings, "TargetColumn", 250);
		targetColumn.setText(Messages.DatabindingsProvider_targetColumnText);
		// model column
		TableColumn modelColumn = UiUtils.createSmartColumn(table, settings, "ModelColumn", 250);
		modelColumn.setText(Messages.DatabindingsProvider_modelColumnText);
		// strategy column
		TableColumn strategyColumn = UiUtils.createSmartColumn(table, settings, "StrategyColumn", 170);
		strategyColumn.setText(Messages.DatabindingsProvider_strategyColumnText);
		// binding variable column
		TableColumn variableBindingColumn =
				UiUtils.createSmartColumn(table, settings, "VariableBindingColumn", 250);
		variableBindingColumn.setText(Messages.DatabindingsProvider_bindingColumnText);
		// label provider
		viewer.setLabelProvider(new BindingLabelProvider());
	}

	@Override
	public List<IBindingInfo> getBindings() {
		return CoreUtils.cast(m_rootInfo.getBindings());
	}

	@Override
	public String getBindingPresentationText(IBindingInfo ibinding) throws Exception {
		BindingInfo binding = (BindingInfo) ibinding;
		return "Binding["
		+ binding.getTargetPresentationText(true)
		+ " : "
		+ binding.getModelPresentationText(true)
		+ "]";
	}

	@Override
	public void gotoDefinition(IBindingInfo ibinding) {
		try {
			// calculate variable
			BindingInfo binding = (BindingInfo) ibinding;
			String variable = binding.getVariableIdentifier();
			if (variable != null) {
				if (variable.endsWith("()")) {
					variable = variable.substring(0, variable.length() - 2);
				}
				// calculate position
				int position = m_javaInfoRoot.getEditor().getEnclosingNode(variable).getStartPosition();
				// sets position
				IDesignPageSite site = IDesignPageSite.Helper.getSite(m_javaInfoRoot);
				site.openSourcePosition(position);
			}
		} catch (Throwable e) {
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Types
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public List<ObserveType> getTypes() {
		return m_types;
	}

	@Override
	public ObserveType getTargetStartType() {
		return m_targetStartType;
	}

	@Override
	public ObserveType getModelStartType() {
		return m_modelStartType;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Observes
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public IBaseLabelProvider createPropertiesViewerLabelProvider(TreeViewer viewer) {
		return new ObserveDecoratingLabelProvider(viewer);
	}

	@Override
	public List<PropertyFilter> getObservePropertyFilters() {
		if (m_filters == null) {
			m_filters = new ArrayList<>();
			// advanced
			m_filters.add(new HideAdvancedPropertyFilter());
			m_filters.add(new ShowAdvancedPropertyFilter());
			// any type
			m_filters.add(new AllPropertiesFilter(Messages.DatabindingsProvider_allTypes,
					TypeImageProvider.OBJECT_IMAGE));
			// String, byte, char
			m_filters.add(new TypesPropertyFilter("String",
					TypeImageProvider.STRING_IMAGE,
					String.class,
					byte.class,
					char.class));
			// boolean
			m_filters.add(new TypesPropertyFilter("Boolean",
					TypeImageProvider.BOOLEAN_IMAGE,
					boolean.class,
					Boolean.class));
			// int, short, long, float, double
			m_filters.add(new TypesPropertyFilter("Numbers",
					TypeImageProvider.NUMBER_IMAGE,
					int.class,
					short.class,
					long.class,
					float.class,
					double.class));
			// Swing Color
			m_filters.add(new TypesPropertyFilter("Color",
					TypeImageProvider.COLOR_IMAGE,
					java.awt.Color.class));
			// Swing Font
			m_filters.add(new TypesPropertyFilter("Font",
					TypeImageProvider.FONT_IMAGE,
					java.awt.Font.class));
			// Swing Image
			m_filters.add(new TypesPropertyFilter("Image",
					TypeImageProvider.IMAGE_IMAGE,
					java.awt.Image.class,
					javax.swing.Icon.class));
		}
		return m_filters;
	}

	@Override
	public List<IObserveInfo> getObserves(ObserveType type) {
		ObserveTypeContainer container = m_typeToContainer.get(type);
		return container == null ? Collections.<IObserveInfo>emptyList() : container.getObservables();
	}

	@Override
	public void synchronizeObserves() throws Exception {
		// prepare editor
		AstEditor editor = m_javaInfoRoot.getEditor();
		// prepare node
		TypeDeclaration rootNode = JavaInfoUtils.getTypeDeclaration(m_javaInfoRoot);
		if (rootNode == null) {
			// use first type declaration from compilation unit
			CompilationUnit astUnit = editor.getAstUnit();
			rootNode = (TypeDeclaration) astUnit.types().get(0);
		}
		// synchronize
		for (ObserveTypeContainer container : m_containers) {
			container.synchronizeObserves(m_javaInfoRoot, editor, rootNode);
		}
	}

	@Override
	public void fillExternalBindingActions(ToolBar toolBar, Menu contextMenu) {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Editing
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public List<IUiContentProvider> getContentProviders(IBindingInfo ibinding, IPageListener listener)
			throws Exception {
		List<IUiContentProvider> providers = new ArrayList<>();
		BindingInfo binding = (BindingInfo) ibinding;
		binding.createContentProviders(m_rootInfo.getBindings(), providers, listener, this);
		return providers;
	}

	@Override
	public boolean validate(IObserveInfo target,
			IObserveInfo targetProperty,
			IObserveInfo model,
			IObserveInfo modelProperty) throws Exception {
		// ignore itself
		if (target == model && targetProperty == modelProperty) {
			return false;
		}
		// prepare ObserveInfo's
		ObserveInfo targetObserve = (ObserveInfo) target;
		ObserveInfo targetPropertyObserve = (ObserveInfo) targetProperty;
		ObserveInfo modelObserve = (ObserveInfo) model;
		ObserveInfo modelPropertyObserve = (ObserveInfo) modelProperty;
		// check swing bindings
		boolean isTargetSwingBinding = isSwingBinding(targetObserve, targetPropertyObserve);
		boolean isModelSwingBinding = isSwingBinding(modelObserve, modelPropertyObserve);
		if (isTargetSwingBinding && isModelSwingBinding) {
			return false;
		}
		if (isTargetSwingBinding) {
			if (findBinding(targetObserve, targetPropertyObserve)) {
				return false;
			}
		} else if (isModelSwingBinding) {
			if (findBinding(modelObserve, modelPropertyObserve)) {
				return false;
			}
		}
		// check virtual bindings
		if (targetObserve.getCreationType() == ObserveCreationType.VirtualBinding) {
			return isModelSwingBinding;
		}
		if (modelObserve.getCreationType() == ObserveCreationType.VirtualBinding) {
			return isTargetSwingBinding;
		}
		// check over reference's
		return !targetObserve.getReference().equals(modelObserve.getReference())
				|| !targetPropertyObserve.getReference().equals(modelPropertyObserve.getReference());
	}

	private boolean findBinding(IObserveInfo object, IObserveInfo property) {
		for (BindingInfo binding : m_rootInfo.getBindings()) {
			// check target
			if (binding.getTarget() == object && binding.getTargetProperty() == property) {
				return true;
			}
			// check model
			if (binding.getModel() == object && binding.getModelProperty() == property) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return <code>true</code> if given pair {@link ObserveInfo} object + property is swing binding
	 *         type.
	 */
	public static boolean isSwingBinding(ObserveInfo observe, ObserveInfo propertyObserve) {
		ObserveCreationType creationType = observe.getCreationType();
		return propertyObserve.getCreationType() == ObserveCreationType.SelfProperty
				&& (creationType == ObserveCreationType.JListBinding
				|| creationType == ObserveCreationType.JTableBinding || creationType == ObserveCreationType.JComboBoxBinding);
	}

	@Override
	public IBindingInfo createBinding(IObserveInfo target,
			IObserveInfo targetProperty,
			IObserveInfo model,
			IObserveInfo modelProperty) throws Exception {
		// prepare ObserveInfo's
		ObserveInfo targetObserve = (ObserveInfo) target;
		ObserveInfo targetPropertyObserve = (ObserveInfo) targetProperty;
		ObserveInfo modelObserve = (ObserveInfo) model;
		ObserveInfo modelPropertyObserve = (ObserveInfo) modelProperty;
		// check virtual bindings
		if (targetObserve.getCreationType() == ObserveCreationType.VirtualBinding
				|| modelObserve.getCreationType() == ObserveCreationType.VirtualBinding) {
			return new VirtualBindingInfo(targetObserve, targetPropertyObserve, createProperty(
					targetObserve,
					targetPropertyObserve), modelObserve, modelPropertyObserve, createProperty(
							modelObserve,
							modelPropertyObserve));
		}
		// check swing bindings
		if (targetObserve.getCreationType() != ObserveCreationType.AutoBinding) {
			IBindingInfo binding =
					createSwingBinding(
							targetObserve,
							targetPropertyObserve,
							modelObserve,
							modelPropertyObserve);
			if (binding != null) {
				return binding;
			}
		}
		if (modelObserve.getCreationType() != ObserveCreationType.AutoBinding) {
			IBindingInfo binding =
					createSwingBinding(
							modelObserve,
							modelPropertyObserve,
							targetObserve,
							targetPropertyObserve);
			if (binding != null) {
				return binding;
			}
		}
		// auto bindings
		UpdateStrategyInfo strategy = new UpdateStrategyInfo(UpdateStrategyInfo.Value.READ);
		PropertyInfo targetAstProperty = createProperty(targetObserve, targetPropertyObserve);
		PropertyInfo modelAstProperty = createProperty(modelObserve, modelPropertyObserve);
		//
		return new AutoBindingInfo(strategy,
				targetObserve,
				targetPropertyObserve,
				targetAstProperty,
				modelObserve,
				modelPropertyObserve,
				modelAstProperty);
	}

	private IBindingInfo createSwingBinding(ObserveInfo component,
			ObserveInfo componentProperty,
			ObserveInfo model,
			ObserveInfo modelProperty) throws Exception {
		// check swing binding type
		if (componentProperty.getCreationType() != ObserveCreationType.SelfProperty) {
			return null;
		}
		if (modelProperty.getCreationType() != ObserveCreationType.ListSelfProperty
				&& modelProperty.getCreationType() != ObserveCreationType.ListProperty) {
			return null;
		}
		// create strategy
		UpdateStrategyInfo strategy = new UpdateStrategyInfo(UpdateStrategyInfo.Value.READ);
		// create properties
		PropertyInfo componentAstProperty = createProperty(component, componentProperty);
		PropertyInfo modelAstProperty = createProperty(model, modelProperty);
		// create binding
		switch (component.getCreationType()) {
		case JListBinding :
			return new JListBindingInfo(strategy,
					component,
					componentProperty,
					componentAstProperty,
					model,
					modelProperty,
					modelAstProperty);
		case JTableBinding :
			return new JTableBindingInfo(strategy,
					component,
					componentProperty,
					componentAstProperty,
					model,
					modelProperty,
					modelAstProperty);
		case JComboBoxBinding :
			return new JComboBoxBindingInfo(strategy,
					component,
					componentProperty,
					componentAstProperty,
					model,
					modelProperty,
					modelAstProperty);
		}
		//
		return null;
	}

	private PropertyInfo createProperty(ObserveInfo observe, ObserveInfo observeProperty)
			throws Exception {
		PropertyInfo astProperty = observeProperty.createProperty(observe);
		//
		if (observeProperty.canShared()) {
			for (BindingInfo binding : m_rootInfo.getBindings()) {
				// check target
				PropertyInfo targetAstProperty = binding.getTargetAstProperty();
				if (targetAstProperty.getClass() == astProperty.getClass()) {
					if (binding.getTarget() == observe
							&& binding.getTargetProperty() == observeProperty
							|| targetAstProperty.canShared(astProperty)) {
						return targetAstProperty;
					}
				}
				// check model
				PropertyInfo modelAstProperty = binding.getModelAstProperty();
				if (modelAstProperty.getClass() == astProperty.getClass()) {
					if (binding.getModel() == observe
							&& binding.getModelProperty() == observeProperty
							|| modelAstProperty.canShared(astProperty)) {
						return modelAstProperty;
					}
				}
			}
		}
		//
		return astProperty;
	}

	@Override
	public void addBinding(final IBindingInfo ibinding) {
		// add
		getBindings().add(ibinding);
		// post process
		ExecutionUtils.runLog(new RunnableEx() {
			@Override
			public void run() throws Exception {
				BindingInfo binding = (BindingInfo) ibinding;
				binding.create(m_rootInfo.getBindings());
			}
		});
		// save
		saveEdit();
	}

	@Override
	public void editBinding(final IBindingInfo ibinding) {
		// edit
		ExecutionUtils.runLog(new RunnableEx() {
			@Override
			public void run() throws Exception {
				BindingInfo binding = (BindingInfo) ibinding;
				binding.edit(m_rootInfo.getBindings());
			}
		});
		// save
		saveEdit();
	}

	@Override
	public void deleteBinding(final IBindingInfo ibinding) {
		// delete
		ExecutionUtils.runLog(new RunnableEx() {
			@Override
			public void run() throws Exception {
				BindingInfo binding = (BindingInfo) ibinding;
				if (binding.delete(m_rootInfo.getBindings())) {
					binding.postDelete();
					getBindings().remove(binding);
				}
			}
		});
		// save
		saveEdit();
	}

	@Override
	public void deleteAllBindings() {
		// delete all
		ExecutionUtils.runLog(new RunnableEx() {
			@Override
			public void run() throws Exception {
				for (BindingInfo binding : m_rootInfo.getBindings()) {
					binding.postDelete();
				}
			}
		});
		getBindings().clear();
		// save
		saveEdit();
	}

	@Override
	public void deleteBindings(JavaInfo javaInfo) throws Exception {
		if (m_javaInfoDeleteManager != null) {
			m_javaInfoDeleteManager.deleteJavaInfo(javaInfo);
		}
	}

	@Override
	public boolean canMoveBinding(IBindingInfo binding, int targetIndex, boolean upDown) {
		// don't move detail binding
		if (binding instanceof DetailBindingInfo) {
			return false;
		}
		// column binding can move only relative to column bindings
		IBindingInfo target = getBindings().get(targetIndex);
		if (binding instanceof ColumnBindingInfo) {
			return target instanceof ColumnBindingInfo;
		}
		// enable move to up for JListBindingInfo and JTableBindingInfo
		if (target instanceof DetailBindingInfo || target instanceof ColumnBindingInfo) {
			return upDown;
		}
		return true;
	}

	@Override
	public void moveBinding(IBindingInfo binding, int sourceIndex, int targetIndex, boolean upDown) {
		// configure target index
		if (upDown) {
			targetIndex = configureMoveUpDown(binding, sourceIndex, targetIndex);
		}
		// do reorder
		List<IBindingInfo> bindings = getBindings();
		bindings.remove(sourceIndex);
		bindings.add(targetIndex, binding);
		// binding handle
		BindingInfo bindingInfo = (BindingInfo) binding;
		bindingInfo.move(m_rootInfo.getBindings());
		// save
		saveEdit();
	}

	private int configureMoveUpDown(IBindingInfo binding, int sourceIndex, int targetIndex) {
		if (!(binding instanceof ColumnBindingInfo)) {
			List<IBindingInfo> bindings = getBindings();
			IBindingInfo target = bindings.get(targetIndex);
			boolean up = sourceIndex > targetIndex;
			// configure target index
			if (target instanceof ColumnBindingInfo column) {
				// calculate column index
				if (up) {
					targetIndex = bindings.indexOf(column.getJTableBinding());
				} else {
					// skip all columns
					targetIndex += column.getJTableBinding().getColumns().size();
					//
					target = bindings.get(targetIndex);
					if (target instanceof JTableBindingInfo tableBinding) {
						targetIndex += tableBinding.getColumns().size();
					} else if (target instanceof JListBindingInfo) {
						// skip detail binding
						targetIndex++;
					}
				}
			} else if (target instanceof DetailBindingInfo) {
				if (up) {
					DetailBindingInfo detail = (DetailBindingInfo) target;
					targetIndex = bindings.indexOf(detail.getJListBinding());
				} else {
					// skip detail binding
					targetIndex++;
					//
					target = bindings.get(targetIndex);
					if (target instanceof JTableBindingInfo tableBinding) {
						targetIndex += tableBinding.getColumns().size();
					} else if (target instanceof JListBindingInfo) {
						// skip detail binding
						targetIndex++;
					}
				}
			} else if (target instanceof JTableBindingInfo tableBinding && !up) {
				targetIndex += tableBinding.getColumns().size();
			} else if (target instanceof JListBindingInfo && !up) {
				// skip detail binding
				targetIndex++;
			}
		}
		return targetIndex;
	}

	@Override
	public void setBindingPage(Object bindingPage) {
		m_bindingPage = (BindingDesignPage) bindingPage;
	}

	@Override
	public void refreshDesigner() {
	}

	@Override
	public void saveEdit() {
		final boolean[] reparse = new boolean[1];
		ExecutionUtils.run(m_javaInfoRoot, new RunnableEx() {
			@Override
			public void run() throws Exception {
				try {
					m_rootInfo.preCommit();
					reparse[0] = m_rootInfo.commit();
				} finally {
					m_rootInfo.postCommit();
				}
				// check reparse
				if (reparse[0]) {
					BindingDesignPage.handleReparse(m_bindingPage, m_javaInfoRoot);
				}
			}
		});
		// check synchronize
		if (!reparse[0]) {
			ExecutionUtils.runLog(new RunnableEx() {
				@Override
				public void run() throws Exception {
					synchronizeObserves();
				}
			});
		}
	}
}