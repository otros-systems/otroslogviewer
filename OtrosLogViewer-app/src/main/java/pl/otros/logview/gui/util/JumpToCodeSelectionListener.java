package pl.otros.logview.gui.util;

import com.google.common.util.concurrent.ListenableScheduledFuture;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import org.apache.commons.lang.StringUtils;
import org.jdesktop.swingx.JXTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otros.logview.api.LogData;
import pl.otros.logview.api.ConfKeys;
import pl.otros.logview.api.LogDataTableModel;
import pl.otros.logview.api.OtrosApplication;
import pl.otros.logview.api.LocationInfo;
import pl.otros.logview.api.services.JumpToCodeService;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.Optional;

/**
 * JumpToCodeSelectionListener perorms jump to code in IDE if selected log event contains location info, applications has focus and jump to code is enabled
 */
public class JumpToCodeSelectionListener implements ListSelectionListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(JumpToCodeSelectionListener.class.getName());

    private final OtrosApplication otrosApplication;
    private final LogDataTableModel dataTableModel;
    private final JXTable table;
    private Optional<? extends ListenableScheduledFuture<?>> scheduledJump;
    private int delayMs;

    /**
     * Constructor
     * @param otrosApplication OtrosApplication
     * @param dataTableModel data model
     * @param table table
     * @param delayMs delay in ms to perform jump. If another selection was made before delay, previous jump will be cancelled
     */
    public JumpToCodeSelectionListener(OtrosApplication otrosApplication, LogDataTableModel dataTableModel, JXTable table, int delayMs) {
        this.otrosApplication = otrosApplication;
        this.dataTableModel = dataTableModel;
        this.table = table;
        this.delayMs = delayMs;
        scheduledJump = Optional.empty();
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        boolean hasFocus = otrosApplication.getApplicationJFrame().isFocused();
        final boolean enabled = otrosApplication.getConfiguration().getBoolean(ConfKeys.JUMP_TO_CODE_AUTO_JUMP_ENABLED, false);
        if (hasFocus && enabled && !e.getValueIsAdjusting()) {
            try {
                final LogData logData = dataTableModel.getLogData(table.convertRowIndexToModel(e.getFirstIndex()));
                Optional<Integer> line = Optional.empty();
                if (StringUtils.isNotBlank(logData.getLine()) && StringUtils.isAlphanumeric(logData.getLine())){
                  line = Optional.of( Integer.valueOf(logData.getLine()));
                }
                final LocationInfo li = new LocationInfo(
                  Optional.ofNullable(logData.getClazz()).orElseGet(logData::getLoggerName),
                  logData.getMethod(), logData.getFile(),
                  line,
                  Optional.ofNullable(logData.getMessage()));
                final JumpToCodeService jumpToCodeService = otrosApplication.getServices().getJumpToCodeService();
                final boolean ideAvailable = jumpToCodeService.isIdeAvailable();
                if (ideAvailable) {
                    scheduledJump.map(input -> {
                        input.cancel(false);
                        return Boolean.TRUE;
                    });
                    ListeningScheduledExecutorService scheduledExecutorService = otrosApplication.getServices().getTaskSchedulerService().getListeningScheduledExecutorService();
                    delayMs = 300;
                    ListenableScheduledFuture<?> jump = scheduledExecutorService.schedule(
                            new JumpRunnable(li, jumpToCodeService), delayMs, TimeUnit.MILLISECONDS
                    );

                    scheduledJump = Optional.of(jump);
                }
            } catch (Exception e1) {
                LOGGER.warn("Can't perform jump to code: " + e1.getMessage(),e1);
              e1.printStackTrace();
            }

        }
    }

    private class JumpRunnable implements Runnable {
        private final LocationInfo li;
        private final JumpToCodeService jumpToCodeService;

        public JumpRunnable(LocationInfo li, JumpToCodeService jumpToCodeService) {
            this.li = li;
            this.jumpToCodeService = jumpToCodeService;
        }

        public void run() {
            LOGGER.trace("Jumping to " + li);
            try {
                jumpToCodeService.jump(li);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }
}
