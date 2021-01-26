package com.bzcom.neurotec.biometricface;

import com.bzcom.neurotec.commons.BasePanel;
import com.bzcom.neurotec.commons.FaceTools;
import com.bzcom.neurotec.commons.LicensingPanel;
import com.neurotec.biometrics.*;
import com.neurotec.biometrics.client.NBiometricClient;
import com.neurotec.biometrics.swing.NFaceView;
import com.neurotec.devices.NCamera;
import com.neurotec.devices.NDevice;
import com.neurotec.devices.NDeviceManager;
import com.neurotec.devices.NDeviceType;
import com.neurotec.io.NFile;
import com.neurotec.swing.NViewZoomSlider;
import com.neurotec.util.concurrent.CompletionHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;

//4
public final class EnrollFromCamera extends BasePanel implements ActionListener {

	// ===========================================================
	// Nested classes
	// ===========================================================

	private static class CameraSelectionListener implements ItemListener {
		@Override
		public void itemStateChanged(ItemEvent e) {
			FaceTools.getInstance().getClient().setFaceCaptureDevice((NCamera) e.getItem());
		}

	}

	// ===========================================================
	// Private static fields
	// ===========================================================

	private static final long serialVersionUID = 1L;

	private static final String PANEL_TITLE = "Enroll from camera";
	private static final String REFRESH_LIST_BUTTON_TEXT = "Refresh list";
	private static final String START_CAPTIRIN_BUTTON_TEXT = "Start capturing";
	private static final String STOP_CAPTURING_BUTTON_TEXT = "Stop capturing";
	private static final String RESET_DEFAULTS_BUTTON_TEXT = "Default";
	private static final String START_EXTRACTION_BUTTON_TEXT = "Start extraction";
	private static final String SAVE_TEMPLATE_BUTTON_TEXT = "Save template";
	private static final String SAVE_IMAGE_BUTTON_TEXT = "Save image";
	private static final int DEFAULT_STREAM_DURATION_IN_FRAMES_VALUE = 10;

	// ===========================================================
	// Private fields
	// ===========================================================

	private final JFileChooser fc;
	private NFaceView view;
	private NViewZoomSlider zoomSlider;
	private JPanel panelToolbar;
	private JPanel panelControls;
	private JPanel panelCameras;
	private JPanel panelCameraControls;
	private JPanel panelStatusBar;
	private JPanel panelSouthPanel;
	private JPanel panelSave;
	private JLabel lblStatus;
	private JLabel lblQuality;
	private JButton btnRefreshList;
	private JButton btnStartCapturing;
	private JButton btnStopCapturing;
	private JButton btnStartExtraction;
	private JButton btnSaveTemplate;
	private JButton btnSaveImage;
	private JComboBox comboBoxCameras;
	private JScrollPane scrollPane;
	private JCheckBox cbAutomatic;
	private JCheckBox cbCheckLiveness;

	private NSubject subject;
	private boolean capturing;
	private final NDeviceManager deviceManager;

	private final CaptureCompletionHandler captureCompletionHandler = new CaptureCompletionHandler();

	// ===========================================================
	// Public constructor
	// ===========================================================

	public EnrollFromCamera() {
		super();
		this.setName(PANEL_TITLE);

		requiredLicenses.add("Biometrics.FaceSegmentation"); //for liveness checking
		requiredLicenses.add("Biometrics.FaceExtraction");
		requiredLicenses.add("Devices.Cameras");

		FaceTools.getInstance().getClient().setUseDeviceManager(true);
		deviceManager = FaceTools.getInstance().getClient().getDeviceManager();
		deviceManager.setDeviceTypes(EnumSet.of(NDeviceType.CAMERA));
		deviceManager.initialize();
		fc = new JFileChooser();

	}

	// ===========================================================
	// Private methods
	// ===========================================================

	private void updateCamerasList() {
		DefaultComboBoxModel model = (DefaultComboBoxModel) comboBoxCameras.getModel();
		model.setSelectedItem(null);
		model.removeAllElements();
		for (NDevice device : deviceManager.getDevices()) {
			model.addElement(device);
		}
		NCamera camera = FaceTools.getInstance().getClient().getFaceCaptureDevice();
		if ((camera == null) && (model.getSize() > 0)) {
			comboBoxCameras.setSelectedIndex(0);
		} else if (camera != null) {
			model.setSelectedItem(camera);
		}
	}

