/**
 * OrbisGIS is a GIS application dedicated to scientific spatial simulation.
 * This cross-platform GIS is developed at French IRSTV institute and is able to
 * manipulate and create vector and raster spatial information.
 *
 * OrbisGIS is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2012 IRSTV (FR CNRS 2488)
 *
 * This file is part of OrbisGIS.
 *
 * OrbisGIS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * OrbisGIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * OrbisGIS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */
package org.orbisgis.view.output;

import java.awt.Color;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.SwingUtilities;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.orbisgis.core.events.EventException;
import org.orbisgis.core.events.Listener;
import org.orbisgis.core.events.ListenerContainer;

/**
 * A LOG4J Appender connected with the LogPanel.
 */
public class PanelAppender extends AppenderSkeleton {
    public interface ShowMessageListener extends Listener<ShowMessageEventData> {
            
    }
    //New duplicata message is ignored if the time interval is lower than this constant value.
    public static final int SAME_MESSAGE_IGNORE_INTERVAL = 500; //ms
    public static final Color COLOR_ERROR = Color.RED;
    public static final Color COLOR_WARNING = Color.ORANGE.darker();
    public static final Color COLOR_DEBUG = Color.BLUE;
    public static final Color COLOR_INFO = Color.BLACK;
    private Level lastLevel = Level.INFO;
    private Color lastLevelColor = getLevelColor(lastLevel);
    private OutputPanel guiPanel;
    private int lastMessageHash = 0;
    private Long lastMessageTime = 0L;
    
    //Messages are stored here before being pushed in the gui
    private Queue<LoggingEvent> leQueue = new LinkedList<LoggingEvent>();
    private AtomicBoolean processingQueue=new AtomicBoolean(false); /*!< If true a swing runnable */
    
    private ListenerContainer<ShowMessageEventData> messageEvent = new ListenerContainer<ShowMessageEventData>();

    public ListenerContainer<ShowMessageEventData> getMessageEvent() {
        return messageEvent;
    }
    
    /**
     * 
     * @return The linked GuiPanel
     */
    public OutputPanel getGuiPanel() {
        return guiPanel;
    }
    
    /**
     * Find the corresponding color depending on error level
     * @param level
     * @return The color associated
     */
    public static Color getLevelColor(Level level) {
        switch(level.toInt()) {
            case Level.INFO_INT:
                return COLOR_INFO;
            case Level.WARN_INT:
                return COLOR_WARNING;
            case Level.DEBUG_INT:
                return COLOR_DEBUG;
            case Level.ERROR_INT:
                return COLOR_ERROR;
            case Level.FATAL_INT:
                return COLOR_ERROR;
            default:
                return COLOR_INFO;
        }
    }
    public PanelAppender(OutputPanel guiPanel) {
        this.guiPanel = guiPanel;
    }

    /**
     * The logging event has been filtered and formated
     * @param le 
     */
    @Override
    protected void append(LoggingEvent le) {
        leQueue.add(le);
        // Show the application when Swing will be ready
        if(!processingQueue.getAndSet(true)) {
            SwingUtilities.invokeLater( new ShowMessage());
        }
    }

    @Override
    public void close() {
        //Nothing to close
    }
    /**
     * This appender need a layout
     * @return 
     */
    @Override
    public boolean requiresLayout() {
        return true;
    }
    
    /**
     * Output the message on each listener
     * @param text Message text
     * @param textColor Message color
     */
    private void firePrintMessage(String text,Color textColor) {
        try {
            messageEvent.callListeners(new ShowMessageEventData(text, textColor, this));
        } catch (EventException ex) {
            //Do nothing on listener error
        }
    } 
   /**
    * Push awaiting messages to the gui
    */ 
   private class ShowMessage implements Runnable {
       /**
        * Push awaiting messages to the gui
        */
        @Override
        public void run(){
            try {
                while(!leQueue.isEmpty()) {
                    LoggingEvent le = leQueue.poll();
                    if(le.getMessage()!=null) {
                        int messageHash = le.getMessage().hashCode();
                        if(messageHash!=lastMessageHash ||
                            le.getTimeStamp()-lastMessageTime>SAME_MESSAGE_IGNORE_INTERVAL) {
                            lastMessageHash = messageHash;
                            lastMessageTime = le.getTimeStamp();
                            //Update the color if the level change
                            if(!le.getLevel().equals(lastLevel)) {
                                lastLevel = le.getLevel();
                                lastLevelColor = getLevelColor(lastLevel);
                                guiPanel.setDefaultColor(lastLevelColor);
                            }
                            StringBuilder outputString = new StringBuilder();
                            outputString.append(layout.format(le));
                            if(layout.ignoresThrowable()) {
                                String[] s = le.getThrowableStrRep();
                                if (s != null) {
                                    int len = s.length;
                                    for(int i = 0; i < len; i++) {
                                        outputString.append(s[i]);
                                        outputString.append("\n");
                                    }
                                }
                            }
                            String outputStr=outputString.toString();
                            guiPanel.print(outputStr);
                            firePrintMessage(outputStr,lastLevelColor);
                        }
                    }
                }
            } finally {
                processingQueue.set(false);                
            }
        }
    }
}
