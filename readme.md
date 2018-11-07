# The Udacity Popular Movies Project

This builds out the Udacity Popular movies project, the goal is to create a 2 page app that displays a list of movies and allows the user to select one to see details of that movie.  Multiple types of sorting is supported for the list of movies, and the movie information will be taken from The Movie DB API's.

# Image handeling
This will be handeled with the Picasso image library see: http://square.github.io/picasso/

# themoviedb.org API
API Docs https://www.themoviedb.org/settings/api

### API key
Put the API key in the global gradle.properties using the following property name -
- themoviedb_v3_ApiKey

Alternativly the api key coule be put in the app/build.gradle in this property

`buildConfigField "String", "THEMOVIEDB_V3_APIKEY", "\"${themoviedb_v3_ApiKey}\""`