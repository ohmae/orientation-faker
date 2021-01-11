package net.mm2d.orientation.util

import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel

@MainThread
inline fun <reified VM : ViewModel> Fragment.parentViewModels() =
    viewModels<VM>(
        ownerProducer = { requireParentFragment() },
        factoryProducer = { requireParentFragment().defaultViewModelProviderFactory }
    )
