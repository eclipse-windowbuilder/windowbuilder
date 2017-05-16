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
package org.eclipse.wb.internal.core.databinding.ui;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.eclipse.wb.internal.core.databinding.Activator;
import org.eclipse.wb.internal.core.databinding.Messages;
import org.eclipse.wb.internal.core.databinding.model.IDatabindingsProvider;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.ui.ObserveType.ExpandedStrategy;
import org.eclipse.wb.internal.core.databinding.ui.filter.PropertyFilter;
import org.eclipse.wb.internal.core.databinding.ui.providers.ObserveLabelProvider;
import org.eclipse.wb.internal.core.databinding.ui.providers.ObserveTreeContentProvider;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;

import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.dialogs.SearchPattern;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Composite for viewers Beans or Widgets or other objects with properties.
 *
 * @author lobas_av
 * @coverage bindings.ui
 */
final class ObserveElementsComposite extends SashForm {
  private final IDatabindingsProvider m_databindingsProvider;
  private final String m_title;
  private final Label m_titleLabel;
  private final Text m_filterText;
  private final ToolItem m_clearFilterAction;
  private final TreeViewer m_masterViewer;
  private boolean m_enabledFilter;
  private final ObserveFilter m_filter = new ObserveFilter();
  private final ToolItem m_propertiesFilterAction;
  private final ToolItem m_bindAction;
  private final TreeViewer m_propertiesViewer;
  private final Menu m_propertiesFilterMenu;
  private final PropertiesFilter m_propertiesFilter = new PropertiesFilter();
  private final List<ToolItem> m_typeActions = Lists.newArrayList();
  private final SelectionListener m_changeTypeListener = new SelectionAdapter() {
    @Override
    public void widgetSelected(SelectionEvent e) {
      // prepare action
      ToolItem action = (ToolItem) e.widget;
      // handle select
      if (action.getSelection()) {
        ObserveType type = (ObserveType) action.getData();
        if (type != m_currentType) {
          showPage(type, false);
        }
      }
    }
  };
  private final Map<ObserveType, ISelection[]> m_typeToSelection = Maps.newHashMap();
  private final Map<ObserveType, Object[][]> m_typeToExpanded = Maps.newHashMap();
  private ObserveType m_currentType;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ObserveElementsComposite(Composite parent,
      int style,
      String title,
      IDatabindingsProvider databindingsProvider,
      boolean addBindAction) {
    super(parent, style | SWT.VERTICAL);
    m_title = title;
    m_databindingsProvider = databindingsProvider;
    // master composite
    Composite masterComposite = new Composite(this, SWT.NONE);
    GridLayoutFactory.create(masterComposite).columns(2);
    //
    m_titleLabel = new Label(masterComposite, SWT.NONE);
    GridDataFactory.create(m_titleLabel).fillH().grabH().spanH(2);
    m_titleLabel.setText(title);
    //
    m_filterText = new Text(masterComposite, SWT.BORDER | SWT.SINGLE | SWT.SEARCH);
    GridDataFactory.create(m_filterText).fillH().grabH();
    m_filterText.setText(Messages.ObserveElementsComposite_filterText);
    m_filterText.addListener(SWT.FocusIn, new Listener() {
      public void handleEvent(Event event) {
        m_filterText.selectAll();
      }
    });
    m_filterText.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        String filterText = m_filterText.getText();
        if (m_enabledFilter) {
          if (filterText.length() == 0) {
            // disabled filter
            m_enabledFilter = false;
            resetFilter();
            m_clearFilterAction.setEnabled(false);
          } else {
            // update filter value
            refreshFilter(filterText, false);
          }
        } else {
          // enabled
          m_enabledFilter = true;
          refreshFilter(filterText, true);
          m_clearFilterAction.setEnabled(true);
        }
        // expand all elements
        m_masterViewer.expandAll();
      }
    });
    //
    ToolBar toolbar = new ToolBar(masterComposite, SWT.NONE);
    // clear filter action
    m_clearFilterAction = new ToolItem(toolbar, SWT.NONE);
    m_clearFilterAction.setImage(Activator.getImage("clear_filter.gif"));
    m_clearFilterAction.setToolTipText(Messages.ObserveElementsComposite_clearAction);
    m_clearFilterAction.setEnabled(false);
    m_clearFilterAction.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        m_filterText.setText("");
      }
    });
    // separator
    new ToolItem(toolbar, SWT.SEPARATOR);
    // type actions
    for (ObserveType observeType : databindingsProvider.getTypes()) {
      ToolItem action = new ToolItem(toolbar, SWT.RADIO);
      action.setImage(observeType.getImage());
      action.setToolTipText(observeType.getName());
      action.setData(observeType);
      action.addSelectionListener(m_changeTypeListener);
      m_typeActions.add(action);
    }
    // master viewer
    m_masterViewer = new TreeViewer(masterComposite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
    GridDataFactory.create(m_masterViewer.getControl()).fill().grab().spanH(2);
    m_masterViewer.setContentProvider(new ObserveTreeContentProvider(IObserveInfo.ChildrenContext.ChildrenForMasterTable));
    m_masterViewer.setLabelProvider(new ObserveLabelProvider());
    m_masterViewer.addPostSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event) {
        updatePageTitle();
        IStructuredSelection selection = (IStructuredSelection) event.getSelection();
        if (UiUtils.isEmpty(selection)) {
          m_propertiesViewer.setInput(Collections.EMPTY_LIST);
        } else {
          m_propertiesViewer.setInput(selection.getFirstElement());
        }
      }
    });
    // properties composite
    Composite propertiesComposite = new Composite(this, SWT.NONE);
    GridLayoutFactory.create(propertiesComposite).columns(2);
    //
    Label propertiesLabel = new Label(propertiesComposite, SWT.NONE);
    GridDataFactory.create(propertiesLabel).fillH().grabH();
    propertiesLabel.setText(Messages.ObserveElementsComposite_propertiesLabel);
    //
    ToolBar propertiesToolbar = new ToolBar(propertiesComposite, SWT.FLAT);
    GridDataFactory.create(propertiesToolbar).alignHR().grabH();
    //
    m_propertiesFilterAction = new ToolItem(propertiesToolbar, SWT.DROP_DOWN);
    m_propertiesFilterAction.setImage(Activator.getImage("filter_action.gif"));
    m_propertiesFilterAction.setToolTipText(Messages.ObserveElementsComposite_propertiesFilterToolTip);
    //
    m_propertiesFilterMenu = new Menu(propertiesToolbar);
    m_propertiesFilterAction.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        // prepare menu bounds
        Rectangle bounds = m_propertiesFilterAction.getBounds();
        Point location =
            m_propertiesFilterAction.getParent().toDisplay(
                new Point(bounds.x, bounds.y + bounds.height));
        m_propertiesFilterMenu.setLocation(location.x, location.y);
        // show context menu
        m_propertiesFilterMenu.setVisible(true);
      }
    });
    //
    if (addBindAction) {
      new ToolItem(propertiesToolbar, SWT.SEPARATOR);
      //
      m_bindAction = new ToolItem(propertiesToolbar, SWT.NONE);
      m_bindAction.setImage(Activator.getImage("link_add_action.png"));
      m_bindAction.setToolTipText(Messages.ObserveElementsComposite_bindToolTip);
      m_bindAction.setEnabled(false);
    } else {
      m_bindAction = null;
    }
    //
    m_propertiesViewer =
        new TreeViewer(propertiesComposite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
    GridDataFactory.create(m_propertiesViewer.getControl()).fill().grab().spanH(2);
    m_propertiesViewer.setContentProvider(new ObserveTreeContentProvider(IObserveInfo.ChildrenContext.ChildrenForPropertiesTable));
    //
    IBaseLabelProvider propertiesLabelProvider =
        m_databindingsProvider.createPropertiesViewerLabelProvider(m_propertiesViewer);
    m_propertiesViewer.setLabelProvider(propertiesLabelProvider == null
        ? new ObserveLabelProvider()
        : propertiesLabelProvider);
    //
    m_propertiesViewer.addPostSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event) {
        updatePageTitle();
      }
    });
    //
    fillPropertiesFilters();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Page
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Show page with give {@link ObserveType}.
   */
  public void showPage(ObserveType type) {
    showPage(type, true);
  }

  private void showPage(ObserveType type, boolean setActionSelection) {
    // check store current type
    if (m_currentType != null && m_currentType != type) {
      storeSelection(
          m_currentType,
          m_masterViewer.getSelection(),
          m_propertiesViewer.getSelection());
      storeExpanded(
          m_currentType,
          m_masterViewer.getExpandedElements(),
          m_propertiesViewer.getExpandedElements());
    }
    // calculate update type
    boolean setInput = m_currentType != type;
    // store new type
    m_currentType = type;
    // update actions
    if (setActionSelection) {
      for (ToolItem action : m_typeActions) {
        action.setSelection(action.getData() == m_currentType);
      }
    }
    // sets new title and inputs
    if (setInput) {
      updatePageTitle();
      m_masterViewer.setInput(m_databindingsProvider.getObserves(m_currentType));
      m_propertiesViewer.setInput(Collections.EMPTY_LIST);
    }
    // restore expanded
    Object[][] expandeds = m_typeToExpanded.get(m_currentType);
    if (expandeds == null || m_enabledFilter) {
      if (m_enabledFilter || m_currentType.getExpandedStrategy() == ExpandedStrategy.ExpandedAll) {
        m_masterViewer.expandAll();
      }
    } else {
      if (!m_enabledFilter) {
        m_masterViewer.setExpandedElements(expandeds[0]);
      }
      m_propertiesViewer.setExpandedElements(expandeds[1]);
    }
    // restore selection
    ISelection[] selections = m_typeToSelection.get(m_currentType);
    if (selections != null) {
      m_masterViewer.setSelection(selections[0], true);
      m_propertiesViewer.setSelection(selections[1], true);
    }
  }

  private void updatePageTitle() {
    StringBuffer buffer = new StringBuffer();
    appendObserves(m_masterViewer, buffer);
    appendObserves(m_propertiesViewer, buffer);
    if (buffer.length() == 0) {
      buffer.append("<empty>");
    }
    m_titleLabel.setText(m_title + " (" + m_currentType.getName() + "): " + buffer.toString());
  }

  private static void appendObserves(Viewer viewer, StringBuffer commonBuffer) {
    IStructuredSelection selection = UiUtils.getSelection(viewer);
    if (!UiUtils.isEmpty(selection)) {
      StringBuffer buffer = new StringBuffer();
      final IObserveInfo[] observe = {(IObserveInfo) selection.getFirstElement()};
      while (true) {
        buffer.insert(0, ExecutionUtils.runObjectLog(new RunnableObjectEx<String>() {
          public String runObject() throws Exception {
            return observe[0].getPresentation().getTextForBinding();
          }
        }, "<error>"));
        observe[0] = observe[0].getParent();
        if (observe[0] == null) {
          break;
        }
        buffer.insert(0, ".");
      }
      if (commonBuffer.length() > 0 && buffer.length() > 0) {
        commonBuffer.append(".");
      }
      commonBuffer.append(buffer);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Selection
  //
  ////////////////////////////////////////////////////////////////////////////
  public void setSelection(IObserveInfo observeObject, IObserveInfo observeProperty) {
    ObserveType type = observeObject.getType();
    ISelection masterSelection = new StructuredSelection(observeObject);
    ISelection propertiesSelection = new StructuredSelection(observeProperty);
    if (type == m_currentType
        && masterSelection.equals(m_masterViewer.getSelection())
        && propertiesSelection.equals(m_propertiesViewer.getSelection())) {
      return;
    }
    //
    storeSelection(type, masterSelection, propertiesSelection);
    showPage(type);
  }

  private void storeSelection(ObserveType type,
      ISelection masterSelection,
      ISelection propertiesSelection) {
    // prepare cached value
    ISelection[] selections = m_typeToSelection.get(type);
    // check create
    if (selections == null) {
      selections = new ISelection[2];
      m_typeToSelection.put(type, selections);
    }
    // update values
    selections[0] = masterSelection;
    selections[1] = propertiesSelection;
  }

  private void storeExpanded(ObserveType type,
      Object[] masterExpandedObjects,
      Object[] propertiesExpandedObjects) {
    // prepare cached value
    Object[][] expandeds = m_typeToExpanded.get(type);
    // check create
    if (expandeds == null) {
      expandeds = new Object[2][];
      m_typeToExpanded.put(type, expandeds);
    }
    // update values
    expandeds[0] = masterExpandedObjects;
    expandeds[1] = propertiesExpandedObjects;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public ToolItem getBindAction() {
    return m_bindAction;
  }

  public TreeViewer getMasterViewer() {
    return m_masterViewer;
  }

  public TreeViewer getPropertiesViewer() {
    return m_propertiesViewer;
  }

  public IObserveInfo getMasterObserve() {
    IStructuredSelection selection = (IStructuredSelection) m_masterViewer.getSelection();
    return UiUtils.isEmpty(selection) ? null : (IObserveInfo) selection.getFirstElement();
  }

  public IObserveInfo getPropertyObserve() {
    IStructuredSelection selection = (IStructuredSelection) m_propertiesViewer.getSelection();
    return UiUtils.isEmpty(selection) ? null : (IObserveInfo) selection.getFirstElement();
  }

  public ObserveType getCurrentType() {
    return m_currentType;
  }

  public void refresh() {
    m_masterViewer.refresh();
    m_propertiesViewer.refresh();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Filter
  //
  ////////////////////////////////////////////////////////////////////////////
  protected final void resetFilter() {
    try {
      m_masterViewer.getControl().setRedraw(false);
      m_masterViewer.resetFilters();
    } finally {
      m_masterViewer.getControl().setRedraw(true);
    }
  }

  protected final void refreshFilter(String filterText, boolean addFilter) {
    try {
      m_masterViewer.getControl().setRedraw(false);
      m_filter.setPattern(filterText);
      //
      if (addFilter) {
        m_masterViewer.addFilter(m_filter);
      } else {
        m_masterViewer.refresh();
      }
    } finally {
      m_masterViewer.getControl().setRedraw(true);
    }
  }

  private static class ObserveFilter extends ViewerFilter {
    private final SearchPattern m_matcher = new SearchPattern();

    ////////////////////////////////////////////////////////////////////////////
    //
    // Access
    //
    ////////////////////////////////////////////////////////////////////////////
    public void setPattern(String pattern) {
      m_matcher.setPattern(pattern);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // ViewerFilter
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean select(Viewer viewer, Object parent, Object element) {
      final IObserveInfo observe = (IObserveInfo) element;
      for (IObserveInfo child : observe.getChildren(IObserveInfo.ChildrenContext.ChildrenForMasterTable)) {
        if (select(viewer, element, child)) {
          return true;
        }
      }
      return ExecutionUtils.runObjectLog(new RunnableObjectEx<Boolean>() {
        public Boolean runObject() throws Exception {
          return m_matcher.matches(observe.getPresentation().getText());
        }
      }, Boolean.FALSE);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties Filter
  //
  ////////////////////////////////////////////////////////////////////////////
  private void fillPropertiesFilters() {
    SelectionListener listener = new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        // prepare menu item
        MenuItem item = (MenuItem) e.widget;
        // handle change selection
        if (item.getSelection()) {
          PropertyFilter filter = (PropertyFilter) item.getData();
          m_propertiesFilter.setCurrentFilter(filter);
          m_propertiesViewer.refresh();
        }
      }
    };
    // prepare filters
    List<PropertyFilter> filters = m_databindingsProvider.getObservePropertyFilters();
    // fill menu
    for (PropertyFilter filter : filters) {
      MenuItem item = new MenuItem(m_propertiesFilterMenu, SWT.RADIO);
      item.setText(filter.getName());
      item.setImage(filter.getImage());
      item.setData(filter);
      item.addSelectionListener(listener);
    }
    // set viewer filter
    if (!filters.isEmpty()) {
      m_propertiesFilter.setCurrentFilter(filters.get(0));
      m_propertiesViewer.addFilter(m_propertiesFilter);
      m_propertiesFilterMenu.getItem(0).setSelection(true);
    }
  }

  private static class PropertiesFilter extends ViewerFilter {
    private PropertyFilter m_currentFilter;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Access
    //
    ////////////////////////////////////////////////////////////////////////////
    public void setCurrentFilter(PropertyFilter currentFilter) {
      m_currentFilter = currentFilter;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // ViewerFilter
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
      if (m_currentFilter != null) {
        return m_currentFilter.select(viewer, (IObserveInfo) element);
      }
      return true;
    }
  }
}