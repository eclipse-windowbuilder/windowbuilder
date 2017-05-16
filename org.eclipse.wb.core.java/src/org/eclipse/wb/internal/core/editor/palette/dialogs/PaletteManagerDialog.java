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
package org.eclipse.wb.internal.core.editor.palette.dialogs;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.editor.palette.model.AbstractElementInfo;
import org.eclipse.wb.core.editor.palette.model.CategoryInfo;
import org.eclipse.wb.core.editor.palette.model.EntryInfo;
import org.eclipse.wb.core.editor.palette.model.PaletteInfo;
import org.eclipse.wb.core.editor.palette.model.entry.ComponentEntryInfo;
import org.eclipse.wb.core.editor.palette.model.entry.ToolEntryInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.editor.Messages;
import org.eclipse.wb.internal.core.editor.palette.command.CategoryMoveCommand;
import org.eclipse.wb.internal.core.editor.palette.command.CategoryRemoveCommand;
import org.eclipse.wb.internal.core.editor.palette.command.Command;
import org.eclipse.wb.internal.core.editor.palette.command.ElementVisibilityCommand;
import org.eclipse.wb.internal.core.editor.palette.command.EntryMoveCommand;
import org.eclipse.wb.internal.core.editor.palette.command.EntryRemoveCommand;
import org.eclipse.wb.internal.core.editor.palette.dialogs.factory.FactoriesAddDialog;
import org.eclipse.wb.internal.core.editor.palette.dialogs.factory.FactoryAddDialog;
import org.eclipse.wb.internal.core.editor.palette.dialogs.factory.FactoryEditDialog;
import org.eclipse.wb.internal.core.editor.palette.model.entry.InstanceFactoryEntryInfo;
import org.eclipse.wb.internal.core.editor.palette.model.entry.StaticFactoryEntryInfo;
import org.eclipse.wb.internal.core.utils.GenericsUtils;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.ui.EmptyTransfer;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.core.utils.ui.dialogs.ResizableTitleAreaDialog;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.dialogs.SearchPattern;

import org.apache.commons.lang.ArrayUtils;

import java.text.MessageFormat;
import java.util.List;
import java.util.Set;

/**
 * Dialog for modifying palette.
 *
 * @author scheglov_ke
 * @coverage core.editor.palette.ui
 */
