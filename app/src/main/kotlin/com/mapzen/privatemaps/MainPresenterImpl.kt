package com.mapzen.privatemaps

import com.mapzen.pelias.gson.Result

public class MainPresenterImpl : MainPresenter {
    override var viewController: ViewController? = null
    override var currentSearchTerm: String? = null

    private var result: Result? = null

    override fun onSearchResultsAvailable(result: Result?) {
        this.result = result
        viewController?.showSearchResults(result?.getFeatures())
        viewController?.hideProgress()
    }

    override fun restoreViewState() {
        if (result != null) {
            viewController?.showSearchResults(result?.getFeatures())
        }
    }

    override fun onExpandSearchView() {
        viewController?.hideOverflowMenu()
    }

    override fun onCollapseSearchView() {
        result = null;
        viewController?.hideSearchResults()
        viewController?.showOverflowMenu()
    }

    override fun onQuerySubmit() {
        viewController?.showProgress()
    }

    override fun onSearchResultSelected(position: Int) {
        if (result != null) {
            viewController?.centerOnCurrentFeature(result?.getFeatures())
        }
    }
}