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
package org.eclipse.wb.internal.rcp.model.rcp.perspective;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.eclipse.wb.core.editor.palette.PaletteEventListener;
import org.eclipse.wb.core.editor.palette.model.CategoryInfo;
import org.eclipse.wb.core.editor.palette.model.entry.SelectionToolEntryInfo;
import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.association.AssociationObjects;
import org.eclipse.wb.core.model.broadcast.JavaInfoTreeAlmostComplete;
import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.JavaInfoUtils.IMoveTargetProvider;
import org.eclipse.wb.internal.core.model.TopBoundsSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.MethodDescription;
import org.eclipse.wb.internal.core.model.generation.statement.block.BlockStatementGenerator;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.converter.FloatConverter;
import org.eclipse.wb.internal.core.model.property.converter.StringConverter;
import org.eclipse.wb.internal.core.model.variable.LocalUniqueVariableSupport;
import org.eclipse.wb.internal.core.utils.GenericsUtils;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.core.utils.ui.TabFolderDecorator;
import org.eclipse.wb.internal.rcp.model.rcp.ExtensionPropertyHelper;
import org.eclipse.wb.internal.rcp.model.rcp.PdeUtils;
import org.eclipse.wb.internal.rcp.model.rcp.PdeUtils.PerspectiveInfo;
import org.eclipse.wb.internal.rcp.model.rcp.PdeUtils.ViewCategoryInfo;
import org.eclipse.wb.internal.rcp.model.rcp.PdeUtils.ViewInfo;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.shortcuts.FastViewContainerInfo;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.shortcuts.PerspectiveShortcutContainerInfo;
import org.eclipse.wb.internal.rcp.model.rcp.perspective.shortcuts.ViewShortcutContainerInfo;
import org.eclipse.wb.internal.rcp.palette.PerspectivePerspectiveDropEntryInfo;
import org.eclipse.wb.internal.rcp.palette.PerspectiveViewDropEntryInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Model for {@link IPageLayout}, used in {@link IPerspectiveFactory}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.rcp
 */
