/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.baseline.collection;

import com.polopoly.cm.collections.ContentList;

/**
 * Content list that can indicate if its complete or truncated.
 * @see example.collection.ContentListLazy
 */
public interface ContentListLazy extends ContentList {

    boolean isComplete();

}
