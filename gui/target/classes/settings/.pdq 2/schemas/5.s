<?xml version="1.0" encoding="UTF-8"?>
<schema name="Demo API" description="">
	<sources>
		<source name="geonames" file="services/geonames-services.xml"
			discoverer="uk.ac.ox.cs.pdq.runtime.io.xml.ServiceReader" />
		<source name="yahoo" file="services/yahoo-services.xml"
			discoverer="uk.ac.ox.cs.pdq.runtime.io.xml.ServiceReader" />
		<source name="google" file="services/google-services.xml"
			discoverer="uk.ac.ox.cs.pdq.runtime.io.xml.ServiceReader" />
		<source name="worldbank" file="services/worldbank-services.xml"
			discoverer="uk.ac.ox.cs.pdq.runtime.io.xml.ServiceReader" />
	</sources>
	<relations>
		<relation name="Continent">
			<attribute name="id" type="java.lang.String" />
			<attribute name="name" type="java.lang.String" />
			<attribute name="surface" type="java.lang.Integer" />
			<attribute name="bbNorth" type="java.lang.Double" />
			<attribute name="bbSouth" type="java.lang.Double" />
			<attribute name="bbEast" type="java.lang.Double" />
			<attribute name="bbWest" type="java.lang.Double" />
		</relation>
		<relation name="Country">
			<attribute name="id" type="java.lang.String" />
			<attribute name="isoCode" type="java.lang.String" />
			<attribute name="name" type="java.lang.String" />
			<attribute name="continent" type="java.lang.String" />
			<attribute name="capital" type="java.lang.String" />
			<attribute name="currencyCode" type="java.lang.String" />
			<attribute name="surface" type="java.lang.Integer" />
			<attribute name="bbNorth" type="java.lang.Double" />
			<attribute name="bbSouth" type="java.lang.Double" />
			<attribute name="bbEast" type="java.lang.Double" />
			<attribute name="bbWest" type="java.lang.Double" />
		</relation>
		<relation name="City">
			<attribute name="id" type="java.lang.String" />
			<attribute name="name" type="java.lang.String" />
			<attribute name="countryCode" type="java.lang.String" />
			<attribute name="cityBBNorth" type="java.lang.Double" />
			<attribute name="cityBBSouth" type="java.lang.Double" />
			<attribute name="cityBBEast" type="java.lang.Double" />
			<attribute name="cityBBWest" type="java.lang.Double" />
		</relation>
		<relation name="CapitalCity">
			<attribute name="id" type="java.lang.String" />
			<attribute name="name" type="java.lang.String" />
			<attribute name="countryCode" type="java.lang.String" />
			<attribute name="cityBBNorth" type="java.lang.Double" />
			<attribute name="cityBBSouth" type="java.lang.Double" />
			<attribute name="cityBBEast" type="java.lang.Double" />
			<attribute name="cityBBWest" type="java.lang.Double" />
		</relation>
		<relation name="YahooContinents" source="yahoo" size="7">
			<access-method name="yh_geo_continent" type="FREE"
				cost="1.0" />
		</relation>
		<relation name="YahooCountries" source="yahoo" size="250">
			<access-method name="yh_geo_country" type="FREE" cost="1.0" />
		</relation>
		<relation name="YahooPlaces" source="yahoo" size="10000000">
			<access-method name="yh_geo_name" type="LIMITED"
				inputs="2" cost="100.0" />
			<access-method name="yh_geo_woeid" type="LIMITED"
				inputs="1" cost="1.0" />
			<access-method name="yh_geo_type" type="LIMITED"
				inputs="2,3" cost="50.0" />
		</relation>
		<relation name="YahooPlaceCode" source="yahoo" size="100000000">
			<access-method name="yh_geo_code" type="LIMITED"
				inputs="1,2" cost="1.0" />
		</relation>
		<relation name="YahooBelongsTo" source="yahoo" size="100000000">
			<access-method name="yh_geo_belongs" type="LIMITED"
				inputs="1" cost="50.0" />
		</relation>
		<relation name="YahooWeather" source="yahoo" size="100000000">
			<access-method name="yh_wtr_woeid" type="LIMITED"
				inputs="1" cost="10.0" />
		</relation>
		<relation name="GNCountryInfo" source="geonames" size="250">
			<access-method name="gn_cinfo" type="FREE" cost="100.0" />
			<access-method name="gn_cinfo_cc" type="LIMITED"
				inputs="2" cost="1.0" />
		</relation>
		<relation name="WBCountries" source="worldbank" size="250">
			<access-method name="c_free" type="FREE" cost="1.0" />
			<access-method name="c_il" type="LIMITED" inputs="4"
				cost="1.0" />
		</relation>
	</relations>
	<dependencies>
		<!-- Required for Example 1 -->
		<dependency>
			<body>
				<atom name="YahooCountries">
					<variable name="woeid" />
					<variable name="type" />
					<constant value="Country" />
					<variable name="countryName" />
				</atom>
			</body>
			<head>
				<atom name="YahooPlaces">
					<variable name="woeid" />
					<variable name="countryName" />
					<variable name="type" />
					<constant value="Country" />
					<variable /><variable /><variable />
					<variable /><variable /><variable />
					<variable /><variable /><variable />
					<variable /><variable /><variable />
					<variable /><variable />
				</atom>
			</head>
		</dependency>
		<dependency>
			<body>
				<atom name="YahooPlaces">
					<variable name="woeid" />
					<variable name="countryName" />
					<variable name="type" />
					<constant value="Country" />
					<variable /><variable /><variable />
					<variable /><variable /><variable />
					<variable /><variable /><variable />
					<variable /><variable /><variable />
					<variable /><variable />
				</atom>
			</body>
			<head>
				<atom name="YahooCountries">
					<variable name="woeid" />
					<variable name="type" />
					<constant value="Country" />
					<variable name="countryName" />
				</atom>
			</head>
		</dependency>

		<!-- Required for Example 2 -->
		<dependency>
			<body>
				<atom name="CapitalCity">
					<variable name="cityId" />
					<variable name="name" />
					<variable name="countryCode" />
					<variable /><variable /><variable />
					<variable />
				</atom>
			</body>
			<head>
				<atom name="City">
					<variable name="cityId" />
					<variable name="name" />
					<variable name="countryCode" />
					<variable /><variable /><variable />
					<variable />
				</atom>
			</head>
		</dependency>
		<dependency>
			<body>
				<atom name="City">
					<variable name="cityId" />
					<variable name="cityName" />
					<variable name="countryCode" />
					<variable /><variable /><variable />
					<variable />
				</atom>
				<atom name="Country">
					<variable name="countryId" />
					<variable name="countryCode" />
					<variable name="countryName" />
					<variable name="continentName" />
					<variable name="capital" />
					<variable /><variable /><variable />
					<variable /><variable /><variable />
				</atom>
			</body>
			<head>
				<atom name="YahooPlaces">
					<variable name="cityId" />
					<variable name="cityName" />
					<variable name="type" />
					<constant value="Town" />
					<variable /><variable /><variable />
					<variable /><variable /><variable />
					<variable /><variable /><variable />
					<variable /><variable /><variable />
					<variable /><variable />
				</atom>
				<atom name="YahooCountries">
					<variable name="countryId" />
					<variable name="type" />
					<constant value="Country" />
					<variable name="countryName" />
				</atom>
				<atom name="YahooBelongsTo">
					<variable name="cityId" />
					<variable name="countryId" />
					<variable /><variable /><variable />
				</atom>
			</head>
		</dependency>
		<dependency>
			<body>
				<atom name="YahooPlaces">
					<variable name="cityId" />
					<variable name="cityName" />
					<variable name="type" />
					<constant value="Town" />
					<variable /><variable /><variable />
					<variable /><variable /><variable />
					<variable /><variable /><variable />
					<variable /><variable /><variable />
					<variable /><variable />
				</atom>
				<atom name="YahooCountries">
					<variable name="countryId" />
					<variable name="type" />
					<constant value="Country" />
					<variable name="countryName" />
				</atom>
				<atom name="YahooBelongsTo">
					<variable name="cityId" />
					<variable name="countryId" />
					<variable /><variable /><variable />
				</atom>
			</body>
			<head>
				<atom name="City">
					<variable name="cityId" />
					<variable name="cityName" />
					<variable name="countryCode" />
					<variable /><variable /><variable />
					<variable />
				</atom>
				<atom name="Country">
					<variable name="countryId" />
					<variable name="countryCode" />
					<variable name="countryName" />
					<variable name="continentName" />
					<variable name="capital" />
					<variable /><variable /><variable />
					<variable /><variable /><variable />
				</atom>
			</head>
		</dependency>
		<dependency>
			<body>
				<atom name="Continent">
					<variable name="continentId" />
					<variable name="continentName" />
					<variable /><variable /><variable />
					<variable /><variable />
				</atom>
				<atom name="Country">
					<variable name="countryId" />
					<variable name="countryCode" />
					<variable name="countryName" />
					<variable name="continentName" />
					<variable /><variable /><variable />
					<variable /><variable /><variable />
					<variable />
				</atom>
			</body>
			<head>
				<atom name="GNCountryInfo">
					<variable name="geonameId" />
					<variable name="countryCode" />
					<variable name="countryName" />
					<variable name="continentCode" />
					<variable name="continentName" />
					<variable /><variable /><variable />
					<variable /><variable /><variable />
					<variable /><variable /><variable />
					<variable /><variable /><variable />
				</atom>
			</head>
		</dependency>
		<dependency>
			<body>
				<atom name="GNCountryInfo">
					<variable name="geonameId" />
					<variable name="countryCode" />
					<variable name="countryName" />
					<variable name="continentCode" />
					<variable name="continentName" />
					<variable /><variable /><variable />
					<variable /><variable /><variable />
					<variable /><variable /><variable />
					<variable /><variable /><variable />
				</atom>
			</body>
			<head>
				<atom name="Continent">
					<variable name="continentId" />
					<variable name="continentName" />
					<variable /><variable /><variable />
					<variable /><variable />
				</atom>
				<atom name="Country">
					<variable name="countryId" />
					<variable name="countryCode" />
					<variable name="countryName" />
					<variable name="continentName" />
					<variable /><variable /><variable />
					<variable /><variable /><variable />
					<variable />
				</atom>
			</head>
		</dependency>

		<dependency>
			<body>
				<atom name="Country">
					<variable name="countryId" />
					<variable name="countryCode" />
					<variable name="countryName" />
					<variable /><variable /><variable />
					<variable /><variable /><variable />
					<variable /><variable />
				</atom>
				<atom name="CapitalCity">
					<variable name="cityId" />
					<variable name="capital" />
					<variable name="countryCode" />
					<variable /><variable /><variable />
					<variable />
				</atom>
			</body>
			<head>
				<atom name="GNCountryInfo">
					<variable name="geonameId" />
					<variable name="countryCode" />
					<variable name="countryName" />
					<variable /><variable /><variable />
					<variable /><variable />
					<variable name="capital" />
					<variable /><variable /><variable />
					<variable /><variable /><variable />
					<variable /><variable />
				</atom>
			</head>
		</dependency>
		<dependency>
			<body>
				<atom name="GNCountryInfo">
					<variable name="geonameId" />
					<variable name="countryCode" />
					<variable name="countryName" />
					<variable /><variable /><variable />
					<variable /><variable />
					<variable name="capital" />
					<variable /><variable /><variable />
					<variable /><variable /><variable />
					<variable /><variable />
				</atom>
			</body>
			<head>
				<atom name="Country">
					<variable name="countryId" />
					<variable name="countryCode" />
					<variable name="countryName" />
					<variable /><variable /><variable />
					<variable /><variable /><variable />
					<variable /><variable />
				</atom>
				<atom name="CapitalCity">
					<variable name="cityId" />
					<variable name="capital" />
					<variable name="countryCode" />
					<variable /><variable /><variable />
					<variable />
				</atom>
			</head>
		</dependency>

		<dependency>
			<body>
				<atom name="Continent">
					<variable name="continentId" />
					<variable name="continentName" />
					<variable /><variable /><variable />
					<variable /><variable />
				</atom>
				<atom name="Country">
					<variable name="countryId" />
					<variable name="countryCode" />
					<variable name="countryName" />
					<variable name="continentName" />
					<variable /><variable /><variable />
					<variable /><variable /><variable />
					<variable />
				</atom>
			</body>
			<head>
				<atom name="YahooContinents">
					<variable name="continentId" />
					<variable name="type" />
					<constant value="Continent" />
					<variable name="continentName" />
				</atom>
				<atom name="YahooCountries">
					<variable name="countryId" />
					<variable name="type" />
					<constant value="Country" />
					<variable name="countryName" />
				</atom>
				<atom name="YahooBelongsTo">
					<variable name="countryId" />
					<variable name="continentId" />
					<variable /><variable /><variable />
				</atom>
			</head>
		</dependency>
		<dependency>
			<body>
				<atom name="YahooContinents">
					<variable name="continentId" />
					<variable name="type" />
					<constant value="Continent" />
					<variable name="continentName" />
				</atom>
				<atom name="YahooCountries">
					<variable name="countryId" />
					<variable name="type" />
					<constant value="Country" />
					<variable name="countryName" />
				</atom>
				<atom name="YahooBelongsTo">
					<variable name="countryId" />
					<variable name="continentId" />
					<variable /><variable /><variable />
				</atom>
			</body>
			<head>
				<atom name="Continent">
					<variable name="continentId" />
					<variable name="continentName" />
					<variable /><variable /><variable />
					<variable /><variable />
				</atom>
				<atom name="Country">
					<variable name="countryId" />
					<variable name="countryCode" />
					<variable name="countryName" />
					<variable name="continentName" />
					<variable /><variable /><variable />
					<variable /><variable /><variable />
					<variable />
				</atom>
			</head>
		</dependency>
		<dependency>
			<body>
				<atom name="WBCountries">
					<variable name="wbid" />
					<variable name="countryCode" />
					<variable name="countryName" />
					<variable name="incomeLevel" />
					<variable name="lendingType" />
					<variable name="capital" />
					<variable name="latitude" />
					<variable name="longitude" />
				</atom>
			</body>
			<head>
				<atom name="Country">
					<variable name="countryId" />
					<variable name="countryCode" />
					<variable name="countryName" />
					<variable name="continentName" />
					<variable /><variable /><variable />
					<variable /><variable /><variable />
					<variable />
				</atom>
				<atom name="CapitalCity">
					<variable name="cityId" />
					<variable name="capital" />
					<variable name="countryCode" />
					<variable /><variable /><variable />
					<variable />
				</atom>
			</head>
		</dependency>
		<dependency>
			<body>
				<atom name="Country">
					<variable name="countryId" />
					<variable name="countryCode" />
					<variable name="countryName" />
					<variable name="continentName" />
					<variable /><variable /><variable />
					<variable /><variable /><variable />
					<variable />
				</atom>
				<atom name="CapitalCity">
					<variable name="cityId" />
					<variable name="capital" />
					<variable name="countryCode" />
					<variable /><variable /><variable />
					<variable />
				</atom>
			</body>
			<head>
				<atom name="WBCountries">
					<variable name="wbid" />
					<variable name="countryCode" />
					<variable name="countryName" />
					<variable name="incomeLevel" />
					<variable name="lendingType" />
					<variable name="capital" />
					<variable name="latitude" />
					<variable name="longitude" />
				</atom>
			</head>
		</dependency>

	</dependencies>
</schema>

