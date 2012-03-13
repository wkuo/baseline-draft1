/*
 * (c) Polopoly AB (publ).
 * This software is protected by copyright law and international copyright
 * treaties as well as other intellectual property laws and treaties.
 * All title and rights in and to this software and any copies thereof
 * are the sole property of Polopoly AB (publ).
 * Polopoly is a registered trademark of Polopoly AB (publ).
 */

package com.atex.plugins.baseline.collection.searchbased;

import org.apache.solr.client.solrj.SolrServerException;

import com.atex.plugins.baseline.collection.ContentListLazyMessageHandler;
import com.atex.plugins.baseline.collection.RefreshableContentListProvider;
import com.polopoly.cm.app.util.PolicyWidgetUtil;
import com.polopoly.cm.app.widget.OLayoutWidget;
import com.polopoly.cm.app.widget.OPolicyWidget;
import com.polopoly.cm.collections.ContentListProvider;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.ajax.AjaxEvent;
import com.polopoly.orchid.ajax.JSCallback;
import com.polopoly.orchid.ajax.OAjaxTrigger;
import com.polopoly.orchid.ajax.event.ClickEvent;
import com.polopoly.orchid.ajax.lifecyclehook.StandardAjaxBusyIndicator;
import com.polopoly.orchid.ajax.listener.StandardAjaxEventListener;
import com.polopoly.orchid.ajax.trigger.JsEventTriggerType;
import com.polopoly.orchid.ajax.trigger.OAjaxTriggerOnEvent;
import com.polopoly.orchid.context.OrchidContext;
import com.polopoly.orchid.widget.OButton;
import com.polopoly.orchid.widget.OWidget;
import com.polopoly.util.LocaleUtil;

/**
 * Button that updates the searched content list.
 * @see example.collection.searchbased.OUpdateSearchButton
 */
public class OUpdateSearchButton extends OLayoutWidget {

    /**
     * 
     */
    private static final long serialVersionUID = 1199873987738101273L;
    private OButton updateSearchButton;
    private OAjaxTrigger triggerOnClick;
    private transient RefreshableContentListProvider topPolicy;
    private OPolicyWidget topWidget;
    private transient ContentListLazyMessageHandler contentListMessageHandler;

    public void initSelf(OrchidContext oc) throws OrchidException {
        super.initSelf(oc);

        topPolicy = (RefreshableContentListProvider) getContentSession().getTopPolicy();

        topWidget = (OPolicyWidget) getContentSession().getTopWidget();

        updateSearchButton = new OButton();
        String label = LocaleUtil.format("cm.template.example.pq.refresh", oc.getMessageBundle());
        updateSearchButton.setLabel(label);
        updateSearchButton.setTitle(label);
        addAndInitChild(oc, updateSearchButton);
        setupOnClickUpdateSearchEvent(oc);

        updateSearchButton.setEnabled(true);
        contentListMessageHandler = new ContentListLazyMessageHandler();
    }

    private void setupOnClickUpdateSearchEvent(OrchidContext orchidContext) throws OrchidException {
        // Create the listener
        StandardAjaxEventListener onClickListener = new StandardAjaxEventListener() {

            public boolean triggeredBy(OrchidContext oc, AjaxEvent e) {
                return e instanceof ClickEvent;
            }

            public JSCallback processEvent(OrchidContext oc, AjaxEvent event) throws OrchidException {
                try {
                    if (PolicyWidgetUtil.isEditMode(OUpdateSearchButton.this)) {
                        topWidget.store();
                    }
                    topPolicy.refresh();

                    ContentListProvider contentListProvider = topPolicy;

                    contentListMessageHandler.setupWarningMessages(oc, contentListProvider);
                } catch (SolrServerException e) {
                    handleError(oc, LocaleUtil.format("p.siteengine.search.expression.syntax.error", oc.getMessageBundle()));
                } catch (Exception e) {
                    handleError(oc, LocaleUtil.format("p.siteengine.refreshError", oc.getMessageBundle()));
                }

                return null; // No callback
            }
        };

        onClickListener.addRenderWidget(topWidget);

        // Render error messages
        OWidget widget = getTree().getWidget("messagearea");
        if (widget != null) {
            onClickListener.addRenderWidget(widget);
        }

        // Create the trigger
        triggerOnClick = new OAjaxTriggerOnEvent(updateSearchButton, JsEventTriggerType.CLICK);
        triggerOnClick.setLifecycleHookFactory(StandardAjaxBusyIndicator.getInstance());

        onClickListener.addDecodeWidget(topWidget);
        triggerOnClick.setFormPostSource(topWidget);
        addAndInitChild(orchidContext, triggerOnClick);

        // Attach the listener to the button
        getTree().registerAjaxEventListener(updateSearchButton, onClickListener);
    }
}
