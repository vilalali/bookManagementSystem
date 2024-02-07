package com.sismics.books.core.dao.file.theme;

import com.google.common.collect.Lists;
import com.sismics.books.core.util.DirectoryUtil;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * Theme DAO.
 *
 * @author jtremeaux 
 */
public class ThemeDao {
    private final static FilenameFilter CSS_FILTER = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            return name.endsWith(".css") || name.endsWith(".less");
        }
    };
    
    /**
     * Return the list of all themes.
     * 
     * @return List of themes
     */
    public List<String> findAll() {
        final File themeDirectory = DirectoryUtil.getThemeDirectory();
        if (themeDirectory != null) {
            return Lists.newArrayList(themeDirectory.list(CSS_FILTER));
        } else {
            return new ArrayList<String>();
        }
    }

}
