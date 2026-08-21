package org.opengridseg.ui;

import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

final class ProgressDisplay extends JPanel {
    private final JLabel label=new JLabel();
    private final JProgressBar bar=new JProgressBar();

    ProgressDisplay(){
        super(new BorderLayout(8,4));
        bar.setStringPainted(true);bar.setVisible(false);
        add(label,BorderLayout.CENTER);add(bar,BorderLayout.SOUTH);
    }
    void startIndeterminate(String message){label.setText(message);bar.setIndeterminate(true);bar.setString("Working …");bar.setVisible(true);}
    void update(int completed,int total,String message){
        label.setText(message);bar.setIndeterminate(false);bar.setMinimum(0);bar.setMaximum(Math.max(1,total));bar.setValue(Math.max(0,Math.min(completed,total)));
        int percent=total<=0?0:(int)Math.round(100.0*completed/total);bar.setString(percent+"%");bar.setVisible(true);
    }
    void finish(String message){label.setText(message);bar.setIndeterminate(false);bar.setVisible(false);}
    String text(){return label.getText();}int value(){return bar.getValue();}int maximum(){return bar.getMaximum();}boolean visibleProgress(){return bar.isVisible();}
}
