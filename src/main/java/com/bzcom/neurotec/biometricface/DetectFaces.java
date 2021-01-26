package com.bzcom.neurotec.biometricface;

import com.bzcom.neurotec.commons.BasePanel;
import com.bzcom.neurotec.commons.FaceTools;
import com.bzcom.neurotec.commons.LicensingPanel;
import com.bzcom.neurotec.swing.ImageThumbnailFileChooser;
import com.bzcom.neurotec.util.Utils;
import com.neurotec.biometrics.NFace;
import com.neurotec.biometrics.client.NBiometricClient;
import com.neurotec.biometrics.swing.NFaceView;
import com.neurotec.images.NImage;
import com.neurotec.licensing.NLicense;
import com.neurotec.swing.NViewZoomSlider;
import com.neurotec.util.concurrent.CompletionHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//3
public final class DetectFaces extends BasePanel implements ActionListener {

    // ===========================================================
    // Private static fields
    // ===========================================================

    private static final long serialVersionUID = 1L;
    /*private static final String PANEL_TITLE = "Detect faces";
    private static final String LOAD_IMAGE_BUTTON_TEXT = "Load image";
    private static final String MAX_ROLL_ANGLE_DEVIATION_LABEL_TEXT = "Roll angle deviation";
    private static final String MAX_YAW_ANGLE_DEVIATION_LABEL_TEXT = "Max yaw angle deviation";
    private static final String DETECT_FACIAL_FEATURES_BUTTON_TEXT = "Detect facial features";*/

    //translate english to vietnamese
    private static final String PANEL_TITLE = "Phát hiện khuôn mặt";
    private static final String LOAD_IMAGE_BUTTON_TEXT = "Tải hình ảnh";
    private static final String MAX_ROLL_ANGLE_DEVIATION_LABEL_TEXT = "Độ lệch góc cuộn";
    private static final String MAX_YAW_ANGLE_DEVIATION_LABEL_TEXT = "Độ lệch góc hàm tối đa";
    private static final String DETECT_FACIAL_FEATURES_BUTTON_TEXT = "Phát hiện các đặc điểm trên khuôn mặt";

    // ===========================================================
    // Private fields
    // ===========================================================

    private NImage image;
    private JPanel panelToolbar;
    private JPanel panelControls;
    private JLabel lblMaxRollAngleDeviation;
    private JLabel lblMaxYawAngleDeviation;
    private JButton btnLoadImage;
    private JButton btnDetectFacialFeatures;
    private JComboBox comboBoxMaxRollAngleDeviation;
    private JComboBox comboBoxMaxYawAngleDeviation;
    private NFaceView view;
    private NViewZoomSlider zoomSlider;
    private JScrollPane scrollPane;
    private final ImageThumbnailFileChooser imageThumbnailFileChooser;

    private final FaceDetectionHandler faceDetectionHandler = new FaceDetectionHandler();

    // ===========================================================
    // Public constructor
    // ===========================================================

    public DetectFaces() {
        super();
        this.setName(PANEL_TITLE);
        requiredLicenses.add("Biometrics.FaceDetection");
        optionalLicenses.add("Biometrics.FaceSegmentsDetection");

        imageThumbnailFileChooser = new ImageThumbnailFileChooser();
        imageThumbnailFileChooser.setIcon(Utils.createIconImage("images/Logo16x16.png"));
    }

    // ===========================================================
    // Private methods
    // ===========================================================

    private void detectFace(NImage faceImage) {
        NBiometricClient client = FaceTools.getInstance().getClient();
        updateFacesTools();
        client.detectFaces(faceImage, null, faceDetectionHandler);
    }

    private void updateComboBoxes() {
        updateRollAngleDeviationComboBox();
        updateYawAngleDeviationComboBox();
    }

    private void updateRollAngleDeviationComboBox() {
        DefaultComboBoxModel model = (DefaultComboBoxModel) comboBoxMaxRollAngleDeviation.getModel();
        Float item = FaceTools.getInstance().getClient().getFacesMaximalRoll();
        updateComboBoxValues(model, item, 0, 180);
    }

    private void updateYawAngleDeviationComboBox() {
        DefaultComboBoxModel model = (DefaultComboBoxModel) comboBoxMaxYawAngleDeviation.getModel();
        Float item = FaceTools.getInstance().getClient().getFacesMaximalYaw();
        updateComboBoxValues(model, item, 0, 90);
    }

    private void updateComboBoxValues(DefaultComboBoxModel model, Float item, int min, int max) {
        List<Float> items = new ArrayList<Float>();
        for (float i = min; i <= max; i += 15) {
            items.add((i));
        }

        if (!items.contains(item)) {
            items.add(item);
        }

        Collections.sort(items);
        for (int i = 0; i != items.size(); i++) {
            model.addElement(items.get(i));
        }
        model.setSelectedItem(item);
    }

