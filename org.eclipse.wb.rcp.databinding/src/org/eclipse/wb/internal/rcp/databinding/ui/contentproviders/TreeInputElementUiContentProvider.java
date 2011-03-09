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
package org.eclipse.wb.internal.rcp.databinding.ui.contentproviders;

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.databinding.ui.editor.ICompleteListener;
import org.eclipse.wb.internal.core.databinding.ui.editor.IPageListener;
import org.eclipse.wb.internal.core.databinding.ui.editor.IUiContentProvider;
import org.eclipse.wb.internal.core.databinding.ui.editor.PageListenerWrapper;
import org.eclipse.wb.internal.core.databinding.ui.editor.UiContentProviderComposite;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassAndPropertiesConfiguration;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassConfiguration;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassRouter;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassUiContentProvider;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.PropertyAdapter;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.TabFactory;
import org.eclipse.wb.internal.rcp.databinding.Activator;
import org.eclipse.wb.internal.rcp.databinding.DatabindingsProvider;
import org.eclipse.wb.internal.rcp.databinding.model.GlobalFactoryHelper;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.SimpleClassObjectInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.bindables.BeanBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.bindables.BeanPropertyBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.ListBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.MapsBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.SetBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.properties.ListPropertyCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.properties.SetPropertyCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.standard.BeanObservableListCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.standard.BeanObservableSetCodeSupport;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.AbstractFactoryInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.AbstractLabelProviderInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.BeanFieldInputObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.BeansObservableListFactoryInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.BeansObservableSetFactoryInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.KnownElementsObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.LabelProviderInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.ObservableCollectionTreeContentProviderInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.ObservableFactoryInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.ObservableListTreeContentProviderInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.ObservableMapLabelProviderInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.TreeStructureAdvisorInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.TreeViewerInputBindingInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.designer.BeansListObservableFactoryInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.designer.BeansObservableFactoryInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.designer.BeansSetObservableFactoryInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.designer.TreeBeanAdvisorInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.designer.TreeObservableLabelProviderInfo;
import org.eclipse.wb.internal.rcp.databinding.preferences.IPreferenceConstants;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * Content provider for edit (tree viewer input, content and label providers)
 * {@link TreeViewerInputBindingInfo}.
 * 
 * @author lobas_av
 * @coverage bindings.rcp.ui
 */
