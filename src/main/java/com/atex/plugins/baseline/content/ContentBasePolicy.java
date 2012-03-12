/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.baseline.content;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.atex.plugins.baseline.util.ParentPathResolver;
import com.polopoly.cm.ContentId;
import com.polopoly.cm.ContentInfo;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.app.policy.SingleValued;
import com.polopoly.cm.app.util.PreviewContext;
import com.polopoly.cm.app.util.PreviewContextURLBuilder;
import com.polopoly.cm.app.util.PreviewURLBuilder;
import com.polopoly.cm.app.util.Previewable;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.ContentOperationFailedException;
import com.polopoly.cm.client.InputTemplate;
import com.polopoly.cm.path.PathSegment;
import com.polopoly.cm.path.SimpleContentPathTranslator;
import com.polopoly.cm.policy.ContentPolicy;
import com.polopoly.cm.policy.Policy;
import com.polopoly.cm.policy.PolicyUtil;
import com.polopoly.cm.policy.UserDataPolicy;
import com.polopoly.siteengine.structure.Page;
import com.polopoly.siteengine.structure.SiteRoot;
import com.polopoly.siteengine.util.FriendlyUrlConverter;

/**
 * Base policy for all policies.
 * @see example.content.ContentBasePolicy
 */
public class ContentBasePolicy extends ContentPolicy
        implements PathSegment, PreviewURLBuilder, PreviewContextURLBuilder, Previewable, ContentBaseModelTypeDescription,
        CacheKeyProvider {
    protected volatile ContentId[] parentIds;

    private static Logger LOG = Logger.getLogger(ContentBasePolicy.class.getName());

    /**
     * Initializes parent id array.
     */
    protected void initSelf() {
        super.initSelf();

        initSelfBase();
    }

    protected void initSelfBase() {
    }

    /**
     * Gets id of parent content using security parent (or insert parent if it
     * exists).
     * 
     * @return the content id of the parent
     */
    public ContentId getParentId() throws CMException {
        ContentId id = getContentReference("polopoly.Parent", "insertParentId");

        if (id == null) {
            id = getSecurityParentId();
        }

        return id;
    }

    /**
     * Gets an array of parent ids up to policy implementing {@link SiteRoot}.
     * Uses insert parent with security parent as fallback. If no
     * {@link SiteRoot} exists in parent path, an array containg only the id of
     * this policy itself is returned.
     */
    public ContentId[] getParentIds() {
        ContentId[] result = parentIds;

        if (result == null) {
            synchronized (this) {
                result = parentIds;

                if (result == null) {
                    parentIds = result = new ParentPathResolver().getParentPathNoExceptions(this, getCMServer());
                }
            }
        }

        return result;
    }

    /**
     * Convenience method to access value of <code>SingleValued</code> child
     * policies. The method is null-safe and will return "" if the child policy
     * or component doesn't exist.
     * 
     * @param name
     *            name of the child policy
     * 
     * @return the value of the child policy
     * @exception CMException
     *                if an error occurs
     */
    public final String getChildValue(String name) {
        return getChildValue(name, "");
    }

    /**
     * Convenience method to access value of <code>SingleValued</code> child
     * policies. The method is null-safe and will return the given default value
     * if the child policy or component doesn't exist.
     * 
     * @param name
     *            the name of the child policy
     * @param defaultValue
     *            the desired default value
     * 
     * @return the value of the child policy
     * @exception CMException
     *                if an error occurs
     */
    public final String getChildValue(String name, String defaultValue) {
        try {
            SingleValued child = (SingleValued) getChildPolicy(name);

            if (child == null) {
                return defaultValue;
            }

            return (child.getValue() != null) ? child.getValue() : defaultValue;
        } catch (ClassCastException cce) {
            LOG.warning(name + " in " + getContentId() + " has unsupported policy.");
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Error getting child value", e);
        }

        return defaultValue;
    }

    protected void setChildValue(String singleValuedPolicyName, String value) throws CMException {
        Policy singleValued = getChildPolicy(singleValuedPolicyName);
        if (singleValued instanceof SingleValued) {
            ((SingleValued) singleValued).setValue(value);
        } else {
            LOG.warning("Failed to set value=" + value + " for " + singleValuedPolicyName + " in " + getContentId()
                    + "(policy was " + (singleValued != null ? singleValued.getClass().getName() : "null") + ")");
        }
    }

    /**
     * Gets creation date of this content in format (yyyy-MM-dd).
     */
    public Date getCreated() throws CMException {
        ContentInfo info = getCMServer().getContentInfo(getContentId());
        return new Date(info.getCreationTime());
    }

    /**
     * Gets modification date in format (yyyy-MM-dd). Uses commit date of
     * content. Will return now if content is not committed, e.g. in preview.
     */
    public Date getModified() {
        Date commitDate = getVersionInfo().getVersionCommitDate();

        // If is preview, this version will not have been committed yet. Use now
        // insetead.
        if (commitDate != null) {
            return commitDate;
        }

        return new Date();
    }

    /**
     * Gets creator, i.e. the creator of the first version of this content.
     * 
     * @return the {@link UserDataPolicy} of the creator
     * @throws CMException
     */
    public UserDataPolicy getCreator() throws CMException {
        ContentInfo info = getCMServer().getContentInfo(getContentId());

        UserDataPolicy userPolicy = (UserDataPolicy) getCMServer().getPolicy(
                new ExternalContentId(info.getCreatedBy().getPrincipalIdString()));

        return userPolicy;
    }

    /**
     * Get modifier, i.e. the creator of the latest version of this content.
     */
    public UserDataPolicy getModifier() throws CMException {
        UserDataPolicy userPolicy = (UserDataPolicy) getCMServer().getPolicy(
                new ExternalContentId(getCreatedBy().getPrincipalIdString()));

        return userPolicy;
    }

    /**
     * Returns a friendly path segment representation of this content. Uses path
     * segment field if such exists. Otherwise converts content name to a
     * friendly format.
     */
    public String getPathSegmentString() throws CMException {
        String pathSegment = getChildValue("pathsegment");

        if (pathSegment.length() == 0) {
            pathSegment = getName() != null ? getName() : "";
        }

        return FriendlyUrlConverter.convertPermissive(pathSegment).toLowerCase();
    }

    /**
     * Gets url used for preview, tries to forward to child policy
     * previewContextURLBuilder, else return ${link
     * {@link #getPreviewURL(String)}
     */
    public String getPreviewURL(PreviewContext context) {
        try {
            if (getChildPolicy("previewContextURLBuilder") != null) {
                Policy builder = getChildPolicy("previewContextURLBuilder");
                if (builder instanceof PreviewContextURLBuilder) {
                    String previewURL = ((PreviewContextURLBuilder) builder).getPreviewURL(context);
                    if (previewURL != null) {
                        return previewURL;
                    }
                }
            }
        } catch (CMException e) {
            logger.log(Level.WARNING, "Failed to get child policy previewContextURLBuilder", e);
        }
        return getPreviewURL(context.getPreviewDispatcherURL());
    }

    /**
     * Gets url used for preview. Uses full path to home. When the path to the
     * content is not known e.g. the content is created using the quick creator
     * without a default target, the home department of the current user is used
     * as preview context.
     */
    public String getPreviewURL(String previewServletURL) {
        SimpleContentPathTranslator pathTranslator = new SimpleContentPathTranslator();
        pathTranslator.setPolicyCMServer(getCMServer());

        try {
            ContentId[] currentParentIds = getParentIds();
            if (currentParentIds.length == 1 && currentParentIds[0].getContentId().equalsIgnoreVersion(getContentId())) {
                Policy currentCaller = PolicyUtil.getCurrentCallerPolicy(getCMServer());
                currentParentIds = new ContentId[] { currentCaller.getContent().getSecurityParentId(), currentParentIds[0] };
            }

            return previewServletURL + pathTranslator.createPath(currentParentIds);
        } catch (CMException e) {
            logger.warning("Failed to create preview url: " + e.getMessage());
        }

        return null;
    }

    public boolean isPreviewable() {
        try {
            // Check if output template exist
            ContentId itid = getInputTemplateId();
            InputTemplate it = (InputTemplate) getCMServer().getContent(itid);
            String[] modes = it.getAvailableOutputTemplateModes();
            if (modes == null || modes.length == 0) {
                return false;
            }
            boolean hasOutputTemplate = false;
            for (int i = 0; i < modes.length; i++) {
                if (getOutputTemplate(modes[i]) != null) {
                    hasOutputTemplate = true;
                    break;
                }
            }
            if (!hasOutputTemplate) {
                return false;
            }
            // Check if parent is ok
            Policy policy;
            try {
                policy = getCMServer().getPolicy(getParentId());
            } catch (ContentOperationFailedException e) {
                // Parent might not be committed yet, check for uncommitted
                // version
                policy = getCMServer().getPolicy(getParentId().getLatestVersionId());
            }
            if (policy instanceof Page) {
                if (getChildPolicy("previewable") instanceof Previewable) {
                    return ((Previewable) getChildPolicy("previewable")).isPreviewable();
                }
                return true;
            }
        } catch (CMException e) {
            logger.log(Level.WARNING, "Failed to check if " + getContentId().getContentIdString()
                    + " is previewable (assuming it is)", e);
            return true;
        }
        return false;
    }

    protected void setParentIds(ContentId[] newParentIds) {
        synchronized (this) {
            parentIds = newParentIds;
        }
    }

    /**
     * In most cases, the versioned content ID is a good cache key since the
     * content changes only when new versions are created. For content that is
     * e.g. context dependent, the context needs to be used to generate the key.
     * 
     * @return the versioned content ID
     * @throws CMException
     */
    public Object getCacheKey() throws CMException {
        return getContentId();
    }
}
