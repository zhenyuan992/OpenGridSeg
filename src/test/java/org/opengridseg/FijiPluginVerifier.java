package org.opengridseg;

import org.scijava.Context;
import org.scijava.command.Command;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.PluginService;

public final class FijiPluginVerifier {
    private FijiPluginVerifier() {}

    public static void main(String[] args) {
        Context context=new Context(PluginService.class);
        try {
            PluginService plugins=context.service(PluginService.class);
            for (PluginInfo<Command> info : plugins.getPluginsOfType(Command.class)) {
                if (OpenGridSegPlugin.class.getName().equals(info.getClassName())) {
                    System.out.println("FIJI_PLUGIN_FOUND class="+info.getClassName()+" menu="+info.getMenuPath());
                    return;
                }
            }
            throw new IllegalStateException("OpenGridSeg was not discovered by Fiji's PluginService");
        } finally {
            context.dispose();
        }
    }
}