public final class TreeInputElementUiContentProvider implements IUiContentProvider {
  private static final Image CHECK_IMAGE = Activator.getImage("tab_selection_true.gif");
  private static final Image UNCHECK_IMAGE = Activator.getImage("tab_selection_false.gif");
  //
  private final TreeViewerInputBindingInfo m_binding;
  private final IPageListener m_pageListener;
  private final DatabindingsProvider m_provider;
  private final List<SimpleClassObjectInfo> m_defaultObjects = Lists.newArrayList();
  private ICompleteListener m_completeListener;
  private TabFolder m_tabFolder;
  private final ChooseClassUiContentProvider m_elementTypeDesignerUIProvider;
  private final JFaceElementTypeUiProvider m_elementTypeJFaceUIProvider;
  private final ChooseClassUiContentProvider[] m_elementTypeUIProviders;
  private TreeContentLabelProvidersUiContentProvider m_contentLabelProvidersEditor;
  private ChooseClassUiContentProvider m_labelProviderJFaceEditor;
  private UiContentProviderComposite m_designerComposite;
  private UiContentProviderComposite m_jfaceComposite;
  private PageListenerWrapper m_designerPageListener;
  private PageListenerWrapper m_jfacePageListener;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TreeInputElementUiContentProvider(TreeViewerInputBindingInfo binding,
      IPageListener listener,
      DatabindingsProvider provider) throws Exception {
    m_binding = binding;
    m_pageListener = listener;
    m_provider = provider;
    //
    m_elementTypeDesignerUIProvider = createDesignerElementTypeUIProvider();
    m_elementTypeJFaceUIProvider = createJFaceElementTypeUIProvider();
    //
    m_elementTypeUIProviders =
        new ChooseClassUiContentProvider[]{
            m_elementTypeDesignerUIProvider,
            m_elementTypeJFaceUIProvider};
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public ChooseClassUiContentProvider[] getElementTypeUIProviders() {
    return m_elementTypeUIProviders;
  }

  public Class<?> getCurrentElementType() throws Exception {
    return isDesignerMode()
        ? m_elementTypeDesignerUIProvider.getChoosenClass()
        : m_elementTypeJFaceUIProvider.getChoosenClass();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Complete
  //
  ////////////////////////////////////////////////////////////////////////////
  public void setCompleteListener(ICompleteListener listener) {
    m_completeListener = listener;
  }

  public String getErrorMessage() {
    if (isDesignerMode()) {
      return m_designerPageListener.getErrorMessage();
    }
    return m_jfacePageListener.getErrorMessage();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  public int getNumberOfControls() {
    return 1;
  }

  public void createContent(Composite parent, int columns) {
    // create folder
    m_tabFolder = new TabFolder(parent, SWT.NONE);
    GridDataFactory.create(m_tabFolder).fill().grab().spanH(columns);
    // create designer page
    m_designerPageListener = new PageListenerWrapper(m_pageListener, m_completeListener);
    //
    m_designerComposite =
        new UiContentProviderComposite(m_designerPageListener,
            createDesignerProviders(),
            m_tabFolder,
            SWT.NONE);
    TabFactory.item(m_tabFolder).text("Designer support").image(CHECK_IMAGE).control(
        m_designerComposite);
    // create jface page
    m_jfacePageListener = new PageListenerWrapper(m_pageListener, m_completeListener);
    //
    m_jfaceComposite =
        new UiContentProviderComposite(m_jfacePageListener,
            createJFaceProviders(),
            m_tabFolder,
            SWT.NONE);
    TabFactory.item(m_tabFolder).text("JFace support").image(UNCHECK_IMAGE).control(
        m_jfaceComposite);
    //
    m_tabFolder.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        handleTabSelection();
        calculateFinish();
      }
    });
  }

  /**
   * Fill Designer page.
   */
  private List<IUiContentProvider> createDesignerProviders() {
    List<IUiContentProvider> providers = Lists.newArrayList();
    providers.add(m_elementTypeDesignerUIProvider);
    //
    if (m_binding.isDesignerMode()) {
      // add life objects
      createDefaultProviders(providers, true);
    } else {
      // create fake objects
      ObservableCollectionTreeContentProviderInfo contentProvider = m_binding.getContentProvider();
      // create factory
      boolean asList = contentProvider instanceof ObservableListTreeContentProviderInfo;
      BeansObservableFactoryInfo factory = null;
      try {
        factory =
            GlobalFactoryHelper.createTreeObservableFactory(m_binding.getInputObservable(), asList);
      } catch (Throwable e) {
        DesignerPlugin.log(e);
      }
      if (factory == null) {
        factory =
            asList ? new BeansListObservableFactoryInfo() : new BeansSetObservableFactoryInfo();
      }
      //
      m_defaultObjects.add(factory);
      factory.createContentProviders(providers, m_provider);
      // create advisor
      TreeBeanAdvisorInfo advisor = null;
      try {
        advisor = GlobalFactoryHelper.createTreeBeanAdvisor(m_binding.getInputObservable());
      } catch (Throwable e) {
        DesignerPlugin.log(e);
      }
      if (advisor == null) {
        advisor = new TreeBeanAdvisorInfo();
      }
      m_defaultObjects.add(advisor);
      advisor.createContentProviders(providers, m_provider);
      // content provider always exist
      contentProvider.createContentProviders(providers, m_provider, true);
      // create label provider
      KnownElementsObservableInfo allElementsObservable =
          new KnownElementsObservableInfo(contentProvider);
      TreeObservableLabelProviderInfo labelProvider = null;
      try {
        labelProvider =
            GlobalFactoryHelper.createTreeLabelProvider(
                m_binding.getInputObservable(),
                allElementsObservable);
      } catch (Throwable e) {
        DesignerPlugin.log(e);
      }
      if (labelProvider == null) {
        labelProvider = new TreeObservableLabelProviderInfo(allElementsObservable);
      }
      m_defaultObjects.add(labelProvider);
      labelProvider.createContentProviders(providers, m_provider, true);
    }
    // content provider for properties: parent/children/has children/text/image
    m_contentLabelProvidersEditor = new TreeContentLabelProvidersUiContentProvider(m_binding);
    providers.add(m_contentLabelProvidersEditor);
    return providers;
  }

