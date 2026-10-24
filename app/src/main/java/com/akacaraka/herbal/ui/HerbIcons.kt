package com.akacaraka.herbal.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.ui.graphics.vector.ImageVector

internal object HerbIcons {
    fun HomeFilled(): ImageVector = Icons.Filled.Home
    fun HomeOutlined(): ImageVector = Icons.Outlined.Home
    fun SearchFilled(): ImageVector = Icons.Filled.Search
    fun SearchOutlined(): ImageVector = Icons.Outlined.Search
    fun FavoriteFilled(): ImageVector = Icons.Filled.Favorite
    fun FavoriteOutlined(): ImageVector = Icons.Outlined.FavoriteBorder
    fun PersonFilled(): ImageVector = Icons.Filled.Person
    fun PersonOutlined(): ImageVector = Icons.Outlined.Person
    fun Plant(): ImageVector = Icons.Filled.Spa
    fun Leaf(): ImageVector = Icons.Filled.Spa
    fun Drink(): ImageVector = Icons.Filled.LocalCafe
    fun Back(): ImageVector = Icons.AutoMirrored.Outlined.ArrowBack
    fun Forward(): ImageVector = Icons.AutoMirrored.Filled.ArrowForward
    fun Close(): ImageVector = Icons.Filled.Close
    fun Success(): ImageVector = Icons.Filled.CheckCircle
    fun Warning(): ImageVector = Icons.Filled.Warning
    fun Error(): ImageVector = Icons.Filled.Error
    fun Info(): ImageVector = Icons.Filled.Info
    fun Report(): ImageVector = Icons.Filled.Description
}