    private void openFile() throws IOException {
        if (imageThumbnailFileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            image = NImage.fromFile(imageThumbnailFileChooser.getSelectedFile().getAbsolutePath());
            detectFace(image);
        }
    }

    private void detectFacialFeaturesClick() {
        if (image != null) {
            detectFace(image);
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
                licensing = new LicensingPanel(requiredLicenses, optionalLicenses);
                panelToolbar.add(licensing);
            }
            {
                panelControls = new JPanel();
                panelControls.setLayout(new FlowLayout(FlowLayout.LEFT));
                panelControls.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                panelControls.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
                panelToolbar.add(panelControls);
                {
                    btnLoadImage = new JButton(LOAD_IMAGE_BUTTON_TEXT);
                    btnLoadImage.addActionListener(this);
                    panelControls.add(btnLoadImage);
                }
                {
                    lblMaxRollAngleDeviation = new JLabel(MAX_ROLL_ANGLE_DEVIATION_LABEL_TEXT);
                    panelControls.add(lblMaxRollAngleDeviation);
                }
                {
                    comboBoxMaxRollAngleDeviation = new JComboBox();
                    panelControls.add(comboBoxMaxRollAngleDeviation);
                }
                {
                    lblMaxYawAngleDeviation = new JLabel(MAX_YAW_ANGLE_DEVIATION_LABEL_TEXT);
                    panelControls.add(lblMaxYawAngleDeviation);
                }
                {
                    comboBoxMaxYawAngleDeviation = new JComboBox();
                    panelControls.add(comboBoxMaxYawAngleDeviation);
                }
                {
                    btnDetectFacialFeatures = new JButton(DETECT_FACIAL_FEATURES_BUTTON_TEXT);
                    btnDetectFacialFeatures.addActionListener(this);
                    panelControls.add(btnDetectFacialFeatures);
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
            zoomSlider = new NViewZoomSlider();
            zoomSlider.setView(view);
            add(zoomSlider, BorderLayout.SOUTH);
        }
        updateComboBoxes();
    }

    @Override
    protected void setDefaultValues() {
        Float defaultMaxRoll = FaceTools.getInstance().getDefaultClient().getFacesMaximalRoll();
        comboBoxMaxRollAngleDeviation.setSelectedItem(defaultMaxRoll);
        if (!defaultMaxRoll.equals(comboBoxMaxRollAngleDeviation.getSelectedItem())) {
            comboBoxMaxRollAngleDeviation.addItem(defaultMaxRoll);
            comboBoxMaxRollAngleDeviation.setSelectedItem(defaultMaxRoll);
        }
        Float defaultMaxYaw = FaceTools.getInstance().getDefaultClient().getFacesMaximalYaw();
        comboBoxMaxYawAngleDeviation.setSelectedItem(defaultMaxYaw);
        if (!defaultMaxYaw.equals(comboBoxMaxYawAngleDeviation.getSelectedItem())) {
            comboBoxMaxYawAngleDeviation.addItem(defaultMaxYaw);
            comboBoxMaxYawAngleDeviation.setSelectedItem(defaultMaxYaw);
        }
    }

    @Override
    protected void updateControls() {
        // Nothing to update.
    }

    protected void updateFacesTools() {
        NBiometricClient client = FaceTools.getInstance().getClient();
        client.reset();
        client.setFacesMaximalRoll((Float) comboBoxMaxRollAngleDeviation.getSelectedItem());
        client.setFacesMaximalYaw((Float) comboBoxMaxYawAngleDeviation.getSelectedItem());
        boolean faceSegmentsDetectionActivated;
        try {
            faceSegmentsDetectionActivated = NLicense.isComponentActivated("Biometrics.FaceSegmentsDetection");
        } catch (IOException e) {
            e.printStackTrace();
            faceSegmentsDetectionActivated = false;
        }
        client.setFacesDetectAllFeaturePoints(faceSegmentsDetectionActivated);
        client.setFacesDetectBaseFeaturePoints(faceSegmentsDetectionActivated);
    }

    // ===========================================================
    // Package private methods
    // ===========================================================

    void setFace(NFace face) {
        view.setFace(face);
    }

    // ===========================================================
    // Public methods
    // ===========================================================

    @Override
    public void actionPerformed(ActionEvent ev) {
        try {
            if (ev.getSource().equals(btnLoadImage)) {
                openFile();
            } else if (ev.getSource().equals(btnDetectFacialFeatures)) {
                detectFacialFeaturesClick();
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, e, "Error", JOptionPane.ERROR_MESSAGE);
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

    private class FaceDetectionHandler implements CompletionHandler<NFace, Object> {

        @Override
        public void completed(final NFace result, final Object attachment) {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    setFace(result);
                }

            });
        }

        @Override
        public void failed(final Throwable th, final Object attachment) {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    showError(th);
                }

            });
        }

    }
}
