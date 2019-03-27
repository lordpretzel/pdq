<?xml version="1.0" encoding="UTF-8"?>
<schema name="Google / FreeBase API" description="">
<sources>
<source name="google" file="services/google-services.xml" discoverer="uk.ac.ox.cs.pdq.runtime.io.xml.ServiceReader"/>
</sources>
<relations>
<relation name="GoogleElevation" source="google" size="100000000">
<access-method name="by_location" type="LIMITED" inputs="1,2" cost="1.0"/>
</relation>
<relation name="GoogleGeoCode" source="google" size="100000000">
<access-method name="by_address" type="LIMITED" inputs="1" cost="5.0"/>
<access-method name="by_coord" type="LIMITED" inputs="2,3" cost="1.0"/>
</relation>
<relation name="GoogleTimezone" source="google" size="100000000">
<access-method name="g_timezone" type="LIMITED" inputs="1,2,3" cost="1.0"/>
</relation>
<relation name="FreeBasePeople" source="google" size="17000000">
</relation>
<relation name="FreeBaseCountries" source="google" size="700">
<access-method name="fb_country_free" type="FREE" cost="100.0"/>
</relation>
</relations>
<dependencies>
</dependencies>
</schema>