RealTime + RealWeather
-----------
Powered by Weather Underground


For Minecraft 1.7.10

<a href='http://jenkins.petercashel.net/job/RealTime/'><img src='http://jenkins.petercashel.net/buildStatus/icon?job=RealTime'></a>

This mod uses weather data from Weather Underground. Neither myself or this mod is affiliated with Weather Underground.
Weather Underground is a registered trademark of The Weather Channel, LLC. both in the United States and internationally. The Weather Underground Logo is a trademark of Weather Underground, LLC.


----------
 Config
----------

    B:EnforceSync=false
	
	//Enables RealTime
    B:RealTimeEnabled=false
	
	//TimeZone
    S:RealTime_TimeZone_Name=     #Defaults to local timezone
    # Supports 'GMT-4' time zone format as well as 'Australia/Brisbane'
	# See TZ column in the following page if unsure. https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
	
	//Weather (WIP)
	B:RealWeatherEnabled=false
	
	S:RealWeather_APIKEY=
	# Put your Weather Underground API key here. Required for RealWeather
    
	
    S:RealWeather_City=
	S:RealWeather_Country=
	# These are for your location for weather and moon phases
	
--------------------------------------------------------------------
<a href="http://www.wunderground.com/?apiref=248dadb7aa8e6c37">
<img src="http://icons.wxug.com/logos/PNG/wundergroundLogo_4c_horz.png">
</a>
