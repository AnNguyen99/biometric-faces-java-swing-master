package com.bzcom.neurotec.biometricface;

import com.bzcom.neurotec.commons.BasePanel;
import com.bzcom.neurotec.commons.FaceTools;
import com.bzcom.neurotec.commons.LicensingPanel;
import com.bzcom.neurotec.swing.ImageThumbnailFileChooser;
import com.bzcom.neurotec.util.Utils;
import com.neurotec.biometrics.NBiometricStatus;
import com.neurotec.biometrics.NFace;
import com.neurotec.biometrics.NSubject;
import com.neurotec.biometrics.swing.NFaceView;
import com.neurotec.images.NImage;
import com.neurotec.images.NImages;
import com.neurotec.io.NFile;
import com.neurotec.swing.NViewZoomSlider;
import com.neurotec.util.concurrent.CompletionHandler;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

//6
public final class GeneralizeFace extends BasePanel implements ActionListener {

	// ===========================================================
	// Private static fields
	// ===========================================================

	private static final String PANEL_TITLE = "Generalize face";
	private static final long serialVersionUID = 1L;

	// ===========================================================
	// Private fields
	// ===========================================================

	private ImageThumbnailFileChooser fcOpen;
	private ImageThumbnailFileChooser fcSave;
	private JButton btnOpenImage;
	private JButton btnSaveTemplate;
	private JFileChooser fcSaveTemplate;
	private JLabel lblStatus;
	private JLabel lblImagesLoaded;
	private JLabel lblImageCount;
	private JPanel actionButtonsPanel;
	private JPanel panelNorth;
	private JPanel panelCenter;
	private JPanel panelSouth;
	private JPanel saveTemplatePanel;
	private JScrollPane scrollPaneImage;
	private NFaceView faceView;
	private NViewZoomSlider zoomSlider;
	private final TemplateCreationHandler templateCreationHandler = new TemplateCreationHandler();
	private NSubject subject = null;

	// ===========================================================
	// Public constructor
	// ===========================================================

	public GeneralizeFace() {
		super();
		setName(PANEL_TITLE);
		requiredLicenses.add("Biometrics.FaceExtraction");
	}

	// ===========================================================
	// Private methods
	// ===========================================================

	private void openImages() throws IOException {
		subject = null;
		faceView.setFace(null);
		btnSaveTemplate.setEnabled(false);
		lblImageCount.setText("0");
		lblStatus.setVisible(false);

		if (fcOpen.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			File[] files = fcOpen.getSelectedFiles();
			subject = new NSubject();
			for (File file : files) {
				NFace face = new NFace();
				face.setImage(NImage.fromFile(file.getAbsolutePath()));
				face.setSessionId(1); // all faces with same session will be generalized
				subject.getFaces().add(face);
			}
			lblImageCount.setText(String.valueOf(files.length));
			lblStatus.setText("Status: performing extraction and generalization");
			lblStatus.setVisible(true);
			updateControls();
			updateFacesTools();
			FaceTools.getInstance().getClient().createTemplate(subject, null, templateCreationHandler);
		}
	}

	private void saveTemplate() throws IOException {
		if (subject != null) {
			if (fcSaveTemplate.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
				try {
					NFile.writeAllBytes(fcSaveTemplate.getSelectedFile().getAbsolutePath(), subject.getTemplateBuffer());
				} catch (Exception e) {
					showError(e);
				}
			}
		}
	}

	// ===========================================================
	// Protected methods
	// ===========================================================

