package com.vaadin.tapio.googlemaps.client;

import com.google.gwt.ajaxloader.client.AjaxLoader;
import com.google.gwt.ajaxloader.client.AjaxLoader.AjaxLoaderOptions;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.AbstractComponentConnector;
import com.vaadin.shared.ui.Connect;
import com.vaadin.tapio.googlemaps.GoogleMap;
import com.vaadin.tapio.googlemaps.client.events.MapMoveListener;
import com.vaadin.tapio.googlemaps.client.events.MarkerClickListener;
import com.vaadin.tapio.googlemaps.client.events.MarkerDragListener;

/**
 * The connector for the Google Maps JavaScript API v3.
 * 
 * @author Tapio Aali <tapio@vaadin.com>
 */
@Connect(GoogleMap.class)
public class GoogleMapConnector extends AbstractComponentConnector implements
        MarkerClickListener, MapMoveListener, MarkerDragListener {

    protected static boolean apiLoaded = false;
    private boolean deferred = false;
    private GoogleMapMarkerClickedRpc markerClickedRpc = RpcProxy.create(
            GoogleMapMarkerClickedRpc.class, this);
    private GoogleMapMovedRpc mapMovedRpc = RpcProxy.create(
            GoogleMapMovedRpc.class, this);
    private GoogleMapMarkerDraggedRpc markerDraggedRpc = RpcProxy.create(
            GoogleMapMarkerDraggedRpc.class, this);

    public GoogleMapConnector() {
        if (!apiLoaded) {
            loadMapApi();
            apiLoaded = true;
        } else {
            initMap();
        }
    }

    private void initMap() {
        getWidget().initMap(getState().center, getState().zoom,
                getState().mapTypeId);
        getWidget().setMarkerClickListener(this);
        getWidget().setMapMoveListener(this);
        getWidget().setMarkerDragListener(this);
    }

    @Override
    protected Widget createWidget() {
        return GWT.create(GoogleMapWidget.class);
    }

    @Override
    public GoogleMapWidget getWidget() {
        return (GoogleMapWidget) super.getWidget();
    }

    @Override
    public GoogleMapState getState() {
        return (GoogleMapState) super.getState();
    }

    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {
        super.onStateChanged(stateChangeEvent);

        if (getState().limitCenterBounds) {
            getWidget().setCenterBoundLimits(getState().centerNELimit,
                    getState().centerSWLimit);
        } else {
            getWidget().clearCenterBoundLimits();
        }

        if (getState().limitVisibleAreaBounds) {
            getWidget().setVisibleAreaBoundLimits(
                    getState().visibleAreaNELimit,
                    getState().visibleAreaSWLimit);
        } else {
            getWidget().clearVisibleAreaBoundLimits();
        }

        if (!getWidget().isMapInitiated()) {
            deferred = true;
            return;
        }

        // do not set zoom/center again if the change orginated from client
        if (!getState().locationFromClient) {
            if (getState().center.getLat() != getWidget().getLatitude()
                    || getState().center.getLon() != getWidget().getLongitude()) {
                getWidget().setCenter(getState().center);
            }
            if (stateChangeEvent.hasPropertyChanged("zoom")
                    && getState().zoom != getWidget().getZoom()) {
                getWidget().setZoom(getState().zoom);
            }
        }

        if (stateChangeEvent.hasPropertyChanged("markers")) {
            getWidget().setMarkers(getState().markers);
        }

        if (stateChangeEvent.hasPropertyChanged("polygons")) {
            getWidget().setPolygonOverlays(getState().polygons);
        }
        if (stateChangeEvent.hasPropertyChanged("polylines")) {
            getWidget().setPolylineOverlays(getState().polylines);
        }
        if (stateChangeEvent.hasPropertyChanged("mapTypeId")) {
            getWidget().setMapType(getState().mapTypeId);
        }

        if (stateChangeEvent.hasPropertyChanged("controls")) {
            getWidget().setControls(getState().controls);
        }

        if (stateChangeEvent.hasPropertyChanged("draggable")) {
            getWidget().setDraggable(getState().draggable);
        }
        if (stateChangeEvent.hasPropertyChanged("keyboardShortcutsEnabled")) {
            getWidget().setKeyboardShortcutsEnabled(
                    getState().keyboardShortcutsEnabled);
        }
        if (stateChangeEvent.hasPropertyChanged("scrollWheelEnabled")) {
            getWidget().setScrollWheelEnabled(getState().scrollWheelEnabled);
        }
        if (stateChangeEvent.hasPropertyChanged("minZoom")) {
            getWidget().setMinZoom(getState().minZoom);
        }
        if (stateChangeEvent.hasPropertyChanged("maxZoom")) {
            getWidget().setMaxZoom(getState().maxZoom);
        }

    }

    private void loadMapApi() {
        AjaxLoaderOptions options = AjaxLoaderOptions.newInstance();
        options.setOtherParms("sensor=false");
        Runnable callback = new Runnable() {
            public void run() {
                initMap();
                if (deferred) {
                    loadDeferred();
                    deferred = false;
                }
            }
        };
        AjaxLoader.init(getState().apiKey);
        AjaxLoader.loadApi("maps", "3", callback, options);
    }

    private void loadDeferred() {
        getWidget().setMarkers(getState().markers);
        getWidget().setPolygonOverlays(getState().polygons);
        getWidget().setPolylineOverlays(getState().polylines);
        getWidget().setMapType(getState().mapTypeId);
        getWidget().setControls(getState().controls);
        getWidget().setDraggable(getState().draggable);
        getWidget().setKeyboardShortcutsEnabled(
                getState().keyboardShortcutsEnabled);
        getWidget().setScrollWheelEnabled(getState().scrollWheelEnabled);
        getWidget().setMinZoom(getState().minZoom);
        getWidget().setMaxZoom(getState().maxZoom);
    }

    @Override
    public void markerClicked(GoogleMapMarker clickedMarker) {
        markerClickedRpc.markerClicked(clickedMarker);
    }

    @Override
    public void mapMoved(double zoomLevel, LatLon center, LatLon boundsNE,
            LatLon boundsSW) {
        mapMovedRpc.mapMoved(zoomLevel, center, boundsNE, boundsSW);
    }

    @Override
    public void markerDragged(GoogleMapMarker draggedMarker, LatLon newPosition) {
        markerDraggedRpc.markerDragged(draggedMarker, newPosition);
    }
}
