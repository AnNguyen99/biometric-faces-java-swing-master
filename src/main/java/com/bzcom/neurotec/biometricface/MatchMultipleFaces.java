package com.bzcom.neurotec.biometricface;

import com.bzcom.neurotec.commons.BasePanel;
import com.bzcom.neurotec.commons.FaceTools;
import com.bzcom.neurotec.commons.LicensingPanel;
import com.bzcom.neurotec.swing.ImageThumbnailFileChooser;
import com.bzcom.neurotec.util.Utils;
import com.neurotec.biometrics.*;
import com.neurotec.biometrics.client.NBiometricClient;
import com.neurotec.biometrics.swing.NFaceView;
import com.neurotec.swing.NViewZoomSlider;
import com.neurotec.util.concurrent.CompletionHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;

//9
public final class MatchMultipleFaces extends BasePanel implements ActionListener {

	// ===========================================================
	// Private static fields
	// ===========================================================

	private static final long serialVersionUID = 1L;

	private static final String PANEL_TITLE = "Match multiple Faces";
	private static final String OPEN_REFERENCE_IMAGE_BUTTON_TEXT = "Open reference image";
	private static final String OPEN_MULTIPLE_FACE_IMAGE_BUTTON_TEXT = "Open multiple face image";

	private static final String ATTACHMENT_REFERENCE = "reference";
	private static final String ATTACHMENT_MULTIPLE_FACES = "multiple_faces";

	// ===========================================================
	// Private fields
	// ===========================================================

	private NSubject reference;
	private NSubject multipleFaces;

	private final EnrollHandler enrollHandler = new EnrollHandler();
	private final IdentificationHandler identificationHandler = new IdentificationHandler();
	private final TemplateCreationHandler templateCreationHandler = new TemplateCreationHandler();

	private final ImageThumbnailFileChooser fc;
	private JPanel panelToolBar;
	private JPanel panelView;
	private JPanel panelControls;
	private JButton btnOpenReferenceImage;
	private JButton btnOpenMultipleFaceImage;
	private NFaceView viewReference;
	private NFaceView viewMultipleFace;
	private JScrollPane scrollPaneReference;
	private JScrollPane scrollPaneMultipleFace;
	private JPanel referencePanel;
	private JPanel multipleFacePanel;
	private JPanel panelReferenceZoomSlider;
	private NViewZoomSlider referenceZoomSlider;
	private NViewZoomSlider multipleFaceZoomSlider;

	// ===========================================================
	// Public constructor
	// ===========================================================

	public MatchMultipleFaces() {
		super();
		this.setName(PANEL_TITLE);

		requiredLicenses.add("Biometrics.FaceExtraction");
		requiredLicenses.add("Biometrics.FaceMatching");

		fc = new ImageThumbnailFileChooser();
		fc.setIcon(Utils.createIconImage("images/Logo16x16.png"));
	}

	// ===========================================================
	// Private methods
	// ===========================================================