	@Override
	protected void initGUI() {
		setLayout(new BorderLayout());
		{
			panelNorth = new JPanel();
			panelNorth.setLayout(new BoxLayout(panelNorth, BoxLayout.Y_AXIS));
			add(panelNorth, BorderLayout.NORTH);

			licensing = new LicensingPanel(requiredLicenses, optionalLicenses);
			panelNorth.add(licensing);
			{
				actionButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
				actionButtonsPanel.setBorder(new TitledBorder(null, "Load face images", TitledBorder.LEADING, TitledBorder.TOP, null, null));
				panelNorth.add(actionButtonsPanel);
				{
					btnOpenImage = new JButton("Open image");
					btnOpenImage.addActionListener(this);
					actionButtonsPanel.add(btnOpenImage);
				}
				{
					lblImagesLoaded = new JLabel("Images loaded:");
					actionButtonsPanel.add(lblImagesLoaded);
				}
				{
					lblImageCount = new JLabel("0");
					actionButtonsPanel.add(lblImageCount);
				}
			}
			{
				panelCenter = new JPanel();
				panelCenter.setLayout(new GridLayout(1, 1));
				add(panelCenter, BorderLayout.CENTER);
				{
					scrollPaneImage = new JScrollPane();
					scrollPaneImage.setMinimumSize(new Dimension(100, 100));
					scrollPaneImage.setPreferredSize(new Dimension(200, 200));
					faceView = new NFaceView();
					faceView.setAutofit(true);
					scrollPaneImage.setViewportView(faceView);
					panelCenter.add(scrollPaneImage);
				}
			}
			{
				panelSouth = new JPanel();
				panelSouth.setLayout(new BorderLayout());
				add(panelSouth, BorderLayout.SOUTH);
				{
					saveTemplatePanel = new JPanel();
					saveTemplatePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
					panelSouth.add(saveTemplatePanel, BorderLayout.WEST);
					{
						btnSaveTemplate = new JButton("Save template");
						btnSaveTemplate.setEnabled(false);
						btnSaveTemplate.addActionListener(this);
						saveTemplatePanel.add(btnSaveTemplate);
					}
					{
						lblStatus = new JLabel("Status: None");
						saveTemplatePanel.add(lblStatus);
					}
				}
				{
					zoomSlider = new NViewZoomSlider();
					zoomSlider.setView(faceView);
					panelSouth.add(zoomSlider, BorderLayout.EAST);
				}
			}

		}

		fcOpen = new ImageThumbnailFileChooser();
		fcOpen.setIcon(Utils.createIconImage("images/Logo16x16.png"));
		fcOpen.setFileFilter(new Utils.ImageFileFilter(NImages.getOpenFileFilter()));
		fcOpen.setMultiSelectionEnabled(true);
		fcSaveTemplate = new JFileChooser();
		fcSave = new ImageThumbnailFileChooser();
		fcSave.setIcon(Utils.createIconImage("images/Logo16x16.png"));
		fcSave.setFileFilter(new Utils.ImageFileFilter(NImages.getSaveFileFilter()));
	}

	@Override
	protected void setDefaultValues() {
	}

	@Override
	protected void updateControls() {
		btnSaveTemplate.setEnabled((subject != null) && (subject.getStatus() == NBiometricStatus.OK));
	}

	protected void updateFacesTools() {
		FaceTools.getInstance().getClient().reset();
	}

	// ===========================================================
	// Public methods
	// ===========================================================

	@Override
	public void actionPerformed(ActionEvent ev) {
		try {
			if (ev.getSource() == btnOpenImage) {
				openImages();
			} else if (ev.getSource() == btnSaveTemplate) {
				saveTemplate();
			}
		} catch (Exception e) {
			showError(e);
			updateControls();
		}
	}

	// ===========================================================
	// Inner classes
	// ===========================================================

	private class TemplateCreationHandler implements CompletionHandler<NBiometricStatus, Object> {

		@Override
		public void completed(final NBiometricStatus status, final Object attachment) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					if (status == NBiometricStatus.OK) {
						lblStatus.setText("Status: OK");
						btnSaveTemplate.setEnabled(true);
						faceView.setFace(subject.getFaces().get(subject.getFaces().size() - 1));
					} else {
						lblStatus.setText(String.format("Status: %s", status));
					}
					btnOpenImage.setEnabled(true);
				}
			});
		}

		@Override
		public void failed(final Throwable th, final Object attachment) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					lblStatus.setText("Status: Error occurred");
					showError(th);
					btnOpenImage.setEnabled(true);
				}
			});
		}
	}

	@Override
	public void onDestroy() {
	}

	@Override
	public void onClose() {
	}
}
