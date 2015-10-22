package com.mapzen.erasermap.presenter

import android.location.Location
import com.mapzen.erasermap.dummy.TestHelper.getTestFeature
import com.mapzen.erasermap.dummy.TestHelper.getTestLocation
import com.mapzen.erasermap.model.LocationChangeEvent
import com.mapzen.erasermap.model.RouteEvent
import com.mapzen.erasermap.model.RoutePreviewEvent
import com.mapzen.erasermap.model.TestAppSettings
import com.mapzen.erasermap.model.TestMapzenLocation
import com.mapzen.erasermap.model.TestRouterFactory
import com.mapzen.erasermap.presenter.ViewStateManager.ViewState.DEFAULT
import com.mapzen.erasermap.presenter.ViewStateManager.ViewState.ROUTE_DIRECTION_LIST
import com.mapzen.erasermap.presenter.ViewStateManager.ViewState.ROUTE_PREVIEW
import com.mapzen.erasermap.presenter.ViewStateManager.ViewState.ROUTING
import com.mapzen.erasermap.presenter.ViewStateManager.ViewState.SEARCH_RESULTS
import com.mapzen.erasermap.view.TestMainController
import com.mapzen.erasermap.view.TestRouteController
import com.mapzen.pelias.gson.Feature
import com.mapzen.pelias.gson.Result
import com.mapzen.tangram.LngLat
import com.mapzen.valhalla.Route
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import java.util.ArrayList

public class MainPresenterTest {
    private val mainController: TestMainController = TestMainController()
    private val routeController: TestRouteController = TestRouteController()
    private val mapzenLocation: TestMapzenLocation = TestMapzenLocation()
    private val routerFactory: TestRouterFactory = TestRouterFactory()
    private val settings: TestAppSettings = TestAppSettings()
    private val bus: Bus = Bus()
    private val vsm: ViewStateManager = ViewStateManager()
    private val presenter = MainPresenterImpl(mapzenLocation, routerFactory, settings, vsm)

    @Before fun setUp() {
        presenter.mainViewController = mainController
        presenter.routeViewController = routeController
        presenter.bus = bus
    }

    @Test fun shouldNotBeNull() {
        assertThat(presenter).isNotNull()
    }

    @Test fun onSearchResultsAvailable_shouldShowSearchResults() {
        val result = Result()
        val features = ArrayList<Feature>()
        result.setFeatures(features)
        presenter.onSearchResultsAvailable(result)
        assertThat(mainController.searchResults).isEqualTo(features)
    }

    @Test fun onReverseGeocodeResultsAvailable_shouldShowSearchResults() {
        val result = Result()
        val features = ArrayList<Feature>()
        result.setFeatures(features)
        presenter.onReverseGeocodeResultsAvailable(result)
        assertThat(mainController.isReverseGeocodeVisible).isTrue()
    }

    @Test fun onRestoreViewState_shouldRestorePreviousSearchResults() {
        val result = Result()
        val features = ArrayList<Feature>()
        result.setFeatures(features)
        presenter.onSearchResultsAvailable(result)

        val newController = TestMainController()
        presenter.mainViewController = newController
        presenter.onRestoreViewState()
        assertThat(newController.searchResults).isEqualTo(features)
    }

    @Test fun onRestoreViewState_shouldRestoreRoutePreview() {
        presenter.onRoutePreviewEvent(RoutePreviewEvent(getTestFeature()))
        val newController = TestMainController()
        presenter.mainViewController = newController
        presenter.onRestoreViewState()
        assertThat(newController.isRoutePreviewVisible).isTrue()
    }

    @Test fun onRestoreViewState_shouldShowRoutingMode() {
        presenter.onRoutePreviewEvent(RoutePreviewEvent(getTestFeature()))
        val newController = TestMainController()
        presenter.mainViewController = newController
        presenter.routingEnabled = true
        presenter.onRestoreViewState()
        assertThat(newController.isRoutingModeVisible).isTrue()
    }

    @Test fun onCollapseSearchView_shouldHideSearchResults() {
        val result = Result()
        val features = ArrayList<Feature>()
        result.setFeatures(features)
        presenter.onSearchResultsAvailable(result)
        presenter.onCollapseSearchView()
        assertThat(mainController.searchResults).isNull()
    }

