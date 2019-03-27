<?xml version="1.0" encoding="UTF-8"?>
<schema name="Yahoo! API" description="">
<sources>
<source name="yahoo" file="services/yahoo-services.xml" discoverer="uk.ac.ox.cs.pdq.runtime.io.xml.ServiceReader"/>
</sources>
<relations>
<relation name="YahooPlaces" source="yahoo" size="10000000">
<access-method name="yh_geo_name" type="LIMITED" inputs="2" cost="100.0"/>
<access-method name="yh_geo_woeid" type="LIMITED" inputs="1" cost="1.0"/>
<access-method name="yh_geo_type" type="LIMITED" inputs="2,3" cost="50.0"/>
</relation>
<relation name="YahooPlaceType" source="yahoo" size="20">
<access-method name="yh_geo_types" type="FREE" cost="50.0"/>
<access-method name="yh_geo_types_name" type="LIMITED" inputs="2" cost="5.0"/>
</relation>
<relation name="YahooPlaceCommonAncestor" source="yahoo" size="10000000">
<access-method name="yh_com_anc" type="LIMITED" inputs="1,2" cost="25.0"/>
</relation>
<relation name="YahooPlaceRelationship" source="yahoo" size="10000000">
<access-method name="yh_geo_rel" type="LIMITED" inputs="1,2" cost="50.0"/>
</relation>
<relation name="YahooPlaceCode" source="yahoo" size="10000000">
<access-method name="yh_geo_code" type="LIMITED" inputs="1,2" cost="1.0"/>
</relation>
<relation name="YahooContinents" source="yahoo" size="7">
<access-method name="yh_geo_continent" type="FREE" cost="1.0"/>
</relation>
<relation name="YahooCountries" source="yahoo" size="250">
<access-method name="yh_geo_country" type="FREE" cost="1.0"/>
</relation>
<relation name="YahooSeas" source="yahoo" size="51">
<access-method name="yh_geo_sea" type="FREE" cost="1.0"/>
</relation>
<relation name="YahooOceans" source="yahoo" size="5">
<access-method name="yh_geo_ocean" type="FREE" cost="1.0"/>
</relation>
<relation name="YahooWeather" source="yahoo" size="100000000">
<access-method name="yh_wtr_woeid" type="LIMITED" inputs="1" cost="10.0"/>
</relation>
</relations>
<dependencies>
<dependency>
<body>
<atom name="YahooPlaces">
<variable name="woeid"/>
<variable name="name"/>
<variable name="type"/>
<variable name="placeTypeName"/>
<variable name="country"/>
<variable name="admin1"/>
<variable name="admin2"/>
<variable name="admin3"/>
<variable name="locality1"/>
<variable name="locality2"/>
<variable name="postal"/>
<variable name="latitude"/>
<variable name="longitude"/>
<variable name="bboxNorth"/>
<variable name="bboxSouth"/>
<variable name="bboxEast"/>
<variable name="bboxWest"/>
<variable name="timezone"/>
</atom>
</body>
<head>
<atom name="YahooPlaceType">
<variable name="placeTypeName"/>
<variable name="type"/>
<variable name="uri"/>
</atom>
</head>
</dependency>
<dependency>
<body>
<atom name="YahooPlaceRelationship">
<constant value="descendants"/>
<variable name="x"/>
<variable name="y"/>
<variable name="x1"/>
<variable name="x2"/>
<variable name="x3"/>
</atom>
<atom name="YahooPlaceRelationship">
<constant value="children"/>
<variable name="y"/>
<variable name="z"/>
<variable name="y1"/>
<variable name="y2"/>
<variable name="y3"/>
</atom>
</body>
<head>
<atom name="YahooPlaceRelationship">
<constant value="descendants"/>
<variable name="x"/>
<variable name="z"/>
<variable name="z1"/>
<variable name="z2"/>
<variable name="z3"/>
</atom>
</head>
</dependency>
<dependency>
<body>
<atom name="YahooPlaceRelationship">
<constant value="children"/>
<variable name="x"/>
<variable name="y"/>
<variable name="x1"/>
<variable name="x2"/>
<variable name="x3"/>
</atom>
<atom name="YahooPlaceRelationship">
<constant value="children"/>
<variable name="y"/>
<variable name="z"/>
<variable name="y1"/>
<variable name="y2"/>
<variable name="y3"/>
</atom>
</body>
<head>
<atom name="YahooPlaceRelationship">
<constant value="descendants"/>
<variable name="x"/>
<variable name="z"/>
<variable name="z1"/>
<variable name="z2"/>
<variable name="z3"/>
</atom>
</head>
</dependency>
<dependency>
<body>
<atom name="YahooPlaceRelationship">
<constant value="ancestors"/>
<variable name="x"/>
<variable name="y"/>
<variable name="x1"/>
<variable name="x2"/>
<variable name="x3"/>
</atom>
</body>
<head>
<atom name="YahooPlaceRelationship">
<constant value="descendants"/>
<variable name="y"/>
<variable name="x"/>
<variable name="y1"/>
<variable name="y2"/>
<variable name="y3"/>
</atom>
</head>
</dependency>
<dependency>
<body>
<atom name="YahooPlaceRelationship">
<constant value="ancestors"/>
<variable name="x"/>
<variable name="y"/>
<variable name="x1"/>
<variable name="x2"/>
<variable name="x3"/>
</atom>
<atom name="YahooPlaceRelationship">
<constant value="ancestors"/>
<variable name="z"/>
<variable name="y"/>
<variable name="y1"/>
<variable name="y2"/>
<variable name="y3"/>
</atom>
</body>
<head>
<atom name="YahooPlaceCommonAncestor">
<variable name="x"/>
<variable name="z"/>
<variable name="y"/>
<variable name="w2"/>
<variable name="w3"/>
<variable name="w4"/>
</atom>
</head>
</dependency>
<dependency>
<body>
<atom name="YahooPlaceCode">
<variable name="namespace"/>
<variable name="code"/>
<variable name="woeid"/>
</atom>
</body>
<head>
<atom name="YahooPlaces">
<variable name="woeid"/>
<variable name="name"/>
<variable name="type"/>
<variable name="placeTypeName"/>
<variable name="country"/>
<variable name="admin1"/>
<variable name="admin2"/>
<variable name="admin3"/>
<variable name="locality1"/>
<variable name="locality2"/>
<variable name="postal"/>
<variable name="latitude"/>
<variable name="longitude"/>
<variable name="bboxNorth"/>
<variable name="bboxSouth"/>
<variable name="bboxEast"/>
<variable name="bboxWest"/>
<variable name="timezone"/>
</atom>
</head>
</dependency>
<dependency>
<body>
<atom name="YahooPlaces">
<variable name="woeid"/>
<variable name="name"/>
<variable name="type"/>
<constant value="Continent"/>
<variable name="country"/>
<variable name="admin1"/>
<variable name="admin2"/>
<variable name="admin3"/>
<variable name="locality1"/>
<variable name="locality2"/>
<variable name="postal"/>
<variable name="latitude"/>
<variable name="longitude"/>
<variable name="bboxNorth"/>
<variable name="bboxSouth"/>
<variable name="bboxEast"/>
<variable name="bboxWest"/>
<variable name="timezone"/>
</atom>
</body>
<head>
<atom name="YahooContinents">
<variable name="woeid"/>
<variable name="type"/>
<variable name="placeTypeName"/>
<variable name="name"/>
</atom>
</head>
</dependency>
<dependency>
<body>
<atom name="YahooPlaces">
<variable name="woeid"/>
<variable name="name"/>
<variable name="type"/>
<constant value="Country"/>
<variable name="country"/>
<variable name="admin1"/>
<variable name="admin2"/>
<variable name="admin3"/>
<variable name="locality1"/>
<variable name="locality2"/>
<variable name="postal"/>
<variable name="latitude"/>
<variable name="longitude"/>
<variable name="bboxNorth"/>
<variable name="bboxSouth"/>
<variable name="bboxEast"/>
<variable name="bboxWest"/>
<variable name="timezone"/>
</atom>
</body>
<head>
<atom name="YahooCountries">
<variable name="woeid"/>
<variable name="type"/>
<variable name="placeTypeName"/>
<variable name="name"/>
</atom>
</head>
</dependency>
<dependency>
<body>
<atom name="YahooPlaces">
<variable name="woeid"/>
<variable name="name"/>
<variable name="type"/>
<constant value="Sea"/>
<variable name="country"/>
<variable name="admin1"/>
<variable name="admin2"/>
<variable name="admin3"/>
<variable name="locality1"/>
<variable name="locality2"/>
<variable name="postal"/>
<variable name="latitude"/>
<variable name="longitude"/>
<variable name="bboxNorth"/>
<variable name="bboxSouth"/>
<variable name="bboxEast"/>
<variable name="bboxWest"/>
<variable name="timezone"/>
</atom>
</body>
<head>
<atom name="YahooSeas">
<variable name="woeid"/>
<variable name="type"/>
<variable name="placeTypeName"/>
<variable name="name"/>
</atom>
</head>
</dependency>
<dependency>
<body>
<atom name="YahooPlaces">
<variable name="woeid"/>
<variable name="name"/>
<variable name="type"/>
<constant value="Ocean"/>
<variable name="country"/>
<variable name="admin1"/>
<variable name="admin2"/>
<variable name="admin3"/>
<variable name="locality1"/>
<variable name="locality2"/>
<variable name="postal"/>
<variable name="latitude"/>
<variable name="longitude"/>
<variable name="bboxNorth"/>
<variable name="bboxSouth"/>
<variable name="bboxEast"/>
<variable name="bboxWest"/>
<variable name="timezone"/>
</atom>
</body>
<head>
<atom name="YahooOceans">
<variable name="woeid"/>
<variable name="type"/>
<variable name="placeTypeName"/>
<variable name="name"/>
</atom>
</head>
</dependency>
</dependencies>
</schema>