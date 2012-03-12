/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.baseline.content;

import com.polopoly.cm.client.CMException;

/**
 * An interface to be implemented by policies that need a cache key more complex
 * than just the versioned content ID.
 * @see example.content.CacheKeyProvider
 */
public interface CacheKeyProvider {

    /**
     * Returns an object to be used as a cache key for the object.
     * 
     * @return a cache key
     * @throws CMException
     *             If the cache key can't be constructed
     */
    public abstract Object getCacheKey() throws CMException;
}