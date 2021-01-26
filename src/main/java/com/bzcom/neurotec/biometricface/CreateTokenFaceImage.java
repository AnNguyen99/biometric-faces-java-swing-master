package com.bzcom.neurotec.biometricface;

import com.bzcom.neurotec.commons.BasePanel;
import com.bzcom.neurotec.commons.FaceTools;
import com.bzcom.neurotec.commons.LicensingPanel;
import com.bzcom.neurotec.swing.ImageThumbnailFileChooser;
import com.bzcom.neurotec.util.Utils;
import com.neurotec.biometrics.*;
import com.neurotec.biometrics.swing.NFaceView;
import com.neurotec.swing.NViewZoomSlider;
import com.neurotec.util.concurrent.CompletionHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EnumSet;


//2
public class CreateTokenFaceImage extends BasePanel implements ActionListener {

	// ===========================================================
	// Private static fields
	// ===========================================================

	private static final long serialVersionUID = 1L;

	// ===========================================================
	// Private fields
	// ===========================================================

	private NSubject subject;
	private NFace tokenFace;

	private final ImageThumbnailFileChooser fc;
	private NFaceView viewOriginalImage;
	private NFaceView viewTokenImage;
	private NViewZoomSlider originalImageZoomSlider;
	private NViewZoomSlider tokenImageZoomSlider;

	private final ImageCreationHandler imageCreationHandler = new ImageCreationHandler();

	private JButton btnOpen;
	private JButton btnSave;
	private JLabel lblBackground;
	private JLabel lblDensity;
	private JLabel lblOriginalImage;
	private JLabel lblQuality;
	private JLabel lblSharpness;
	private JLabel lblTokenImage;
	private JPanel panelBottom;
	private JPanel panelCenter;
	private JPanel panelImageLabels;
	private JPanel panelMain;
	private JPanel panelOriginalImage;
	private JPanel panelTokenImage;
	private JPanel panelTokenImageView;
	private JPanel panelTop;
	private JPanel tokenImageZoomPanel;
	private JPanel originalImageZoomSliderPanel;
	private JScrollPane spOriginalImage;
	private JScrollPane spTokenImage;

	// ===========================================================
	// Public constructor
	// ===========================================================

	public CreateTokenFaceImage() {
		super();
		setName("Create token face image");

		requiredLicenses.add("Biometrics.FaceDetection");
		requiredLicenses.add("Biometrics.FaceSegmentation");
		requiredLicenses.add("Biometrics.FaceQualityAssessment");

		fc = new ImageThumbnailFileChooser();
		fc.setIcon(Utils.createIconImage("images/Logo16x16.png"));
	}

	// ===========================================================
	// Private methods
	// ===========================================================

	private void createImage(String imagePath) {
		NFace face = new NFace();
		face.setFileName(imagePath);
		subject = new NSubject();
		subject.getFaces().add(face);
		viewOriginalImage.setFace(face);
		NBiometricTask task = FaceTools.getInstance().getClient().createTask(EnumSet.of(NBiometricOperation.SEGMENT, NBiometricOperation.ASSESS_QUALITY), subject);
		FaceTools.getInstance().getClient().performTask(task, null, imageCreationHandler);
	}

	private void showTokenAttributes() {
		NLAttributes attributes = tokenFace.getObjects().get(0);
		lblQuality.setText("Quality: " + attributes.getQuality());
		lblSharpness.setText("Sharpness score: " + attributes.getSharpness());
		lblBackground.setText("Background uniformity score: " + attributes.getBackgroundUniformity());
		lblDensity.setText("Grayscale density score: " + attributes.getGrayscaleDensity());
	}

	private void showAttributeLabels(boolean show) {
		lblQuality.setVisible(show);
		lblSharpness.setVisible(show);
		lblBackground.setVisible(show);
		lblDensity.setVisible(show);
	}

	// ===========================================================
	// Protected methods
	// ===========================================================

