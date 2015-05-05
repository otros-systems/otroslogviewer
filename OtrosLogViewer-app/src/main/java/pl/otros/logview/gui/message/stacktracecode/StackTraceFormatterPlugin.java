package pl.otros.logview.gui.message.stacktracecode;

import pl.otros.logview.api.plugins.*;
import pl.otros.logview.gui.message.MessageFormatter;
import pl.otros.logview.gui.services.jumptocode.JumpToCodeService;
import pl.otros.logview.pluginable.PluginableElementsContainer;

public class StackTraceFormatterPlugin implements Plugin {

    @Override
    public void initialize(PluginContext pluginContext) throws PluginException {
        final JumpToCodeService jumpToCodeService = pluginContext.getOtrosApplication().getServices().getJumpToCodeService();
        final StackTraceFormatter formatter = new StackTraceFormatter(jumpToCodeService);
        final PluginableElementsContainer<MessageFormatter> messageFormatters = pluginContext.getOtrosApplication().getAllPluginables().getMessageFormatters();
        messageFormatters.addElement(formatter);
    }

    @Override
    public PluginInfo getPluginInfo() {
        return new PluginInfoBuilder()
                .setName("StackTraceFormatter")
                .setDescription("StackTraceFormatter add code fragment to stacktrace using JumpToCode plugin in IDE")
                .setPluginableId(this.getClass().getName())
                .setApiVersion(PLUGIN_VERSION_1)
                .setPluginClass(this.getClass())
                .createPluginInfo();
    }
}