  /**
   * Fill JFace page.
   */
  private List<IUiContentProvider> createJFaceProviders() {
    List<IUiContentProvider> providers = Lists.newArrayList();
    //
    if (m_binding.isDesignerMode()) {
      // create fake objects
      // create factory
      ObservableFactoryInfo factory = new ObservableFactoryInfo("");
      m_defaultObjects.add(factory);
      factory.createContentProviders(providers, m_provider);
      // create advisor
      TreeStructureAdvisorInfo advisor = new TreeStructureAdvisorInfo("");
      m_defaultObjects.add(advisor);
      advisor.createContentProviders(providers, m_provider);
      // content provider always exist
      m_binding.getContentProvider().createContentProviders(providers, m_provider, false);
      // create label provider
      LabelProviderInfo labelProvider = new LabelProviderInfo("");
      m_defaultObjects.add(labelProvider);
      labelProvider.createContentProviders(providers, m_provider, false);
    } else {
      // add life objects
      createDefaultProviders(providers, false);
    }
    //
    final ObservableInfo inputObservable = m_binding.getInputObservable();
    if (inputObservable instanceof ListBeanObservableInfo
        || inputObservable instanceof SetBeanObservableInfo
        || inputObservable instanceof BeanFieldInputObservableInfo) {
      final ChooseClassUiContentProvider contentProviderJFaceEditor =
          (ChooseClassUiContentProvider) providers.get(0);
      //
      try {
        String factoryValue =
            inputObservable.getBindableProperty().getReference()
                + ", "
                + ClassUtils.getShortClassName(inputObservable.getBindableObject().getObjectType())
                + ".class)";
        //
        if (inputObservable instanceof ListBeanObservableInfo) {
          factoryValue = "listFactory(" + factoryValue;
          contentProviderJFaceEditor.getConfiguration().addDefaultStart(factoryValue);
          contentProviderJFaceEditor.addClassToCombo(factoryValue);
        } else if (inputObservable instanceof SetBeanObservableInfo) {
          factoryValue = "setFactory(" + factoryValue;
          contentProviderJFaceEditor.getConfiguration().addDefaultStart(factoryValue);
          contentProviderJFaceEditor.addClassToCombo(factoryValue);
        }
      } catch (Throwable e) {
      }
      //
      new ChooseClassRouter(contentProviderJFaceEditor, new Runnable() {
        public void run() {
          String className = contentProviderJFaceEditor.getClassName();
          if (className.startsWith("listFactory") || className.startsWith("setFactory")) {
            m_elementTypeJFaceUIProvider.setClassName(CoreUtils.getClassName(inputObservable.getBindableObject().getObjectType()));
          }
        }
      });
    }
    //
    m_labelProviderJFaceEditor = (ChooseClassUiContentProvider) providers.get(providers.size() - 1);
    new ChooseClassRouter(m_labelProviderJFaceEditor, new Runnable() {
      public void run() {
        m_elementTypeJFaceUIProvider.calculateFinish();
      }
    });
    //
    providers.add(m_elementTypeJFaceUIProvider);
    return providers;
  }

  private void createDefaultProviders(List<IUiContentProvider> providers, boolean useClear) {
    ObservableCollectionTreeContentProviderInfo contentProvider = m_binding.getContentProvider();
    // factory
    contentProvider.getFactoryInfo().createContentProviders(providers, m_provider);
    // advisor
    contentProvider.getAdvisorInfo().createContentProviders(providers, m_provider);
    // content provider
    contentProvider.createContentProviders(providers, m_provider, useClear);
    // label provider
    m_binding.getLabelProvider().createContentProviders(providers, m_provider, useClear);
  }