	private void openReferenceImage() throws IOException {
		fc.setMultiSelectionEnabled(false);
		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			reference = new NSubject();
			NFace face = new NFace();
			face.setFileName(fc.getSelectedFile().getAbsolutePath());
			reference.getFaces().add(face);
			viewReference.setFace(face);

			updateFacesTools();

			// Set template size (medium is recommended for matching). (optional)
			FaceTools.getInstance().getClient().setFacesTemplateSize(NTemplateSize.MEDIUM);

			FaceTools.getInstance().getClient().createTemplate(reference, ATTACHMENT_REFERENCE, templateCreationHandler);
		}
	}

	private void openMultipleFaceImage() throws IOException {
		fc.setMultiSelectionEnabled(false);
		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			FaceTools.getInstance().getClient().clear();
			multipleFaces = new NSubject();
			NFace face = new NFace();
			face.setFileName(fc.getSelectedFile().getAbsolutePath());
			multipleFaces.getFaces().add(face);
			viewMultipleFace.setFace(face);

			// Image can have more than one faces.
			multipleFaces.setMultipleSubjects(true);

			updateFacesTools();

			// Set template size (large is recommended for enrollment). (optional)
			FaceTools.getInstance().getClient().setFacesTemplateSize(NTemplateSize.LARGE);

			FaceTools.getInstance().getClient().createTemplate(multipleFaces, ATTACHMENT_MULTIPLE_FACES, templateCreationHandler);
		}
	}

	// ===========================================================
	// Protected methods
	// ===========================================================

	@Override
	protected void initGUI() {
		setLayout(new BorderLayout());
		panelToolBar = new JPanel();
		panelToolBar.setLayout(new BoxLayout(panelToolBar, BoxLayout.Y_AXIS));
		add(panelToolBar, BorderLayout.PAGE_START);
		{
			licensing = new LicensingPanel(requiredLicenses, Collections.<String>emptyList());
			panelToolBar.add(licensing);
		}
		{
			panelControls = new JPanel();
			panelControls.setLayout(new FlowLayout());
			panelControls.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
			panelToolBar.add(panelControls);
			{
				btnOpenReferenceImage = new JButton(OPEN_REFERENCE_IMAGE_BUTTON_TEXT);
				btnOpenReferenceImage.addActionListener(this);
				panelControls.add(btnOpenReferenceImage);
			}
			{
				btnOpenMultipleFaceImage = new JButton(OPEN_MULTIPLE_FACE_IMAGE_BUTTON_TEXT);
				btnOpenMultipleFaceImage.addActionListener(this);
				panelControls.add(btnOpenMultipleFaceImage);
			}
		}
		panelView = new JPanel();
		panelView.setLayout(new GridLayout());
		add(panelView, BorderLayout.CENTER);
		{
			referencePanel = new JPanel();
			referencePanel.setLayout(new BorderLayout());
			panelView.add(referencePanel);
			{
				scrollPaneReference = new JScrollPane();
				viewReference = new NFaceView();
				viewReference.setAutofit(true);
				scrollPaneReference.setViewportView(viewReference);
				referencePanel.add(scrollPaneReference, BorderLayout.CENTER);
			}
			{
				panelReferenceZoomSlider = new JPanel();
				panelReferenceZoomSlider.setLayout(new BorderLayout());
				referencePanel.add(panelReferenceZoomSlider, BorderLayout.SOUTH);
				{
					referenceZoomSlider = new NViewZoomSlider();
					referenceZoomSlider.setView(viewReference);
					panelReferenceZoomSlider.add(referenceZoomSlider, BorderLayout.WEST);
				}
			}
		}
		{
			multipleFacePanel = new JPanel();
			multipleFacePanel.setLayout(new BorderLayout());
			panelView.add(multipleFacePanel);
			{
				scrollPaneMultipleFace = new JScrollPane();
				viewMultipleFace = new NFaceView();
				viewMultipleFace.setAutofit(true);
				scrollPaneMultipleFace.setViewportView(viewMultipleFace);
				multipleFacePanel.add(scrollPaneMultipleFace);
			}
			{
				JPanel multipleFaceZoomSliderPanel = new JPanel();
				multipleFaceZoomSliderPanel.setLayout(new BorderLayout());
				multipleFacePanel.add(multipleFaceZoomSliderPanel, BorderLayout.SOUTH);
				{
					multipleFaceZoomSlider = new NViewZoomSlider();
					multipleFaceZoomSlider.setView(viewMultipleFace);
					multipleFaceZoomSliderPanel.add(multipleFaceZoomSlider, BorderLayout.EAST);
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
		// No controls to update.
	}

	protected void updateFacesTools() {
		NBiometricClient client = FaceTools.getInstance().getClient();
		client.setFacesDetectAllFeaturePoints(true);
	}

	// ===========================================================
	// Package private methods
	// ===========================================================

	void enrollMultipleFacesSubject() {
		NBiometricTask enrollTask = new NBiometricTask(EnumSet.of(NBiometricOperation.ENROLL));

		// Enroll all faces.
		multipleFaces.setId("firstSubject");
		enrollTask.getSubjects().add(multipleFaces);

		int i = 0;
		for (NSubject subject : multipleFaces.getRelatedSubjects()) {
			subject.setId("relatedSubject" + i);
			i++;
			enrollTask.getSubjects().add(subject);
		}
		FaceTools.getInstance().getClient().performTask(enrollTask, null, enrollHandler);
	}

	NSubject getReference() {
		return reference;
	}

	void setReference(NSubject reference) {
		this.reference = reference;
	}

	NSubject getMultipleFaces() {
		return multipleFaces;
	}

	void setMultipleFaces(NSubject multipleFaces) {
		this.multipleFaces = multipleFaces;
	}

	void setScores(String[] scores) {
		viewMultipleFace.setFaceIds(scores);
	}

	// ===========================================================
	// Public methods
	// ===========================================================

	@Override
	public void actionPerformed(ActionEvent ev) {
		try {
			if (ev.getSource().equals(btnOpenReferenceImage)) {
				openReferenceImage();
			} else if (ev.getSource().equals(btnOpenMultipleFaceImage)) {
				openMultipleFaceImage();
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e, "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	@Override
	public void onDestroy() {
	}

	@Override
	public void onClose() {
	}

	// ===========================================================
	// Inner classes
	// ===========================================================

	private class TemplateCreationHandler implements CompletionHandler<NBiometricStatus, String> {

		@Override
		public void completed(NBiometricStatus status, String subject) {
			if (status == NBiometricStatus.OK) {
				if (ATTACHMENT_REFERENCE.equals(subject)) {
					if (getMultipleFaces() != null) {
						FaceTools.getInstance().getClient().identify(getReference(), null, identificationHandler);
					}
				} else if (ATTACHMENT_MULTIPLE_FACES.equals(subject)) {
					enrollMultipleFacesSubject();
				} else {
					throw new AssertionError("Unknown attachment: " + subject);
				}
			} else {
				if (ATTACHMENT_REFERENCE.equals(subject)) {
					setReference(null);
				} else if (ATTACHMENT_MULTIPLE_FACES.equals(subject)) {
					setMultipleFaces(null);
				} else {
					throw new AssertionError("Unknown attachment: " + subject);
				}
				JOptionPane.showMessageDialog(MatchMultipleFaces.this, "Template was not created: " + status, "Error", JOptionPane.WARNING_MESSAGE);
			}
		}

		@Override
		public void failed(Throwable th, String subject) {
			showError(th);
		}

	}

	private class EnrollHandler implements CompletionHandler<NBiometricTask, Object> {

		@Override
		public void completed(NBiometricTask task, Object attachment) {
			if (task.getStatus() == NBiometricStatus.OK) {

				// Identify current subject in enrolled ones.
				if (getReference() != null) {
					FaceTools.getInstance().getClient().identify(getReference(), null, identificationHandler);
				}
			} else {
				JOptionPane.showMessageDialog(MatchMultipleFaces.this, "Enrollment failed: " + task.getStatus(), "Error", JOptionPane.WARNING_MESSAGE);
			}
		}

		@Override
		public void failed(Throwable th, Object attachment) {
			showError(th);
		}

	}

	private class IdentificationHandler implements CompletionHandler<NBiometricStatus, Object> {

		@Override
		public void completed(final NBiometricStatus status, final Object attachment) {
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					if ((status == NBiometricStatus.OK) || (status == NBiometricStatus.MATCH_NOT_FOUND)) {

						int multipleFacesCount = getMultipleFaces().getFaces().size() + getMultipleFaces().getRelatedSubjects().size();
						String[] results = new String[multipleFacesCount];

						// Get matching scores.
						for (NMatchingResult result : getReference().getMatchingResults()) {
							int score = result.getScore();
							if (result.getId().equals(getMultipleFaces().getId())) {
								results[0] = String.format("Score: %d (match)", score);
							} else {
								for (int j = 0; j < getMultipleFaces().getRelatedSubjects().size(); j++) {
									if (result.getId().equals(getMultipleFaces().getRelatedSubjects().get(j).getId())) {
										results[j + 1] = String.format("Score: %d (match)", score);
									}
								}
							}
						}

						// All not matched faces have score 0.
						for (int i = 0; i < results.length; i++) {
							if (results[i] == null) {
								results[i] = "Score: 0";
							}
						}
						setScores(results);
					} else {
						JOptionPane.showMessageDialog(MatchMultipleFaces.this, "Identification failed: " + status, "Error", JOptionPane.WARNING_MESSAGE);
					}
				}

			});
		}

		@Override
		public void failed(Throwable th, Object attachment) {
			showError(th);
		}

	}

}