public final class PaletteManagerDialog extends ResizableTitleAreaDialog {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Static images
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final Image IMAGE_CLEAR_FILTER =
      DesignerPlugin.getImage("palette/clear_filter.gif");
  private static final Image IMAGE_CATEGORY = DesignerPlugin.getImage("palette/category.gif");
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance fields
  //
  ////////////////////////////////////////////////////////////////////////////
  private final AstEditor m_editor;
  private final PaletteInfo m_palette;
  private final Set<EntryInfo> m_goodEntryInfos;
  private final List<Command> m_commands = Lists.newArrayList();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public PaletteManagerDialog(AstEditor editor, PaletteInfo palette, Set<EntryInfo> goodEntryInfos) {
    super(DesignerPlugin.getShell(), DesignerPlugin.getDefault());
    m_editor = editor;
    m_palette = palette;
    m_goodEntryInfos = goodEntryInfos;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  private CheckboxTreeViewer m_viewer;

  @Override
  public void create() {
    super.create();
    // configure title area
    getShell().setText(Messages.PaletteManagerDialog_shellTitle);
    setTitle(Messages.PaletteManagerDialog_title);
    setMessage(Messages.PaletteManagerDialog_message);
  }

  @Override
  protected final Control createDialogArea(Composite parent) {
    Composite area = (Composite) super.createDialogArea(parent);
    //
    Composite container = new Composite(area, SWT.NONE);
    GridDataFactory.create(container).grab().fill();
    createControls(container);
    //
    return area;
  }

  /**
   * Creates controls on this dialog.
   */
  private void createControls(Composite container) {
    GridLayoutFactory.create(container).columns(2);
    createFilterComposite(container);
    new Label(container, SWT.NONE);
    createViewer(container);
    createButtonsComposite(container);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI: filter
  //
  ////////////////////////////////////////////////////////////////////////////
  private final PaletteViewerFilter m_filter = new PaletteViewerFilter();
  private Text m_filterText;
  private ToolItem m_filterClearItem;

  /**
   * Creates {@link Composite} with filter for {@link #m_viewer}.
   */
  private void createFilterComposite(Composite container) {
    Composite filterComposite = new Composite(container, SWT.NONE);
    GridDataFactory.create(filterComposite).fillH();
    GridLayoutFactory.create(filterComposite).columns(2).noMargins().noSpacing();
    {
      m_filterText = new Text(filterComposite, SWT.BORDER);
      GridDataFactory.create(m_filterText).grabH().fillH();
      // listener
      m_filterText.addListener(SWT.Modify, new Listener() {
        public void handleEvent(Event event) {
          refreshFilter();
        }
      });
    }
    {
      ToolBar toolBar = new ToolBar(filterComposite, SWT.FLAT);
      m_filterClearItem = new ToolItem(toolBar, SWT.NONE);
      m_filterClearItem.setImage(IMAGE_CLEAR_FILTER);
      m_filterClearItem.setToolTipText(Messages.PaletteManagerDialog_clearItem);
      m_filterClearItem.setEnabled(false);
      // listener
      m_filterClearItem.addListener(SWT.Selection, new Listener() {
        public void handleEvent(Event event) {
          m_filterText.setText("");
        }
      });
    }
  }

  /**
   * Updates filter and viewer.
   */
  private void refreshFilter() {
    m_viewer.getTree().setRedraw(false);
    try {
      String pattern = m_filterText.getText();
      if (pattern.length() == 0) {
        m_filterClearItem.setEnabled(false);
        m_viewer.resetFilters();
      } else {
        m_filterClearItem.setEnabled(true);
        // set updated filter
        m_filter.setPattern(pattern);
        m_viewer.resetFilters();
        m_viewer.addFilter(m_filter);
        // expand to show entries
        m_viewer.expandAll();
      }
      // do refresh to update visibility state
      refreshViewer();
    } finally {
      m_viewer.getTree().setRedraw(true);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Palette filter
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Implementation of {@link ViewerFilter} for filtering {@link PaletteInfo} elements.
   */
  private static class PaletteViewerFilter extends ViewerFilter {
    private SearchPattern m_pattern;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Access
    //
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Sets new pattern to match.
     */
    public void setPattern(String pattern) {
      if (pattern.indexOf('*') == -1) {
        pattern += '*';
      }
      m_pattern = new SearchPattern();
      m_pattern.setPattern(pattern);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // ViewerFilter
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean select(Viewer viewer, Object parent, Object element) {
      if (element instanceof CategoryInfo) {
        CategoryInfo category = (CategoryInfo) element;
        for (EntryInfo entry : category.getEntries()) {
          if (select(viewer, category, entry)) {
            return true;
          }
        }
      } else if (element instanceof EntryInfo) {
        EntryInfo entry = (EntryInfo) element;
        return m_pattern.matches(entry.getName());
      }
      return false;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI: viewer
  //
  ////////////////////////////////////////////////////////////////////////////
  private final ITreeContentProvider m_contentProvider = new ITreeContentProvider() {
    public Object[] getElements(Object inputElement) {
      return getCategoriesWithGoodEntries().toArray();
    }

    public Object[] getChildren(Object parentElement) {
      if (parentElement instanceof CategoryInfo) {
        return ((CategoryInfo) parentElement).getEntries().toArray();
      }
      return ArrayUtils.EMPTY_OBJECT_ARRAY;
    }

    public Object getParent(Object element) {
      if (element instanceof EntryInfo) {
        return ((EntryInfo) element).getCategory();
      }
      return null;
    }

    public boolean hasChildren(Object element) {
      return getChildren(element).length != 0;
    }

    public void dispose() {
    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Utils
    //
    ////////////////////////////////////////////////////////////////////////////
    private List<CategoryInfo> getCategoriesWithGoodEntries() {
      List<CategoryInfo> goodCategories = Lists.newArrayList();
      for (CategoryInfo category : m_palette.getCategories()) {
        if (!category.isOptional() || hasGoodEntries(category)) {
          goodCategories.add(category);
        }
      }
      return goodCategories;
    }

    private boolean hasGoodEntries(CategoryInfo category) {
      List<EntryInfo> entries = category.getEntries();
      for (EntryInfo entry : entries) {
        if (m_goodEntryInfos.contains(entry)) {
          return true;
        }
      }
      return false;
    }
  };

  /**
   * Creates {@link CheckboxTreeViewer} for palette categories/entries.
   */
  private void createViewer(Composite parent) {
    m_viewer = new CheckboxTreeViewer(parent, SWT.BORDER | SWT.MULTI);
    GridDataFactory.create(m_viewer.getTree()).hintC(60, 20).grab().fill();
    // providers
    m_viewer.setContentProvider(m_contentProvider);
    m_viewer.setLabelProvider(new LabelProvider() {
      @Override
      public Image getImage(Object element) {
        if (element instanceof EntryInfo) {
          return ((EntryInfo) element).getIcon();
        }
        return IMAGE_CATEGORY;
      }

      @Override
      public String getText(Object element) {
        return ((AbstractElementInfo) element).getName();
      }
    });
    // set input
    m_viewer.setInput(m_palette);
    refreshViewer();
    // listeners
    m_viewer.addCheckStateListener(new ICheckStateListener() {
      public void checkStateChanged(CheckStateChangedEvent event) {
        AbstractElementInfo elementInfo = (AbstractElementInfo) event.getElement();
        commands_add(new ElementVisibilityCommand(elementInfo, event.getChecked()));
      }
    });
    m_viewer.addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event) {
        updateButtons();
      }
    });
    m_viewer.addDoubleClickListener(new IDoubleClickListener() {
      public void doubleClick(DoubleClickEvent event) {
        onEdit();
      }
    });
    // DND
    configureDND();
  }

  /**
   * Shows actual state of palette in {@link #m_viewer}.
   */
  private void refreshViewer() {
    m_viewer.refresh();
    // update "visible"
    {
      List<Object> visibleElements = Lists.newArrayList();
      for (CategoryInfo category : m_palette.getCategories()) {
        if (category.isVisible()) {
          visibleElements.add(category);
        }
        for (EntryInfo entry : category.getEntries()) {
          if (entry.isVisible()) {
            visibleElements.add(entry);
          }
        }
      }
      // update viewer
      m_viewer.setCheckedElements(visibleElements.toArray());
    }
  }

  /**
   * @return the {@link List} of selected palette elements.
   */
  @SuppressWarnings("unchecked")
  private List<Object> getSelectedElements() {
    IStructuredSelection selection = (IStructuredSelection) m_viewer.getSelection();
    return selection.toList();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI: buttons
  //
  ////////////////////////////////////////////////////////////////////////////
  private Button m_addEntryButton;
  private Button m_editButton;
  private Button m_removeButton;
  private Button m_moveUpButton;
  private Button m_moveDownButton;

  /**
   * Creates {@link Composite} with buttons to modify palette.
   */
  private void createButtonsComposite(Composite parent) {
    Composite buttonsComposite = new Composite(parent, SWT.NONE);
    GridDataFactory.create(buttonsComposite).grabV().fill();
    GridLayoutFactory.create(buttonsComposite).noMargins();
    //
    createButton(buttonsComposite, Messages.PaletteManagerDialog_addCategoryButton, new Listener() {
      public void handleEvent(Event event) {
        onAddCategory();
      }
    });
    m_addEntryButton =
        createButton(
            buttonsComposite,
            Messages.PaletteManagerDialog_addEntryButton,
            new Listener() {
              public void handleEvent(Event event) {
                onAddEntry();
              }
            });
    createButton(buttonsComposite, Messages.PaletteManagerDialog_importJarButton, new Listener() {
      public void handleEvent(Event event) {
        onImportJar();
      }
    });
    //
    createButtonSeparator(buttonsComposite);
    m_editButton =
        createButton(buttonsComposite, Messages.PaletteManagerDialog_editButton, new Listener() {
          public void handleEvent(Event event) {
            onEdit();
          }
        });
    m_removeButton =
        createButton(buttonsComposite, Messages.PaletteManagerDialog_removeButton, new Listener() {
          public void handleEvent(Event event) {
            onRemove();
          }
        });
    //
    createButtonSeparator(buttonsComposite);
    m_moveUpButton =
        createButton(buttonsComposite, Messages.PaletteManagerDialog_upButton, new Listener() {
          public void handleEvent(Event event) {
            onMove(-1);
          }
        });
    m_moveDownButton =
        createButton(buttonsComposite, Messages.PaletteManagerDialog_downButton, new Listener() {
          public void handleEvent(Event event) {
            onMove(+2);
          }
        });
    //
    createButtonSeparator(buttonsComposite);
    createButton(buttonsComposite, Messages.PaletteManagerDialog_collapseAllButton, new Listener() {
      public void handleEvent(Event event) {
        m_viewer.collapseAll();
      }
    });
    createButton(buttonsComposite, Messages.PaletteManagerDialog_expandAllButton, new Listener() {
      public void handleEvent(Event event) {
        m_viewer.expandAll();
      }
    });
    // update buttons first time
    updateButtons();
  }

  /**
   * Creates {@link Button} with given text and {@link SWT#Selection} listener.
   */
  private static Button createButton(Composite parent, String text, Listener selectionListener) {
    Button button = new Button(parent, SWT.NONE);
    GridDataFactory.create(button).grabH().fillH();
    button.setText(text);
    button.addListener(SWT.Selection, selectionListener);
    return button;
  }

  /**
   * Creates separator between buttons on vertical buttons bar.
   */
  private static void createButtonSeparator(Composite parent) {
    Label separator = new Label(parent, SWT.NONE);
    GridDataFactory.create(separator).hintV(7);
  }

  /**
   * Updates buttons according to selection in {@link #m_viewer}.
   */
  private void updateButtons() {
    List<Object> selection = getSelectedElements();
    m_addEntryButton.setEnabled(!selection.isEmpty());
    m_editButton.setEnabled(selection.size() == 1);
    m_removeButton.setEnabled(!selection.isEmpty());
    // move up/down
    List<CategoryInfo> categories = m_palette.getCategories();
    {
      boolean upEnabled = !selection.isEmpty();
      boolean downEnabled = !selection.isEmpty();
      for (Object element : selection) {
        if (element instanceof CategoryInfo) {
          upEnabled &= categories.indexOf(element) != 0;
          downEnabled &= categories.indexOf(element) != categories.size() - 1;
        } else if (element instanceof EntryInfo) {
          EntryInfo entry = (EntryInfo) element;
          List<EntryInfo> entries = entry.getCategory().getEntries();
          upEnabled &= entries.indexOf(entry) != 0;
          downEnabled &= entries.indexOf(entry) != entries.size() - 1;
        }
      }
      m_moveUpButton.setEnabled(upEnabled);
      m_moveDownButton.setEnabled(downEnabled);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Operations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds new {@link CategoryInfo}.
   */
  private void onAddCategory() {
    CategoryAddDialog dialog = new CategoryAddDialog(getShell(), m_palette, null);
    if (dialog.open() == Window.OK) {
      commands_add(dialog.getCommand());
      refreshViewer();
    }
  }

  /**
   * Adds new {@link EntryInfo}.
   */
  private void onAddEntry() {
    final CategoryInfo targetCategory;
    {
      Object element = getSelectedElements().get(0);
      if (element instanceof CategoryInfo) {
        targetCategory = (CategoryInfo) element;
      } else if (element instanceof EntryInfo) {
        targetCategory = ((EntryInfo) element).getCategory();
      } else {
        return;
      }
    }
    // prepare menu
    //Menu menu = new Menu(getShell(), SWT.POP_UP);
    MenuManager menuManager = new MenuManager();
    // component
    {
      IAction action = new Action(Messages.PaletteManagerDialog_addComponentAction) {
        @Override
        public void run() {
          ComponentAddDialog dialog =
              new ComponentAddDialog(getShell(), m_editor, m_palette, targetCategory);
          if (dialog.open() == Window.OK) {
            commands_add(dialog.getCommand());
            refreshViewer();
          }
        }
      };
      menuManager.add(action);
    }
    // static factory
    {
      menuManager.add(new Separator());
      menuManager.add(onAddEntry_factory(targetCategory, true));
      menuManager.add(onAddEntry_factories(targetCategory, true));
    }
    // instance factory
    {
      menuManager.add(new Separator());
      menuManager.add(onAddEntry_factory(targetCategory, false));
      menuManager.add(onAddEntry_factories(targetCategory, false));
    }
    // open menu
    {
      Point location = m_addEntryButton.toDisplay(0, m_addEntryButton.getSize().y);
      Menu menu = menuManager.createContextMenu(getShell());
      menu.setLocation(location);
      menu.setVisible(true);
    }
  }

  /**
   * @return the {@link IAction} for adding single factory method.
   */
  private IAction onAddEntry_factory(final CategoryInfo category, final boolean forStatic) {
    return new Action(Messages.PaletteManagerDialog_addFactorySingle
        + (forStatic
            ? Messages.PaletteManagerDialog_addFactorySingleStatic
            : Messages.PaletteManagerDialog_addFactorySingleInstance)
        + Messages.PaletteManagerDialog_addFactorySingleFactory) {
      @Override
      public void run() {
        FactoryAddDialog dialog =
            new FactoryAddDialog(getShell(), m_editor, forStatic, m_palette, category);
        if (dialog.open() == Window.OK) {
          commands_add(dialog.getCommand());
          refreshViewer();
        }
      }
    };
  }

  /**
   * @return the {@link IAction} for adding several factory methods.
   */
  private IAction onAddEntry_factories(final CategoryInfo category, final boolean forStatic) {
    return new Action(Messages.PaletteManagerDialog_addFactorySeveral
        + (forStatic
            ? Messages.PaletteManagerDialog_addFactorySeveralStatic
            : Messages.PaletteManagerDialog_addFactorySeveralInstance)
        + Messages.PaletteManagerDialog_addFactorySeveralFactories) {
      @Override
      public void run() {
        // prepare dialog
        FactoriesAddDialog dialog =
            new FactoriesAddDialog(getShell(), m_editor, m_palette, category, forStatic);
        // open dialog
        if (dialog.open() == Window.OK) {
          // add commands
          for (Command command : dialog.getCommands()) {
            commands_add(command);
          }
          // refresh
          refreshViewer();
        }
      }
    };
  }

  /**
   * Import Jar file to palette.
   */
  private void onImportJar() {
    // prepare selection
    List<Object> allSelection = getSelectedElements();
    Object selection = allSelection.isEmpty() ? null : allSelection.get(0);
    // prepare initial "next category"
    CategoryInfo nextCategory = null;
    if (selection instanceof CategoryInfo) {
      nextCategory = (CategoryInfo) selection;
    } else if (selection instanceof EntryInfo) {
      EntryInfo entry = (EntryInfo) selection;
      nextCategory = entry.getCategory();
    }
    // open dialog
    ImportArchiveDialog dialog = new ImportArchiveDialog(getShell(), m_palette, nextCategory);
    if (dialog.open() == Window.OK) {
      for (Command command : dialog.getCommands()) {
        commands_add(command);
      }
      // refresh
      refreshViewer();
    }
  }

  /**
   * Edits palette element.
   */
  private void onEdit() {
    Object element = getSelectedElements().get(0);
    if (element instanceof CategoryInfo) {
      CategoryInfo category = (CategoryInfo) element;
      CategoryEditDialog dialog = new CategoryEditDialog(getShell(), category);
      if (dialog.open() == Window.OK) {
        commands_add(dialog.getCommand());
        refreshViewer();
      }
    } else if (element instanceof ToolEntryInfo) {
      AbstractPaletteElementDialog dialog = null;
      // prepare editing dialog
      if (element instanceof ComponentEntryInfo) {
        ComponentEntryInfo entryInfo = (ComponentEntryInfo) element;
        dialog = new ComponentEditDialog(getShell(), m_editor, entryInfo);
      } else if (element instanceof StaticFactoryEntryInfo) {
        StaticFactoryEntryInfo entryInfo = (StaticFactoryEntryInfo) element;
        dialog = new FactoryEditDialog(getShell(), m_editor, true, entryInfo);
      } else if (element instanceof InstanceFactoryEntryInfo) {
        InstanceFactoryEntryInfo entryInfo = (InstanceFactoryEntryInfo) element;
        dialog = new FactoryEditDialog(getShell(), m_editor, false, entryInfo);
      }
      // execute dialog
      if (dialog != null && dialog.open() == Window.OK) {
        commands_add(dialog.getCommand());
        refreshViewer();
      }
    }
  }

  /**
   * Removes palette element.
   */
  private void onRemove() {
    List<Object> selection = getSelectedElements();
    if (MessageDialog.openConfirm(
        getShell(),
        Messages.PaletteManagerDialog_removeTitle,
        MessageFormat.format(Messages.PaletteManagerDialog_removeMessage, selection.size()))) {
      for (Object element : selection) {
        if (element instanceof CategoryInfo) {
          commands_add(new CategoryRemoveCommand((CategoryInfo) element));
        } else if (element instanceof EntryInfo) {
          commands_add(new EntryRemoveCommand((EntryInfo) element));
        }
      }
      refreshViewer();
    }
  }

  /**
   * Moves selected elements up/down.
   */
  private void onMove(int delta) {
    m_viewer.getTree().setRedraw(false);
    try {
      for (Object element : getSelectedElements()) {
        if (element instanceof CategoryInfo) {
          CategoryInfo category = (CategoryInfo) element;
          List<CategoryInfo> categories = m_palette.getCategories();
          int index = categories.indexOf(element);
          int targetIndex = index + delta;
          CategoryInfo nextCategory =
              targetIndex < categories.size() ? (CategoryInfo) categories.get(targetIndex) : null;
          commands_add(new CategoryMoveCommand(category, nextCategory));
        } else if (element instanceof EntryInfo) {
          EntryInfo entry = (EntryInfo) element;
          CategoryInfo category = entry.getCategory();
          List<EntryInfo> entries = category.getEntries();
          int index = entries.indexOf(entry);
          commands_add(new EntryMoveCommand(entry, category, entries.get(index + delta)));
        }
      }
      refreshViewer();
      updateButtons();
    } finally {
      m_viewer.getTree().setRedraw(true);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // DND
  //
  ////////////////////////////////////////////////////////////////////////////
  private List<Object> m_dragElements;
  private boolean m_dragCategory;

  /**
   * Configures DND in {@link #m_viewer}.
   */
  private void configureDND() {
    Transfer[] transfers = new Transfer[]{EmptyTransfer.INSTANCE};
    m_viewer.addDragSupport(DND.DROP_MOVE, transfers, new DragSourceListener() {
      public void dragStart(DragSourceEvent event) {
        m_dragElements = getSelectedElements();
        m_dragCategory = m_dragElements.get(0) instanceof CategoryInfo;
        // check that we drag only categories or only entries
        for (Object element : m_dragElements) {
          if (m_dragCategory != element instanceof CategoryInfo) {
            event.doit = false;
          }
        }
      }

      public void dragSetData(DragSourceEvent event) {
      }

      public void dragFinished(DragSourceEvent event) {
      }
    });
    ViewerDropAdapter dropAdapter = new ViewerDropAdapter(m_viewer) {
      @Override
      protected int determineLocation(DropTargetEvent event) {
        if (event.item instanceof Item) {
          Item item = (Item) event.item;
          Point coordinates = m_viewer.getControl().toControl(event.x, event.y);
          Rectangle bounds = getBounds(item);
          // when drag entry, relation with category can be only ON
          if (!m_dragCategory && determineTarget(event) instanceof CategoryInfo) {
            return LOCATION_ON;
          }
          // in all other cases, drop before/after
          return coordinates.y < bounds.y + bounds.height / 2 ? LOCATION_BEFORE : LOCATION_AFTER;
        }
        return LOCATION_NONE;
      }

      @Override
      public boolean validateDrop(Object target, int operation, TransferData transferType) {
        // category can be dragged only relative other category
        if (m_dragCategory) {
          return target instanceof CategoryInfo;
        }
        // all other cases are valid
        return true;
      }

      @Override
      public boolean performDrop(Object data) {
        Object target = getCurrentTarget();
        int location = getCurrentLocation();
        if (m_dragCategory) {
          Assert.instanceOf(CategoryInfo.class, target);
          Assert.isTrue(location == LOCATION_BEFORE || location == LOCATION_AFTER);
          // prepare next category
          CategoryInfo nextCategory;
          {
            List<CategoryInfo> categories = m_palette.getCategories();
            int index = categories.indexOf(target);
            if (location == LOCATION_BEFORE) {
              nextCategory = categories.get(index);
            } else {
              nextCategory = GenericsUtils.getNextOrNull(categories, index);
            }
          }
          // add commands
          for (Object element : m_dragElements) {
            CategoryInfo category = (CategoryInfo) element;
            commands_add(new CategoryMoveCommand(category, nextCategory));
          }
        } else if (target instanceof CategoryInfo) {
          Assert.isTrue(location == LOCATION_ON);
          CategoryInfo targetCategory = (CategoryInfo) target;
          for (Object element : m_dragElements) {
            EntryInfo entry = (EntryInfo) element;
            commands_add(new EntryMoveCommand(entry, targetCategory, null));
          }
        } else {
          EntryInfo targetEntry = (EntryInfo) target;
          CategoryInfo targetCategory = targetEntry.getCategory();
          // prepare next entry
          EntryInfo nextEntry;
          {
            List<EntryInfo> entries = targetCategory.getEntries();
            int index = entries.indexOf(targetEntry);
            if (location == LOCATION_BEFORE) {
              nextEntry = entries.get(index);
            } else {
              nextEntry = GenericsUtils.getNextOrNull(entries, index);
            }
          }
          // add commands
          for (Object element : m_dragElements) {
            EntryInfo entry = (EntryInfo) element;
            commands_add(new EntryMoveCommand(entry, targetCategory, nextEntry));
          }
        }
        // refresh viewer to show result of applying commands
        refreshViewer();
        return true;
      }
    };
    dropAdapter.setScrollExpandEnabled(false);
    m_viewer.addDropSupport(DND.DROP_MOVE, transfers, dropAdapter);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link List} of {@link Command}'s to edit palette.
   */
  public List<Command> getCommands() {
    return m_commands;
  }

  /**
   * Adds given {@link Command} to the list (and executes it).
   */
  private void commands_add(Command command) {
    try {
      command.execute(m_palette);
      command.addToCommandList(m_commands);
    } catch (Throwable e) {
    }
  }
}