  private ChooseClassUiContentProvider createDesignerElementTypeUIProvider() throws Exception {
    ChooseClassConfiguration configuration = new ChooseClassConfiguration();
    configuration.setDialogFieldLabel("Element bean class:");
    configuration.setValueScope("beans");
    configuration.setChooseInterfaces(true);
    configuration.setEmptyClassErrorMessage("Choose a element bean class that contains properties.");
    configuration.setErrorMessagePrefix("Element bean class");
    //
    GlobalFactoryHelper.configureChooseElementForTreeViewerInput(
        m_binding.getInputObservable(),
        configuration);
    //
    return new ChooseClassUiContentProvider(configuration) {
      public void updateFromObject() throws Exception {
        Class<?> elementType = m_binding.getElementType();
        if (elementType != null) {
          setClassName(CoreUtils.getClassName(elementType));
        } else {
          calculateFinish();
        }
      }

      @Override
      protected void calculateFinish() {
        super.calculateFinish();
        if (getErrorMessage() == null) {
          // route choose class events to properties editor
          ExecutionUtils.runLog(new RunnableEx() {
            public void run() throws Exception {
              m_contentLabelProvidersEditor.setElementType(getChoosenClass());
            }
          });
        }
      }

      public void saveToObject() throws Exception {
        // store element type
        Class<?> elementType = getChoosenClass();
        //
        ObservableCollectionTreeContentProviderInfo contentProvider =
            m_binding.getContentProvider();
        // to factory
        BeansObservableFactoryInfo factory =
            (BeansObservableFactoryInfo) contentProvider.getFactoryInfo();
        factory.setElementType(elementType);
        // to advisor
        TreeBeanAdvisorInfo advisor = (TreeBeanAdvisorInfo) contentProvider.getAdvisorInfo();
        advisor.setElementType(elementType);
        // to label provider
        TreeObservableLabelProviderInfo labelProvider =
            (TreeObservableLabelProviderInfo) m_binding.getLabelProvider();
        labelProvider.setElementType(elementType);
        // to input
        InputElementUiContentProvider.setElementTypeToInput(m_binding, elementType);
      }
    };
  }

