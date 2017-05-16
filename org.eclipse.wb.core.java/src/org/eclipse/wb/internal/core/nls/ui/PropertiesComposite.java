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
package org.eclipse.wb.internal.core.nls.ui;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.nls.Messages;
import org.eclipse.wb.internal.core.nls.SourceDescription;
import org.eclipse.wb.internal.core.nls.edit.IEditableSource;
import org.eclipse.wb.internal.core.nls.edit.IEditableSupport;
import org.eclipse.wb.internal.core.nls.edit.IEditableSupportListener;
import org.eclipse.wb.internal.core.nls.edit.StringPropertyInfo;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;

import org.apache.commons.lang.ArrayUtils;

import java.util.List;

/**
 * Composite for externalizing properties of components tree.
 *
 * @author scheglov_ke
 * @coverage core.nls.ui
 */
public final class PropertiesComposite extends Composite {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Model objects
  //
  ////////////////////////////////////////////////////////////////////////////
  private final IEditableSupport m_support;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public PropertiesComposite(Composite parent, int style, IEditableSupport support) {
    super(parent, style);
    m_support = support;
    m_support.addListener(new IEditableSupportListener() {
      @Override
      public void sourceAdded(IEditableSource source) {
        m_sourcesViewer.refresh();
      }

      @Override
      public void externalizedPropertiesChanged() {
        m_propertiesViewer.refresh();
        m_propertiesViewer.expandAll();
      }
    });
    //
    setLayout(new GridLayout());
    createSourcesGroup();
    createPropertyGroup();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Sources group
  //
  ////////////////////////////////////////////////////////////////////////////
  private ListViewer m_sourcesViewer;
  private org.eclipse.swt.widgets.List m_sourcesList;

  /**
   * Creates the {@link Group} with {@link IEditableSource}'s.
   */
  private void createSourcesGroup() {
    Group sourceGroup = new Group(this, SWT.NONE);
    GridDataFactory.create(sourceGroup).alignHF().grabH();
    GridLayoutFactory.create(sourceGroup).columns(2);
    sourceGroup.setText(Messages.PropertiesComposite_sourceGroup);
    {
      Label label = new Label(sourceGroup, SWT.NONE);
      GridDataFactory.create(label).fill().grabH().spanH(2);
      label.setText(Messages.PropertiesComposite_existingSourcesLabel);
    }
    {
      m_sourcesViewer = new ListViewer(sourceGroup, SWT.BORDER);
      m_sourcesViewer.setContentProvider(new SourcesContentProvider());
      m_sourcesViewer.setLabelProvider(new SourcesLabelProvider());
      m_sourcesViewer.setSorter(new ViewerSorter());
      //
      m_sourcesList = m_sourcesViewer.getList();
      GridDataFactory.create(m_sourcesList).fill().grab().hintVC(5);
      // fill sources list
      m_sourcesViewer.setInput(this);
      m_sourcesList.select(0);
      // install selection listener
      m_sourcesViewer.addSelectionChangedListener(new ISelectionChangedListener() {
        public void selectionChanged(SelectionChangedEvent event) {
          updateExternalizeButton();
        }
      });
    }
    {
      Composite buttonsComposite = new Composite(sourceGroup, SWT.NONE);
      GridDataFactory.create(buttonsComposite).alignVF();
      GridLayoutFactory.create(buttonsComposite).noMargins();
      {
        Button addButton = new Button(buttonsComposite, SWT.NONE);
        setButtonLayoutData(addButton);
        addButton.setText(Messages.PropertiesComposite_newbutton);
        //
        addButton.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            addNewSource();
          }
        });
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties group
  //
  ////////////////////////////////////////////////////////////////////////////
  private CheckboxTreeViewer m_propertiesViewer;
  private Tree m_propertiesTree;
  private Button m_externalizeButton;
  private Button m_copyToAllLocalesButton;

  /**
   * Creates the {@link Group} with components/properties tree.
   */
  private void createPropertyGroup() {
    Group propertyGroup = new Group(this, SWT.NONE);
    GridDataFactory.create(propertyGroup).grab().fill();
    GridLayoutFactory.create(propertyGroup).columns(2);
    propertyGroup.setText(Messages.PropertiesComposite_propertiesGroup);
    {
      Label label = new Label(propertyGroup, SWT.NONE);
      GridDataFactory.create(label).fill().grabH().spanH(2);
      label.setText(Messages.PropertiesComposite_propertiesLabel);
    }
    {
      m_propertiesViewer = new CheckboxTreeViewer(propertyGroup, SWT.BORDER);
      m_propertiesViewer.setContentProvider(new PropertiesContentProvider());
      m_propertiesViewer.setLabelProvider(new PropertiesLabelProvider());
      // set tree layout data
      m_propertiesTree = m_propertiesViewer.getTree();
      GridDataFactory.create(m_propertiesTree).grab().fill();
      // fill properties viewer
      m_propertiesViewer.setInput(this);
      m_propertiesViewer.expandAll();
      m_propertiesViewer.setGrayedElements(m_support.getComponents().toArray());
      // install check listener
      m_propertiesViewer.addCheckStateListener(new ICheckStateListener() {
        public void checkStateChanged(CheckStateChangedEvent event) {
          Object element = event.getElement();
          // if we change "check" state for component, change state for all its properties
          if (element instanceof JavaInfo) {
            JavaInfo component = (JavaInfo) element;
            List<StringPropertyInfo> properties = m_support.getProperties(component);
            for (StringPropertyInfo property : properties) {
              m_propertiesViewer.setChecked(property, event.getChecked());
            }
          }
          // in any case update state
          updateExternalizeButton();
        }
      });
    }
    //
    createPropertyButtons(propertyGroup);
    // create "Copy strings to all locales" check box
    {
      m_copyToAllLocalesButton = new Button(propertyGroup, SWT.CHECK);
      m_copyToAllLocalesButton.setText(Messages.PropertiesComposite_copyButton);
    }
  }

  private void createPropertyButtons(final Group propertiesGroup) {
    Composite buttonsComposite = new Composite(propertiesGroup, SWT.NONE);
    GridDataFactory.create(buttonsComposite).alignVF();
    GridLayoutFactory.create(buttonsComposite).noMargins();
    {
      Button enableAllButton = new Button(buttonsComposite, SWT.NONE);
      setButtonLayoutData(enableAllButton);
      enableAllButton.setText(Messages.PropertiesComposite_enableAllButton);
      // install handler
      enableAllButton.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          m_propertiesViewer.setSubtreeChecked(m_support.getRoot(), true);
          updateExternalizeButton();
        }
      });
    }
    {
      Button disableAllButton = new Button(buttonsComposite, SWT.NONE);
      setButtonLayoutData(disableAllButton);
      disableAllButton.setText(Messages.PropertiesComposite_disableAllButton);
      // install handler
      disableAllButton.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          m_propertiesViewer.setSubtreeChecked(m_support.getRoot(), false);
          updateExternalizeButton();
        }
      });
    }
    {
      Label label = new Label(buttonsComposite, SWT.HORIZONTAL | SWT.SEPARATOR);
      GridDataFactory.create(label).alignHF();
      label.setVisible(false);
    }
    {
      m_externalizeButton = new Button(buttonsComposite, SWT.NONE);
      setButtonLayoutData(m_externalizeButton);
      m_externalizeButton.setText(Messages.PropertiesComposite_externalizeButton);
      m_externalizeButton.setEnabled(false);
      // install handler
      m_externalizeButton.addSelectionListener(new SelectionAdapter() {
        @Override
        public void widgetSelected(SelectionEvent e) {
          externalizeSelectedProperties();
        }
      });
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Add new source
  //
  ////////////////////////////////////////////////////////////////////////////
  protected void addNewSource() {
    NewSourceDialog newSourceDialog = new NewSourceDialog(getShell(), m_support.getRoot());
    if (newSourceDialog.open() == Window.OK) {
      // add new source
      IEditableSource editableSource = newSourceDialog.getNewEditableSource();
      SourceDescription sourceDescription = newSourceDialog.getNewSourceDescription();
      Object parameters = newSourceDialog.getNewSourceParameters();
      // notify support that we have new source, this will also update viewer
      m_support.addSource(editableSource, sourceDescription, parameters);
      m_sourcesViewer.setSelection(new StructuredSelection(editableSource));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Externalize
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Update enable state of "Externalize" button.
   */
  private void updateExternalizeButton() {
    boolean hasSource = !m_sourcesViewer.getSelection().isEmpty();
    boolean hasProperties = !getSelectedProperties().isEmpty();
    m_externalizeButton.setEnabled(hasSource && hasProperties);
  }

  /**
   * Externalize selected properties in selected source.
   */
  private void externalizeSelectedProperties() {
    IEditableSource source = getSelectedSource();
    List<StringPropertyInfo> selectedProperties = getSelectedProperties();
    boolean copyToAllLocales = m_copyToAllLocalesButton.getSelection();
    // externalize properties in selected source
    for (StringPropertyInfo propertyInfo : selectedProperties) {
      m_support.externalizeProperty(propertyInfo, source, copyToAllLocales);
    }
    // refresh UI
    updateExternalizeButton();
  }

  /**
   * Return selected properties.
   */
  private List<StringPropertyInfo> getSelectedProperties() {
    List<StringPropertyInfo> properties = Lists.newArrayList();
    //
    Object[] checkedElements = m_propertiesViewer.getCheckedElements();
    for (Object element : checkedElements) {
      if (element instanceof StringPropertyInfo) {
        properties.add((StringPropertyInfo) element);
      }
    }
    //
    return properties;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the selected {@link IEditableSource}.
   */
  private IEditableSource getSelectedSource() {
    IStructuredSelection selection = (IStructuredSelection) m_sourcesViewer.getSelection();
    return (IEditableSource) selection.getFirstElement();
  }

  /**
   * Sets good layout data for {@link Button}.
   */
  private static void setButtonLayoutData(Button button) {
    GridDataFactory.create(button).hintHU(IDialogConstants.BUTTON_WIDTH);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Sources: content provider
  //
  ////////////////////////////////////////////////////////////////////////////
  private class SourcesContentProvider implements IStructuredContentProvider {
    public Object[] getElements(Object inputElement) {
      return m_support.getEditableSources().toArray();
    }

    public void dispose() {
    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Sources: label provider
  //
  ////////////////////////////////////////////////////////////////////////////
  private class SourcesLabelProvider extends LabelProvider {
    @Override
    public String getText(Object element) {
      IEditableSource source = (IEditableSource) element;
      return source.getLongTitle();
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties: content provider
  //
  ////////////////////////////////////////////////////////////////////////////
  private class PropertiesContentProvider
      implements
        IStructuredContentProvider,
        ITreeContentProvider {
    public Object[] getElements(Object inputElement) {
      return ExecutionUtils.runObjectLog(new RunnableObjectEx<Object[]>() {
        public Object[] runObject() throws Exception {
          JavaInfo root = m_support.getRoot();
          // show root only if there are properties
          if (m_support.hasPropertiesInTree(root)) {
            return new Object[]{root};
          }
          // else, show empty tree
          return ArrayUtils.EMPTY_OBJECT_ARRAY;
        }
      }, ArrayUtils.EMPTY_OBJECT_ARRAY);
    }

    public Object[] getChildren(Object parentElement) {
      if (parentElement instanceof JavaInfo) {
        final JavaInfo component = (JavaInfo) parentElement;
        final List<Object> children = Lists.newArrayList();
        // add properties
        children.addAll(m_support.getProperties(component));
        // add children with properties
        ExecutionUtils.runLog(new RunnableEx() {
          public void run() throws Exception {
            for (JavaInfo child : m_support.getTreeChildren(component)) {
              if (m_support.hasPropertiesInTree(child)) {
                children.add(child);
              }
            }
          }
        });
        //
        return children.toArray();
      }
      //
      return ArrayUtils.EMPTY_OBJECT_ARRAY;
    }

    public boolean hasChildren(Object element) {
      return getChildren(element).length > 0;
    }

    public Object getParent(Object element) {
      if (element instanceof ObjectInfo) {
        return ((ObjectInfo) element).getParent();
      } else {
        Assert.instanceOf(StringPropertyInfo.class, element);
        return ((StringPropertyInfo) element).getComponent();
      }
    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

    public void dispose() {
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties: label provider
  //
  ////////////////////////////////////////////////////////////////////////////
  private class PropertiesLabelProvider extends LabelProvider {
    @Override
    public String getText(final Object element) {
      return ExecutionUtils.runObjectLog(new RunnableObjectEx<String>() {
        public String runObject() throws Exception {
          if (element instanceof JavaInfo) {
            JavaInfo component = (JavaInfo) element;
            return component.getPresentation().getText();
          } else {
            Assert.instanceOf(StringPropertyInfo.class, element);
            StringPropertyInfo propertyInfo = (StringPropertyInfo) element;
            return propertyInfo.getTitle();
          }
        }
      }, null);
    }

    @Override
    public Image getImage(final Object element) {
      return ExecutionUtils.runObjectLog(new RunnableObjectEx<Image>() {
        public Image runObject() throws Exception {
          if (element instanceof JavaInfo) {
            JavaInfo component = (JavaInfo) element;
            return component.getPresentation().getIcon();
          } else {
            Assert.instanceOf(StringPropertyInfo.class, element);
            return DesignerPlugin.getImage("nls/property.gif");
          }
        }
      }, null);
    }
  }
}
