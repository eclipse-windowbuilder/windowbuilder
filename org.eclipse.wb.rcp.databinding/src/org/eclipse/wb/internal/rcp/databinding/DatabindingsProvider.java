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
package org.eclipse.wb.internal.rcp.databinding;

import com.google.common.collect.Lists;
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
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;
import org.eclipse.wb.internal.rcp.databinding.model.AbstractBindingInfo;
import org.eclipse.wb.internal.rcp.databinding.model.BindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.ControllerSupport;
import org.eclipse.wb.internal.rcp.databinding.model.DataBindingsRootInfo;
import org.eclipse.wb.internal.rcp.databinding.model.IObservableFactory;
import org.eclipse.wb.internal.rcp.databinding.model.IObservableFactory.Type;
import org.eclipse.wb.internal.rcp.databinding.model.JavaInfoDecorator;
import org.eclipse.wb.internal.rcp.databinding.model.JavaInfoDeleteManager;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.context.BindingInfo;
import org.eclipse.wb.internal.rcp.databinding.model.context.ListBindingInfo;
import org.eclipse.wb.internal.rcp.databinding.model.context.SetBindingInfo;
import org.eclipse.wb.internal.rcp.databinding.model.context.ValueBindingInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.AbstractViewerInputBindingInfo;
import org.eclipse.wb.internal.rcp.databinding.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.rcp.databinding.ui.filter.AdvancedPropertyFilter;
import org.eclipse.wb.internal.rcp.databinding.ui.filter.TypesPropertyFilter;
import org.eclipse.wb.internal.rcp.databinding.ui.property.JavaInfoPropertiesManager;
import org.eclipse.wb.internal.rcp.databinding.ui.providers.BindingLabelProvider;
import org.eclipse.wb.internal.rcp.databinding.ui.providers.TypeImageProvider;

import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * {@link IDatabindingsProvider} for support JFace bindings API.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model
 */
public final class DatabindingsProvider implements IDatabindingsProvider {
	private static List<PropertyFilter> m_filters;
	private final JavaInfo m_javaInfoRoot;
	private final List<ObserveTypeContainer> m_containers;
	private final List<ObserveType> m_types = Lists.newArrayList();
	private final Map<ObserveType, ObserveTypeContainer> m_typeToContainer = Maps.newHashMap();
	private ObserveType m_targetStartType;
	private ObserveType m_modelStartType;
	private BindingDesignPage m_bindingPage;
	private JavaInfoDeleteManager m_javaInfoDeleteManager;
	private final DataBindingsRootInfo m_rootInfo = new DataBindingsRootInfo();
	private TypeDeclaration m_rootNode;
	private AstEditor m_astEditor;
	private boolean m_controller;
	private String m_controllerViewerField;
	private boolean m_synchronizeObserves = true;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	////////////////////////////////////////////////////////////////////////////
	protected DatabindingsProvider() {
		m_javaInfoRoot = null;
		m_containers = Lists.newArrayList();
	}

