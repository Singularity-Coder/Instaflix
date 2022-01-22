package com.singularitycoder.viewmodelstuff2.about.model

// https://krishanmadushankadev.medium.com/how-to-make-graphql-request-in-android-5a42c91acf7
// Generate new token - https://github.com/settings/tokens/new
// Test the amount of data you want to receive and prepare the query to get that data here - https://docs.github.com/en/graphql/overview/explorer
// Format query here - https://jsonformatter.org/graphql-formatter

// Graph QL
//

data class GitHubProfileQueryModel(val data: Data)

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
