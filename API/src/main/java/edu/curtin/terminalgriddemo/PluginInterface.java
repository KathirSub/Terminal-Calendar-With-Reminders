package edu.curtin.terminalgriddemo;

import java.util.List;

public interface PluginInterface {
    List<String> processPlugin(String[] pluginParts);

    void notify(String text);

}
