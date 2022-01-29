package com.singularitycoder.viewmodelstuff2.utils

const val DB_ANIME = "db_anime"
const val DB_ABOUT_ME = "db_about_me"
const val TABLE_ANIME_DATA = "table_anime_data"
const val TABLE_DESCRIPTIONS = "table_descriptions"
const val TABLE_ABOUT_ME = "table_about_me"
const val BASE_URL_ANI_API = "https://api.aniapi.com"
const val BASE_URL_GITHUB = "https://api.github.com/"
val GRAPH_QL_GITHUB_PROFILE_QUERY = """
    query {
        repositoryOwner(login: "Singularity-Coder") {
          login
          ... on User {
            pinnedItems(first: 3) {
              nodes {
                ... on Repository {
                  name
                  description
                  stargazerCount
                  primaryLanguage {
                    name
                  }
                  owner {
                    avatarUrl
                    login
                  }
                }
              }
            }
            topRepositories(first: 10, orderBy: { field: NAME, direction: ASC }) {
              edges {
                node {
                  name
                  description
                  stargazerCount
                  primaryLanguage {
                    name
                  }
                  owner {
                    avatarUrl
                    login
                  }
                }
              }
            }
            email
            name
            avatarUrl
            starredRepositories {
              totalCount
            }
            followers {
              totalCount
            }
            following {
              totalCount
            }
          }
        }
    }
""".trimIndentsAndNewLines()

enum class Tab(name: String) {
    HOME(name = "Home"),
    FAVORITES(name = "Favorites"),
    NOTIFICATIONS(name = "Notifications"),
    MORE(name = "More")
}
