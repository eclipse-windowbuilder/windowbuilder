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
import com.google.common.collect.Sets;

import org.eclipse.wb.core.editor.palette.model.CategoryInfo;
import org.eclipse.wb.core.editor.palette.model.PaletteInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.editor.Messages;
import org.eclipse.wb.internal.core.editor.palette.command.CategoryAddCommand;
import org.eclipse.wb.internal.core.editor.palette.command.Command;
import org.eclipse.wb.internal.core.editor.palette.command.ComponentAddCommand;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.core.utils.ui.TableFactory;
import org.eclipse.wb.internal.core.utils.ui.UiUtils;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

/**
 * Dialog choose entries from JAR archives.
 *
 * @author lobas_av
 * @coverage core.editor.palette.ui
 */
public class ImportArchiveDialog extends AbstractPaletteDialog {
  private static final int CHECK_ALL_ID = IDialogConstants.CLIENT_ID + 1;
  private static final int UNCHECK_ALL_ID = IDialogConstants.CLIENT_ID + 2;
  private static final String JAR_LIST_ID = "HISTORY_JAR_LIST";
  private static final String JAR_SUFFIX = ".jar";
  private static final String JAVA_BEAN_KEY = "Java-Bean";
  private static final String JAVA_BEAN_VALUE = "True";
  private static final String JAVA_BEAN_CLASS_SUFFIX = ".class";
  private static final int JAR_COMBO_SIZE = 10;
  //
  private final PaletteInfo m_palette;
  private final CategoryInfo m_initialCategory;
  //
  private Object m_initSelection;
  private Object[] m_initExpanded;
  //
  private Combo m_fileArchiveCombo;
  private CheckboxTableViewer m_classesViewer;
  private Button m_ignoreManifestButton;
  private ToolItem m_browseItem;
  private Menu m_browseMenu;
  private Label m_categoryLabel;
  private Combo m_categoryCombo;
  private Text m_categoryText;
  private Button m_checkButton;
  private Button m_uncheckButton;
  //
  private String m_jarPath;
  private List<PaletteElementInfo> m_elements = Collections.emptyList();
  private List<Command> m_commands = Collections.emptyList();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ImportArchiveDialog(Shell parentShell, PaletteInfo palette, CategoryInfo initialCategory) {
    super(parentShell,
        Messages.ImportArchiveDialog_shellTitle,
        Messages.ImportArchiveDialog_title,
        null,
        Messages.ImportArchiveDialog_message);
    setShellStyle(SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);
    // initial state
    m_palette = palette;
    m_initialCategory = initialCategory;
    //
    m_initSelection = EditorState.getActiveJavaInfo().getEditor().getJavaProject().getProject();
    m_initExpanded = new Object[]{m_initSelection};
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.addControlListener(new ControlAdapter() {
      private boolean m_handle;

      @Override
      public void controlResized(ControlEvent e) {
        if (m_handle) {
          Table table = m_classesViewer.getTable();
          Rectangle clientArea = getShell().getClientArea();
          if (clientArea.width > 50) {
            TableColumn[] columns = table.getColumns();
            columns[0].setWidth(clientArea.width - 30);
          }
        }
        m_handle = true;
      }
    });
  }

