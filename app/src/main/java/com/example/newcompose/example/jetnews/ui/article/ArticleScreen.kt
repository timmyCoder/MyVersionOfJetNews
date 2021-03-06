/*
 * Copyright 2019 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.newcompose.example.jetnews.ui.article

import android.content.Context
import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.compose.Composable
import androidx.compose.getValue
import androidx.compose.setValue
import androidx.compose.state
import androidx.ui.core.ContextAmbient
import androidx.ui.core.Modifier
import androidx.ui.foundation.Box
import androidx.ui.foundation.Icon
import androidx.ui.foundation.Text
import androidx.ui.foundation.contentColor
import androidx.ui.layout.Row
import androidx.ui.layout.Spacer
import androidx.ui.layout.fillMaxSize
import androidx.ui.layout.padding
import androidx.ui.layout.preferredHeight
import androidx.ui.layout.preferredSize
import androidx.ui.material.AlertDialog
import androidx.ui.material.IconButton
import androidx.ui.material.MaterialTheme
import androidx.ui.material.Scaffold
import androidx.ui.material.Surface
import androidx.ui.material.TextButton
import androidx.ui.material.TopAppBar
import androidx.ui.material.icons.Icons
import androidx.ui.material.icons.filled.ArrowBack
import androidx.ui.res.vectorResource
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.dp
import com.example.newcompose.example.jetnews.data.posts.PostsRepository
import com.example.newcompose.example.jetnews.data.posts.impl.BlockingFakePostsRepository
import com.example.newcompose.example.jetnews.data.posts.impl.post3
import com.example.newcompose.example.jetnews.data.successOr
import com.example.newcompose.example.jetnews.model.Post
import com.example.newcompose.R
import com.example.newcompose.example.jetnews.ui.Screen
import com.example.newcompose.example.jetnews.ui.ThemedPreview
import com.example.newcompose.example.jetnews.ui.darkThemeColors
import com.example.newcompose.example.jetnews.ui.effect.fetchPost
import com.example.newcompose.example.jetnews.ui.home.BookmarkButton
import com.example.newcompose.example.jetnews.ui.home.isFavorite
import com.example.newcompose.example.jetnews.ui.home.toggleBookmark
import com.example.newcompose.example.jetnews.ui.navigateTo
import com.example.newcompose.example.jetnews.ui.state.UiState

@Composable
fun ArticleScreen(postId: String, postsRepository: PostsRepository) {
    val postsState = fetchPost(postId, postsRepository)
    if (postsState is UiState.Success<Post>) {
        ArticleScreen(postsState.data)
    }
}

@Composable
private fun ArticleScreen(post: Post) {

    var showDialog by state { false }
    if (showDialog) {
        FunctionalityNotAvailablePopup { showDialog = false }
    }

    Scaffold(
        topAppBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Published in: ${post.publication?.name}",
                        // FIXME(b/143626708): this contentColor() is a bug workaround
                        style = MaterialTheme.typography.subtitle2.copy(color = contentColor())
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navigateTo(Screen.Home) }) {
                        Icon(Icons.Filled.ArrowBack)
                    }
                }
            )
        },
        bodyContent = { modifier ->
            PostContent(post, modifier)
        },
        bottomAppBar = {
            BottomBar(post) { showDialog = true }
        }
    )
}

@Composable
private fun BottomBar(post: Post, onUnimplementedAction: () -> Unit) {
    val context = ContextAmbient.current
    Surface(elevation = 2.dp) {
        Box(modifier = Modifier.preferredHeight(56.dp).fillMaxSize()) {
            Row {
                BottomBarAction(R.drawable.ic_favorite) { onUnimplementedAction() }
                BookmarkButton(
                    isBookmarked = isFavorite(postId = post.id),
                    onBookmark = { toggleBookmark(postId = post.id) }
                )
                BottomBarAction(R.drawable.ic_share) { sharePost(post, context) }
                Spacer(modifier = Modifier.weight(1f))
                BottomBarAction(R.drawable.ic_text_settings) { onUnimplementedAction() }
            }
        }
    }
}

@Composable
private fun BottomBarAction(
    @DrawableRes id: Int,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick, modifier = Modifier.padding(12.dp).preferredSize(24.dp, 24.dp)) {
        Icon(vectorResource(id))
    }
}

@Composable
private fun FunctionalityNotAvailablePopup(onDismiss: () -> Unit) {
    AlertDialog(
        onCloseRequest = onDismiss,
        text = {
            Text(
                text = "Functionality not available \uD83D\uDE48",
                style = MaterialTheme.typography.body2
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "CLOSE")
            }
        }
    )
}

private fun sharePost(post: Post, context: Context) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TITLE, post.title)
        putExtra(Intent.EXTRA_TEXT, post.url)
    }
    context.startActivity(Intent.createChooser(intent, "Share post"))
}

@Preview("Article screen")
@Composable
fun PreviewArticle() {
    ThemedPreview {
        val post = loadFakePost(post3.id)
        ArticleScreen(post)
    }
}

@Preview("Article screen dark theme")
@Composable
fun PreviewArticleDark() {
    ThemedPreview(darkThemeColors) {
        val post = loadFakePost(post3.id)
        ArticleScreen(post)
    }
}

@Composable
private fun loadFakePost(postId: String): Post {
    var post: Post? = null
    BlockingFakePostsRepository(ContextAmbient.current).getPost(postId) { result ->
        post = result.successOr(null)
    }
    return post!!
}
