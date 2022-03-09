package com.singularitycoder.viewmodelstuff2.helpers.constants

import com.singularitycoder.viewmodelstuff2.helpers.extensions.trimIndentsAndNewLines

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
