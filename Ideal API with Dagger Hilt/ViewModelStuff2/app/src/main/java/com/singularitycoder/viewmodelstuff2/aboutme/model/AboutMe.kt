package com.singularitycoder.viewmodelstuff2.aboutme.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.singularitycoder.viewmodelstuff2.utils.TABLE_ABOUT_ME
import com.singularitycoder.viewmodelstuff2.utils.network.Skip

// https://krishanmadushankadev.medium.com/how-to-make-graphql-request-in-android-5a42c91acf7
// Generate new token - https://github.com/settings/tokens/new
// Test the amount of data you want to receive and prepare the query to get that data here - https://docs.github.com/en/graphql/overview/explorer
// Format query here - https://jsonformatter.org/graphql-formatter

// Disadvantages of REST
// 1. Over-fetching
// 2. Multiple requests for multiple resources
// 3. Waterfall network requests on nested data
// 4. Each client need to know the location of each service
// 5. API versioning becomes a hassle sometimes

// Graph QL
// Query Language and server-side runtime for APIs that prioritizes giving clients exactly the data they request and no more - Red Hat

// When to use graph-ql
// 1. While building apps where band-width usage matters
// 2. Apps where nested data needs to be fetched in a single call - like how facebook fetches posts along with their comments data
// 3. Where Apps retrieve data from multiple storage APIs - Like a dashboard that fetches data from multiple sources such as logging services, backend for consumption stats, 3rd party analytics tools to capture end user interactions, etc

// This query fetches user information (image, name, login, email, followers, following), 3 first of pinned repositories and 10 first of top repositories of a selected user in github.

// Provide a fixed primary key instead of auto generate
@Entity(tableName = TABLE_ABOUT_ME)
data class GitHubProfileQueryModel(
    @PrimaryKey @Skip val id: Long,
    val data: Data
)

data class Data(val repositoryOwner: RepositoryOwner)

data class RepositoryOwner(
    val avatarUrl: String,
    val email: String,
    val followers: Followers,
    val following: Following,
    val login: String,
    val name: String,
    val pinnedItems: PinnedItems,
    val starredRepositories: StarredRepositories,
    val topRepositories: TopRepositories
)

data class Followers(val totalCount: Int)

data class Following(val totalCount: Int)

data class PinnedItems(val nodes: List<Node>)

data class StarredRepositories(val totalCount: Int)

data class TopRepositories(val edges: List<Edge>)

data class Edge(val node: NodeX)

data class Node(
    val description: String,
    val name: String,
    val owner: Owner,
    val primaryLanguage: PrimaryLanguage,
    val stargazerCount: Int
)

data class Owner(val avatarUrl: String, val login: String)

data class PrimaryLanguage(val name: String)

data class NodeX(
    val description: String,
    val name: String,
    val owner: OwnerX,
    val primaryLanguage: PrimaryLanguageX,
    val stargazerCount: Int
)

data class OwnerX(val avatarUrl: String, val login: String)

data class PrimaryLanguageX(val name: String)