  @Override
  protected void createControls(Composite container) {
    GridLayoutFactory.create(container).columns(3);
    // title
    new Label(container, SWT.NONE).setText(Messages.ImportArchiveDialog_archiveLabel);
    // jar combo
    m_fileArchiveCombo = new Combo(container, SWT.READ_ONLY);
    GridDataFactory.create(m_fileArchiveCombo).fillH().grabH();
    UiUtils.setVisibleItemCount(m_fileArchiveCombo, JAR_COMBO_SIZE);
    // load history
    IDialogSettings settings = getDialogSettings();
    String[] jarHistory = settings.getArray(JAR_LIST_ID);
    if (jarHistory != null) {
      m_fileArchiveCombo.setItems(jarHistory);
    }
    //
    m_fileArchiveCombo.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        chooseFromText(m_fileArchiveCombo.getText());
      }
    });
    // choose buttons
    ToolBar browseToolBar = new ToolBar(container, SWT.FLAT | SWT.RIGHT);
    // choose menu
    m_browseItem = new ToolItem(browseToolBar, SWT.DROP_DOWN);
    m_browseItem.setToolTipText(Messages.ImportArchiveDialog_browseToolTip);
    m_browseItem.setImage(DesignerPlugin.getImage("palette/category.gif"));
    //
    m_browseMenu = new Menu(browseToolBar);
    // Classpath
    MenuItem classpathItem = new MenuItem(m_browseMenu, SWT.NONE);
    classpathItem.setText(Messages.ImportArchiveDialog_classpathItem);
    classpathItem.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        chooseFromClasspath();
      }
    });
    // Workspace
    MenuItem workspaceItem = new MenuItem(m_browseMenu, SWT.NONE);
    workspaceItem.setText(Messages.ImportArchiveDialog_workspaceItem);
    workspaceItem.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        chooseFromWorkspace();
      }
    });
    // FileSystem
    MenuItem filesystemItem = new MenuItem(m_browseMenu, SWT.NONE);
    filesystemItem.setText(Messages.ImportArchiveDialog_fileSystemItem);
    filesystemItem.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        chooseFromFilesystem();
      }
    });
    //
    m_browseItem.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        // prepare menu bounds
        Rectangle bounds = m_browseItem.getBounds();
        Point location =
            m_browseItem.getParent().toDisplay(new Point(bounds.x, bounds.y + bounds.height));
        m_browseMenu.setLocation(location.x, location.y);
        // show context menu
        m_browseMenu.setVisible(true);
      }
    });
    // manifest button
    m_ignoreManifestButton = new Button(container, SWT.CHECK);
    GridDataFactory.create(m_ignoreManifestButton).spanH(3);
    m_ignoreManifestButton.setText(Messages.ImportArchiveDialog_ignoreManifestFlag);
    // classes viewer
    m_classesViewer = CheckboxTableViewer.newCheckList(container, SWT.BORDER | SWT.FULL_SELECTION);
    GridDataFactory.create(m_classesViewer.getControl()).fill().grab().spanH(3).hint(300, 150);
    TableFactory.modify(m_classesViewer).headerVisible(true).linesVisible(true);
    TableFactory.modify(m_classesViewer).newColumn().text(
        Messages.ImportArchiveDialog_classesColumn).width(getInitialSize().x - 30);
    m_classesViewer.setContentProvider(new ArrayContentProvider());
    m_classesViewer.setLabelProvider(new LabelProvider());
    m_classesViewer.addCheckStateListener(new ICheckStateListener() {
      public void checkStateChanged(CheckStateChangedEvent event) {
        calculateFinish();
      }
    });
    // category title
    m_categoryLabel = new Label(container, SWT.NONE);
    m_categoryLabel.setText(Messages.ImportArchiveDialog_categoryLabel);
    // category combo
    m_categoryCombo = new Combo(container, SWT.READ_ONLY);
    GridDataFactory.create(m_categoryCombo).fillH().grabH().spanH(2);
    UiUtils.setVisibleItemCount(m_categoryCombo, 15);
    // load categories
    m_categoryCombo.add(Messages.ImportArchiveDialog_categoryNew);
    for (CategoryInfo category : m_palette.getCategories()) {
      m_categoryCombo.add(category.getName());
    }
    if (m_initialCategory == null) {
      m_categoryCombo.select(0);
    } else {
      m_categoryCombo.select(m_palette.getCategories().indexOf(m_initialCategory) + 1);
    }
    //
    m_categoryCombo.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        calculateFinish();
      }
    });
    //
    new Label(container, SWT.NONE);
    // category name text
    m_categoryText = new Text(container, SWT.BORDER);
    GridDataFactory.create(m_categoryText).fillH().grabH().spanH(2);
    m_categoryText.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        calculateFinish();
      }
    });
  }

  @Override
  protected void createButtonsForButtonBar(Composite parent) {
    m_checkButton =
        createButton(parent, CHECK_ALL_ID, Messages.ImportArchiveDialog_selectAllButton, false);
    m_uncheckButton =
        createButton(parent, UNCHECK_ALL_ID, Messages.ImportArchiveDialog_deselectAllButton, false);
    super.createButtonsForButtonBar(parent);
    calculateFinish();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Handle
  //
  ////////////////////////////////////////////////////////////////////////////
  private void calculateFinish() {
    // unchecked state
    Object[] checkedElements = m_classesViewer.getCheckedElements();
    m_uncheckButton.setEnabled(checkedElements.length > 0);
    // checked state
    m_checkButton.setEnabled(checkedElements.length < m_elements.size());
    // category state
    boolean catalogEnabled = !m_elements.isEmpty();
    m_categoryLabel.setEnabled(catalogEnabled);
    m_categoryCombo.setEnabled(catalogEnabled);
    m_categoryText.setEnabled(catalogEnabled && m_categoryCombo.getSelectionIndex() == 0);
    //
    validateAll();
  }

  @Override
  protected String validate() throws Exception {
    // check choose content
    if (m_elements.isEmpty()) {
      return Messages.ImportArchiveDialog_validateSelectArchive;
    }
    // check selected content
    Object[] checkedElements = m_classesViewer.getCheckedElements();
    if (checkedElements.length == 0) {
      return Messages.ImportArchiveDialog_validateSelectClass;
    }
    // check category name
    String categoryName = m_categoryText.getText();
    if (m_categoryCombo.getSelectionIndex() == 0 && categoryName.length() == 0) {
      return Messages.ImportArchiveDialog_validateEmptyCategoryName;
    }
    //
    return null;
  }

  @Override
  protected void buttonPressed(int buttonId) {
    if (buttonId == CHECK_ALL_ID || buttonId == UNCHECK_ALL_ID) {
      // handle checked
      m_classesViewer.setAllChecked(buttonId == CHECK_ALL_ID);
      calculateFinish();
    } else {
      // save history
      IDialogSettings settings = getDialogSettings();
      settings.put(JAR_LIST_ID, m_fileArchiveCombo.getItems());
      // handle finish
      if (buttonId == IDialogConstants.OK_ID) {
        checkClasspath();
        createCommands();
      }
      super.buttonPressed(buttonId);
    }
  }

  private void checkClasspath() {
    try {
      IJavaProject javaProject = EditorState.getActiveJavaInfo().getEditor().getJavaProject();
      // check load classes
      boolean addToClassPath = false;
      for (PaletteElementInfo element : m_elements) {
        if (javaProject.findType(element.className) == null) {
          addToClassPath = true;
          break;
        }
      }
      if (addToClassPath) {
        addToClassPath =
            MessageDialog.openQuestion(
                getShell(),
                Messages.ImportArchiveDialog_JarNotInClasspathTitle,
                MessageFormat.format(
                    Messages.ImportArchiveDialog_JarNotInClasspathMessage,
                    m_jarPath.toString(),
                    m_jarPath.toString()));
        if (addToClassPath) {
          // copy library to project and add to .classpath
          ProjectUtils.addJar(javaProject, m_jarPath, null);
        }
      }
    } catch (Throwable t) {
      DesignerPlugin.log(t);
    }
  }

  private void createCommands() {
    m_commands = Lists.newArrayList();
    // handle category command
    int categoryIndex = m_categoryCombo.getSelectionIndex();
    CategoryInfo category = null;
    if (categoryIndex == 0) {
      // create new category
      String name = m_categoryText.getText();
      String id = name + "_" + Long.toString(System.currentTimeMillis());
      String description =
          MessageFormat.format(Messages.ImportArchiveDialog_newCategoryDescription, m_jarPath);
      m_commands.add(new CategoryAddCommand(id, name, description, true, true, null));
      category = new CategoryInfo(id);
    } else {
      // prepare exist category
      category = m_palette.getCategories().get(categoryIndex - 1);
    }
    // handle components command
    for (Object checkedElement : m_classesViewer.getCheckedElements()) {
      PaletteElementInfo element = (PaletteElementInfo) checkedElement;
      String description =
          MessageFormat.format(
              Messages.ImportArchiveDialog_newComponentDescription,
              element.className);
      m_commands.add(new ComponentAddCommand(element.className
          + "_"
          + Long.toString(System.currentTimeMillis()),
          element.name,
          description,
          true,
          element.className,
          category));
    }
  }

  private void chooseFromClasspath() {
    try {
      //prepare classpath projects
      HashSet<IProject> includeObjects = Sets.newHashSet();
      IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
      IJavaProject javaProject = EditorState.getActiveJavaInfo().getEditor().getJavaProject();
      includeObjects.add(javaProject.getProject());
      for (String project : javaProject.getRequiredProjectNames()) {
        includeObjects.add(workspaceRoot.getProject(project));
      }
      // open dialog
      chooseFromWorkspace(
          Messages.ImportArchiveDialog_classpathJarSelection,
          new JarFileFilter(includeObjects));
    } catch (Throwable e) {
      DesignerPlugin.log(e);
    }
  }

  private void chooseFromWorkspace() {
    chooseFromWorkspace(Messages.ImportArchiveDialog_workspaceJarSelection, new JarFileFilter());
  }

  private void chooseFromWorkspace(String title, JarFileFilter filter) {
    // prepare dialog
    JarSelectionDialog dialog =
        new JarSelectionDialog(getShell(),
            new WorkbenchLabelProvider(),
            new WorkbenchContentProvider());
    dialog.setTitle(title);
    dialog.setMessage(Messages.ImportArchiveDialog_choosefromWorkspaceMessage);
    dialog.addFilter(filter);
    // sets initial settings
    if (m_initSelection != null) {
      dialog.setInitialSelection(m_initSelection);
    }
    //
    dialog.setInitialExpanded(m_initExpanded);
    dialog.setInput(ResourcesPlugin.getWorkspace());
    // open dialog
    dialog.open();
    // save state
    m_initSelection = dialog.getSelection();
    m_initExpanded = dialog.getExpandedElements();
    // parse result
    Object[] elements = dialog.getResult();
    if (elements == null || elements.length == 0) {
      return;
    }
    IFile jarFile = null;
    for (int i = 0; i < elements.length; i++) {
      Object element = elements[i];
      if (filter.select(null, null, element)) {
        jarFile = (IFile) element;
        break;
      }
    }
    if (jarFile == null) {
      return;
    }
    // handle archive
    chooseArchive(jarFile, null);
  }

  private void chooseFromFilesystem() {
    // prepare dialog
    FileDialog dialog = new FileDialog(getShell());
    dialog.setFilterExtensions(new String[]{"*.jar"});
    // open dialog
    String jarPath = dialog.open();
    // handle archive
    if (jarPath != null) {
      chooseFromText(jarPath);
    }
  }

  private void chooseFromText(String jarName) {
    // find file over direct file system name
    File jarFile = new File(jarName);
    if (jarFile.exists()) {
      chooseArchive(null, jarFile);
    } else {
      // find file into workspace
      IResource res = ResourcesPlugin.getWorkspace().getRoot().findMember(jarName);
      jarFile = new File(res.getLocation().toString());
      if (jarFile.exists()) {
        chooseArchive(null, jarFile);
      }
    }
  }

  private void chooseArchive(IFile jarIFile, File jarFile) {
    try {
      // prepare path
      boolean canFile = jarIFile == null;
      m_jarPath = canFile ? jarFile.getAbsolutePath() : jarIFile.getLocation().toPortableString();
      // load elements over manifest
      boolean ignoreManifest = m_ignoreManifestButton.getSelection();
      m_elements = Collections.emptyList();
      if (!ignoreManifest) {
        JarInputStream jarStream =
            new JarInputStream(canFile ? new FileInputStream(jarFile) : jarIFile.getContents(true));
        m_elements = extractElementsFromJarByManifest(jarStream);
        jarStream.close();
      }
      // check load all elements
      if (ignoreManifest || m_elements.isEmpty()) {
        if (!ignoreManifest) {
          String message =
              MessageFormat.format(Messages.ImportArchiveDialog_hasManifestMessage, m_jarPath);
          ignoreManifest =
              MessageDialog.openQuestion(
                  getShell(),
                  Messages.ImportArchiveDialog_hasManifestTitle,
                  message);
        }
        if (ignoreManifest) {
          JarInputStream jarStream =
              new JarInputStream(canFile
                  ? new FileInputStream(jarFile)
                  : jarIFile.getContents(true));
          m_elements = extractElementsFromJarAllClasses(jarStream);
          jarStream.close();
        }
      }
      // sets elements
      m_classesViewer.setInput(m_elements.toArray());
      // handle jar combo
      int removeIndex = m_fileArchiveCombo.indexOf(m_jarPath);
      if (removeIndex != -1) {
        m_fileArchiveCombo.remove(removeIndex);
      }
      m_fileArchiveCombo.add(m_jarPath, 0);
      int archiveCount = m_fileArchiveCombo.getItemCount();
      if (archiveCount > JAR_COMBO_SIZE) {
        m_fileArchiveCombo.remove(JAR_COMBO_SIZE, archiveCount - 1);
      }
      if (!m_fileArchiveCombo.getText().equals(m_jarPath)) {
        m_fileArchiveCombo.setText(m_jarPath);
        m_fileArchiveCombo.setSelection(new Point(0, m_jarPath.length()));
      }
      // handle category
      if (m_elements.isEmpty()) {
        m_categoryText.setText("");
      } else {
        // convert 'foo.jar' to 'foo'
        String categoryName = canFile ? jarFile.getName() : jarIFile.getName();
        m_categoryText.setText(categoryName.substring(
            0,
            categoryName.length() - JAR_SUFFIX.length()));
      }
    } catch (Throwable t) {
      m_jarPath = null;
      m_elements = Collections.emptyList();
      m_classesViewer.setInput(ArrayUtils.EMPTY_OBJECT_ARRAY);
      m_categoryText.setText("");
    } finally {
      calculateFinish();
    }
  }

  private List<PaletteElementInfo> extractElementsFromJarAllClasses(JarInputStream jarStream)
      throws Exception {
    // load all classes
    List<PaletteElementInfo> elements = Lists.newArrayList();
    try {
      while (true) {
        JarEntry jarEntry = jarStream.getNextJarEntry();
        if (jarEntry == null) {
          break;
        }
        String jarEntryName = jarEntry.getName();
        if (jarEntryName.endsWith(JAVA_BEAN_CLASS_SUFFIX)) {
          // convert 'aaa/bbb/ccc.class' to 'aaa.bbb.ccc'
          PaletteElementInfo element = new PaletteElementInfo();
          element.className =
              StringUtils.substringBeforeLast(jarEntryName, JAVA_BEAN_CLASS_SUFFIX).replace(
                  '/',
                  '.');
          element.name = CodeUtils.getShortClass(element.className);
          elements.add(element);
        }
      }
    } catch (Throwable e) {
      DesignerPlugin.log(e);
    }
    // sort element over class name
    Collections.sort(elements, new Comparator<PaletteElementInfo>() {
      public int compare(PaletteElementInfo element0, PaletteElementInfo element1) {
        return element0.className.compareToIgnoreCase(element1.className);
      }
    });
    return elements;
  }

  private List<PaletteElementInfo> extractElementsFromJarByManifest(JarInputStream jarStream)
      throws Exception {
    List<PaletteElementInfo> elements = Lists.newArrayList();
    Manifest manifest = jarStream.getManifest();
    // check manifest, if null find it
    if (manifest == null) {
      try {
        while (true) {
          JarEntry entry = jarStream.getNextJarEntry();
          if (entry == null) {
            break;
          }
          if (JarFile.MANIFEST_NAME.equalsIgnoreCase(entry.getName())) {
            // read manifest data
            byte[] buffer = IOUtils.toByteArray(jarStream);
            jarStream.closeEntry();
            // create manifest
            manifest = new Manifest(new ByteArrayInputStream(buffer));
            break;
          }
        }
      } catch (Throwable e) {
        DesignerPlugin.log(e);
        manifest = null;
      }
    }
    if (manifest != null) {
      // extract all "Java-Bean: True" classes
      for (Iterator<Map.Entry<String, Attributes>> I = manifest.getEntries().entrySet().iterator(); I.hasNext();) {
        Map.Entry<String, Attributes> mapElement = I.next();
        Attributes attributes = mapElement.getValue();
        if (JAVA_BEAN_VALUE.equalsIgnoreCase(attributes.getValue(JAVA_BEAN_KEY))) {
          String beanClass = mapElement.getKey();
          if (beanClass == null || beanClass.length() <= JAVA_BEAN_CLASS_SUFFIX.length()) {
            continue;
          }
          // convert 'aaa/bbb/ccc.class' to 'aaa.bbb.ccc'
          PaletteElementInfo element = new PaletteElementInfo();
          element.className =
              StringUtils.substringBeforeLast(beanClass, JAVA_BEAN_CLASS_SUFFIX).replace('/', '.');
          element.name = CodeUtils.getShortClass(element.className);
          elements.add(element);
        }
      }
    }
    return elements;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Command
  //
  ////////////////////////////////////////////////////////////////////////////
  public List<Command> getCommands() {
    return m_commands;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // LabelProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  private static class LabelProvider extends org.eclipse.jface.viewers.LabelProvider
      implements
        ITableLabelProvider {
    public String getColumnText(Object element, int columnIndex) {
      PaletteElementInfo paletteElement = (PaletteElementInfo) element;
      return paletteElement.name;
    }

    public Image getColumnImage(Object element, int columnIndex) {
      return null;
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Dialog for choose workspace JAR.
  //
  ////////////////////////////////////////////////////////////////////////////
  private static class JarSelectionDialog extends ElementTreeSelectionDialog {
    private Object[] m_expanded;
    private Object m_selection;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public JarSelectionDialog(Shell parent,
        ILabelProvider labelProvider,
        ITreeContentProvider contentProvider) {
      super(parent, labelProvider, contentProvider);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Access
    //
    ////////////////////////////////////////////////////////////////////////////
    public Object[] getExpandedElements() {
      return m_expanded;
    }

    public Object getSelection() {
      return m_selection;
    }

    public void setInitialExpanded(Object[] initExpanded) {
      m_expanded = initExpanded;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // GUI
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    protected TreeViewer createTreeViewer(Composite parent) {
      TreeViewer viewer = super.createTreeViewer(parent);
      if (m_expanded != null && m_expanded.length > 0) {
        viewer.setExpandedElements(m_expanded);
      }
      return viewer;
    }

    @Override
    protected void buttonPressed(int buttonId) {
      // save expanded state
      TreeViewer viewer = getTreeViewer();
      m_expanded = viewer.getExpandedElements();
      // save selection state
      IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
      m_selection = selection.getFirstElement();
      // super
      super.buttonPressed(buttonId);
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // @see org.eclipse.pde.internal.ui.editor.build.JARFileFilter
  //
  ////////////////////////////////////////////////////////////////////////////
  private static class JarFileFilter extends ViewerFilter {
    private final Collection<?> m_includeObjects;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructors
    //
    ////////////////////////////////////////////////////////////////////////////
    public JarFileFilter() {
      this(null);
    }

    public JarFileFilter(Collection<?> includeObjects) {
      m_includeObjects = includeObjects;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // ViewerFilter
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean select(Viewer viewer, Object parent, Object element) {
      // filter files
      if (element instanceof IFile) {
        IFile file = (IFile) element;
        return "jar".equals(file.getProjectRelativePath().getFileExtension());
      }
      // filter containers
      if (element instanceof IContainer) {
        // check "included" project
        if (m_includeObjects != null
            && element instanceof IProject
            && !m_includeObjects.contains(element)) {
          return false;
        }
        // filter sub folders
        try {
          if (!((IContainer) element).isAccessible()) {
            return false;
          }
          IResource[] resources = ((IContainer) element).members();
          for (int i = 0; i < resources.length; i++) {
            if (select(viewer, parent, resources[i])) {
              return true;
            }
          }
        } catch (CoreException e) {
          DesignerPlugin.log(e);
        }
      }
      return false;
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Palette
  //
  ////////////////////////////////////////////////////////////////////////////
  private static class PaletteElementInfo {
    public String className;
    public String name;
  }
}