package com.sismics.util;

import java.util.Locale;

import org.apache.commons.lang.StringUtils;

import com.sismics.books.core.constant.Constants;
import com.sismics.books.core.dao.jpa.LocaleDao;

/**
 * Locale utilities.
 *
 * @author jtremeaux
 */
public class LocaleUtil {
    /**
     * Returns the locale from its language / country code (ex: fr_FR).
     * 
     * @param localeCode Locale code
     * @return Locate instance
     */
    public static final Locale getLocale(String localeCode) {
        String[] localeCodeArray = localeCode.split("_");
        String language = localeCodeArray[0];
        String country = "";
        String variant = "";
        if (localeCodeArray.length >= 2) {
            country = localeCodeArray[1];
        }
        if (localeCodeArray.length >= 3) {
            variant = localeCodeArray[2];
        }
        return new Locale(language, country, variant);
    }
    
    /**
     * Extracts the ID of the locale from the HTTP Accept-Language header.
     * 
     * @param acceptLanguageHeader header
     * @return Locale ID
     */
    public static String getLocaleIdFromAcceptLanguage(String acceptLanguageHeader) {
        String localeId = null;
        if (StringUtils.isNotBlank(acceptLanguageHeader)) {
            acceptLanguageHeader = acceptLanguageHeader.replaceAll("-", "_");
            localeId = acceptLanguageHeader.split(",")[0];
        }
        if (StringUtils.isNotBlank(localeId)) {
            LocaleDao localeDao = new LocaleDao();
            com.sismics.books.core.model.jpa.Locale locale = localeDao.getById(localeId);
            if (locale != null) {
                localeId = locale.getId();
            } else {
                // The client provided an unknown locale
                localeId = Constants.DEFAULT_LOCALE_ID;
            }
        }
        if (StringUtils.isBlank(localeId)) {
            localeId = Constants.DEFAULT_LOCALE_ID;
        }
        return localeId;
    }
}