	private void startCapturing() {
		lblStatus.setText("");
		lblQuality.setText("");
		if (FaceTools.getInstance().getClient().getFaceCaptureDevice() == null) {
			JOptionPane.showMessageDialog(this, "Please select camera from the list.", "No camera selected", JOptionPane.PLAIN_MESSAGE);
			return;
		}
		// Set face capture from stream.
		NFace face = new NFace();
		EnumSet<NBiometricCaptureOption> options = EnumSet.of(NBiometricCaptureOption.STREAM);
		if (!cbAutomatic.isSelected()) {
			options.add(NBiometricCaptureOption.MANUAL);
		}
		if (cbCheckLiveness.isSelected()) {
			FaceTools.getInstance().getClient().setFacesLivenessMode(NLivenessMode.PASSIVE_AND_ACTIVE);
		} else {
			FaceTools.getInstance().getClient().setFacesLivenessMode(NLivenessMode.NONE);
		}
		face.setCaptureOptions(options);
		subject = new NSubject();
		subject.getFaces().add(face);
		view.setFace(face);

		// Begin capturing.
		capturing = true;
		FaceTools.getInstance().getClient().capture(subject, null, captureCompletionHandler);
		updateControls();
	}

	private void stopCapturing() {
		FaceTools.getInstance().getClient().cancel();
	}

	private void startExtraction() {
		lblStatus.setText("Extracting...");
		updateFacesTools();
		FaceTools.getInstance().getClient().forceStart();
	}