  private JFaceElementTypeUiProvider createJFaceElementTypeUIProvider() {
    ChooseClassAndPropertiesConfiguration configuration =
        new ChooseClassAndPropertiesConfiguration();
    configuration.setDialogFieldLabel("Element bean class:");
    configuration.setValueScope("beans");
    configuration.setChooseInterfaces(true);
    configuration.setEmptyClassErrorMessage("Choose a element bean class that contains properties.");
    configuration.setErrorMessagePrefix("Element bean class");
    configuration.setPropertiesLabel("Property\n(for ObservableMapLabelProvider):");
    configuration.setPropertiesErrorMessage("Choose a property for label provider.");
    configuration.setLoadedPropertiesCheckedStrategy(ChooseClassAndPropertiesConfiguration.LoadedPropertiesCheckedStrategy.None);
    //
    return new JFaceElementTypeUiProvider(configuration);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Handle
  //
  ////////////////////////////////////////////////////////////////////////////
  private boolean isDesignerMode() {
    return m_tabFolder.getSelectionIndex() == 0;
  }

  private void handleTabSelection() {
    int selection = m_tabFolder.getSelectionIndex();
    int count = m_tabFolder.getItemCount();
    for (int i = 0; i < count; i++) {
      TabItem item = m_tabFolder.getItem(i);
      if (i == selection) {
        item.setImage(CHECK_IMAGE);
      } else {
        item.setImage(UNCHECK_IMAGE);
      }
    }
  }

  private void calculateFinish() {
    m_completeListener.calculateFinish();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Update
  //
  ////////////////////////////////////////////////////////////////////////////
  public void updateFromObject() throws Exception {
    m_designerComposite.performInitialize();
    m_jfaceComposite.performInitialize();
    if (!m_binding.isDesignerMode()) {
      m_tabFolder.setSelection(1);
      handleTabSelection();
    }
    calculateFinish();
  }

  public void saveToObject() throws Exception {
    // check change mode
    boolean editorMode = isDesignerMode();
    if (m_binding.isDesignerMode() != editorMode) {
      ObservableCollectionTreeContentProviderInfo contentProvider = m_binding.getContentProvider();
      contentProvider.setFactoryInfo((ObservableFactoryInfo) m_defaultObjects.get(0));
      contentProvider.setAdvisorInfo((TreeStructureAdvisorInfo) m_defaultObjects.get(1));
      m_binding.setLabelProvider((LabelProviderInfo) m_defaultObjects.get(2));
    }
    // save edit objects
    if (editorMode) {
      m_designerComposite.performFinish();
    } else {
      m_jfaceComposite.performFinish();
      saveJFaceObjects();
    }
  }

  private void saveJFaceObjects() throws Exception {
    ObservableCollectionTreeContentProviderInfo contentProvider = m_binding.getContentProvider();
    ObservableFactoryInfo factoryInfo = contentProvider.getFactoryInfo();
    if (factoryInfo instanceof AbstractFactoryInfo) {
      AbstractFactoryInfo aFactoryInfo = (AbstractFactoryInfo) factoryInfo;
      //
      if (aFactoryInfo.isCancel()) {
        //
        ObservableFactoryInfo newFactoryInfo =
            new ObservableFactoryInfo(aFactoryInfo.getOriginalClassName());
        newFactoryInfo.setVariableIdentifier(aFactoryInfo.getVariableIdentifier());
        contentProvider.setFactoryInfo(newFactoryInfo);
        //
        boolean version_1_3 =
            Activator.getStore().getBoolean(IPreferenceConstants.GENERATE_CODE_FOR_VERSION_1_3);
        ObservableInfo inputObservable = m_binding.getInputObservable();
        ObservableInfo newInputObservable = null;
        if (contentProvider instanceof ObservableListTreeContentProviderInfo) {
          newInputObservable =
              new ListBeanObservableInfo((BeanBindableInfo) inputObservable.getBindableObject(),
                  (BeanPropertyBindableInfo) inputObservable.getBindableProperty());
          if (version_1_3) {
            newInputObservable.setCodeSupport(new ListPropertyCodeSupport());
          } else {
            newInputObservable.setCodeSupport(new BeanObservableListCodeSupport());
          }
        } else {
          newInputObservable =
              new SetBeanObservableInfo((BeanBindableInfo) inputObservable.getBindableObject(),
                  (BeanPropertyBindableInfo) inputObservable.getBindableProperty());
          if (version_1_3) {
            newInputObservable.setCodeSupport(new SetPropertyCodeSupport());
          } else {
            newInputObservable.setCodeSupport(new BeanObservableSetCodeSupport());
          }
        }
        newInputObservable.setVariableIdentifier(inputObservable.getVariableIdentifier());
        m_binding.setInputObservable(newInputObservable);
      }
    } else if (factoryInfo.getClassName().startsWith("listFactory(")
        || factoryInfo.getClassName().startsWith("setFactory(")) {
      //
      AbstractFactoryInfo newFactoryInfo = null;
      //
      if (contentProvider instanceof ObservableListTreeContentProviderInfo) {
        newFactoryInfo = new BeansObservableListFactoryInfo();
      } else {
        newFactoryInfo = new BeansObservableSetFactoryInfo();
      }
      //
      ObservableInfo inputObservable = m_binding.getInputObservable();
      //
      newFactoryInfo.setPropertyName(inputObservable.getBindableProperty().getReference());
      newFactoryInfo.setElementType(inputObservable.getBindableObject().getObjectType());
      newFactoryInfo.setVariableIdentifier(factoryInfo.getVariableIdentifier());
      contentProvider.setFactoryInfo(newFactoryInfo);
      //
      m_binding.setInputObservable(new BeanFieldInputObservableInfo(inputObservable.getBindableObject(),
          inputObservable.getBindableProperty()));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Classes
  //
  ////////////////////////////////////////////////////////////////////////////
  private class JFaceElementTypeUiProvider extends ChooseClassAndTreePropertiesUiContentProvider2 {
    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public JFaceElementTypeUiProvider(ChooseClassAndPropertiesConfiguration configuration) {
      super(configuration);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Properties
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    protected List<PropertyAdapter> getProperties(Class<?> choosenClass) throws Exception {
      return isObservableMapLabelProvider()
          ? super.getProperties(choosenClass)
          : Collections.<PropertyAdapter>emptyList();
    }

    @Override
    public void calculateFinish() {
      super.calculateFinish();
      try {
        boolean mapLabelProvider = isObservableMapLabelProvider();
        m_propertiesLabel.setEnabled(mapLabelProvider);
        m_propertiesViewer.getViewer().getControl().setEnabled(mapLabelProvider);
        if (!mapLabelProvider) {
          setErrorMessage(null);
        }
      } catch (Throwable e) {
      }
    }

    private boolean isObservableMapLabelProvider() throws Exception {
      if (m_labelProviderJFaceEditor.getErrorMessage() == null) {
        Class<?> labelProviderChoosenClass = m_labelProviderJFaceEditor.getChoosenClass();
        if (loadClass("org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider").isAssignableFrom(
            labelProviderChoosenClass)
            || loadClass("org.eclipse.jface.databinding.viewers.ObservableMapCellLabelProvider").isAssignableFrom(
                labelProviderChoosenClass)) {
          return true;
        }
      }
      return false;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Update
    //
    ////////////////////////////////////////////////////////////////////////////
    public void updateFromObject() throws Exception {
      Class<?> elementType = m_binding.getElementType();
      if (elementType != null) {
        if (!m_binding.isDesignerMode()
            && m_binding.getLabelProvider() instanceof ObservableMapLabelProviderInfo) {
          // prepare initial properties
          ObservableMapLabelProviderInfo labelProviderInfo =
              (ObservableMapLabelProviderInfo) m_binding.getLabelProvider();
          MapsBeanObservableInfo mapsObservable = labelProviderInfo.getMapsObservable();
          String[] properties = mapsObservable.getProperties();
          //
          if (properties == null) {
            setClassName(CoreUtils.getClassName(elementType));
          } else {
            List<String> checkedProperties = Lists.newArrayList();
            for (int i = 0; i < properties.length; i++) {
              checkedProperties.add("\"" + properties[i] + "\"");
            }
            setClassNameAndProperties(elementType, null, checkedProperties);
          }
        } else {
          setClassName(CoreUtils.getClassName(elementType));
        }
      } else {
        calculateFinish();
      }
    }

    @Override
    protected void saveToObject(Class<?> choosenClass, List<PropertyAdapter> choosenProperties)
        throws Exception {
      // store element type
      Class<?> elementType = getChoosenClass();
      // to label provider
      AbstractLabelProviderInfo labelProvider = m_binding.getLabelProvider();
      if (isObservableMapLabelProvider()) {
        ObservableMapLabelProviderInfo mapsLabelProvider;
        if (labelProvider instanceof ObservableMapLabelProviderInfo) {
          mapsLabelProvider = (ObservableMapLabelProviderInfo) labelProvider;
        } else {
          // create content provider accessor
          KnownElementsObservableInfo knownElementsObservable =
              new KnownElementsObservableInfo(m_binding.getContentProvider());
          // create label provider
          MapsBeanObservableInfo observeMaps =
              GlobalFactoryHelper.createObserveMaps(
                  m_binding.getInputObservable(),
                  knownElementsObservable,
                  elementType,
                  new boolean[1]);
          if (observeMaps == null) {
            observeMaps = new MapsBeanObservableInfo(knownElementsObservable, elementType, null);
          }
          //
          mapsLabelProvider =
              new ObservableMapLabelProviderInfo(labelProvider.getClassName(), observeMaps);
          mapsLabelProvider.setVariableIdentifier(labelProvider.getVariableIdentifier());
          m_binding.setLabelProvider(mapsLabelProvider);
        }
        // sets label provider element properties
        Object[] checkedElements = m_propertiesViewer.getCheckedElements();
        String[] properties = new String[checkedElements.length];
        for (int i = 0; i < checkedElements.length; i++) {
          ObservePropertyAdapter adapter = (ObservePropertyAdapter) checkedElements[i];
          properties[i] = StringUtils.remove(adapter.getProperty().getReference(), '"');
        }
        mapsLabelProvider.getMapsObservable().setElementType(choosenClass);
        mapsLabelProvider.getMapsObservable().setProperties(properties);
      } else if (labelProvider instanceof ObservableMapLabelProviderInfo) {
        LabelProviderInfo newLabelProvider = new LabelProviderInfo(labelProvider.getClassName());
        newLabelProvider.setVariableIdentifier(labelProvider.getVariableIdentifier());
        m_binding.setLabelProvider(newLabelProvider);
      }
      // to input
      InputElementUiContentProvider.setElementTypeToInput(m_binding, elementType);
    }
  }
}