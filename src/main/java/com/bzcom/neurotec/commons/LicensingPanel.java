package com.bzcom.neurotec.commons;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


//2
public final class LicensingPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private static final String REQUIRED_COMPONENT_LICENSES_LABEL_TEXT = "Required component licenses: ";
    private static final String COMPONENTS_OBTAINED_STATUS_TEXT = "Component licenses successfully obtained";
    private static final String COMPONENTS_NOT_OBTAINED_STATUS_TEXT = "Component licenses not obtained";

    private static final Color COMPONENTS_OBTAINED_STATUS_TEXT_COLOR = Color.green.darker();
    private static final Color COMPONENTS_NOT_OBTAINED_STATUS_TEXT_COLOR = Color.red.darker();

    private static final int BORDER_WIDTH_TOP = 5;
    private static final int BORDER_WIDTH_LEFT = 5;
    private static final int BORDER_WIDTH_BOTTOM = 5;
    private static final int BORDER_WIDTH_RIGHT = 5;

    private final List<String> requiredComponents;
    private final List<String> optionalComponents;

    private JLabel lblRequiredComponentLicenses;
    private JLabel lblRequiredComponentLicensesList;
    private JLabel lblStatus;



    public LicensingPanel(List<String> required, List<String> optional) {
        super(new BorderLayout(), true);
        init();
        if (required == null){
            requiredComponents = new ArrayList<String>();
        } else {
            requiredComponents = new ArrayList<String>(required);
        }
        if (optional == null){
            optionalComponents = new ArrayList<String>();
        } else {
          optionalComponents = new ArrayList<String>(optional);
        }
    }

    public LicensingPanel(){
        this(null, null);
    }

    private void init(){
        setBorder(BorderFactory.createLineBorder(Color.BLACK));
        {
            lblRequiredComponentLicenses = new JLabel(REQUIRED_COMPONENT_LICENSES_LABEL_TEXT);
            lblRequiredComponentLicenses.setFont(new Font(lblRequiredComponentLicenses.getFont().getName(), Font.BOLD, 11));
            lblRequiredComponentLicenses.setBorder(BorderFactory.createEmptyBorder(BORDER_WIDTH_TOP, BORDER_WIDTH_LEFT, BORDER_WIDTH_BOTTOM, BORDER_WIDTH_RIGHT));
            this.add(lblRequiredComponentLicenses, BorderLayout.LINE_START);
        }
        {
            lblRequiredComponentLicensesList = new JLabel();
            lblRequiredComponentLicensesList.setFont(new Font(lblRequiredComponentLicensesList.getFont().getName(), Font.PLAIN, 11));
            lblRequiredComponentLicensesList.setBorder(BorderFactory.createEmptyBorder(BORDER_WIDTH_TOP, BORDER_WIDTH_LEFT, BORDER_WIDTH_BOTTOM, BORDER_WIDTH_RIGHT));
            this.add(lblRequiredComponentLicensesList, BorderLayout.CENTER);
        }
        {
            lblStatus = new JLabel();
            lblStatus.setFont(new Font(lblStatus.getFont().getName(), Font.PLAIN, 11));
            lblStatus.setBorder(BorderFactory.createEmptyBorder(BORDER_WIDTH_TOP, BORDER_WIDTH_LEFT, BORDER_WIDTH_BOTTOM, BORDER_WIDTH_RIGHT));
            setComponentObtainingStatus(false);
            this.add(lblStatus, BorderLayout.PAGE_END);
        }
    }

    private String getRequiredComponentsString() {
        StringBuilder result = new StringBuilder();
        Map<String, Boolean> licenses = FaceTools.getInstance().getLicenses();
        for (String component : requiredComponents) {
            if (licenses.get(component)) {
                result.append("<font color=green>").append(component).append("</font>, ");
            } else {
                result.append("<font color=red>").append(component).append("</font>, ");
            }
        }
        if (result.length() > 0) {
            result.delete(result.length() - 2, result.length());
        }
        return result.toString();
    }

    private String getOptionalComponentsString(){
        if (optionalComponents == null){
            return "";
        }

        StringBuilder result = new StringBuilder();
        Map<String, Boolean> licenses = FaceTools.getInstance().getLicenses();
        for (String component : optionalComponents){
            if (licenses.get(component)){
                result.append("<font color=green").append(component).append(" (optional)</font>, ");
            } else {
                result.append("<font color=red>").append(component).append(" (optional)</font>, ");
            }
            if (result.length() > 0){
                result.delete(result.length() - 2, result.length());
            }
        }
        return result.toString();
    }

    private void updateList(){
        StringBuilder result = new StringBuilder("<html>").append(getRequiredComponentsString());
        if (!optionalComponents.isEmpty()){
            result.append(", ").append(getOptionalComponentsString());
        }
        result.append("</html>");
        lblRequiredComponentLicensesList.setText(result.toString());
    }

    public void setRequiredComponents(List<String> components){
        requiredComponents.clear();
        requiredComponents.addAll(components);
        updateList();
    }

    public void setOptionalComponents(List<String> components){
        optionalComponents.clear();
        optionalComponents.addAll(components);
        updateList();
    }

    public void setComponentObtainingStatus(boolean succeeded){
        if (succeeded){
            lblStatus.setText(COMPONENTS_OBTAINED_STATUS_TEXT);
            lblStatus.setForeground(COMPONENTS_OBTAINED_STATUS_TEXT_COLOR);
        } else {
            lblStatus.setText(COMPONENTS_NOT_OBTAINED_STATUS_TEXT);
            lblStatus.setForeground(COMPONENTS_NOT_OBTAINED_STATUS_TEXT_COLOR);
        }
    }
}