    @Test fun onQuerySubmit_shouldShowProgress() {
        presenter.onQuerySubmit()
        assertThat(mainController.isProgressVisible).isTrue()
    }

    @Test fun onSearchResultsAvailable_shouldHideProgress() {
        mainController.showProgress()
        presenter.onSearchResultsAvailable(Result())
        assertThat(mainController.isProgressVisible).isFalse()
    }

    @Test fun onExpandSearchView_shouldHideOverflowMenu() {
        mainController.isOverflowVisible = true
        presenter.onExpandSearchView()
        assertThat(mainController.isOverflowVisible).isFalse()
    }

    @Test fun onCollapseSearchView_shouldShowOverflowMenu() {
        mainController.isOverflowVisible = false
        presenter.onCollapseSearchView()
        assertThat(mainController.isOverflowVisible).isTrue()
    }

    @Test fun onSearchResultsAvailable_shouldShowActionViewAll() {
        mainController.isViewAllVisible = false
        val result = Result()
        val features = ArrayList<Feature>()
        features.add(Feature())
        features.add(Feature())
        features.add(Feature())
        result.features = features
        presenter.onSearchResultsAvailable(result)
        assertThat(mainController.isViewAllVisible).isTrue()
    }

    @Test fun onCollapseSearchView_shouldHideActionViewAll() {
        mainController.isViewAllVisible = true
        presenter.onCollapseSearchView()
        assertThat(mainController.isViewAllVisible).isFalse()
    }

    @Test fun onRoutePreviewEvent_shouldCollapseSearchView() {
        mainController.isSearchVisible = true
        presenter.onRoutePreviewEvent(RoutePreviewEvent(getTestFeature()))
        assertThat(mainController.isSearchVisible).isFalse()
    }

    @Test fun onRoutePreviewEvent_shouldShowRoutePreview() {
        mainController.isRoutePreviewVisible = false
        presenter.onRoutePreviewEvent(RoutePreviewEvent(getTestFeature()))
        assertThat(mainController.isRoutePreviewVisible).isTrue()
    }

    @Test fun onBackPressed_shouldHideRoutePreview() {
        presenter.onRoutePreviewEvent(RoutePreviewEvent(getTestFeature()))
        presenter.onBackPressed()
        assertThat(mainController.isRoutePreviewVisible).isFalse()
    }

    @Test fun onBackPressed_shouldClearRouteLine() {
        presenter.onRoutePreviewEvent(RoutePreviewEvent(getTestFeature()))
        mainController.routeLine = ArrayList<LngLat>()
        presenter.onBackPressed()
        assertThat(mainController.routeLine).isNull()
    }

    @Test fun onClickViewList_shouldMakeDirectionsVisible() {
        presenter.onClickViewList()
        assertThat(mainController.isDirectionListVisible).isTrue()
    }

    @Test fun onClickStartNavigation_shouldMakeRoutingModeVisible() {
        presenter.onRoutePreviewEvent(RoutePreviewEvent(getTestFeature()))
        presenter.onClickStartNavigation()
        assertThat(mainController.isRoutingModeVisible).isTrue()
    }

    @Test fun onClickStartNavigation_shouldPublishRouteEvent() {
        val subscriber = RouteEventSubscriber()
        presenter.bus?.register(subscriber)
        presenter.onClickStartNavigation()
        assertThat(subscriber.event).isNotNull()
    }

    @Test fun onLocationChanged_shouldNotifyRouteControllerIfRoutingIsEnabled() {
        presenter.routingEnabled = false
        presenter.onLocationChangeEvent(LocationChangeEvent(getTestLocation()))
        assertThat(routeController.location).isNull()

        presenter.routingEnabled = true
        presenter.onLocationChangeEvent(LocationChangeEvent(getTestLocation()))
        assertThat(routeController.location).isNotNull()
    }

    @Test fun onSearchResultSelected_shouldCenterOnCurrentFeature() {
        val result = Result()
        val features = ArrayList<Feature>()
        result.setFeatures(features)
        presenter.onSearchResultsAvailable(result)
        presenter.onSearchResultSelected(0)
        assertThat(mainController.isCenteredOnCurrentFeature).isTrue()
    }

    @Test fun onSlidingPanelOpen_shouldShowRouteDirectionList() {
        presenter.onSlidingPanelOpen()
        assertThat(routeController.isDirectionListVisible).isTrue()
    }