	@Override
	protected void initGUI() {
		GridBagConstraints gridBagConstraints;
		setLayout(new BorderLayout());
		{
			licensing = new LicensingPanel(requiredLicenses, optionalLicenses);
			add(licensing, BorderLayout.NORTH);
		}
		{
			panelMain = new JPanel();
			panelMain.setLayout(new BorderLayout());
			add(panelMain, BorderLayout.CENTER);
			{
				panelTop = new JPanel();
				panelTop.setLayout(new FlowLayout(FlowLayout.LEADING));
				panelMain.add(panelTop, BorderLayout.NORTH);
				{
					btnOpen = new JButton();
					btnOpen.setText("Open image");
					btnOpen.addActionListener(this);
					panelTop.add(btnOpen);
				}
			}
			{
				panelCenter = new JPanel();
				panelCenter.setLayout(new GridLayout(1, 2, 5, 0));
				panelMain.add(panelCenter, BorderLayout.CENTER);
				{
					panelOriginalImage = new JPanel();
					panelOriginalImage.setLayout(new BorderLayout());
					panelCenter.add(panelOriginalImage);
					{
						lblOriginalImage = new JLabel();
						lblOriginalImage.setBackground(SystemColor.activeCaption);
						lblOriginalImage.setHorizontalAlignment(SwingConstants.CENTER);
						lblOriginalImage.setText("Original face image");
						lblOriginalImage.setToolTipText("");
						lblOriginalImage.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
						lblOriginalImage.setOpaque(true);
						panelOriginalImage.add(lblOriginalImage, BorderLayout.NORTH);
					}
					{
						spOriginalImage = new JScrollPane();
						panelOriginalImage.add(spOriginalImage, BorderLayout.CENTER);
						{
							viewOriginalImage = new NFaceView();
							viewOriginalImage.setAutofit(true);
							spOriginalImage.setViewportView(viewOriginalImage);
						}
						{
							originalImageZoomSliderPanel = new JPanel();
							originalImageZoomSliderPanel.setLayout(new BorderLayout());
							panelOriginalImage.add(originalImageZoomSliderPanel, BorderLayout.SOUTH);
							{
								originalImageZoomSlider = new NViewZoomSlider();
								originalImageZoomSlider.setView(viewOriginalImage);
								originalImageZoomSliderPanel.add(originalImageZoomSlider, BorderLayout.WEST);
							}
						}
					}
				}
				{
					panelTokenImage = new JPanel();
					panelTokenImage.setLayout(new BorderLayout());
					panelCenter.add(panelTokenImage);
					{
						lblTokenImage = new JLabel();
						lblTokenImage.setBackground(SystemColor.activeCaption);
						lblTokenImage.setHorizontalAlignment(SwingConstants.CENTER);
						lblTokenImage.setText("Token face image");
						lblTokenImage.setToolTipText("");
						lblTokenImage.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
						lblTokenImage.setOpaque(true);
						panelTokenImage.add(lblTokenImage, BorderLayout.NORTH);
					}
					{
						spTokenImage = new JScrollPane();
						panelTokenImage.add(spTokenImage, BorderLayout.CENTER);
						{
							panelTokenImageView = new JPanel();
							panelTokenImageView.setLayout(new OverlayLayout(panelTokenImageView));
							spTokenImage.setViewportView(panelTokenImageView);
							{
								panelImageLabels = new JPanel();
								panelImageLabels.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
								panelImageLabels.setOpaque(false);
								GridBagLayout panelImageLabelsLayout = new GridBagLayout();
								panelImageLabelsLayout.rowWeights = new double[] {1.0, 0.0, 0.0, 0.0, 0.0};
								panelImageLabels.setLayout(panelImageLabelsLayout);
								panelTokenImageView.add(panelImageLabels);
								{
									lblQuality = new JLabel();
									lblQuality.setText("Quality: ");
									lblQuality.setOpaque(true);
									gridBagConstraints = new GridBagConstraints();
									gridBagConstraints.gridx = 1;
									gridBagConstraints.gridy = 1;
									gridBagConstraints.anchor = GridBagConstraints.LINE_START;
									panelImageLabels.add(lblQuality, gridBagConstraints);
								}
								{
									lblSharpness = new JLabel();
									lblSharpness.setText("Sharpness score: ");
									lblSharpness.setOpaque(true);
									gridBagConstraints = new GridBagConstraints();
									gridBagConstraints.gridx = 1;
									gridBagConstraints.gridy = 2;
									gridBagConstraints.anchor = GridBagConstraints.LINE_START;
									panelImageLabels.add(lblSharpness, gridBagConstraints);
								}
								{
									lblBackground = new JLabel();
									lblBackground.setText("Background uniformity score: ");
									lblBackground.setOpaque(true);
									gridBagConstraints = new GridBagConstraints();
									gridBagConstraints.gridx = 1;
									gridBagConstraints.gridy = 3;
									gridBagConstraints.anchor = GridBagConstraints.LINE_START;
									panelImageLabels.add(lblBackground, gridBagConstraints);
								}
								{
									lblDensity = new JLabel();
									lblDensity.setText("Grayscale density score: ");
									lblDensity.setOpaque(true);
									gridBagConstraints = new GridBagConstraints();
									gridBagConstraints.gridx = 1;
									gridBagConstraints.gridy = 4;
									gridBagConstraints.anchor = GridBagConstraints.LINE_START;
									panelImageLabels.add(lblDensity, gridBagConstraints);
								}
							}
							{
								viewTokenImage = new NFaceView();
								viewTokenImage.setAutofit(true);
								panelTokenImageView.add(viewTokenImage);
							}
						}
					}
					{
						tokenImageZoomPanel = new JPanel();
						tokenImageZoomPanel.setLayout(new BorderLayout());
						panelTokenImage.add(tokenImageZoomPanel, BorderLayout.SOUTH);
						{
							tokenImageZoomSlider = new NViewZoomSlider();
							tokenImageZoomSlider.setView(viewTokenImage);
							tokenImageZoomPanel.add(tokenImageZoomSlider, BorderLayout.EAST);
						}
					}
				}
			}
			{
				panelBottom = new JPanel();
				panelBottom.setLayout(new FlowLayout(FlowLayout.TRAILING));
				panelMain.add(panelBottom, BorderLayout.PAGE_END);
				{
					btnSave = new JButton();
					btnSave.setText("Save token image");
					btnSave.addActionListener(this);
					panelBottom.add(btnSave);
				}
			}
		}
	}

