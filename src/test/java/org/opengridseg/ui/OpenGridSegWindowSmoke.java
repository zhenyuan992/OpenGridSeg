package org.opengridseg.ui;

import java.nio.file.Paths;
import java.util.List;
import javax.swing.SwingUtilities;
import org.opengridseg.io.DatasetScanner;
import org.opengridseg.io.DatasetValidator;
import org.opengridseg.io.FrameSet;

public final class OpenGridSegWindowSmoke {
    public static void main(String[] args)throws Exception{
        List<FrameSet> sets=DatasetScanner.scan(Paths.get(args[0]));DatasetValidator.validate(sets);
        SwingUtilities.invokeLater(()->new OpenGridSegWindow(Paths.get(args[0]),sets).setVisible(true));
    }
}
