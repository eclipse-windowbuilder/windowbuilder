/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *    DSA - Adding code to allow the icons to have different layouts set
 *******************************************************************************/
package org.eclipse.wb.core.controls.palette;

import org.eclipse.wb.core.editor.constants.CoreImages;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.draw2d.Layer;
import org.eclipse.wb.draw2d.PaletteFigure;
import org.eclipse.wb.draw2d.border.LineBorder;
import org.eclipse.wb.internal.core.utils.GenericsUtils;
import org.eclipse.wb.internal.core.utils.ui.DrawUtils;
import org.eclipse.wb.internal.draw2d.EventManager;
import org.eclipse.wb.internal.draw2d.FigureCanvas;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.ui.palette.PaletteViewerPreferences;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The palette control.
 *
 * @author scheglov_ke
 * @coverage core.control.palette
 */
public final class PaletteComposite extends Composite {
	/**
	 * @deprecated Use {@link PaletteViewerPreferences#LAYOUT_COLUMNS} instead.
	 */
	@Deprecated(forRemoval = true, since = "1.18.0")
	public static int COLUMN_ICONS_TYPE = PaletteViewerPreferences.LAYOUT_COLUMNS;
	/**
	 * @deprecated Use {@link PaletteViewerPreferences#LAYOUT_LIST} instead.
	 */
	@Deprecated(forRemoval = true, since = "1.18.0")
	public static int LIST_ICONS_TYPE = PaletteViewerPreferences.LAYOUT_LIST;
	/**
	 * @deprecated Use {@link PaletteViewerPreferences#LAYOUT_ICONS} instead.
	 */
	@Deprecated(forRemoval = true, since = "1.18.0")
	public static int ONLY_ICONS_TYPE = PaletteViewerPreferences.LAYOUT_ICONS;
	/**
	 * @deprecated Use {@link PaletteViewerPreferences#LAYOUT_DETAILS} instead.
	 */
	@Deprecated(forRemoval = true, since = "1.18.0")
	public static int DETAIL_ICONS_TYPE = PaletteViewerPreferences.LAYOUT_DETAILS;
	////////////////////////////////////////////////////////////////////////////
	//
	// Colors
	//
	////////////////////////////////////////////////////////////////////////////
	private static final Color COLOR_PALETTE_BACKGROUND = ColorConstants.button;
	private static final Color COLOR_TEXT_ENABLED = ColorConstants.listForeground;
	private static final Color COLOR_TEXT_DISABLED = ColorConstants.gray;
	private static final Color COLOR_ENTRY_SELECTED = DrawUtils.getShiftedColor(
			COLOR_PALETTE_BACKGROUND,
			24);
	private static final Color COLOR_CATEGORY_GRAD_BEGIN = DrawUtils.getShiftedColor(
			COLOR_PALETTE_BACKGROUND,
			-8);
	private static final Color COLOR_CATEGORY_GRAD_END = DrawUtils.getShiftedColor(
			COLOR_PALETTE_BACKGROUND,
			16);
	private static final Color COLOR_CATEGORY_SEL_GRAD_BEGIN = DrawUtils.getShiftedColor(
			COLOR_PALETTE_BACKGROUND,
			16);
	private static final Color COLOR_CATEGORY_SEL_GRAD_END = DrawUtils.getShiftedColor(
			COLOR_CATEGORY_GRAD_BEGIN,
			-8);
	////////////////////////////////////////////////////////////////////////////
	//
	// Images
	//
	////////////////////////////////////////////////////////////////////////////
	private static final Image NO_ICON = loadImage("icons/no_icon.gif");
	private static final Image FOLDER_OPEN = CoreImages.getSharedImage(CoreImages.FOLDER_OPEN);
	private static final Image FOLDER_CLOSED = CoreImages.getSharedImage(CoreImages.FOLDER_CLOSED);

