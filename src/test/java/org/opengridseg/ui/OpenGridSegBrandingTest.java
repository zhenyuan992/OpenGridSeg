package org.opengridseg.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.awt.Component;
import java.awt.Container;
import java.awt.GraphicsEnvironment;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import org.opengridseg.OpenGridSegPlugin;
import org.opengridseg.io.DatasetScanner;
import org.opengridseg.io.FrameSet;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.scijava.plugin.Plugin;

final class OpenGridSegBrandingTest {
    @TempDir Path folder;
    @Test void pluginAndMainPanelUseOpenGridSegAndPlainLabels()throws Exception{
        Plugin plugin=OpenGridSegPlugin.class.getAnnotation(Plugin.class);assertEquals("Plugins>OpenGridSeg",plugin.menuPath());
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
        Files.createFile(folder.resolve("sample_Cell1_w1GFP_t1.TIF"));Files.createFile(folder.resolve("sample_Cell1_w2mCherry_t1.TIF"));Files.createFile(folder.resolve("sample_Cell1_w3BF_t1.TIF"));List<FrameSet> sets=DatasetScanner.scan(folder);
        SwingUtilities.invokeAndWait(()->{OpenGridSegWindow window=new OpenGridSegWindow(folder,sets);assertEquals("OpenGridSeg — Open Grid Segmentation",window.getTitle());JLabel template=findLabel(window,"Template bar (length / width, px)");assertNotNull(template);assertTrue(template.getToolTipText().contains("first number"));JLabel fov=findLabel(window,"FOV");assertNotNull(fov);String tip=fov.getToolTipText();assertTrue(tip.contains("same folder")&&tip.contains("_w1")&&tip.contains("GFP")&&tip.contains("_t1"),tip);window.dispose();});
    }
    private static JLabel findLabel(Container root,String text){for(Component component:root.getComponents()){if(component instanceof JLabel&&text.equals(((JLabel)component).getText()))return (JLabel)component;if(component instanceof Container){JLabel found=findLabel((Container)component,text);if(found!=null)return found;}}return null;}
}
