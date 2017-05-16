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
package org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders;

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.databinding.Messages;
import org.eclipse.wb.internal.core.databinding.ui.UiUtils;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;

import java.util.Collections;
import java.util.List;

/**
 * {@link ChooseClassUiContentProvider} with {@link CheckboxTableViewer} for represented properties
 * for chosen classes.
 *
 * @author lobas_av
 * @coverage bindings.ui
 */
public abstract class ChooseClassAndPropertiesUiContentProvider
    extends
      ChooseClassUiContentProvider {
  protected final ChooseClassAndPropertiesConfiguration m_configuration;
  //
  private ICheckboxViewerWrapper m_propertiesViewer;
  protected Label m_propertiesLabel;
  private Button m_selectAllButton;
  private Button m_deselectAllButton;
  private Button m_upButton;
  private Button m_downButton;
  //
  private List<PropertyAdapter> m_properties = Collections.emptyList();
  protected Class<?> m_choosenClass;
  private PropertyAdapter m_dragObject;
  private PropertyAdapter m_defaultProperty;
  //
  private ChooseClassAndPropertiesSupportListener m_supportListener;
  private ChooseClassAndPropertiesRouter m_router;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ChooseClassAndPropertiesUiContentProvider(ChooseClassAndPropertiesConfiguration configuration) {
    super(configuration);
    m_configuration = configuration;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Access to {@link ICheckboxViewerWrapper} properties viewer.
   */
  public final ICheckboxViewerWrapper getPropertiesViewer() {
    return m_propertiesViewer;
  }

  /**
   * Defines the listener for this content provider.
   */
  public final void setSupportListener(ChooseClassAndPropertiesSupportListener supportListener) {
    m_supportListener = supportListener;
  }

  final void setRouter(ChooseClassAndPropertiesRouter router) {
    m_router = router;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  protected void super_createContent(Composite parent, int columns) {
    super.createContent(parent, columns);
  }

  @Override
  public void createContent(Composite parent, int columns) {
    // create super content
    super_createContent(parent, columns);
    // create properties title
    m_propertiesLabel = new Label(parent, SWT.NONE);
    String propertiesLabelText = m_configuration.getPropertiesLabel();
    if (propertiesLabelText != null) {
      m_propertiesLabel.setText(propertiesLabelText);
    }
    // create properties viewer
    Control viewers = createViewers(parent);
    m_propertiesViewer.getCheckable().addCheckStateListener(new ICheckStateListener() {
      public void checkStateChanged(CheckStateChangedEvent event) {
        if (!m_configuration.isPropertiesMultiChecked() && event.getChecked()) {
          m_propertiesViewer.setCheckedElements(new Object[]{event.getElement()});
        }
        calculatePropertiesFinish();
      }
    });
    GridDataFactory.create(viewers).fill().grab().spanH(columns - 1).minVC(5);
    // check reorder mode
    if (m_configuration.isReorderMode()) {
      GridDataFactory.modify(viewers).spanV(3);
      configureDND();
      m_propertiesViewer.getViewer().addPostSelectionChangedListener(
          new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
              calculateUpDowButtons();
            }
          });
      // grid filler
      new Label(parent, SWT.NONE);
      // buttons
      Composite buttonsComposite = new Composite(parent, SWT.NONE);
      GridLayoutFactory.create(buttonsComposite).marginsH(0).spacingH(0);
      GridDataFactory.create(buttonsComposite).alignHR().alignVM();
      //
      if (m_configuration.isPropertiesMultiChecked() && m_configuration.isShowSelectionButtons()) {
        // select all button
        m_selectAllButton = new Button(buttonsComposite, SWT.NONE);
        GridDataFactory.create(m_selectAllButton).fillH();
        m_selectAllButton.setText(Messages.ChooseClassAndPropertiesUiContentProvider_selectAllButton);
        m_selectAllButton.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            handleAllSelection(true);
          }
        });
        // deselect all button
        m_deselectAllButton = new Button(buttonsComposite, SWT.NONE);
        GridDataFactory.create(m_deselectAllButton).fillH();
        m_deselectAllButton.setText(Messages.ChooseClassAndPropertiesUiContentProvider_deselectAllButton);
        m_deselectAllButton.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            handleAllSelection(false);
          }
        });
      }
      // move up button
      m_upButton = new Button(buttonsComposite, SWT.NONE);
      GridDataFactory.create(m_upButton).fillH();
      m_upButton.setText(Messages.ChooseClassAndPropertiesUiContentProvider_upButton);
      m_upButton.setEnabled(false);
      m_upButton.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          handleMoveElement(true);
        }
      });
      // move down button
      m_downButton = new Button(buttonsComposite, SWT.NONE);
      GridDataFactory.create(m_downButton).fillH();
      m_downButton.setText(Messages.ChooseClassAndPropertiesUiContentProvider_downButton);
      m_downButton.setEnabled(false);
      m_downButton.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          handleMoveElement(false);
        }
      });
    }
  }

  protected Control createViewers(Composite parent) {
    setPropertiesViewer(createPropertiesViewer(parent));
    return m_propertiesViewer.getViewer().getControl();
  }

  protected final void setPropertiesViewer(ICheckboxViewerWrapper propertiesViewer) {
    m_propertiesViewer = propertiesViewer;
  }

  protected ICheckboxViewerWrapper createPropertiesViewer(Composite parent) {
    CheckboxTableViewer tableViewer =
        CheckboxTableViewer.newCheckList(parent, SWT.BORDER
            | SWT.FULL_SELECTION
            | SWT.H_SCROLL
            | SWT.V_SCROLL);
    tableViewer.setContentProvider(new ArrayContentProvider());
    tableViewer.setLabelProvider(m_configuration.getPropertiesLabelProvider());
    return new CheckboxTableViewerWrapper(tableViewer);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Handling
  //
  ////////////////////////////////////////////////////////////////////////////
  protected void configureDND() {
    configureDND(m_propertiesViewer.getViewer(), m_properties);
  }

  protected final void configureDND(final StructuredViewer viewer,
      final List<PropertyAdapter> properties) {
    Transfer[] transfers = new Transfer[]{TreeTransfer.INSTANCE};
    // prepare drag
    viewer.addDragSupport(DND.DROP_MOVE, transfers, new DragSourceListener() {
      public void dragStart(DragSourceEvent event) {
        event.doit = properties.size() > 1;
        m_dragObject = (PropertyAdapter) UiUtils.getSelection(viewer).getFirstElement();
      }

      public void dragSetData(DragSourceEvent event) {
      }

      public void dragFinished(DragSourceEvent event) {
      }
    });
    // prepare drop
    viewer.addDropSupport(DND.DROP_MOVE, transfers, new ViewerDropAdapter(viewer) {
      @Override
      protected int determineLocation(DropTargetEvent event) {
        return LOCATION_ON;
      }

      @Override
      public boolean validateDrop(Object target, int operation, TransferData transferType) {
        return m_dragObject != target && target != null;
      }

      @Override
      public boolean performDrop(Object data) {
        // prepare indexes
        int sourceIndex = properties.indexOf(m_dragObject);
        int targetIndex = properties.indexOf(getCurrentTarget());
        // move elements
        properties.remove(sourceIndex);
        properties.add(targetIndex, m_dragObject);
        // update viewer
        viewer.refresh();
        calculateUpDowButtons();
        //
        return true;
      }
    });
  }

  protected void calculateUpDowButtons() {
    calculateUpDowButtons(m_propertiesViewer.getViewer(), m_properties);
  }

  protected final void calculateUpDowButtons(StructuredViewer viewer, List<?> properties) {
    IStructuredSelection selection = UiUtils.getSelection(viewer);
    boolean up = false;
    boolean down = false;
    // prepare state
    if (!UiUtils.isEmpty(selection)) {
      int lastIndex = properties.size() - 1;
      if (lastIndex > 0) {
        int elementIndex = properties.indexOf(selection.getFirstElement());
        up = elementIndex > 0;
        down = elementIndex < lastIndex;
      }
    }
    // set state
    m_upButton.setEnabled(up);
    m_downButton.setEnabled(down);
  }

  protected void handleMoveElement(boolean up) {
    handleMoveElement(up, m_propertiesViewer.getViewer(), m_properties);
  }

  protected final void handleMoveElement(boolean up, StructuredViewer viewer, List<?> properties) {
    // prepare move indexes
    IStructuredSelection selection = UiUtils.getSelection(viewer);
    int elementIndex = properties.indexOf(selection.getFirstElement());
    int newElementIndex = up ? elementIndex - 1 : elementIndex + 1;
    // move elements
    Collections.swap(properties, elementIndex, newElementIndex);
    // update viewer
    viewer.refresh();
    calculateUpDowButtons();
  }

  private void handleAllSelection(boolean selection) {
    if (!m_properties.isEmpty()) {
      m_propertiesViewer.setAllChecked(selection);
      calculatePropertiesFinish();
    }
  }

  @Override
  protected void calculateFinish() {
    String className = getClassName();
    // check state
    if (m_configuration.isDefaultString(className)) {
      // default starts
      m_choosenClass = null;
      m_properties = Lists.newArrayList();
      m_properties.add(m_defaultProperty);
      m_propertiesViewer.getViewer().setInput(m_properties);
      m_propertiesViewer.setCheckedElements(new Object[]{m_defaultProperty});
      setErrorMessage(null);
      // route events
      if (m_router != null) {
        m_router.handle();
      }
    } else {
      // check class state
      super.calculateFinish();
      // check properties
      if (getErrorMessage() == null) {
        try {
          // load properties
          m_choosenClass = loadClass(className);
          m_properties = getProperties(m_choosenClass);
          m_properties = m_configuration.filterProperties(m_choosenClass, m_properties);
          m_propertiesViewer.getViewer().setInput(m_properties);
          // checked properties
          if (!m_properties.isEmpty()) {
            switch (m_configuration.getLoadedPropertiesCheckedStrategy()) {
              case First :
                m_propertiesViewer.setCheckedElements(new Object[]{m_properties.get(0)});
                break;
              case Last :
                m_propertiesViewer.setCheckedElements(new Object[]{m_properties.get(m_properties.size() - 1)});
                break;
              case All :
                m_propertiesViewer.setCheckedElements(m_properties.toArray());
                break;
              case None :
                m_propertiesViewer.setCheckedElements(ArrayUtils.EMPTY_OBJECT_ARRAY);
                break;
            }
          }
          // send loaded event
          if (m_supportListener != null) {
            m_supportListener.loadProperties(true);
          }
        } catch (Throwable e) {
          setEmptyProperties();
        } finally {
          calculatePropertiesFinish();
        }
      } else {
        // error load class
        setEmptyProperties();
        if (m_router != null) {
          m_router.handle();
        }
      }
    }
  }

  private void setEmptyProperties() {
    m_choosenClass = null;
    m_properties = Collections.emptyList();
    m_propertiesViewer.getViewer().setInput(m_properties);
    m_propertiesViewer.setCheckedElements(ArrayUtils.EMPTY_OBJECT_ARRAY);
    // send loaded event
    if (m_supportListener != null) {
      m_supportListener.loadProperties(false);
    }
  }

  protected final void calculatePropertiesFinish() {
    // prepare checked elements
    Object[] checkedElements = m_propertiesViewer.getCheckedElements();
    // check properties state
    if (checkedElements.length == 0) {
      setErrorMessage(m_configuration.getPropertiesErrorMessage());
    } else {
      setErrorMessage(null);
    }
    // route events
    if (m_router != null) {
      m_router.handle();
    }
  }

  /**
   * Load properties for given {@link Class}.
   *
   * @see PropertyAdapter
   */
  protected abstract List<PropertyAdapter> getProperties(Class<?> choosenClass) throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Value
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Class<?> getChoosenClass() throws Exception {
    return m_choosenClass;
  }

  /**
   * Select given class name and property. If <code>className</code> is't class (maybe special
   * default value) sets <code>loadClass</code> as <code>false</code>.
   */
  protected final void setClassNameAndProperty(String className,
      PropertyAdapter property,
      boolean loadClass) {
    if (!loadClass) {
      m_defaultProperty = property;
    }
    //
    setClassName(className);
    //
    if (loadClass) {
      if (property != null) {
        m_propertiesViewer.getCheckable().setChecked(property, true);
      }
      calculatePropertiesFinish();
    }
  }

  /**
   * Select given class name and properties.
   */
  protected final void setClassNameAndProperties(String className, List<PropertyAdapter> properties) {
    setClassName(className);
    for (PropertyAdapter property : properties) {
      m_propertiesViewer.getCheckable().setChecked(property, true);
    }
    calculatePropertiesFinish();
  }

  List<PropertyAdapter> getChoosenProperties0() {
    List<PropertyAdapter> properties = Lists.newArrayList();
    CollectionUtils.addAll(properties, m_propertiesViewer.getCheckedElements());
    return properties;
  }

  protected List<PropertyAdapter> getChoosenProperties() {
    return getChoosenProperties0();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Update
  //
  ////////////////////////////////////////////////////////////////////////////
  public final void saveToObject() throws Exception {
    saveToObject(m_choosenClass, getChoosenProperties());
  }

  /**
   * Invoke for save changes class and properties.
   */
  protected abstract void saveToObject(Class<?> choosenClass,
      List<PropertyAdapter> choosenProperties) throws Exception;
}