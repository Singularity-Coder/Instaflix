package com.singularitycoder.viewmodelstuff2.utils

const val DB_FAV_ANIME = "db_fav_anime"
const val TABLE_ANIME_DATA = "table_anime_data"
const val TABLE_DESCRIPTIONS = "table_descriptions"
const val BASE_URL = "https://api.aniapi.com"
const val GRAPH_QL_GITHUB_PROFILE_QUERY = "query{repositoryOwner(login: \"Singularity-Coder\") { login ... on User { pinnedItems(first: 3) { nodes { ... on Repository { name description stargazerCount primaryLanguage { name } owner { avatarUrl login } } } } topRepositories(first: 10, orderBy: {field: NAME, direction: ASC}) { edges { node { name description stargazerCount primaryLanguage { name } owner { avatarUrl login } } } } email name avatarUrl starredRepositories { totalCount } followers { totalCount } following { totalCount } } }}"
