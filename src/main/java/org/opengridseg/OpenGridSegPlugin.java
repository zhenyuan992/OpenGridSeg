package org.opengridseg;

import ij.IJ;
import ij.io.DirectoryChooser;
import java.nio.file.Paths;
import java.util.List;
import javax.swing.SwingUtilities;
import org.opengridseg.io.DatasetScanner;
import org.opengridseg.io.DatasetValidator;
import org.opengridseg.io.FrameSet;
import org.opengridseg.ui.OpenGridSegWindow;
import org.scijava.command.Command;
import org.scijava.plugin.Plugin;

@Plugin(type = Command.class, menuPath = "Plugins>OpenGridSeg")
public final class OpenGridSegPlugin implements Command {
    @Override
    public void run() {
        String directory = System.getProperty("opengridseg.input");
        if (directory == null || directory.trim().isEmpty()) {
            DirectoryChooser chooser = new DirectoryChooser(
                "Choose folder containing BF, GFP, and mCherry TIFF files");
            directory = chooser.getDirectory();
        }
        if (directory == null) return;
        try {
            final java.nio.file.Path inputPath=Paths.get(directory);
            List<FrameSet> sets = DatasetScanner.scan(inputPath);
            DatasetValidator.validate(sets);
            SwingUtilities.invokeLater(() ->
                new OpenGridSegWindow(inputPath, sets).setVisible(true));
        } catch (Exception error) {
            IJ.handleException(error);
        }
    }
}