	/**
	 * @return the {@link Image} using path relative to this class.
	 */
	private static Image loadImage(String path) {
		return DrawUtils.loadImage(PaletteComposite.class, path);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Instance fields
	//
	////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings("removal")
	private IPalettePreferences m_preferences;
	private final FigureCanvas m_figureCanvas;
	private final EventManager m_eventManager;
	private final PaletteRootFigure m_paletteFigure;
	private final Layer m_feedbackLayer;
	@SuppressWarnings("removal")
	private final Map<ICategory, CategoryFigure> m_categoryFigures = new HashMap<>();
	private final ResourceManager m_resourceManager = new LocalResourceManager(JFaceResources.getResources());
	private MenuManager m_menuManager;
	@SuppressWarnings("removal")
	private IPalette m_palette;
	@SuppressWarnings("removal")
	private IEntry m_selectedEntry;
	private Object m_forcedTargetObject;
	private int m_layoutType;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings("removal")
	public PaletteComposite(Composite parent, int style) {
		super(parent, style);
		m_preferences = new DesignerPaletteViewerPreferences();
		//
		setLayout(new FillLayout());
		// prepare draw2d FigureCanvas
		{
			m_figureCanvas = new FigureCanvas(this, SWT.V_SCROLL);
			m_figureCanvas.getRootFigure().setBackgroundColor(COLOR_PALETTE_BACKGROUND);
			m_figureCanvas.getRootFigure().setForegroundColor(COLOR_TEXT_ENABLED);
			m_eventManager = (EventManager) m_figureCanvas.getRootFigure().internalGetEventDispatcher();
		}
		// add palette figure (layer)
		m_paletteFigure = new PaletteRootFigure();
		m_figureCanvas.getRootFigure().add(m_paletteFigure);
		// set menu
		{
			m_menuManager = new MenuManager();
			m_menuManager.setRemoveAllWhenShown(true);
			m_figureCanvas.setMenu(m_menuManager.createContextMenu(m_figureCanvas));
			m_menuManager.addMenuListener(manager -> addPopupActions(manager));
		}
		// add feedback layer
		{
			m_feedbackLayer = new Layer("feedback");
			m_figureCanvas.getRootFigure().add(m_feedbackLayer);
		}
		m_layoutType = m_preferences.getLayoutType();
	}

	/**
	 * Adds {@link Action}'s to the popup menu.
	 */
	@SuppressWarnings("removal")
	private void addPopupActions(IMenuManager menuManager) {
		// prepare target figure
		IFigure targetFigure;
		{
			org.eclipse.swt.graphics.Point cursorLocation = getDisplay().getCursorLocation();
			cursorLocation = m_figureCanvas.toControl(cursorLocation);
			targetFigure = m_figureCanvas.getLightweightSystem().getRootFigure().findFigureAt(cursorLocation.x, cursorLocation.y);
		}
		// prepare target object
		Object targetObject = null;
		if (targetFigure instanceof CategoryFigure) {
			targetObject = ((CategoryFigure) targetFigure).m_category;
		} else if (targetFigure instanceof EntryFigure) {
			targetObject = ((EntryFigure) targetFigure).m_entry;
		}
		// may be replace with forced
		if (m_forcedTargetObject != null) {
			targetObject = m_forcedTargetObject;
		}
		// add actions
		m_palette.addPopupActions(menuManager, targetObject, m_layoutType);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Dispose
	//
	////////////////////////////////////////////////////////////////////////////

	@Override
	public void dispose() {
		super.dispose();
		m_resourceManager.dispose();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Sets {@link IPalette} for displaying.
	 *
	 * @deprecated Use {@link #setPalette(DesignerRoot)}. This method will be
	 *             removed after the 2027-03 release.
	 */
	@Deprecated(since = "1.19", forRemoval = true)
	public void setPalette(IPalette palette) {
		m_palette = palette;
		refreshPalette();
	}

	/**
	 * Sets {@link PaletteRoot} for displaying.
	 */
	@SuppressWarnings("removal")
	public void setPalette(DesignerRoot palette) {
		setPalette((IPalette) palette);
	}

	/**
	 * Refresh palette using current {@link IPalette}.
	 */
	public void refreshPalette() {
		m_paletteFigure.refresh();
	}

	/**
	 * Sets new {@link IPalettePreferences}.
	 *
	 * @deprecated Use {@link #setPreferences(DesignerPaletteViewerPreferences)}.
	 *             This method will be removed after the 2027-03 release.
	 */
	@Deprecated(since = "1.19", forRemoval = true)
	public void setPreferences(IPalettePreferences preferences) {
		m_preferences = preferences;
		m_layoutType = m_preferences.getLayoutType();
		m_paletteFigure.onPreferencesUpdate();
	}

	/**
	 * Sets new {@link IPalettePreferences}.
	 */
	@SuppressWarnings("removal")
	public void setPreferences(DesignerPaletteViewerPreferences preferences) {
		setPreferences((IPalettePreferences) preferences);
	}

	/**
	 * Sets the selected {@link IEntry}.
	 *
	 * @param reload is <code>true</code> if after first using this {@link IEntry}
	 *               should be loaded again, not switched to default entry (usually
	 *               selection).
	 * @deprecated Use {@link #selectEntry(DesignerEntry, boolean)} instead. This
	 *             method will be removed after the 2027-03 release.
	 */
	@Deprecated(since = "3.19", forRemoval = true)
	public void selectEntry(IEntry selectedEntry, boolean reload) {
		// activate new entry
		m_selectedEntry = selectedEntry;
		if (m_selectedEntry != null) {
			boolean activated = m_selectedEntry.activate(reload);
			// if activation was not successful, select default entry
			if (!activated) {
				m_palette.selectDefault();
			}
		}
		// display updated state
		m_paletteFigure.repaint();
	}

	/**
	 * Sets the selected {@link DesignerEntry}.
	 *
	 * @param reload is <code>true</code> if after first using this {@link IEntry}
	 *               should be loaded again, not switched to default entry (usually
	 *               selection).
	 */
	@SuppressWarnings("removal")
	public void selectEntry(DesignerEntry selectedEntry, boolean reload) {
		selectEntry((IEntry) selectedEntry, reload);
	}

	/**
	 * @return the {@link Figure} used for displaying {@link ICategory}.
	 * @deprecated Use {@link #getCategoryFigure(DesignerContainer)} instead. This
	 *             method will be removed after the 2027-03 release.
	 */
	@Deprecated(since = "3.19", forRemoval = true)
	public Figure getCategoryFigure(ICategory category) {
		return m_categoryFigures.get(category);
	}

	/**
	 * @return the {@link IFigure} used for displaying {@link DesignerContainer}.
	 */
	@SuppressWarnings("removal")
	public IFigure getCategoryFigure(DesignerContainer category) {
		return getCategoryFigure((ICategory) category);
	}

	/**
	 * @return the {@link Figure} used for displaying {@link IEntry}.
	 *
	 * @deprecated Use {@link #getEntryFigure(DesignerContainer, DesignerEntry)}
	 *             instead. This method will be removed after the 2027-03 release.
	 */
	@Deprecated(since = "3.19", forRemoval = true)
	public Figure getEntryFigure(ICategory category, IEntry entry) {
		CategoryFigure categoryFigure = m_categoryFigures.get(category);
		return categoryFigure.m_entryFigures.get(entry);
	}

	/**
	 * @return the {@link IFigure} used for displaying {@link DesignerEntry}.
	 */
	@SuppressWarnings("removal")
	public Figure getEntryFigure(DesignerContainer category, DesignerEntry entry) {
		return getEntryFigure((ICategory) category, (IEntry) entry);
	}

	/**
	 * Performs layout operation for palette, after external changes in model.
	 */
	public void layoutPalette() {
		m_paletteFigure.layout();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// PaletteFigure
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Top level {@link Figure} for palette - container for {@link CategoryFigure}'s.
	 *
	 * @author scheglov_ke
	 */
	@SuppressWarnings("removal")
	private final class PaletteRootFigure extends Layer {
		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		public PaletteRootFigure() {
			super("palette");
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Access
		//
		////////////////////////////////////////////////////////////////////////////
		public void refresh() {
			m_categoryFigures.clear();
			removeAll();
			// add new CategoryFigure's
			for (ICategory category : m_palette.getCategories()) {
				CategoryFigure categoryFigure = new CategoryFigure(category);
				m_categoryFigures.put(category, categoryFigure);
				add(categoryFigure);
			}
			// do initial layout
			layout();
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Internal
		//
		////////////////////////////////////////////////////////////////////////////
		/**
		 * This method is invoked when {@link IPalettePreferences} changes.
		 */
		private void onPreferencesUpdate() {
			for (Iterator<? extends IFigure> I = getChildren().iterator(); I.hasNext();) {
				CategoryFigure categoryFigure = (CategoryFigure) I.next();
				categoryFigure.onPreferencesUpdate();
			}
			revalidate();
			layout();
		}

		/**
		 * Lays out {@link CategoryFigure}'s of palette.
		 */
		@Override
		protected void layout() {
			int width = m_figureCanvas.getClientArea().width;
			width -= getInsets().getWidth();
			//
			int y = 0;
			for (Iterator<? extends IFigure> I = getChildren().iterator(); I.hasNext();) {
				CategoryFigure categoryFigure = (CategoryFigure) I.next();
				y += categoryFigure.layout(y, width);
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// CategoryFigure
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * {@link Figure} implementation for {@link ICategory}.
	 *
	 * @author scheglov_ke
	 */
	@SuppressWarnings("removal")
	private final class CategoryFigure extends PaletteFigure {
		private static final int IMAGE_SPACE_LEFT = 4;
		private static final int IMAGE_SPACE_RIGHT = 4;
		private static final int MARGIN_HEIGHT = 2;
		////////////////////////////////////////////////////////////////////////////
		//
		// Instance fields
		//
		////////////////////////////////////////////////////////////////////////////
		private final ICategory m_category;
		private final Map<IEntry, EntryFigure> m_entryFigures = new HashMap<>();
		private int m_columns;
		private int m_titleHeight;

		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		public CategoryFigure(ICategory category) {
			m_category = category;
			hookEvents();
			PaletteComposite.setToolTip(this, null, m_category.getToolTipText());
			onPreferencesUpdate();
			// add entry figures
			for (Object element : m_category.getEntries()) {
				IEntry entry = (IEntry) element;
				// add figure
				EntryFigure entryFigure = new EntryFigure(entry);
				m_entryFigures.put(entry, entryFigure);
				add(entryFigure);
			}
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Access
		//
		////////////////////////////////////////////////////////////////////////////
		/**
		 * This method is invoked when {@link IPalettePreferences} changes.
		 */
		public void onPreferencesUpdate() {
			FontDescriptor fontDescriptor = m_preferences.getCategoryFontDescriptor();
			setFont(fontDescriptor == null ? null : m_resourceManager.create(fontDescriptor));
			for (Iterator<? extends IFigure> I = getChildren().iterator(); I.hasNext();) {
				EntryFigure entryFigure = (EntryFigure) I.next();
				entryFigure.onPreferencesUpdate();
			}
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Events
		//
		////////////////////////////////////////////////////////////////////////////
		private boolean m_mouseOnTitle;
		private boolean m_mouseDown;
		private Point m_downPoint;
		private boolean m_moving;

		/**
		 * Adds events handlers.
		 */
		private void hookEvents() {
			addMouseListener(new MouseListener.Stub() {
				@Override
				public void mousePressed(MouseEvent event) {
					if (event.button == 1) {
						if (m_mouseOnTitle) {
							m_mouseDown = true;
							m_eventManager.setCapture(CategoryFigure.this);
							m_downPoint = new Point(event.x, event.y);
						}
					}
				}

				@Override
				public void mouseReleased(MouseEvent event) {
					if (event.button == 1) {
						m_mouseDown = false;
						m_eventManager.setCapture(null);
						//
						if (m_moving) {
							m_moving = false;
							move_eraseFeedback();
							if (m_moveCommand != null) {
								try {
									m_moveCommand.execute();
								} catch (Throwable e) {
								}
							}
						} else if (m_mouseOnTitle) {
							m_category.setOpen(!m_category.isOpen());
							m_paletteFigure.revalidate();
							m_paletteFigure.layout();
						}
					}
					repaint();
				}
			});
			addMouseMotionListener(new MouseMotionListener.Stub() {
				@Override
				public void mouseMoved(MouseEvent event) {
					if (m_mouseDown) {
						Point p = new Point(event.x, event.y);
						// update moving
						if (!m_moving && m_downPoint.getDistance(p) > 4) {
							m_moving = true;
						}
						// show feedback
						if (m_moving) {
							move_showFeedback(p);
						}
					} else {
						m_mouseOnTitle = getTitleRectangle().contains(event.x, event.y);
						repaint();
					}
				}

				@Override
				public void mouseExited(MouseEvent e) {
					m_mouseOnTitle = false;
					repaint();
				}
			});
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Moving
		//
		////////////////////////////////////////////////////////////////////////////
		private Figure m_feedback;
		private Command m_moveCommand;

		/**
		 * Shows feedback of moving this {@link IEntry}.
		 */
		private void move_showFeedback(Point p) {
			move_eraseFeedback();
			m_moveCommand = null;
			// prepare target figure
			IFigure targetFigure = getTargetFigure(this, p);
			if (targetFigure instanceof EntryFigure) {
				targetFigure = targetFigure.getParent();
			}
			// show feedback for target category
			if (targetFigure instanceof CategoryFigure) {
				// prepare target categories
				final ICategory category_1 = ((CategoryFigure) targetFigure).m_category;
				final ICategory category_2;
				{
					List<? extends IFigure> siblings = targetFigure.getParent().getChildren();
					IFigure nextFigure = GenericsUtils.getNextOrNull(siblings, targetFigure);
					category_2 = nextFigure != null ? ((CategoryFigure) nextFigure).m_category : null;
				}
				// prepare feedback location
				final boolean before;
				Point feedbackLocation;
				{
					// prepare location in target figure
					Point targetLocation = p.getCopy();
					FigureUtils.translateFigureToFigure2(this, targetFigure, targetLocation);
					// check before/after
					if (targetLocation.y > targetFigure.getSize().height / 2) {
						before = true;
						feedbackLocation = targetFigure.getBounds().getBottomLeft();
					} else {
						before = false;
						feedbackLocation = targetFigure.getBounds().getTopLeft();
					}
				}
				FigureUtils.translateFigureToAbsolute(targetFigure, feedbackLocation);
				// add feedback figure
				m_feedback =
						new FeedbackLine(m_feedbackLayer, true, feedbackLocation, targetFigure.getSize().width);
				// create command
				m_moveCommand = new Command() {
					@Override
					public void execute() {
						ICategory targetCategory = before ? category_2 : category_1;
						if (category_1 == m_category) {
						} else {
							m_palette.moveCategory(m_category, targetCategory);
						}
					}
				};
			}
		}

		/**
		 * Erases feedback of moving this {@link IEntry}.
		 */
		private void move_eraseFeedback() {
			if (m_feedback != null) {
				m_feedback.getParent().remove(m_feedback);
				m_feedback = null;
			}
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Layout
		//
		////////////////////////////////////////////////////////////////////////////
		/**
		 * Lays out inner {@link EntryFigure}'s and returns the height of this figure.
		 */
		public int layout(int y, int width) {
			Dimension textExtent = FigureUtilities.getTextExtents(m_category.getText(), getFont());
			m_titleHeight = MARGIN_HEIGHT + textExtent.height() + MARGIN_HEIGHT;
			//
			int height = m_titleHeight;
			if (m_category.isOpen() && !getChildren().isEmpty()) {
				// prepare max size of entry
				int maxWidth = 0;
				int maxHeight = 0;
				boolean onlyIcons = m_preferences.getLayoutType() == ONLY_ICONS_TYPE;
				for (IFigure child : getChildren()) {
					EntryFigure entryFigure = (EntryFigure) child;
					// update size
					Dimension entrySize =
							onlyIcons ? entryFigure.getIconSize() : entryFigure.getIconTextSize();
					maxWidth = Math.max(maxWidth, entrySize.width);
					maxHeight = Math.max(maxHeight, entrySize.height);
				}
				// prepare columns
				{
					m_columns = width / maxWidth;
					if (!onlyIcons) {
						m_columns = Math.max(m_columns, m_preferences.getMinColumns());
					}
					m_columns = Math.min(m_columns, getChildren().size());
					m_columns = Math.max(m_columns, 1);
					if (m_layoutType == LIST_ICONS_TYPE || m_layoutType == DETAIL_ICONS_TYPE) {
						m_columns = 1;
					}
				}
				// layout children
				{
					int column = 0;
					int entryY = height;
					for (IFigure child : getChildren()) {
						EntryFigure entryFigure = (EntryFigure) child;
						// relocate entry
						if (onlyIcons) {
							int x = maxWidth * column;
							entryFigure.setBounds(new Rectangle(x, entryY, maxWidth, maxHeight));
						} else {
							int columnWidth = width / m_columns;
							int x = columnWidth * column;
							entryFigure.setBounds(new Rectangle(x, entryY, columnWidth, maxHeight));
						}
						// update category height
						if (column == 0) {
							height += maxHeight;
						}
						// wrap to next column
						if (++column == m_columns) {
							column = 0;
							entryY += maxHeight;
						}
					}
				}
			}
			// set bounds
			setBounds(new Rectangle(0, y, width, height));
			return height;
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Painting
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		protected void paintClientArea(Graphics graphics) {
			// draw title
			{
				Rectangle r = getClientArea();
				r.height = m_titleHeight;
				// draw title background
				graphics.pushState();
				{
					if (m_mouseOnTitle || m_moving) {
						graphics.setForegroundColor(COLOR_CATEGORY_SEL_GRAD_BEGIN);
						graphics.setBackgroundColor(COLOR_CATEGORY_SEL_GRAD_END);
					} else {
						graphics.setForegroundColor(COLOR_CATEGORY_GRAD_BEGIN);
						graphics.setBackgroundColor(COLOR_CATEGORY_GRAD_END);
					}
					graphics.fillGradient(r, true);
					drawRectangle3D(graphics, r, true);
				}
				graphics.popState();
				// draw state image
				{
					Image stateImage;
					if (m_category.isOpen()) {
						stateImage = FOLDER_OPEN;
					} else {
						stateImage = FOLDER_CLOSED;
					}
					// draw state image
					r.shrinkLeft(IMAGE_SPACE_LEFT);
					drawImageCV(graphics, stateImage, r.x, r.y, r.height);
					// modify title rectangle
					int imageWidth = stateImage.getBounds().width;
					r.shrinkLeft(imageWidth + IMAGE_SPACE_RIGHT);
				}
				// draw title text
				drawStringCV(graphics, m_category.getText(), r);
			}
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Internal
		//
		////////////////////////////////////////////////////////////////////////////
		/**
		 * @return the {@link Rectangle} of title.
		 */
		private Rectangle getTitleRectangle() {
			Rectangle r = Rectangle.SINGLETON;
			r.setBounds(getClientArea());
			translateToParent(r);
			r.height = m_titleHeight;
			return r;
		}

		@Override
		public String toString() {
			return "[%s] %s".formatted(getClass().getSimpleName(), m_category.getText());
		}
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// EntryFigure
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * {@link Figure} implementation for {@link IEntry}.
	 *
	 * @author scheglov_ke
	 */
	@SuppressWarnings("removal")
	private final class EntryFigure extends PaletteFigure {
		private static final int IMAGE_SPACE_RIGHT = 2;
		private static final int MARGIN_WIDTH_1 = 3;
		private static final int MARGIN_WIDTH_2 = 6;
		private static final int MARGIN_HEIGHT = 3;
		////////////////////////////////////////////////////////////////////////////
		//
		// Instance fields
		//
		////////////////////////////////////////////////////////////////////////////
		private final IEntry m_entry;

		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		public EntryFigure(IEntry entry) {
			m_entry = entry;
			if (m_entry.isEnabled()) {
				hookEvents();
			}
			PaletteComposite.setToolTip(this, m_entry.getText(), m_entry.getToolTipText());
			onPreferencesUpdate();
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Access
		//
		////////////////////////////////////////////////////////////////////////////
		/**
		 * This method is invoked when {@link IPalettePreferences} changes.
		 */
		public void onPreferencesUpdate() {
			FontDescriptor fontDescriptor = m_preferences.getEntryFontDescriptor();
			setFont(fontDescriptor == null ? null : m_resourceManager.create(fontDescriptor));
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Events
		//
		////////////////////////////////////////////////////////////////////////////
		private boolean m_mouseInside;
		private boolean m_mouseDown;
		private Point m_downPoint;
		private boolean m_moving;

		/**
		 * Adds events handlers.
		 */
		private void hookEvents() {
			addMouseListener(new MouseListener.Stub() {
				@Override
				public void mousePressed(MouseEvent event) {
					if (event.button == 1) {
						m_mouseDown = true;
						m_mouseInside = true;
						// track moving
						m_eventManager.setCapture(EntryFigure.this);
						m_downPoint = new Point(event.x, event.y);
						m_moving = false;
						//
						repaint();
					}
				}

				@Override
				public void mouseReleased(MouseEvent event) {
					if (event.button == 1 && m_mouseDown) {
						m_mouseDown = false;
						m_eventManager.setCapture(null);
						//
						if (m_moving) {
							move_eraseFeedback();
							if (m_moveCommand != null) {
								try {
									m_moveCommand.execute();
								} catch (Throwable e) {
								}
							}
						} else if (m_mouseInside) {
							boolean reload = (event.getState() & SWT.CTRL) != 0;
							selectEntry(m_entry, reload);
						}
					}
					repaint();
				}
			});
			addMouseMoveListener(event -> {
				// update mouse location
				boolean oldMouseInside = m_mouseInside;
				m_mouseInside = getClientArea().contains(event.x, event.y);
				//
				if (m_mouseDown) {
					Point p = new Point(event.x, event.y);
					// update moving
					if (!m_moving && m_downPoint.getDistance(p) > 4) {
						m_moving = true;
					}
					// show feedback
					if (m_moving) {
						move_showFeedback(p);
					}
				} else if (m_mouseInside != oldMouseInside) {
					repaint();
				}
			});
			addMouseMotionListener(new MouseMotionListener.Stub() {
				@Override
				public void mouseEntered(MouseEvent e) {
					m_mouseInside = true;
					repaint();
				}

				@Override
				public void mouseExited(MouseEvent e) {
					m_mouseInside = false;
					repaint();
				}
			});
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Moving
		//
		////////////////////////////////////////////////////////////////////////////
		private Figure m_feedback;
		private Command m_moveCommand;

		/**
		 * Shows feedback of moving this {@link IEntry}.
		 */
		private void move_showFeedback(Point p) {
			move_eraseFeedback();
			m_moveCommand = null;
			// handle target
			IFigure targetFigure = getTargetFigure(this, p);
			if (targetFigure instanceof final EntryFigure targetEntryFigure) {
				final CategoryFigure targetCategoryFigure = (CategoryFigure) targetFigure.getParent();
				final ICategory targetCategory = targetCategoryFigure.m_category;
				final IEntry targetEntry = targetEntryFigure.m_entry;
				// prepare location in target figure
				Point targetLocation;
				{
					targetLocation = p.getCopy();
					FigureUtils.translateFigureToFigure2(this, targetFigure, targetLocation);
				}
				//
				if (targetCategoryFigure.m_columns == 1) {
					// prepare feedback location and command
					Point feedbackLocation;
					if (targetLocation.y < targetFigure.getSize().height / 2) {
						feedbackLocation = targetFigure.getBounds().getLocation();
						move_commandBefore(targetCategory, targetEntry);
					} else {
						feedbackLocation = targetFigure.getBounds().getBottomLeft();
						move_commandAfter(targetCategory, targetEntry);
					}
					// add feedback figure
					{
						FigureUtils.translateFigureToAbsolute(targetFigure, feedbackLocation);
						m_feedback =
								new FeedbackLine(m_feedbackLayer,
										true,
										feedbackLocation,
										targetFigure.getSize().width);
					}
				} else {
					// prepare feedback location and command
					Point feedbackLocation;
					if (targetLocation.x < targetFigure.getSize().width / 2) {
						feedbackLocation = targetFigure.getBounds().getLocation();
						move_commandBefore(targetCategory, targetEntry);
					} else {
						feedbackLocation = targetFigure.getBounds().getTopRight();
						move_commandAfter(targetCategory, targetEntry);
					}
					// add feedback figure
					{
						FigureUtils.translateFigureToAbsolute(targetFigure, feedbackLocation);
						m_feedback =
								new FeedbackLine(m_feedbackLayer,
										false,
										feedbackLocation,
										targetFigure.getSize().height);
					}
				}
			} else if (targetFigure instanceof CategoryFigure) {
				final ICategory targetCategory = ((CategoryFigure) targetFigure).m_category;
				// add feedback
				{
					m_feedback = new Figure();
					m_feedback.setBorder(new LineBorder(ColorConstants.menuBackgroundSelected, 2));
					//m_feedback.setBorder(PolicyUtils.createTargetBorder());
					// set bounds, add
					Rectangle feedbackBounds = targetFigure.getBounds().getCopy();
					FigureUtils.translateFigureToAbsolute(targetFigure, feedbackBounds);
					m_feedback.setBounds(feedbackBounds);
					m_feedbackLayer.add(m_feedback);
				}
				// create command
				m_moveCommand = new Command() {
					@Override
					public void execute() {
						m_palette.moveEntry(m_entry, targetCategory, null);
					}
				};
			}
		}

		/**
		 * Creates "move before" {@link Command}.
		 */
		private void move_commandBefore(final ICategory targetCategory, final IEntry targetEntry) {
			m_moveCommand = new Command() {
				@Override
				public void execute() {
					if (m_entry != targetEntry) {
						m_palette.moveEntry(m_entry, targetCategory, targetEntry);
					}
				}
			};
		}

		/**
		 * Creates "move after" {@link Command}.
		 */
		private void move_commandAfter(final ICategory targetCategory, final IEntry targetEntry) {
			m_moveCommand = new Command() {
				@Override
				public void execute() {
					if (m_entry != targetEntry) {
						// prepare "real" target
						final IEntry targetEntry2 =
								GenericsUtils.getNextOrNull(targetCategory.getEntries(), targetEntry);
						// do move
						if (m_entry != targetEntry2) {
							m_palette.moveEntry(m_entry, targetCategory, targetEntry2);
						}
					}
				}
			};
		}

		/**
		 * Erases feedback of moving this {@link IEntry}.
		 */
		private void move_eraseFeedback() {
			if (m_feedback != null) {
				m_feedback.getParent().remove(m_feedback);
				m_feedback = null;
			}
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Access
		//
		////////////////////////////////////////////////////////////////////////////
		/**
		 * @return the preferred size to display this {@link Figure} with icon.
		 */
		public Dimension getIconSize() {
			org.eclipse.swt.graphics.Rectangle imageBounds = getIcon().getBounds();
			// prepare total size
			int width = MARGIN_WIDTH_1 + imageBounds.width + MARGIN_WIDTH_2;
			int height = MARGIN_HEIGHT + imageBounds.height + MARGIN_HEIGHT;
			return new Dimension(width, height);
		}

		/**
		 * @return the preferred size to display this {@link Figure} with icon and text.
		 */
		public Dimension getIconTextSize() {
			org.eclipse.swt.graphics.Rectangle imageBounds = getIcon().getBounds();
			Dimension textExtent = FigureUtilities.getTextExtents(m_entry.getText(), getFont());
			// prepare total size
			int width = MARGIN_WIDTH_1 + imageBounds.width + IMAGE_SPACE_RIGHT + textExtent.width() + MARGIN_WIDTH_2;
			int height = MARGIN_HEIGHT + Math.max(imageBounds.height, textExtent.height()) + MARGIN_HEIGHT;
			return new Dimension(width, height);
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Painting
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		protected void paintClientArea(Graphics graphics) {
			Rectangle r = getClientArea().getCopy().shrink(1, 1);
			// draw background
			graphics.pushState();
			boolean isSelected = m_entry == m_selectedEntry;
			if (isSelected || m_mouseDown) {
				if (isSelected) {
					graphics.setBackgroundColor(COLOR_ENTRY_SELECTED);
					graphics.fillRectangle(r);
				}
				drawRectangle3D(graphics, r, false);
				// shift right and bottom
				r.shrinkLeft(1);
				r.shrinkTop(1);
			} else if (m_mouseInside) {
				drawRectangle3D(graphics, r, true);
			}
			graphics.popState();
			// draw icon/text
			graphics.pushState();
			if (m_entry.isEnabled()) {
				paintClientArea_enabled(graphics, r);
			} else {
				paintClientArea_disabled(graphics, r);
			}
			graphics.popState();
		}

		private void paintClientArea_enabled(Graphics graphics, Rectangle r) {
			int x = r.x + MARGIN_WIDTH_1;
			// draw icon
			{
				Image icon = getIcon();
				drawImageCV(graphics, icon, x, r.y, r.height);
				x += icon.getBounds().width + IMAGE_SPACE_RIGHT;
			}
			// draw text
			if (!m_preferences.isOnlyIcons()) {
				graphics.setForegroundColor(COLOR_TEXT_ENABLED);
				if (m_layoutType == DETAIL_ICONS_TYPE) {
					drawStringCV(
							graphics,
							m_entry.getText() + " -" + m_entry.getToolTipText(),
							x,
							r.y,
							r.width - x,
							r.height);
				} else {
					drawStringCV(graphics, m_entry.getText(), x, r.y, r.width - x, r.height);
				}
			}
		}

		private void paintClientArea_disabled(Graphics graphics, Rectangle r) {
			int x = r.x + MARGIN_WIDTH_1;
			// draw icon
			{
				Image icon = getIcon();
				Image disabledIcon = new Image(null, icon, SWT.IMAGE_DISABLE);
				try {
					drawImageCV(graphics, disabledIcon, x, r.y, r.height);
					x += disabledIcon.getBounds().width + IMAGE_SPACE_RIGHT;
				} finally {
					disabledIcon.dispose();
				}
			}
			// draw text
			if (!m_preferences.isOnlyIcons()) {
				graphics.setForegroundColor(COLOR_TEXT_DISABLED);
				drawStringCV(graphics, m_entry.getText(), x, r.y, r.width - x, r.height);
			}
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Utils
		//
		////////////////////////////////////////////////////////////////////////////
		/**
		 * @return the icon of this entry.
		 */
		private Image getIcon() {
			ImageDescriptor icon = m_entry.getIcon();
			if (icon != null) {
				return m_resourceManager.create(icon);
			}
			return NO_ICON;
		}

		@Override
		public String toString() {
			return "[%s] %s".formatted(getClass().getSimpleName(), m_entry.getText());
		}
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// FeedbackLine
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Moving feedback {@link Figure}.
	 *
	 * @author scheglov_ke
	 */
	private static final class FeedbackLine extends Figure {
		private final boolean m_horizontal;

		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		public FeedbackLine(Figure parent, boolean horizontal, Point location, int size) {
			m_horizontal = horizontal;
			parent.add(this);
			//
			Rectangle bounds;
			if (horizontal) {
				bounds = new Rectangle(location.x, location.y - 3, size, 6);
				bounds.y = Math.max(bounds.y, 0);
				bounds.y = Math.min(bounds.y, parent.getClientArea().height - 6);
			} else {
				bounds = new Rectangle(location.x - 3, location.y, 6, size);
				bounds.x = Math.max(bounds.x, 0);
				bounds.x = Math.min(bounds.x, parent.getClientArea().width - 6);
			}
			setBounds(bounds);
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// Painting
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		protected void paintClientArea(Graphics graphics) {
			Rectangle clientArea = getClientArea();
			if (m_horizontal) {
				// left
				{
					int x = 0;
					graphics.drawLine(x, 0, x, 5);
					x++;
					graphics.drawLine(x, 1, x, 4);
				}
				// right
				{
					int x = clientArea.right() - 1;
					graphics.drawLine(x, 0, x, 5);
					x--;
					graphics.drawLine(x, 1, x, 4);
				}
				// center
				{
					graphics.setLineWidth(2);
					graphics.drawLine(0, 3, clientArea.width, 3);
				}
			} else {
				// top
				{
					int y = 0;
					graphics.drawLine(0, y, 5, y);
					y++;
					graphics.drawLine(1, y, 4, y);
				}
				// bottom
				{
					int y = clientArea.height - 1;
					graphics.drawLine(0, y, 5, y);
					y--;
					graphics.drawLine(1, y, 4, y);
				}
				// center
				{
					graphics.setLineWidth(2);
					graphics.drawLine(3, 2, 3, clientArea.height);
				}
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return {@link Figure} under given mouse location.
	 *
	 * @param source
	 *          the source {@link Figure}
	 * @param p
	 *          the mouse location relative to given <code>source</code>
	 */
	private IFigure getTargetFigure(Figure source, Point p) {
		// use absolute coordinates
		Point location = Point.SINGLETON;
		location.setLocation(p);
		source.translateToAbsolute(location);
		IFigure targetFigure = m_figureCanvas.getLightweightSystem().getRootFigure().findFigureAt(location);
		return targetFigure;
	}

	/**
	 * Sets wrapped tooltip for given figure.
	 */
	private static void setToolTip(PaletteFigure figure, String header, String details) {
		if (header != null && details != null) {
			figure.setCustomTooltipProvider(new HtmlPaletteTooltipProvider(header, details));
		} else if (details == null) {
			figure.setCustomTooltipProvider(new SimplePaletteTooltipProvider(header));
		} else if (header == null) {
			figure.setCustomTooltipProvider(new HtmlPaletteTooltipProvider(null, details));
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Drawing utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Draws given text clipped horizontally and centered vertically.
	 */
	public static final void drawStringCV(Graphics graphics, String text, Rectangle r) {
		drawStringCV(graphics, text, r.x, r.y, r.width, r.height);
	}

	/**
	 * Draws given text clipped horizontally and centered vertically.
	 */
	public static final void drawStringCV(Graphics graphics,
			String text,
			int x,
			int y,
			int width,
			int height) {
		int textY = y + (height - graphics.getFontMetrics().getHeight()) / 2;
		String clipString = DrawUtils.clipString(graphics, text, width);
		graphics.drawText(clipString, x, textY);
	}

	/**
	 * Draws image at given <code>x</code> and centered vertically.
	 */
	public static final void drawImageCV(Graphics graphics, Image image, int x, int y, int height) {
		if (image != null) {
			int imageHeight = image.getBounds().height;
			graphics.drawImage(image, x, y + (height - imageHeight) / 2);
		}
	}

	/**
	 * Draws 3D highlight rectangle.
	 */
	private static void drawRectangle3D(Graphics gc, Rectangle r, boolean up) {
		int x = r.x;
		int y = r.y;
		int right = r.right() - 1;
		int bottom = r.bottom() - 1;
		//
		if (up) {
			gc.setForegroundColor(ColorConstants.buttonLightest);
		} else {
			gc.setForegroundColor(ColorConstants.buttonDarker);
		}
		gc.drawLine(x, y, right, y);
		gc.drawLine(x, y, x, bottom);
		//
		if (up) {
			gc.setForegroundColor(ColorConstants.buttonDarker);
		} else {
			gc.setForegroundColor(ColorConstants.buttonLightest);
		}
		gc.drawLine(right, y, right, bottom);
		gc.drawLine(x, bottom, right, bottom);
	}

	public void setLayoutType(int type) {
		m_layoutType = type;
	}

	public void refreshComposite() {
		m_paletteFigure.onPreferencesUpdate();
	}
}