	@Override
	protected void setDefaultValues() {
		// No default values.
	}

	@Override
	protected void updateControls() {
		btnSave.setEnabled(tokenFace != null);
		showAttributeLabels(tokenFace != null);
	}

	protected void updateFacesTools() {
		FaceTools.getInstance().getClient().reset();
	}

	// ===========================================================
	// Public methods
	// ===========================================================

	@Override
	public void onDestroy() {
		// Do nothing.
	}

	@Override
	public void onClose() {
		// Do nothing.
	}

	@Override
	public void actionPerformed(ActionEvent ev) {
		try {
			if (ev.getSource().equals(btnOpen)) {
				if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
					createImage(fc.getSelectedFile().getAbsolutePath());
				}
			} else if (ev.getSource().equals(btnSave)) {
				if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
					tokenFace.getImage().save(fc.getSelectedFile().getAbsolutePath());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	// ===========================================================
	// Inner classes
	// ===========================================================

	private class ImageCreationHandler implements CompletionHandler<NBiometricTask, Void> {

		@Override
		public void completed(final NBiometricTask task, final Void attachment) {
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					NBiometricStatus status = task.getStatus();
					if (status == NBiometricStatus.OK) {
						tokenFace = subject.getFaces().get(1);
						viewTokenImage.setFace(tokenFace);
						showTokenAttributes();
					} else {
						JOptionPane.showMessageDialog(CreateTokenFaceImage.this, "Could not create token face image. Status: " + status);
						tokenFace = null;
						viewTokenImage.setFace(null);
					}
					updateControls();
				}

			});
		}

		@Override
		public void failed(final Throwable th, final Void attachment) {
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					tokenFace = null;
					viewTokenImage.setFace(null);
					showError(th);
				}

			});
		}

	}

}
