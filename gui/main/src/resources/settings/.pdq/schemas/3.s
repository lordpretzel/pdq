<?xml version="1.0" encoding="UTF-8"?>
<schema name="World Bank API" description="">
<sources>
<source name="worldbank" file="services/worldbank-services.xml" discoverer="uk.ac.ox.cs.pdq.runtime.io.xml.ServiceReader"/>
</sources>
<relations>
<relation name="WBData" source="worldbank" size="4282265600">
<access-method name="wb_c" type="LIMITED" inputs="1" cost="100.0"/>
<access-method name="wb_i" type="LIMITED" inputs="2" cost="100.0"/>
<access-method name="wb_ci" type="LIMITED" inputs="1,2" cost="10.0"/>
<access-method name="wb_cd" type="LIMITED" inputs="1,3" cost="10.0"/>
<access-method name="wb_id" type="LIMITED" inputs="2,3" cost="10.0"/>
<access-method name="wb_cid" type="LIMITED" inputs="1,2,3" cost="1.0"/>
</relation>
<relation name="WBIndicators" source="worldbank" size="8804">
<access-method name="i_free" type="FREE" cost="15.0"/>
<access-method name="i_id" type="LIMITED" inputs="1" cost="1.0"/>
<access-method name="i_topicid" type="LIMITED" inputs="2" cost="15.0"/>
</relation>
<relation name="WBCountries" source="worldbank" size="256">
<access-method name="c_free" type="FREE" cost="1.0"/>
<access-method name="c_il" type="LIMITED" inputs="4" cost="1.0"/>
</relation>
<relation name="WBTopics" source="worldbank" size="19">
<access-method name="t_free" type="FREE" cost="2.0"/>
<access-method name="t_id" type="LIMITED" inputs="1" cost="1.0"/>
</relation>
<relation name="WBIncomeLevels" source="worldbank" size="10">
<access-method name="il_free" type="FREE" cost="1.0"/>
<access-method name="il_id" type="LIMITED" inputs="1" cost="1.0"/>
</relation>
<relation name="WBLendingTypes" source="worldbank" size="10">
<access-method name="lt_free" type="FREE" cost="1.0"/>
<access-method name="lt_id" type="LIMITED" inputs="1" cost="1.0"/>
</relation>
</relations>
<dependencies>
<dependency>
<body>
<atom name="WBData">
<variable name="iso"/>
<variable name="indicator"/>
<variable name="date"/>
<variable name="countryName"/>
<variable name="value"/>
</atom>
</body>
<head>
<atom name="WBCountries">
<variable name="id"/>
<variable name="iso"/>
<variable name="countryName"/>
<variable name="incomeLevel"/>
<variable name="lendingType"/>
<variable name="capitalCity"/>
<variable name="latitude"/>
<variable name="longitude"/>
</atom>
</head>
</dependency>
<dependency>
<body>
<atom name="WBData">
<variable name="country"/>
<variable name="indicator"/>
<variable name="date"/>
<variable name="countryName"/>
<variable name="value"/>
</atom>
</body>
<head>
<atom name="WBIndicators">
<variable name="indicator"/>
<variable name="name"/>
<variable name="sourceId"/>
<variable name="sourceNote"/>
<variable name="topicId"/>
</atom>
</head>
</dependency>
<dependency>
<body>
<atom name="WBIndicators">
<variable name="indicator"/>
<variable name="topicId"/>
<variable name="name"/>
<variable name="sourceId"/>
<variable name="sourceNote"/>
</atom>
</body>
<head>
<atom name="WBTopics">
<variable name="topicId"/>
<variable name="topicName"/>
<variable name="topicSourceNote"/>
</atom>
</head>
</dependency>
</dependencies>
</schema>