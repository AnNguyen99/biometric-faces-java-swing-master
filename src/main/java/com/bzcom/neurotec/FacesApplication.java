package com.bzcom.neurotec;

import com.bzcom.neurotec.biometricface.*;
import com.bzcom.neurotec.commons.BasePanel;
import com.bzcom.neurotec.commons.FaceTools;
import com.bzcom.neurotec.util.LibraryManager;
import com.bzcom.neurotec.util.Utils;
import com.neurotec.lang.NCore;
import com.neurotec.licensing.NLicenseManager;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class FacesApplication implements ChangeListener {

    // ===========================================================
    // Private static fields
    // ===========================================================

    private static final String FRAME_TITLE = "Ứng dụng nhận diện khuôn mặt bằng công nghệ sinh trắc học";

    // ===========================================================
    // Static constructor
    // ===========================================================

    static {
        try {
            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
//            Logger.getLogger(Logger.getLogger("global").getName()).log(Level.FINE, e.getMessage(), e);
            Logger.getLogger(Logger.getLogger("toàn cầu").getName()).log(Level.FINE, e.getMessage(), e);
        }
    }

    // ===========================================================
    // Public static methods
    // ===========================================================

    public static void main(String[] args) {
        new FacesApplication();
    }

    // ===========================================================
    // Private fields
    // ===========================================================

    private JTabbedPane tabbedPane;
    private BasePanel panelDetectFaces;
    private BasePanel panelEnrollFromImage;
    private BasePanel panelEnrollFromCamera;
    private BasePanel panelIdentifyFace;
    private BasePanel panelVerifyFace;
    private BasePanel panelMatchMultipleFaces;
    private BasePanel panelCreateTokenFaceImage;
    private BasePanel panelGeneralizeFace;
    private BasePanel panelcaptureIcaoCompliantImage;

    // ===========================================================
    // Private constructor
    // ===========================================================

    private FacesApplication() {
        LibraryManager.initLibraryPath();

        //=========================================================================
        // TRIAL MODE
        //=========================================================================
        // Below code line determines whether TRIAL is enabled or not. To use purchased licenses, don't use below code line.
        // GetTrialModeFlag() method takes value from "Bin/Licenses/TrialFlag.txt" file. So to easily change mode for all our examples, modify that file.
        // Also you can just set TRUE to "TrialMode" property in code.
        //=========================================================================

        // ================================================ =========================
        // CHẾ ĐỘ THỬ NGHIỆM
        // ================================================ =========================
        // Dòng mã bên dưới xác định xem TRIAL có được bật hay không. Để sử dụng giấy phép đã mua, không sử dụng dòng mã bên dưới.
        // Phương thức GetTrialModeFlag () nhận giá trị từ tệp "Bin / Licenses / TrialFlag.txt". Vì vậy, để dễ dàng thay đổi chế độ cho tất cả các ví dụ của chúng tôi, hãy sửa đổi tệp đó.
        // Ngoài ra, bạn có thể đặt TRUE thành thuộc tính "TrialMode" trong mã.
        // ================================================ =========================

        try {
            boolean trialMode = Utils.getTrialModeFlag();
            NLicenseManager.setTrialMode(trialMode);
//            System.out.println("\tTrial mode: " + trialMode);
            System.out.println("\tChế độ thử nghiệm: " + trialMode);
        } catch (IOException e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame frame = new JFrame();
                try {
                    Dimension dimension  = new Dimension(900, 700);

                    frame.setSize(dimension);
                    frame.setMinimumSize(dimension);
                    frame.setPreferredSize(dimension);

                    frame.setResizable(true);
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.setResizable(false);
//                    frame.setTitle("Biometric Faces App");
                    frame.setTitle(FRAME_TITLE);
                    frame.setIconImage(Utils.createIconImage("images/logo.png"));

                    tabbedPane = new JTabbedPane();
                    frame.add(tabbedPane);
                    addTabs(tabbedPane);

                    frame.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosing(WindowEvent e) {
                            dispose();
                        }
                    });
                    frame.setLocationRelativeTo(null);
                    frame.setVisible(true);

                } catch (Throwable e) {
                    showError(null, e);
                }
            }
        });
    }

    // ===========================================================
    // Private methods
    // ===========================================================

    private void addTabs(JTabbedPane tabbedPane) {
        tabbedPane.setBackground(Color.WHITE);
        tabbedPane.addChangeListener(this::stateChanged);

        panelDetectFaces = new DetectFaces();
        panelDetectFaces.init();
        tabbedPane.addTab(panelDetectFaces.getName(), panelDetectFaces);

        panelEnrollFromImage = new EnrollFromImage();
        panelEnrollFromImage.init();
        tabbedPane.addTab(panelEnrollFromImage.getName(), panelEnrollFromImage);

        panelEnrollFromCamera = new EnrollFromCamera();
        panelEnrollFromCamera.init();
        tabbedPane.addTab(panelEnrollFromCamera.getName(), panelEnrollFromCamera);

        panelIdentifyFace = new IdentifyFace();
        panelIdentifyFace.init();
        tabbedPane.addTab(panelIdentifyFace.getName(), panelIdentifyFace);

        panelVerifyFace = new VerifyFace();
        panelVerifyFace.init();
        tabbedPane.addTab(panelVerifyFace.getName(), panelVerifyFace);

        panelMatchMultipleFaces = new MatchMultipleFaces();
        panelMatchMultipleFaces.init();
        tabbedPane.addTab(panelMatchMultipleFaces.getName(), panelMatchMultipleFaces);

        panelCreateTokenFaceImage = new CreateTokenFaceImage();
        panelCreateTokenFaceImage.init();
        tabbedPane.addTab(panelCreateTokenFaceImage.getName(), panelCreateTokenFaceImage);

        panelGeneralizeFace = new GeneralizeFace();
        panelGeneralizeFace.init();
        tabbedPane.addTab(panelGeneralizeFace.getName(), panelGeneralizeFace);

        panelcaptureIcaoCompliantImage = new CaptureIcaoCompliantImage();
        panelcaptureIcaoCompliantImage.init();
        tabbedPane.addTab(panelcaptureIcaoCompliantImage.getName(), panelcaptureIcaoCompliantImage);
    }

    private void showError(Component parentComponent, Throwable e) {
        e.printStackTrace();
        String message;
        if (e.getMessage() != null) {
            message = e.getMessage();
        } else if (e.getCause() != null) {
            message = e.getCause().getMessage();
        } else {
//            message = "An error occurred. Please see log for more details.";
            message = "Đã xảy ra lỗi. Vui lòng xem nhật ký để biết thêm chi tiết.";
        }
//        JOptionPane.showMessageDialog(parentComponent, message, "Error", JOptionPane.ERROR_MESSAGE);
        JOptionPane.showMessageDialog(parentComponent, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    private void dispose() {
        try {
            for (Component component : tabbedPane.getComponents()) {
                if (component instanceof BasePanel) {
                    ((BasePanel) component).onDestroy();
                }
            }
            NCore.shutdown();
        } catch (Exception e){
            new RuntimeException("Thao tác không hợp lệ" + e);
        }
    }

    // ===========================================================
    // Public methods
    // ===========================================================

    public void stateChanged(ChangeEvent ev) {
        if (ev.getSource() == tabbedPane) {

            if (panelEnrollFromCamera != null) {
                panelEnrollFromCamera.onClose();
            }
            try {
                switch (tabbedPane.getSelectedIndex()) {
                    case 0:
                        System.out.println("Start: " + panelDetectFaces.getName());
                        obtainLicenses(panelDetectFaces);
                        FaceTools.getInstance().resetParameters();
                        System.out.println();
                        break;
                    case 1:
                        System.out.println("Start: " + panelEnrollFromImage.getName());
                        obtainLicenses(panelEnrollFromImage);
                        FaceTools.getInstance().resetParameters();
                        System.out.println();
                        break;
                    case 2:
                        System.out.println("Start: " + panelEnrollFromCamera.getName());
                        obtainLicenses(panelEnrollFromCamera);
                        FaceTools.getInstance().resetParameters();
                        System.out.println();
                        break;
                    case 3:
                        System.out.println("Start: " + panelIdentifyFace.getName());
                        obtainLicenses(panelIdentifyFace);
                        FaceTools.getInstance().resetParameters();
                        System.out.println();
                        break;
                    case 4:
                        System.out.println("Start: " + panelVerifyFace.getName());
                        obtainLicenses(panelVerifyFace);
                        FaceTools.getInstance().resetParameters();
                        System.out.println();
                        break;
                    case 5:
                        System.out.println("Start: " + panelMatchMultipleFaces.getName());
                        obtainLicenses(panelMatchMultipleFaces);
                        FaceTools.getInstance().resetParameters();
                        System.out.println();
                        break;
                    case 6:
                        System.out.println("Start: " + panelCreateTokenFaceImage.getName());
                        obtainLicenses(panelCreateTokenFaceImage);
                        FaceTools.getInstance().resetParameters();
                        System.out.println();
                        break;
                    case 7:
                        System.out.println("Start: " + panelGeneralizeFace.getName());
                        obtainLicenses(panelGeneralizeFace);
                        FaceTools.getInstance().resetParameters();
                        System.out.println();
                        break;
                    case 8:
                        System.out.println("Start: " + panelcaptureIcaoCompliantImage.getName());
                        obtainLicenses(panelcaptureIcaoCompliantImage);
                        FaceTools.getInstance().resetParameters();
                        System.out.println();
                        break;
                    default:
//                        throw new IndexOutOfBoundsException("unreachable");
                        throw new IndexOutOfBoundsException("không thể tiếp cận ");
                }
            } catch (Exception e) {
                showError(null, e);
            }
        }
    }

    public void obtainLicenses(BasePanel panel) {
        try {
            if (!panel.isObtained()) {
                boolean status = FaceTools.getInstance().obtainLicenses(panel.getRequiredLicenses());
                FaceTools.getInstance().obtainLicenses(panel.getOptionalLicenses());
                panel.getLicensing().setRequiredComponents(panel.getRequiredLicenses());
                panel.getLicensing().setOptionalComponents(panel.getOptionalLicenses());
                panel.updateLicensing(status);
            }
        } catch (Exception e) {
            showError(null, e);
        }
    }
}
