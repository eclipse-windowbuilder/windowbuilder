/*******************************************************************************
 * Copyright (c) 2023 Patrick Ziegler and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Patrick Ziegler - initial API and implementation
 *******************************************************************************/
package kmeans;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import kmeans.KMeansCluster.ColorReducedImage;

/**
 * UI component required to use the k-means clustering algorithm. To use it, the
 * user first has to select an image via the {@code Browse...} button. Clicking
 * the {@code Calculate...} button then executes the k-means algorithm using the
 * settigns in the {@code Iterations} and {@code Clusters} text fields. Once
 * done, the user can then flip through the generated images using the slider.
 */
public class KMeansFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	private File sourceFile;
	private BufferedImage sourceImage;
	private List<ColorReducedImage> targetImages;
	
	private JPanel contentPane;
	private JTextField filePath;
	private JTextField clustersValue;
	private JButton fileButton;
	private KMeansPanel panel;
	private JLabel widthLabel;
	private JLabel widthValue;
	private JLabel heightLabel;
	private JLabel heightValue;
	private JLabel clustersLabel;
	private JButton calculateButton;
	private JLabel meansLabel;
	private JList<Color> meansList;
	private JSlider slider;
	private JLabel iterationsLabel;
	private JTextField iterationsValue;
	private JScrollPane scrollPane;
	private JLabel lblColors;
	private JProgressBar progressBar;
	private JScrollPane panelScrollPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					KMeansFrame frame = new KMeansFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public KMeansFrame() {
		setTitle("KMeans Color Quantization");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 700, 500);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[] { 0, 50, 100, 0 };
		gbl_contentPane.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_contentPane.columnWeights = new double[] { 1.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_contentPane.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0,
				Double.MIN_VALUE };
		contentPane.setLayout(gbl_contentPane);
		
		filePath = new JTextField();
		filePath.setEditable(false);
		GridBagConstraints gbc_filePath = new GridBagConstraints();
		gbc_filePath.insets = new Insets(0, 0, 5, 5);
		gbc_filePath.fill = GridBagConstraints.BOTH;
		gbc_filePath.gridx = 0;
		gbc_filePath.gridy = 0;
		contentPane.add(filePath, gbc_filePath);
		filePath.setColumns(10);
		
		fileButton = new JButton("Browse...");
		GridBagConstraints gbc_fileButton = new GridBagConstraints();
		gbc_fileButton.gridwidth = 2;
		gbc_fileButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_fileButton.insets = new Insets(0, 0, 5, 0);
		gbc_fileButton.gridx = 1;
		gbc_fileButton.gridy = 0;
		contentPane.add(fileButton, gbc_fileButton);
		fileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();
				if(fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					sourceFile = fileChooser.getSelectedFile();
					try {
						sourceImage = ImageIO.read(sourceFile);
						widthValue.setText(Integer.toString(sourceImage.getWidth()));
						heightValue.setText(Integer.toString(sourceImage.getHeight()));
						//
						Dimension sourceDimension = new Dimension(sourceImage.getWidth(), sourceImage.getHeight());
						panel.setPreferredSize(sourceDimension);
						//
						String sourcePath = sourceFile.getAbsolutePath();
						filePath.setText(sourcePath.replaceFirst("/home/patrick", "~"));
						//
					} catch (IOException ioe) {
						ioe.printStackTrace();
					}
				}
			}
		});
		
		panelScrollPane = new JScrollPane();
		GridBagConstraints gbc_panelScrollPane = new GridBagConstraints();
		gbc_panelScrollPane.fill = GridBagConstraints.BOTH;
		gbc_panelScrollPane.gridheight = 10;
		gbc_panelScrollPane.insets = new Insets(0, 0, 0, 5);
		gbc_panelScrollPane.gridx = 0;
		gbc_panelScrollPane.gridy = 1;
		contentPane.add(panelScrollPane, gbc_panelScrollPane);

		panel = new KMeansPanel();
		panelScrollPane.setViewportView(panel);
		
		widthLabel = new JLabel("Width:");
		GridBagConstraints gbc_widthLabel = new GridBagConstraints();
		gbc_widthLabel.anchor = GridBagConstraints.WEST;
		gbc_widthLabel.insets = new Insets(0, 0, 5, 5);
		gbc_widthLabel.gridx = 1;
		gbc_widthLabel.gridy = 1;
		contentPane.add(widthLabel, gbc_widthLabel);
		
		widthValue = new JLabel("");
		GridBagConstraints gbc_widthValue = new GridBagConstraints();
		gbc_widthValue.insets = new Insets(0, 0, 5, 0);
		gbc_widthValue.gridx = 2;
		gbc_widthValue.gridy = 1;
		contentPane.add(widthValue, gbc_widthValue);
		
		heightLabel = new JLabel("Height:");
		GridBagConstraints gbc_heightLabel = new GridBagConstraints();
		gbc_heightLabel.anchor = GridBagConstraints.WEST;
		gbc_heightLabel.insets = new Insets(0, 0, 5, 5);
		gbc_heightLabel.gridx = 1;
		gbc_heightLabel.gridy = 2;
		contentPane.add(heightLabel, gbc_heightLabel);
		
		heightValue = new JLabel("");
		GridBagConstraints gbc_heightValue = new GridBagConstraints();
		gbc_heightValue.insets = new Insets(0, 0, 5, 0);
		gbc_heightValue.gridx = 2;
		gbc_heightValue.gridy = 2;
		contentPane.add(heightValue, gbc_heightValue);
		
		iterationsLabel = new JLabel("Iterations:");
		GridBagConstraints gbc_iterationsLabel = new GridBagConstraints();
		gbc_iterationsLabel.anchor = GridBagConstraints.WEST;
		gbc_iterationsLabel.insets = new Insets(0, 0, 5, 5);
		gbc_iterationsLabel.gridx = 1;
		gbc_iterationsLabel.gridy = 3;
		contentPane.add(iterationsLabel, gbc_iterationsLabel);
		
		iterationsValue = new JTextField();
		iterationsValue.setText("20");
		GridBagConstraints gbc_iterationsValue = new GridBagConstraints();
		gbc_iterationsValue.insets = new Insets(0, 0, 5, 0);
		gbc_iterationsValue.fill = GridBagConstraints.HORIZONTAL;
		gbc_iterationsValue.gridx = 2;
		gbc_iterationsValue.gridy = 3;
		contentPane.add(iterationsValue, gbc_iterationsValue);
		iterationsValue.setColumns(10);
		
		clustersLabel = new JLabel("Clusters:");
		GridBagConstraints gbc_clustersLabel = new GridBagConstraints();
		gbc_clustersLabel.anchor = GridBagConstraints.WEST;
		gbc_clustersLabel.insets = new Insets(0, 0, 5, 5);
		gbc_clustersLabel.gridx = 1;
		gbc_clustersLabel.gridy = 4;
		contentPane.add(clustersLabel, gbc_clustersLabel);
		
		clustersValue = new JTextField();
		clustersValue.setText("16");
		GridBagConstraints gbc_clustersValue = new GridBagConstraints();
		gbc_clustersValue.insets = new Insets(0, 0, 5, 0);
		gbc_clustersValue.fill = GridBagConstraints.HORIZONTAL;
		gbc_clustersValue.gridx = 2;
		gbc_clustersValue.gridy = 4;
		contentPane.add(clustersValue, gbc_clustersValue);
		clustersValue.setColumns(10);
		
		calculateButton = new JButton("Calculate...");
		GridBagConstraints gbc_calculateButton = new GridBagConstraints();
		gbc_calculateButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_calculateButton.gridwidth = 2;
		gbc_calculateButton.insets = new Insets(0, 0, 5, 0);
		gbc_calculateButton.gridx = 1;
		gbc_calculateButton.gridy = 5;
		contentPane.add(calculateButton, gbc_calculateButton);
		calculateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (sourceImage == null) {
					return;
				}
				int clusters = Integer.parseInt(clustersValue.getText());
				int iterations = Integer.parseInt(iterationsValue.getText());
				//
				calculateButton.setEnabled(false);
				progressBar.setValue(0);
				//
				SwingWorker<Void, Void> worker = new SwingWorker<>() {
					@Override
					protected Void doInBackground() throws Exception {
						targetImages = new ArrayList<>(clusters);
						for (int numberOfColors = 1; numberOfColors <= clusters; ++numberOfColors) {
							targetImages.add(KMeansCluster.createColorReducedImage(sourceImage, numberOfColors, iterations));
							setProgress(Math.min(100, (int) (100.0 * numberOfColors / clusters)));
						}
						setProgress(100);
						return null;
					}

					protected void done() {
						Dictionary<Integer, JLabel> labels = new Hashtable<>();
						labels.put(0, new JLabel(Integer.toString(1)));
						labels.put(clusters - 1, new JLabel(Integer.toString(clusters)));
						//
						slider.setValueIsAdjusting(true);
						slider.setMinimum(0);
						slider.setMaximum(clusters - 1);
						slider.setValue(0);
						slider.setLabelTable(labels);
						slider.setValueIsAdjusting(false);
						//
						calculateButton.setEnabled(true);
					}
				};
				worker.addPropertyChangeListener(event -> {
					if ("progress".equals(event.getPropertyName())) {
						progressBar.setValue((int) event.getNewValue());
					}
				});
				worker.execute();
			}
		});
		
		progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		GridBagConstraints gbc_progressBar = new GridBagConstraints();
		gbc_progressBar.insets = new Insets(0, 0, 5, 0);
		gbc_progressBar.fill = GridBagConstraints.HORIZONTAL;
		gbc_progressBar.gridwidth = 2;
		gbc_progressBar.gridx = 1;
		gbc_progressBar.gridy = 6;
		contentPane.add(progressBar, gbc_progressBar);

		lblColors = new JLabel("----- Number of Colors -----");
		GridBagConstraints gbc_lblColors = new GridBagConstraints();
		gbc_lblColors.gridwidth = 2;
		gbc_lblColors.insets = new Insets(0, 0, 5, 0);
		gbc_lblColors.gridx = 1;
		gbc_lblColors.gridy = 7;
		contentPane.add(lblColors, gbc_lblColors);

		slider = new JSlider();
		slider.setMaximum(0);
		slider.setPaintLabels(true);
		slider.setPaintTicks(true);
		GridBagConstraints gbc_slider = new GridBagConstraints();
		gbc_slider.gridwidth = 2;
		gbc_slider.fill = GridBagConstraints.HORIZONTAL;
		gbc_slider.insets = new Insets(0, 0, 5, 0);
		gbc_slider.gridx = 1;
		gbc_slider.gridy = 8;
		contentPane.add(slider, gbc_slider);
		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if (!source.getValueIsAdjusting() && targetImages != null) {
					ColorReducedImage image = targetImages.get(source.getValue());
					//
					meansList.setListData(image.palette().toArray(Color[]::new));
					//
					panel.setContent(image.image());
					panel.repaint();
				}
			}
		});

		meansLabel = new JLabel("----- Palette -----");
		GridBagConstraints gbc_meansLabel = new GridBagConstraints();
		gbc_meansLabel.gridwidth = 2;
		gbc_meansLabel.insets = new Insets(0, 0, 5, 0);
		gbc_meansLabel.gridx = 1;
		gbc_meansLabel.gridy = 9;
		contentPane.add(meansLabel, gbc_meansLabel);
		
		scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridwidth = 2;
		gbc_scrollPane.gridx = 1;
		gbc_scrollPane.gridy = 10;
		contentPane.add(scrollPane, gbc_scrollPane);

		meansList = new JList<>();
		meansList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		meansList.setFixedCellHeight(16);
		meansList.setCellRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = 1L;
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				JLabel component = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				component.setText("");
				component.setBackground((Color) value);
				return component;
			}
		});
		scrollPane.setViewportView(meansList);
	}

}