    @Test fun onSlidingPanelCollapse_shouldHideRouteDirectionList() {
        routeController.isDirectionListVisible = true
        presenter.onSlidingPanelCollapse()
        assertThat(routeController.isDirectionListVisible).isFalse()
    }

    @Test fun onPause_shouldDisconnectLocationUpdates() {
        presenter.onPause()
        assertThat(mapzenLocation.connected).isFalse()
    }

    @Test fun onPause_shouldNotDisconnectLocationUpdatesWhileRouting() {
        mapzenLocation.connected = true
        presenter.onClickStartNavigation()
        presenter.onSlidingPanelOpen()
        presenter.onPause()
        assertThat(mapzenLocation.connected).isTrue()
    }

    @Test fun onResume_shouldReconnectLocationClientAndInitLocationUpdates() {
        mapzenLocation.connected = false
        vsm.viewState = DEFAULT
        presenter.onResume()
        assertThat(mapzenLocation.connected).isTrue()
    }

    @Test fun onResume_shouldNotReconnectClientAndInitUpdatesWhileRouting() {
        mapzenLocation.connected = false
        presenter.onClickStartNavigation()
        presenter.onResume()
        assertThat(mapzenLocation.connected).isFalse()
    }

    @Test fun onBackPressed_shouldUpdateViewState() {
        vsm.viewState = ROUTE_DIRECTION_LIST
        presenter.onBackPressed()
        assertThat(vsm.viewState).isEqualTo(ROUTING)
        presenter.onBackPressed()
        assertThat(vsm.viewState).isEqualTo(ROUTE_PREVIEW)
        presenter.onBackPressed()
        assertThat(vsm.viewState).isEqualTo(SEARCH_RESULTS)
        presenter.onBackPressed()
        assertThat(vsm.viewState).isEqualTo(DEFAULT)
        presenter.onBackPressed()
        assertThat(vsm.viewState).isEqualTo(DEFAULT)
    }

    @Test fun onCreate_shouldSetMapLocationFirstTimeInvoked() {
        presenter.onCreate()
        assertThat(mainController.location).isNotNull()
    }

    @Test fun onCreate_shouldNotSetMapLocationSecondTimeInvoked() {
        presenter.onCreate()
        mainController.location = null
        presenter.onCreate()
        assertThat(mainController.location).isNull()
    }

    @Test fun onReroute_shouldShowProgress() {
        routerFactory.reset()
        presenter.onRoutePreviewEvent(RoutePreviewEvent(getTestFeature()))
        presenter.onReroute(getTestLocation())
        assertThat(mainController.isProgressVisible).isTrue()
    }

    @Test fun onReroute_shouldFetchRoute() {
        val location = Mockito.mock(javaClass<Location>())
        Mockito.`when`(location.getLatitude()).thenReturn(1.0)
        Mockito.`when`(location.getLongitude()).thenReturn(2.0)

        routerFactory.reset()
        presenter.onRoutePreviewEvent(RoutePreviewEvent(getTestFeature(3.0, 4.0)))
        presenter.onReroute(location)
        assertThat(routerFactory.locations.get(0)[0]).isEqualTo(1.0)
        assertThat(routerFactory.locations.get(0)[1]).isEqualTo(2.0)
        assertThat(routerFactory.locations.get(1)[0]).isEqualTo(3.0)
        assertThat(routerFactory.locations.get(1)[1]).isEqualTo(4.0)
        assertThat(routerFactory.isFetching).isTrue()
    }

    @Test fun onRouteFailure_shouldHideProgress() {
        mainController.isProgressVisible = true
        presenter.failure(404)
        assertThat(mainController.isProgressVisible).isFalse()
    }

    @Test fun onRouteSuccess_shouldHideProgress() {
        mainController.isProgressVisible = true
        presenter.success(Route(JSONObject()))
        assertThat(mainController.isProgressVisible).isFalse()
    }

    @Test fun onRouteSuccess_shouldShowRoutingMode() {
        presenter.onRoutePreviewEvent(RoutePreviewEvent(getTestFeature()))
        mainController.isRoutingModeVisible = false
        presenter.success(Route(JSONObject()))
        assertThat(mainController.isRoutingModeVisible).isTrue()
    }

    class RouteEventSubscriber {
        public var event: RouteEvent? = null

        @Subscribe fun onRouteEvent(event: RouteEvent) {
            this.event = event
        }
    }
}
