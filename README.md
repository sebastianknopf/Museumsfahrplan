# Museumsfahrplan
This is the official project repository of Museumsfahrplan, a former university project
and just now a little playground for transit data in historical and tourist transit agencies.

## What does this?
Museumsfahrplan is an android app for gathering information about transport agencies
offering some services with historical vehicles. This could be a steam driven touristic
train, a historical tram route connecting the inner city with a museum or another special event
or nearly ever other transport mode with a museum charactre you can imagine!

*Currently this app is in a (re-) development phase to fix up some errors we made during
our learning process in university! But soon we plan to publish a beta version on Google's
Play Store where you can test it and make your own expierience!*

## How does this work?
The app itself is a simple android application with basic development patterns.
It does not contain the transit data itself - therefor we created an JSON 
based REST API (over six months of developing and still in beta mode!) to request
the trip information for the end user.

The whole process from the timetable to the app can be imagined with the following scheme:

* We receive the transit data in a proprietary format (for e.g. a leaflet or via email) and 
some additional information from a transport agency offering historical services
* The transit data become formatted in a standard format called [GTFS](https://developers.google.com/transit/gtfs/) (with some 'unofficial' extensions
to provide better data quality) and stored in a big productive database on the servers
backend system
* When the users opens the app, the information are requested via the REST API from the 
database, including realtime service alerts specified in [GTFS-RT](https://developers.google.com/transit/gtfs-realtime/) and displayed in different
cases to the end user

## License 
The whole application project including the module 'staticnet' is licensed under Apache Version 2.0
license. This includes the module *app* and the module *staticnet* which provides access
to the REST API.

See [LICENSE](LICENSE.md) for more information.