	public DatabindingsProvider(JavaInfo javaInfoRoot) throws Exception {
		m_javaInfoRoot = javaInfoRoot;
		// load containers
		m_containers =
				ExternalFactoriesHelper.getElementsInstances(
						ObserveTypeContainer.class,
						"org.eclipse.wb.rcp.databinding.observeTypeContainer",
						"container");
		// prepare containers
		for (Iterator<ObserveTypeContainer> I = m_containers.iterator(); I.hasNext();) {
			ObserveTypeContainer container = I.next();
			if (container.accept(m_javaInfoRoot)) {
				container.initialize(this);
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
			} else {
				I.remove();
			}
		}
		//
		m_rootNode = JavaInfoUtils.getTypeDeclaration(m_javaInfoRoot);
		m_astEditor = m_javaInfoRoot.getEditor();
		// find controller
		if (m_rootNode != null) {
			ControllerSupport.configure(this);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Internals
	//
	////////////////////////////////////////////////////////////////////////////
	public static DatabindingsProvider cast(IDatabindingsProvider provider) {
		return (DatabindingsProvider) provider;
	}

	public List<ObserveTypeContainer> getContainers() {
		return m_containers;
	}

	public ObserveTypeContainer getContainer(ObserveType type) {
		return m_typeToContainer.get(type);
	}

	public JavaInfo getJavaInfoRoot() {
		return m_javaInfoRoot;
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

	public TypeDeclaration getRootNode() {
		return m_rootNode;
	}

	public void setRootNode(TypeDeclaration rootNode) {
		m_rootNode = rootNode;
	}

	public AstEditor getAstEditor() {
		return m_astEditor;
	}

	public void setAstEditor(AstEditor astEditor) {
		m_astEditor = astEditor;
	}

	public boolean isController() {
		return m_controller;
	}

	public void setController(boolean controller) {
		m_controller = controller;
	}

	public String getControllerViewerField() {
		return m_controllerViewerField;
	}

	public void setControllerViewerField(String controllerViewerField) {
		m_controllerViewerField = controllerViewerField;
	}

	public void setSynchronizeObserves(boolean synchronizeObserves) {
		m_synchronizeObserves = synchronizeObserves;
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
		targetColumn.setText(Messages.DatabindingsProvider_targetColumn);
		// model column
		TableColumn modelColumn = UiUtils.createSmartColumn(table, settings, "ModelColumn", 250);
		modelColumn.setText(Messages.DatabindingsProvider_modelColumn);
		// target strategy column
		TableColumn targetStrategyColumn =
				UiUtils.createSmartColumn(table, settings, "TargetStrategyColumn", 170);
		targetStrategyColumn.setText(Messages.DatabindingsProvider_targetStrategyColumn);
		// model strategy column
		TableColumn modelStrategyColumn =
				UiUtils.createSmartColumn(table, settings, "ModelStrategyColumn", 170);
		modelStrategyColumn.setText(Messages.DatabindingsProvider_modelStrategyColumn);
		// binding variable column
		TableColumn variableBindingColumn =
				UiUtils.createSmartColumn(table, settings, "VariableBindingColumn", 250);
		variableBindingColumn.setText(Messages.DatabindingsProvider_bindingColumn);
		// label provider
		viewer.setLabelProvider(new BindingLabelProvider());
	}

	public List<AbstractBindingInfo> getBindings0() {
		return m_rootInfo.getContextInfo().getBindings();
	}

	@Override
	public List<IBindingInfo> getBindings() {
		return CoreUtils.cast(getBindings0());
	}

	@Override
	public String getBindingPresentationText(final IBindingInfo binding) throws Exception {
		return null;
	}

	@Override
	public void gotoDefinition(IBindingInfo ibinding) {
		try {
			AbstractBindingInfo binding = (AbstractBindingInfo) ibinding;
			String source = binding.getDefinitionSource(this);
			//
			if (source != null) {
				int position = m_javaInfoRoot.getEditor().getEnclosingNode(source).getStartPosition();
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
		return observePropertyFilters();
	}

	public static List<PropertyFilter> observePropertyFilters() {
		if (m_filters == null) {
			m_filters = Lists.newArrayList();
			// advanced
			m_filters.add(new AdvancedPropertyFilter());
			// any type
			m_filters.add(new AllPropertiesFilter(Messages.DatabindingsProvider_filterAllTypes,
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
			// SWT Color
			m_filters.add(new TypesPropertyFilter("Color", TypeImageProvider.COLOR_IMAGE, Color.class));
			// SWT Font
			m_filters.add(new TypesPropertyFilter("Font", TypeImageProvider.FONT_IMAGE, Font.class));
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
		if (m_synchronizeObserves) {
			for (ObserveTypeContainer container : m_containers) {
				container.synchronizeObserves(m_javaInfoRoot, m_astEditor, m_rootNode);
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// UI editing
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public List<IUiContentProvider> getContentProviders(IBindingInfo ibinding, IPageListener listener)
			throws Exception {
		AbstractBindingInfo binding = (AbstractBindingInfo) ibinding;
		List<IUiContentProvider> providers = Lists.newArrayList();
		binding.createContentProviders(providers, listener, this);
		return providers;
	}

	@Override
	public void fillExternalBindingActions(ToolBar toolBar, Menu contextMenu) {
		// TODO remove this code?
		if ("".length() > 0 && !m_controller) {
			SelectionListener listener = new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					ControllerSupport.convertToController(
							DatabindingsProvider.this,
							m_javaInfoRoot,
							m_astEditor,
							m_rootNode);
				}
			};
			// separator
			new ToolItem(toolBar, SWT.SEPARATOR);
			// convert to controller
			ToolItem convertToControllerToolItem = new ToolItem(toolBar, SWT.NONE);
			convertToControllerToolItem.setToolTipText(Messages.DatabindingsProvider_convertToController);
			convertToControllerToolItem.setImage(Activator.getImage("newjprj_wiz.gif"));
			convertToControllerToolItem.addSelectionListener(listener);
			// separator
			new MenuItem(contextMenu, SWT.SEPARATOR);
			// convert to controller
			MenuItem convertToControllerMenuItem = new MenuItem(contextMenu, SWT.NONE);
			convertToControllerMenuItem.setText(Messages.DatabindingsProvider_convertToController);
			convertToControllerMenuItem.setImage(Activator.getImage("newjprj_wiz.gif"));
			convertToControllerMenuItem.addSelectionListener(listener);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Validate
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean validate(IObserveInfo target,
			IObserveInfo targetProperty,
			IObserveInfo model,
			IObserveInfo modelProperty) throws Exception {
		return doValidate(getBindings0(), target, targetProperty, model, modelProperty);
	}

	public static boolean doValidate(List<AbstractBindingInfo> bindings,
			IObserveInfo target,
			IObserveInfo targetProperty,
			IObserveInfo model,
			IObserveInfo modelProperty) throws Exception {
		// ignore itself
		if (target == model && targetProperty == modelProperty) {
			return false;
		}
		// prepare binable's
		BindableInfo targetBindable = (BindableInfo) target;
		BindableInfo targetPropertyBindable = (BindableInfo) targetProperty;
		IObservableFactory targetFactory = targetPropertyBindable.getObservableFactory();
		BindableInfo modelBindable = (BindableInfo) model;
		BindableInfo modelPropertyBindable = (BindableInfo) modelProperty;
		IObservableFactory modelFactory = modelPropertyBindable.getObservableFactory();
		// ignore null factories
		if (targetFactory == null || modelFactory == null) {
			return false;
		}
		//
		Type targetType = targetFactory.getType();
		Type modelType = modelFactory.getType();
		//
		if (targetType == Type.Input
				|| targetType == Type.InputCollection
				|| modelType == Type.Input
				|| modelType == Type.InputCollection) {
			// ignore no collection type for input
			if (targetType == modelType) {
				return false;
			}
			if (targetType == Type.InputCollection) {
				if (modelType != Type.Input) {
					return false;
				}
			} else if (modelType == Type.InputCollection) {
				if (targetType != Type.Input) {
					return false;
				}
			} else if (targetType == Type.Input) {
				if (!validateInput(modelType)) {
					return false;
				}
			} else if (!validateInput(targetType)) {
				return false;
			}
			if (targetType == Type.Input) {
				if (findBinding(bindings, target, targetProperty)) {
					return false;
				}
			} else if (findBinding(bindings, model, modelProperty)) {
				return false;
			}
		} else if (targetType == modelType) {
			if (modelType == Type.Detail) {
				return false;
			}
		} else {
			if (targetType == Type.OnlyList) {
				if (!validateOnlyList(modelType)) {
					return false;
				}
			} else if (modelType == Type.OnlyList) {
				if (!validateOnlyList(targetType)) {
					return false;
				}
			} else if (targetType == Type.OnlySet) {
				if (!validateOnlySet(modelType)) {
					return false;
				}
			} else if (modelType == Type.OnlySet) {
				if (!validateOnlySet(targetType)) {
					return false;
				}
			} else if (targetType == Type.OnlyValue) {
				if (!validateOnlyValue(modelType)) {
					return false;
				}
			} else if (modelType == Type.OnlyValue) {
				if (!validateOnlyValue(targetType)) {
					return false;
				}
			} else if (targetType == Type.List) {
				if (!validateList(modelType)) {
					return false;
				}
			} else if (modelType == Type.List) {
				if (!validateList(targetType)) {
					return false;
				}
			} else if (targetType == Type.Set) {
				if (!validateSet(modelType)) {
					return false;
				}
			} else if (modelType == Type.Set) {
				if (!validateSet(targetType)) {
					return false;
				}
			} else if (targetType == Type.Any) {
				if (!validateAny(modelType)) {
					return false;
				}
			} else if (modelType == Type.Any) {
				if (!validateAny(targetType)) {
					return false;
				}
			} else if (targetType == Type.Detail) {
				if (!validateDetail(modelType)) {
					return false;
				}
			} else if (modelType == Type.Detail) {
				if (!validateDetail(targetType)) {
					return false;
				}
			}
		}
		// Sub-properties not allowed for sets, lists, maps.
		// see http://www.eclipse.org/forums/index.php/t/262915/
		if (modelType == Type.List
				|| modelType == Type.OnlyList
				|| modelType == Type.Set
				|| modelType == Type.OnlySet) {
			if (modelProperty.getParent() != null) {
				return false;
			}
		}
		// ignore itself
		return !targetBindable.getReference().equals(modelBindable.getReference())
				|| !targetPropertyBindable.getReference().equals(modelPropertyBindable.getReference());
	}

	private static boolean findBinding(List<AbstractBindingInfo> bindings,
			IObserveInfo object,
			IObserveInfo property) {
		for (AbstractBindingInfo binding : bindings) {
			if (binding.getTarget() == object && binding.getTargetProperty() == property) {
				return true;
			}
			if (binding.getModel() == object && binding.getModelProperty() == property) {
				return true;
			}
		}
		return false;
	}

	private static boolean validateInput(Type type) throws Exception {
		return type == Type.InputCollection
				|| type == Type.OnlyList
				|| type == Type.OnlySet
				|| type == Type.List
				|| type == Type.Set
				|| type == Type.Detail;
	}

	private static boolean validateOnlyList(Type type) {
		return type == Type.List || type == Type.Detail;
	}

	private static boolean validateOnlySet(Type type) {
		return type == Type.Set || type == Type.Detail;
	}

	private static boolean validateOnlyValue(Type type) {
		return type == Type.List || type == Type.Set || type == Type.Any || type == Type.Detail;
	}

	private static boolean validateList(Type type) {
		return type == Type.OnlyList
				|| type == Type.OnlyValue
				|| type == Type.Any
				|| type == Type.Detail;
	}

	private static boolean validateSet(Type type) {
		return type == Type.OnlySet
				|| type == Type.OnlyValue
				|| type == Type.Any
				|| type == Type.Detail;
	}

	private static boolean validateAny(Type type) {
		return type == Type.OnlyValue || type == Type.List || type == Type.Set || type == Type.Detail;
	}

	private static boolean validateDetail(Type type) {
		return type == Type.OnlyList
				|| type == Type.OnlySet
				|| type == Type.OnlyValue
				|| type == Type.List
				|| type == Type.Set
				|| type == Type.Any;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Creation/Editing
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public IBindingInfo createBinding(IObserveInfo target,
			IObserveInfo targetProperty,
			IObserveInfo model,
			IObserveInfo modelProperty) throws Exception {
		// prepare target
		BindableInfo targetBindable = (BindableInfo) target;
		BindableInfo targetBindableProperty = (BindableInfo) targetProperty;
		IObservableFactory targetFactory = targetBindableProperty.getObservableFactory();
		// prepare model
		BindableInfo modelBindable = (BindableInfo) model;
		BindableInfo modelBindableProperty = (BindableInfo) modelProperty;
		IObservableFactory modelFactory = modelBindableProperty.getObservableFactory();
		//
		// calculate type
		Type type = calculateObserveType(targetFactory, modelFactory);
		// handle input type
		if (type == Type.Input) {
			return AbstractViewerInputBindingInfo.createBinding(
					targetBindable,
					targetBindableProperty,
					modelBindable,
					modelBindableProperty,
					this);
		}
		// create observable's
		ObservableInfo targetObservable =
				createObservable(targetFactory, targetBindable, targetBindableProperty, type);
		ObservableInfo modelObservable =
				createObservable(modelFactory, modelBindable, modelBindableProperty, type);
		// create binding
		switch (type) {
		case OnlyValue :
			return new ValueBindingInfo(targetObservable, modelObservable);
		case OnlyList :
			return new ListBindingInfo(targetObservable, modelObservable);
		case OnlySet :
			return new SetBindingInfo(targetObservable, modelObservable);
		}
		//
		Assert.fail("");
		return null;
	}

	public ObservableInfo createObservable(IObservableFactory factory,
			BindableInfo bindable,
			BindableInfo property,
			Type type) throws Exception {
		boolean version_1_3 =
				Activator.getStore().getBoolean(IPreferenceConstants.GENERATE_CODE_FOR_VERSION_1_3);
		ObservableInfo newObservable = factory.createObservable(bindable, property, type, version_1_3);
		//
		for (IBindingInfo ibinding : getBindings()) {
			if (ibinding instanceof BindingInfo) {
				BindingInfo binding = (BindingInfo) ibinding;
				// check target
				ObservableInfo targetObservable = binding.getTargetObservable();
				if (targetObservable.canShared()
						&& binding.getTarget() == bindable
						&& binding.getTargetProperty() == property
						&& targetObservable.getClass() == newObservable.getClass()) {
					return targetObservable;
				}
				// check model
				ObservableInfo modelObservable = binding.getModelObservable();
				if (modelObservable.canShared()
						&& binding.getModel() == bindable
						&& binding.getModelProperty() == property
						&& modelObservable.getClass() == newObservable.getClass()) {
					return modelObservable;
				}
			} else {
				AbstractViewerInputBindingInfo binding = (AbstractViewerInputBindingInfo) ibinding;
				// check viewer input
				ObservableInfo inputObservable = binding.getInputObservable();
				if (inputObservable.canShared()
						&& binding.getModel() == bindable
						&& binding.getModelProperty() == property
						&& inputObservable.getClass() == newObservable.getClass()) {
					return inputObservable;
				}
			}
		}
		return newObservable;
	}

	public static Type calculateObserveType(IObservableFactory targetFactory,
			IObservableFactory modelFactory) throws Exception {
		Type targetType = targetFactory.getType();
		Type modelType = modelFactory.getType();
		// input
		if (targetType == Type.Input || modelType == Type.Input) {
			return Type.Input;
		}
		// equal
		if (targetType == modelType) {
			switch (targetType) {
			case List :
				return Type.OnlyList;
			case Set :
				return Type.OnlySet;
			case Any :
				return Type.OnlyValue;
			}
			return targetType;
		}
		// Value
		if (targetType == Type.OnlyValue
				|| targetType == Type.Any
				|| modelType == Type.OnlyValue
				|| modelType == Type.Any) {
			return Type.OnlyValue;
		}
		// List
		if (targetType == Type.OnlyList
				|| targetType == Type.List
				|| modelType == Type.OnlyList
				|| modelType == Type.List) {
			return Type.OnlyList;
		}
		// Set
		if (targetType == Type.OnlySet
				|| targetType == Type.Set
				|| modelType == Type.OnlySet
				|| modelType == Type.Set) {
			return Type.OnlySet;
		}
		// default
		return Type.OnlyValue;
	}

	@Override
	public void addBinding(final IBindingInfo ibinding) {
		// post process
		ExecutionUtils.runLog(new RunnableEx() {
			@Override
			public void run() throws Exception {
				AbstractBindingInfo binding = (AbstractBindingInfo) ibinding;
				binding.create();
			}
		});
		// add
		getBindings().add(ibinding);
		// save
		saveEdit();
	}

	@Override
	public void editBinding(IBindingInfo binding) {
		saveEdit();
	}

	@Override
	public void deleteBinding(final IBindingInfo ibinding) {
		// delete
		ExecutionUtils.runLog(new RunnableEx() {
			@Override
			public void run() throws Exception {
				AbstractBindingInfo binding = (AbstractBindingInfo) ibinding;
				binding.delete();
			}
		});
		getBindings().remove(ibinding);
		// save
		saveEdit();
	}

	@Override
	public void deleteAllBindings() {
		// delete all
		ExecutionUtils.runLog(new RunnableEx() {
			@Override
			public void run() throws Exception {
				for (AbstractBindingInfo binding : getBindings0()) {
					binding.delete();
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
		return true;
	}

	@Override
	public void moveBinding(IBindingInfo binding, int sourceIndex, int targetIndex, boolean upDown) {
		// do reorder
		List<IBindingInfo> bindings = getBindings();
		bindings.remove(sourceIndex);
		bindings.add(targetIndex, binding);
		// save
		saveEdit();
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
		saveEdit(false);
	}

	public void saveEdit(final boolean needReparse) {
		final boolean[] reparse = new boolean[1];
		ExecutionUtils.run(m_javaInfoRoot, new RunnableEx() {
			@Override
			public void run() throws Exception {
				reparse[0] =
						m_rootInfo.commit(m_astEditor, m_rootNode, m_javaInfoRoot, m_containers, m_controller);
				// check reparse
				if (reparse[0] || needReparse) {
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