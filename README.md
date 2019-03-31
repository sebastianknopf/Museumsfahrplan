# Museumsfahrplan
This is the official project repository of Museumsfahrplan, a former university project
and just now a little playground for transit data in historical and tourist transit agencies.

## What does this?
Museumsfahrplan is an android app for gathering information about transport agencies
offering some services with historical vehicles. This could be a steam driven touristic
train, a historical tram route connecting the inner city with a museum or another special event
or nearly ever other transport mode with a museum charactre you can imagine!

*With April 2019 we're proud to publish the beta version of our app in Google Play Store! That means you can test it for yourself
now and share your expierience with us. The season of 2019 will be our beta-test with several museum transport agencies and some new features we're currently implementing.
If this beta-test runs completely without any big crashes, we're planning to run the project in future too!*

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

## Contributing
If you want to contribute our project you're welcome of course! This does not have to be in form of programming 
or with special programming skills, we'd also like to hear some suggestions or your expierience too.

If you want to build your own version or try to implement a new feature, feel free to do so! What you need
is Android Studio and a file named *secret.xml*. This file contains the api cretentials and some important
keys you need to access the data APIs. The app will not run or compile without this file, so please contact
us if you need some contents of the file, so we can talk about that.

You want to use our data API for another project? No problem! Have a look in the [documentations](documentations/) folder,
where you can find an API reference. We're sorry for providing this in German only, if you want to translate it - feel free ;-)
To access the API you'll also need some credentials. Please contact us to get your own API key.

## License 
The whole application project including the module 'staticnet' is licensed under Apache Version 2.0
license. This includes the module *app* and the module *staticnet* which provides access
to the REST API.

See [LICENSE](LICENSE.md) for more information.

