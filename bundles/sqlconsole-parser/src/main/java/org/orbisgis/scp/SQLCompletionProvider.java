/**
 * OrbisGIS is a GIS application dedicated to scientific spatial simulation.
 * This cross-platform GIS is developed at French IRSTV institute and is able to
 * manipulate and create vector and raster spatial information.
 *
 * OrbisGIS is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2007-2014 IRSTV (FR CNRS 2488)
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
package org.orbisgis.scp;

import java.awt.Point;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import javax.swing.text.JTextComponent;

import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.CompletionProviderBase;
import org.h2.bnf.Bnf;
import org.h2.bnf.context.DbContents;
import org.h2.bnf.context.DbContextRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A SQL CompletionProvider based on a simple Lexer and a simple top-down parser.
 * 
 * @author Antoine Gourlay
 * @author Nicolas Fortin
 * @since 4.0
 */
public class SQLCompletionProvider extends CompletionProviderBase {
    private DataSource dataSource;
    private Bnf parser;
    private Logger log = LoggerFactory.getLogger(SQLCompletionProvider.class);
    private static final int UPDATE_INTERVAL = 30000; // ms, metadata update interval
    private long lastUpdate = 0;

    public SQLCompletionProvider(DataSource dataSource) {
        this.dataSource = dataSource;
        try {
            // Use h2 internal grammar
            updateParser(dataSource);
        } catch (Exception ex) {
            log.warn("Could not load auto-completion engine", ex);
        }
    }
    /**
     * Set the DataSource used by this Parser
     * @param ds
     */
    public void setDataSource(DataSource ds) {
        this.dataSource = ds;
        try {
            updateParser(ds);
        } catch (Exception ex) {
            log.error(ex.getLocalizedMessage(), ex);
        }
    }

    /***
     * Read the data source in order to update parser grammar.
     * @param dataSource New DataSource, to extract meta data, can be null
     */
    public void updateParser(DataSource dataSource) throws SQLException, IOException {
        if(dataSource != null) {
            lastUpdate = System.currentTimeMillis();
            parser = Bnf.getInstance(null);
            try (Connection connection = dataSource.getConnection()) {
                DbContents contents = new DbContents();
                contents.readContents(connection.getMetaData().getURL(), connection);
                DbContextRule columnRule = new DbContextRule(contents, DbContextRule.COLUMN);
                DbContextRule newAliasRule = new DbContextRule(contents, DbContextRule.NEW_TABLE_ALIAS);
                DbContextRule aliasRule = new DbContextRule(contents, DbContextRule.TABLE_ALIAS);
                DbContextRule tableRule = new DbContextRule(contents, DbContextRule.TABLE);
                DbContextRule schemaRule = new DbContextRule(contents, DbContextRule.SCHEMA);
                DbContextRule columnAliasRule = new DbContextRule(contents, DbContextRule.COLUMN_ALIAS);
                DbContextRule procedureRule = new DbContextRule(contents, DbContextRule.PROCEDURE);
                parser.updateTopic("column_name", columnRule);
                parser.updateTopic("new_table_alias", newAliasRule);
                parser.updateTopic("table_alias", aliasRule);
                parser.updateTopic("column_alias", columnAliasRule);
                parser.updateTopic("table_name", tableRule);
                parser.updateTopic("schema_name", schemaRule);
                parser.updateTopic("expression", procedureRule);
                parser.linkStatements();
            } catch (SQLException ex) {
                log.error(ex.getLocalizedMessage(), ex);
            }
        }
    }

    @Override
    protected List getCompletionsImpl(JTextComponent comp) {
        return getCompletionsAtIndex(comp, comp.getCaretPosition());
    }

    @Override
    public String getAlreadyEnteredText(JTextComponent jTextComponent) {
        return "";
    }


    public List<Completion> getCompletionsAtIndex(JTextComponent jTextComponent, int charIndex) {
        long now = System.currentTimeMillis();
        if(lastUpdate + UPDATE_INTERVAL < now) {
            try {
                updateParser(dataSource);
            } catch (Exception ex) {
                log.warn("Could not update auto-completion engine", ex);
            }
        }

        //Completion completion = new BasicCompletion(this, token);
        List<Completion> completionList = new LinkedList<Completion>();

        // Extract the statement at this position
        DocumentSQLReader documentReader = new DocumentSQLReader(jTextComponent.getDocument(), true);
        String statement = "";
        while(documentReader.hasNext() && documentReader.getPosition() + statement.length() < charIndex) {
            statement = documentReader.next();
        }
        // Last word for filtering results
        String wordBegin = "";
        int completionPosition = charIndex - documentReader.getPosition();
        String partialStatement = statement.substring(0, completionPosition);
        // Ask parser for completion list
        Map<String,String> autoComplete = parser.getNextTokenList(partialStatement);
        for(Map.Entry<String, String> entry : autoComplete.entrySet()) {
            String token =  entry.getKey().substring(entry.getKey().indexOf("#") + 1);
            Completion completion = new BnfAutoCompletion(this, token, entry.getValue());
            completionList.add(completion);
        }
        return completionList;
    }

    @Override
    public List getCompletionsAt(JTextComponent jTextComponent, Point point) {
        int pos = jTextComponent.viewToModel(point);
        if(pos==-1) {
            return null;
        } else {
            return getCompletionsAtIndex(jTextComponent, pos);
        }
    }

    @Override
    public List getParameterizedCompletions(JTextComponent jTextComponent) {
        return null;
    }

    private static class BnfAutoCompletion extends BasicCompletion {
        private String append;

        private BnfAutoCompletion(CompletionProvider provider, String completeToken, String append) {
            super(provider, completeToken);
            this.append = append;
        }

        @Override
        public String getAlreadyEntered(JTextComponent comp) {
            String completeToken = getReplacementText();
            return completeToken.substring(0, completeToken.length() - append.length());
        }
    }
}