	private void saveTemplate() throws IOException {
		if (subject != null) {
			fc.setSelectedFile(new File("subject"));
			if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
				NFile.writeAllBytes(fc.getSelectedFile().getAbsolutePath(), subject.getTemplateBuffer());
			}
		}
	}

	private void saveImage() throws IOException {
		if (subject != null) {
			fc.setSelectedFile(new File("image.png"));
			if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
				subject.getFaces().get(0).getImage().save(fc.getSelectedFile().getAbsolutePath());
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
			panelToolbar = new JPanel();
			panelToolbar.setLayout(new BoxLayout(panelToolbar, BoxLayout.Y_AXIS));
			add(panelToolbar, BorderLayout.PAGE_START);
			{
				licensing = new LicensingPanel(requiredLicenses, Collections.<String>emptyList());
				panelToolbar.add(licensing);
			}
			{
				panelControls = new JPanel();
				panelControls.setLayout(new FlowLayout(FlowLayout.LEFT));
				panelControls.setBorder(BorderFactory.createLineBorder(Color.BLACK));
				panelControls.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
				panelToolbar.add(panelControls);
				{
					panelCameras = new JPanel();
					panelCameras.setLayout(new BorderLayout());
					panelCameras.setPreferredSize(new Dimension(560, 75));
					panelCameras.setBorder(BorderFactory.createTitledBorder("Cameras"));
					panelControls.add(panelCameras);
					{
						comboBoxCameras = new JComboBox();
						comboBoxCameras.addItemListener(new CameraSelectionListener());
						panelCameras.add(comboBoxCameras, BorderLayout.PAGE_START);
					}
					{
						panelCameraControls = new JPanel(new FlowLayout(FlowLayout.LEADING));
						panelCameras.add(panelCameraControls, BorderLayout.CENTER);
						{
							btnRefreshList = new JButton(REFRESH_LIST_BUTTON_TEXT);
							btnRefreshList.addActionListener(this);
							panelCameraControls.add(btnRefreshList);
						}
						{
							btnStartCapturing = new JButton(START_CAPTIRIN_BUTTON_TEXT);
							btnStartCapturing.addActionListener(this);
							panelCameraControls.add(btnStartCapturing);
						}
						{
							btnStopCapturing = new JButton(STOP_CAPTURING_BUTTON_TEXT);
							btnStopCapturing.addActionListener(this);
							panelCameraControls.add(btnStopCapturing);
						}
						{
							cbAutomatic = new JCheckBox("Capture automatically");
							panelCameraControls.add(cbAutomatic);
						}
						{
							cbCheckLiveness = new JCheckBox("Check liveness");
							panelCameraControls.add(cbCheckLiveness);
						}
					}
				}
			}
		}
		{
			view = new NFaceView();
			view.setAutofit(true);
			scrollPane = new JScrollPane();
			scrollPane.setViewportView(view);
			add(scrollPane, BorderLayout.CENTER);
		}
		{
			panelSouthPanel = new JPanel();
			panelSouthPanel.setLayout(new BorderLayout());
			add(panelSouthPanel, BorderLayout.PAGE_END);
			{
				panelStatusBar = new JPanel();
				panelStatusBar.setLayout(new FlowLayout(FlowLayout.LEFT));
				panelSouthPanel.add(panelStatusBar, BorderLayout.WEST);
				{
					btnStartExtraction = new JButton(START_EXTRACTION_BUTTON_TEXT);
					btnStartExtraction.addActionListener(this);
					panelStatusBar.add(btnStartExtraction);
				}
				{
					lblStatus = new JLabel();
					lblStatus.setPreferredSize(new Dimension(100, 20));
					panelStatusBar.add(lblStatus);
				}
				{
					lblQuality = new JLabel();
					lblQuality.setPreferredSize(new Dimension(180, 20));
					panelStatusBar.add(lblQuality);
				}
			}
			{
				panelSave = new JPanel();
				panelSave.setLayout(new FlowLayout(FlowLayout.RIGHT));
				panelSouthPanel.add(panelSave, BorderLayout.EAST);
				{
					zoomSlider = new NViewZoomSlider();
					zoomSlider.setView(view);
					panelSave.add(zoomSlider);
				}
				{
					btnSaveTemplate = new JButton(SAVE_TEMPLATE_BUTTON_TEXT);
					btnSaveTemplate.addActionListener(this);
					panelSave.add(btnSaveTemplate);
				}
				{
					btnSaveImage = new JButton(SAVE_IMAGE_BUTTON_TEXT);
					btnSaveImage.addActionListener(this);
					panelSave.add(btnSaveImage);
				}
			}
		}
		updateCamerasList();
	}

	@Override
	protected void setDefaultValues() {
		// No default values.
	}

	@Override
	protected void updateControls() {
		comboBoxCameras.setEnabled(!capturing);
		btnRefreshList.setEnabled(!capturing);
		btnStartCapturing.setEnabled(!capturing);
		btnStopCapturing.setEnabled(capturing);
		btnStartExtraction.setEnabled(capturing && !cbAutomatic.isSelected());
		cbAutomatic.setEnabled(!capturing);
		cbCheckLiveness.setEnabled(!capturing);

		btnSaveTemplate.setEnabled(!capturing && (subject != null) && (subject.getStatus() == NBiometricStatus.OK));
		btnSaveImage.setEnabled(!capturing && (subject != null) && (subject.getStatus() == NBiometricStatus.OK));
	}

	protected void updateFacesTools() {
		NBiometricClient client = FaceTools.getInstance().getClient();
		client.setUseDeviceManager(true);
	}

	// ===========================================================
	// Package private methods
	// ===========================================================

	void updateStatus(String status) {
		lblStatus.setText(status);
		if (status.equals("OK")){
			lblQuality.setText(String.format("Quality: %d", (subject.getFaces().get(0).getObjects().get(0).getQuality() & 0xFF)));
		}
	}

	NSubject getSubject() {
		return subject;
	}

	// ===========================================================
	// Public methods
	// ===========================================================

	@Override
	public void actionPerformed(ActionEvent ev) {
		try {
			if (ev.getSource().equals(btnRefreshList)) {
				updateCamerasList();
			} else if (ev.getSource().equals(btnStartCapturing)) {
				startCapturing();
			} else if (ev.getSource().equals(btnStartExtraction)) {
				startExtraction();
			} else if (ev.getSource().equals(btnStopCapturing)) {
				stopCapturing();
			} else if (ev.getSource().equals(btnSaveTemplate)) {
				saveTemplate();
			} else if (ev.getSource().equals(btnSaveImage)) {
				saveImage();
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	@Override
	public void onDestroy() {
		deviceManager.dispose();
	}

	@Override
	public void onClose() {
		stopCapturing();
	}

	// ===========================================================
	// Inner classes
	// ===========================================================


	private class CaptureCompletionHandler implements CompletionHandler<NBiometricStatus, Object> {

		@Override
		public void completed(final NBiometricStatus result, final Object attachment) {
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					capturing = false;
					updateStatus(result.toString());
					if ((result == NBiometricStatus.OK) || (result == NBiometricStatus.CANCELED)) { // Finished successfully or stop button was pressed.
						updateControls();
					} else {
						// Template creation failed, so start capturing again.
						getSubject().getFaces().get(0).setImage(null);
						FaceTools.getInstance().getClient().capture(getSubject(), null, captureCompletionHandler);
					}
				}

			});
		}

		@Override
		public void failed(final Throwable th, final Object attachment) {
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					capturing = false;
					showError(th);
					updateControls();
				}

			});
		}

	}

}