public final class PageLayoutInfo extends AbstractComponentInfo {
  private final EditorAreaInfo m_editorArea = new EditorAreaInfo(this);
  private final ViewShortcutContainerInfo m_viewShortcutContainer =
      new ViewShortcutContainerInfo(this);
  private final PerspectiveShortcutContainerInfo m_perspectiveShortcutContainer =
      new PerspectiveShortcutContainerInfo(this);
  private final FastViewContainerInfo m_fastViewContainer = new FastViewContainerInfo(this);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public PageLayoutInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
    // move non-visual containers to the end
    addBroadcastListener(new JavaInfoTreeAlmostComplete() {
      @Override
      public void invoke(JavaInfo root, List<JavaInfo> components) throws Exception {
        moveChild(m_fastViewContainer, null);
        moveChild(m_viewShortcutContainer, null);
        moveChild(m_perspectiveShortcutContainer, null);
        removeBroadcastListener(this);
      }
    });
    // palette
    addPaletteListener();
    contributeExtensionProperty();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties
  //
  ////////////////////////////////////////////////////////////////////////////
  private void contributeExtensionProperty() throws Exception {
    new ExtensionPropertyHelper(this, "org.eclipse.ui.perspectives", "perspective") {
      @Override
      protected Property[] createProperties() {
        return new Property[]{
            createStringProperty("name"),
            createIconProperty("icon"),
            createBooleanProperty("fixed", false)};
      }
    };
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Palette
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * {@link PageLayoutInfo} uses toolkit of RCP, but it does not need SWT, JFace components on
   * palette. It needs only views and perspectives.
   */
  private void addPaletteListener() {
    addBroadcastListener(new PaletteEventListener() {
      @Override
      public void canEdit(boolean[] canEdit) {
        canEdit[0] = false;
      }

      @Override
      public void categories(List<CategoryInfo> categories) throws Exception {
        categories.clear();
        // create "System" category
        {
          CategoryInfo category = new CategoryInfo("system");
          category.setName("System");
          category.setDescription("System tools");
          category.setOpen(true);
          categories.add(category);
          // add entries
          {
            SelectionToolEntryInfo entry = new SelectionToolEntryInfo();
            entry.setId("system.selection");
            category.addEntry(entry);
          }
        }
        // prepare sorted list of view categories
        List<ViewCategoryInfo> viewCategories;
        {
          viewCategories = PdeUtils.getViewCategories();
          Collections.sort(viewCategories, new Comparator<ViewCategoryInfo>() {
            @Override
            public int compare(ViewCategoryInfo o1, ViewCategoryInfo o2) {
              return getName(o1).compareTo(getName(o2));
            }

            private String getName(ViewCategoryInfo o1) {
              String name = o1.getName();
              name = StringUtils.remove(name, '&');
              return name;
            }
          });
        }
        // create "Views" categories
        for (ViewCategoryInfo categoryInfo : viewCategories) {
          CategoryInfo category = new CategoryInfo();
          category.setId(getIdForPaletteCategory(categoryInfo));
          category.setName(categoryInfo.getName());
          category.setDescription(categoryInfo.getId());
          category.setOpen(true);
          categories.add(category);
          // add views
          for (ViewInfo viewInfo : categoryInfo.getViews()) {
            category.addEntry(new PerspectiveViewDropEntryInfo(viewInfo));
          }
        }
        // create "Perspectives" category
        {
          CategoryInfo category = new CategoryInfo("perspectives");
          category.setName("Perspectives");
          category.setOpen(true);
          categories.add(category);
          // add entries
          for (PerspectiveInfo perspective : PdeUtils.getPerspectives()) {
            category.addEntry(new PerspectivePerspectiveDropEntryInfo(perspective));
          }
        }
      }

      private String getIdForPaletteCategory(ViewCategoryInfo categoryInfo) {
        String id = categoryInfo.getId();
        if (id == null) {
          id = "other";
        }
        return id;
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link AbstractPartInfo} children.
   */
  public List<AbstractPartInfo> getParts() {
    return getChildren(AbstractPartInfo.class);
  }

  public EditorAreaInfo getEditorArea() {
    return m_editorArea;
  }

  public FastViewContainerInfo getFastViewContainer() {
    return m_fastViewContainer;
  }

  public ViewShortcutContainerInfo getViewShortcutContainer() {
    return m_viewShortcutContainer;
  }

  public PerspectiveShortcutContainerInfo getPerspectiveShortcutContainer() {
    return m_perspectiveShortcutContainer;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // AbstractComponentInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected TopBoundsSupport createTopBoundsSupport() {
    return new PageLayoutTopBoundsSupport(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Hierarchy
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean canBeRoot() {
    return true;
  }

  @Override
  public Object getComponentObject() {
    return m_composite;
  }

  /**
   * @return the {@link Composite} that is used as top level container editor/views/folders.
   */
  public Composite getPartsComposite() {
    return m_partsComposite;
  }

  /**
   * @return the {@link Composite} that is used as top level container for {@link PageLayoutInfo}
   *         GUI.
   */
  public Composite getComposite() {
    return m_composite;
  }

  /**
   * @return the {@link PageLayoutInfo}'s Shell.
   */
  Shell getShell() {
    return m_shell;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  private Shell m_shell;
  private Composite m_composite;
  private Composite m_partsComposite;
  private final Map<String, Control> m_idToControlMap = Maps.newHashMap();

  /**
   * Prepares widgets of this {@link IPageLayout} for filling.
   */
  void render() throws Exception {
    m_shell = new Shell(SWT.SHELL_TRIM | SWT.APPLICATION_MODAL);
    m_shell.setLayout(new FillLayout());
    //
    m_composite = new Composite(m_shell, SWT.NONE);
    GridLayoutFactory.create(m_composite).columns(2).noMargins().noSpacing();
    // create parent for perspective parts: views and folders
    {
      m_partsComposite = new Composite(m_composite, SWT.NONE);
      GridDataFactory.create(m_partsComposite).grab().fill();
    }
    // create shortcuts
    {
      Composite shortcutsComposite = new Composite(m_composite, SWT.NONE);
      GridDataFactory.create(shortcutsComposite).grabV().fillV();
      GridLayoutFactory.create(shortcutsComposite).marginsH(1).marginsV(0).spacingH(0).spacingV(20);
      // create view shortcuts
      {
        Control control = m_viewShortcutContainer.render(shortcutsComposite);
        GridDataFactory.create(control).grabV().fillV();
      }
      // create perspective shortcuts
      {
        Control control = m_perspectiveShortcutContainer.render(shortcutsComposite);
        GridDataFactory.create(control).grabV().fillV();
      }
    }
    // create fast views
    {
      Control control = m_fastViewContainer.render(m_composite);
      GridDataFactory.create(control).grabH().fillH();
    }
    // add editor area
    {
      m_editorArea.render(m_partsComposite);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Layout
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets new {@link SashLineInfo} for given {@link AbstractPartInfo}.
   */
  private void setSashLine(AbstractPartInfo part,
      Control partControl,
      Rectangle refBounds,
      int relation) {
    Rectangle partBounds = new Rectangle(partControl.getBounds());
    int sashSize = SashLineInfo.SASH_SIZE;
    // create line
    SashLineInfo sashLine = null;
    if (relation == IPageLayout.LEFT) {
      sashLine =
          new SashLineInfo(part,
              partBounds,
              refBounds,
              IPositionConstants.EAST,
              new Rectangle(partBounds.right(), partBounds.y, sashSize, partBounds.height));
    } else if (relation == IPageLayout.RIGHT) {
      sashLine =
          new SashLineInfo(part,
              partBounds,
              refBounds,
              IPositionConstants.WEST,
              new Rectangle(partBounds.x - sashSize, partBounds.y, sashSize, partBounds.height));
    } else if (relation == IPageLayout.TOP) {
      sashLine =
          new SashLineInfo(part,
              partBounds,
              refBounds,
              IPositionConstants.SOUTH,
              new Rectangle(partBounds.x, partBounds.bottom(), partBounds.width, sashSize));
    } else if (relation == IPageLayout.BOTTOM) {
      sashLine =
          new SashLineInfo(part,
              partBounds,
              refBounds,
              IPositionConstants.NORTH,
              new Rectangle(partBounds.x, partBounds.y - sashSize, partBounds.width, sashSize));
    }
    // set line
    part.setLine(sashLine);
  }

  /**
   * Layouts top-level {@link AbstractPartInfo}'s using ratio/refId information.
   */
  void layoutPerspectiveParts() {
    Dimension fullSize = new Dimension(m_partsComposite.getSize().x, m_partsComposite.getSize().y);
    // layout "editor area"
    {
      Control editorControl = m_editorArea.getControl();
      editorControl.setBounds(0, 0, fullSize.width, fullSize.height);
    }
    // XXX
    boolean editorAreaVisible = true;
    try {
      editorAreaVisible = (Boolean) getPropertyByTitle("editorAreaVisible").getValue();
    } catch (Throwable e) {
    }
    // layout views/folders
    List<AbstractPartInfo> parts = getParts();
    // prepare last AbstractPart_Info that references "editor area"
    AbstractPartInfo lastPart = null;
    for (AbstractPartInfo part : parts) {
      String refId = part.getRefId();
      if (IPageLayout.ID_EDITOR_AREA.equals(refId)) {
        lastPart = part;
      }
    }
    // do layout AbstractPart_Info's
    for (AbstractPartInfo part : parts) {
      String id = part.getId();
      int relationship = part.getRelationship();
      float ratio = part.getRatio();
      String refId = part.getRefId();
      boolean isLastEditorRef = part == lastPart;
      boolean takeFullEditorSize = isLastEditorRef && !editorAreaVisible;
      //
      Control partControl = m_idToControlMap.get(id);
      Control refControl = m_idToControlMap.get(refId);
      if (refControl != null) {
        org.eclipse.swt.graphics.Rectangle refBounds = refControl.getBounds();
        //
        int sashSize = SashLineInfo.SASH_SIZE;
        if (relationship == IPageLayout.LEFT) {
          int width = calculatePartSize(refBounds.width, ratio, takeFullEditorSize);
          partControl.setBounds(refBounds.x, refBounds.y, width, refBounds.height);
          refControl.setBounds(refBounds.x + width + sashSize, refBounds.y, refBounds.width
              - width
              - sashSize, refBounds.height);
        } else if (relationship == IPageLayout.RIGHT) {
          int width = calculatePartSize(refBounds.width, 1 - ratio, takeFullEditorSize);
          partControl.setBounds(
              refBounds.x + refBounds.width - width,
              refBounds.y,
              width,
              refBounds.height);
          refControl.setBounds(
              refBounds.x,
              refBounds.y,
              refBounds.width - width - sashSize,
              refBounds.height);
        } else if (relationship == IPageLayout.TOP) {
          int height = calculatePartSize(refBounds.height, ratio, takeFullEditorSize);
          partControl.setBounds(refBounds.x, refBounds.y, refBounds.width, height);
          refControl.setBounds(
              refBounds.x,
              refBounds.y + height + sashSize,
              refBounds.width,
              refBounds.height - height - sashSize);
        } else if (relationship == IPageLayout.BOTTOM) {
          int height = calculatePartSize(refBounds.height, 1 - ratio, takeFullEditorSize);
          partControl.setBounds(
              refBounds.x,
              refBounds.y + refBounds.height - height,
              refBounds.width,
              height);
          refControl.setBounds(refBounds.x, refBounds.y, refBounds.width, refBounds.height
              - height
              - sashSize);
        } else {
          throw new IllegalArgumentException("Unknown relationship: " + relationship);
        }
        // add SashLineInfo
        setSashLine(part, partControl, new Rectangle(refBounds), relationship);
      }
    }
  }

  /**
   * Calculate size of part using size of referenced part, ration and <code>refId</code>. If
   * <code>refId</code> is editor and editor marked as invisible, make editor small, but still
   * visible.
   */
  private static int calculatePartSize(int refSize, float ratio, boolean takeFullSize) {
    if (takeFullSize) {
      return refSize;
    }
    return (int) (refSize * ratio);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link CTabFolder} configured to look as editor/view of perspective.
   */
  public static CTabFolder createPartFolder(Composite parent) {
    CTabFolder folder = new CTabFolder(parent, SWT.BORDER);
    // configure look
    folder.setUnselectedCloseVisible(true);
    folder.setMaximizeVisible(true);
    folder.setMinimizeVisible(true);
    folder.setSimple(false);
    // configure colors
    TabFolderDecorator.setActiveTabColors(true, folder);
    return folder;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void refresh_dispose() throws Exception {
    super.refresh_dispose();
    if (m_shell != null) {
      m_shell.dispose();
      m_shell = null;
      m_composite = null;
      m_partsComposite = null;
      m_idToControlMap.clear();
    }
  }

  @Override
  protected void refresh_afterCreate() throws Exception {
    {
      m_idToControlMap.clear();
      m_idToControlMap.put(m_editorArea.getId(), m_editorArea.getControl());
      for (AbstractPartInfo part : getParts()) {
        part.registerLayoutControls(m_idToControlMap);
      }
    }
    super.refresh_afterCreate();
  }

  @Override
  protected void refresh_fetch() throws Exception {
    ControlInfo.refresh_fetch(this, new RunnableEx() {
      @Override
      public void run() throws Exception {
        PageLayoutInfo.super.refresh_fetch();
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Interface for implementing any {@link PageLayoutInfo} command, that operates with
   * {@link AbstractPartInfo}'s.
   */
  private interface IPageLayoutCommand<T> {
    T execute(AstEditor editor, String layoutSource, StatementTarget target, ObjectInfo nextChild)
        throws Exception;
  }

  /**
   * Creates new {@link PageLayoutAddViewInfo}.
   *
   * @param command
   *          the {@link IPageLayoutCommand} that performs operation using prepared parameters.
   * @param reference
   *          the top level element of {@link PageLayoutInfo} to reference.
   * @return the created {@link PageLayoutAddViewInfo}.
   */
  public <T> T command(IPageLayoutCommand<T> command, IPageLayoutTopLevelInfo reference)
      throws Exception {
    AstEditor editor = getEditor();
    // prepare "createInitialLayout" method
    MethodDeclaration layoutMethod;
    {
      layoutMethod =
          AstNodeUtils.getMethodBySignature(
              JavaInfoUtils.getTypeDeclaration(this),
              "createInitialLayout(org.eclipse.ui.IPageLayout)");
      Assert.isNotNull(layoutMethod, "No createInitialLayout() method in %s.", editor.getSource());
    }
    // prepare target for
    StatementTarget target;
    AbstractPartInfo prevChild;
    {
      // find last AbstractPart_Info that references "reference"
      AbstractPartInfo lastRefPart;
      {
        String referenceId = reference.getId();
        lastRefPart = null;
        for (AbstractPartInfo part : getParts()) {
          if (part.getRefId().equals(referenceId)) {
            lastRefPart = part;
          }
        }
      }
      // prepare target relative to last AbstractPart_Info
      if (lastRefPart != null) {
        prevChild = lastRefPart;
        target = getTargetAfter(prevChild);
      } else {
        if (reference instanceof EditorAreaInfo) {
          prevChild = GenericsUtils.getLastOrNull(getParts());
          target = new StatementTarget(layoutMethod, false);
        } else {
          prevChild = (AbstractPartInfo) reference;
          target = getTargetAfter(prevChild);
        }
      }
    }
    // OK, we prepared everything, execute command
    String layoutSource = DomGenerics.parameters(layoutMethod).get(0).getName().getIdentifier();
    ObjectInfo nextChild = GenericsUtils.getNextOrNull(getChildren(), prevChild);
    return command.execute(editor, layoutSource, target, nextChild);
  }

  /**
   * @return the {@link StatementTarget} for position after given {@link AbstractPartInfo}.
   */
  private static StatementTarget getTargetAfter(AbstractPartInfo part) throws Exception {
    PageLayoutInfo page = part.getPage();
    JavaInfo nextChild = GenericsUtils.getNextOrNull(page.getChildrenJava(), part);
    return JavaInfoUtils.getTarget(page, nextChild);
  }

  /**
   * Creates new {@link PageLayoutAddViewInfo}.
   *
   * @return the created {@link PageLayoutAddViewInfo}.
   */
  public PageLayoutAddViewInfo command_CREATE(final String viewId,
      final int relationship,
      final float ratio,
      final IPageLayoutTopLevelInfo reference) throws Exception {
    final PageLayoutInfo page = this;
    return command(new IPageLayoutCommand<PageLayoutAddViewInfo>() {
      @Override
      public PageLayoutAddViewInfo execute(AstEditor editor,
          String layoutSource,
          StatementTarget target,
          ObjectInfo nextChild) throws Exception {
        // insert "addView" invocation
        MethodInvocation newInvocation;
        {
          Statement newStatement =
              editor.addStatement(
                  layoutSource
                      + ".addView("
                      + StringConverter.INSTANCE.toJavaSource(page, viewId)
                      + ", "
                      + getRelationSource(relationship)
                      + ", "
                      + FloatConverter.INSTANCE.toJavaSource(page, ratio)
                      + ", "
                      + reference.getIdSource()
                      + ");",
                  target);
          newInvocation = (MethodInvocation) ((ExpressionStatement) newStatement).getExpression();
        }
        // create new PageLayout_addView_Info
        PageLayoutAddViewInfo newView = new PageLayoutAddViewInfo(page, newInvocation);
        moveChild(newView, nextChild);
        // add related nodes
        {
          newView.bindToExpression(newInvocation);
          newView.addRelatedNodes(newInvocation);
          addRelatedNodes(newInvocation);
        }
        // OK, we have new view
        return newView;
      }
    },
        reference);
  }

  /**
   * Moves existing {@link AbstractPartInfo}.
   */
  public void command_MOVE(final AbstractPartInfo part,
      final int relationship,
      final float ratio,
      final IPageLayoutTopLevelInfo reference) throws Exception {
    final PageLayoutInfo page = this;
    command(new IPageLayoutCommand<PageLayoutAddViewInfo>() {
      @Override
      public PageLayoutAddViewInfo execute(AstEditor editor,
          String layoutSource,
          final StatementTarget target,
          final ObjectInfo nextChild) throws Exception {
        // move creation Statement
        {
          IMoveTargetProvider targetProvider = new IMoveTargetProvider() {
            @Override
            public StatementTarget getTarget() throws Exception {
              return target;
            }

            @Override
            public void add() throws Exception {
            }

            @Override
            public void move() throws Exception {
              moveChild(part, nextChild);
            }
          };
          JavaInfoUtils.moveProvider(part, null, page, targetProvider);
          // ignored
          targetProvider.add();
        }
        // update arguments
        {
          part.setRelationshipSource(getRelationSource(relationship));
          part.setRatioSource(FloatConverter.INSTANCE.toJavaSource(page, ratio));
          part.setRefIdSource(reference.getIdSource());
        }
        // no result
        return null;
      }
    }, reference);
  }

  /**
   * Converts existing {@link FolderViewInfo} into top level {@link AbstractPartInfo}.
   */
  public PageLayoutAddViewInfo command_MOVE(FolderViewInfo folderView,
      int relationship,
      float ratio,
      IPageLayoutTopLevelInfo reference) throws Exception {
    PageLayoutAddViewInfo topView =
        command_CREATE(folderView.getId(), relationship, ratio, reference);
    folderView.delete();
    return topView;
  }

  /**
   * Creates new {@link PageLayoutCreateFolderInfo}.
   *
   * @return the created {@link PageLayoutCreateFolderInfo}.
   */
  public PageLayoutCreateFolderInfo command_CREATE_folder(final int relationship,
      final float ratio,
      final IPageLayoutTopLevelInfo reference) throws Exception {
    return command(new IPageLayoutCommand<PageLayoutCreateFolderInfo>() {
      @Override
      public PageLayoutCreateFolderInfo execute(AstEditor editor,
          String layoutSource,
          StatementTarget target,
          ObjectInfo nextChild) throws Exception {
        PageLayoutCreateFolderInfo newFolder =
            command_CREATE_folder(relationship, ratio, reference.getIdSource(), target);
        moveChild(newFolder, nextChild);
        // OK, we have new "folder"
        return newFolder;
      }
    },
        reference);
  }

  /**
   * Creates new {@link PageLayoutCreateFolderInfo}.
   *
   * @return the created {@link PageLayoutCreateFolderInfo}.
   */
  private PageLayoutCreateFolderInfo command_CREATE_folder(int relationship,
      float ratio,
      String referenceIdSource,
      StatementTarget target) throws Exception {
    // prepare unique ID
    String folderId;
    {
      // prepare set of used ID's for all parts
      final Set<String> usedIDs = Sets.newTreeSet();
      for (AbstractPartInfo part : getParts()) {
        usedIDs.add(part.getId());
      }
      // do generate unique
      folderId = CodeUtils.generateUniqueName("folder", new Predicate<String>() {
        @Override
        public boolean apply(String t) {
          return !usedIDs.contains(t);
        }
      });
    }
    // prepare CreationSupport
    CreationSupport creationSupport;
    {
      String source =
          "createFolder("
              + StringConverter.INSTANCE.toJavaSource(this, folderId)
              + ", "
              + getRelationSource(relationship)
              + ", "
              + FloatConverter.INSTANCE.toJavaSource(this, ratio)
              + ", "
              + referenceIdSource
              + ")";
      MethodDescription description =
          getDescription().getMethod("createFolder(java.lang.String,int,float,java.lang.String)");
      creationSupport = new PageLayoutAddCreationSupport(this, description, source);
    }
    // add PageLayout_createFolder_Info
    PageLayoutCreateFolderInfo newFolder = new PageLayoutCreateFolderInfo(this, creationSupport);
    JavaInfoUtils.add(
        newFolder,
        new LocalUniqueVariableSupport(newFolder),
        BlockStatementGenerator.INSTANCE,
        AssociationObjects.invocationVoid(),
        this,
        null,
        target);
    // OK, we have new "folder"
    return newFolder;
  }

  /**
   * @return the source for position relative to the reference part; one of {@link IPageLayout#TOP},
   *         {@link IPageLayout#BOTTOM}, {@link IPageLayout#LEFT} or {@link IPageLayout#RIGHT}.
   */
  private static String getRelationSource(int relation) throws Exception {
    if (ReflectionUtils.getFieldInt(IPageLayout.class, "TOP") == relation) {
      return "org.eclipse.ui.IPageLayout.TOP";
    }
    if (ReflectionUtils.getFieldInt(IPageLayout.class, "BOTTOM") == relation) {
      return "org.eclipse.ui.IPageLayout.BOTTOM";
    }
    if (ReflectionUtils.getFieldInt(IPageLayout.class, "LEFT") == relation) {
      return "org.eclipse.ui.IPageLayout.LEFT";
    }
    if (ReflectionUtils.getFieldInt(IPageLayout.class, "RIGHT") == relation) {
      return "org.eclipse.ui.IPageLayout.RIGHT";
    }
    throw new IllegalArgumentException("Unknown relationship: " + relation);
  }

  /**
   * Converts given {@link PageLayoutAddViewInfo} into {@link PageLayoutCreateFolderInfo}.
   */
  public PageLayoutCreateFolderInfo convertIntoFolder(PageLayoutAddViewInfo view) throws Exception {
    // move "view" into Block
    {
      Statement statement = AstNodeUtils.getEnclosingStatement(view.getCreationSupport().getNode());
      getEditor().encloseInBlock(statement);
    }
    // add "folder" at same position as "view"
    PageLayoutCreateFolderInfo folder;
    {
      StatementTarget target = JavaInfoUtils.getTarget(this, view);
      folder =
          command_CREATE_folder(
              view.getRelationship(),
              view.getRatio(),
              view.getRefIdSource(),
              target);
      moveChild(folder, view);
    }
    // move "view" on "folder"
    folder.command_MOVE(view, null);
    return folder;
  }
}
